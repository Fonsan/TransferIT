package transferit.net.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import transferit.TransferIT;
import transferit.TransferITController;
import transferit.gui.filebrowsers.FileInfo;
import transferit.net.ConnectionThread;
import static transferit.net.Protocol.ClientCommands;

/**
 * A ServerConnectionThread is a ConnectionThread implementaion on the server 
 * side. 
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class ServerConnectionThread extends ConnectionThread {

    private String rootPath;
    private boolean isInCommandState = false;
    private boolean isDead = false;
    private boolean isClearToRead = false;
    private boolean abort = false;
    private Object objectToWrite = null;
    private Object readLock = new Object();
    private Object writeLock = new Object();

    /**
     * Constructor that initializes a connection. All ConnectionThreads are 
     * connected to a socket which you specifiy here. You also tell what folder
     * the server is sharing.
     * 
     * @param socket the socket.
     * @param rootPath the folder to share.
     */
    public ServerConnectionThread(Socket socket, String rootPath) {
        try {
            this.rootPath = rootPath;
            this.socket = socket;
            socket.setTcpNoDelay(false);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
             
        } catch (SocketException se) {
        } catch (IOException ex) {
        }
    }

    /**
     * Listens for commands from the client.
     */
    @Override
    public void run() {
        ClientCommands command = ClientCommands.NOVALUE;

        String read = readString();
        command = ClientCommands.valueOf(read);
        if (!command.equals(ClientCommands.SENDAUTH)) {
            //Hack attempt
            setChanged();
            notifyObservers(command);
        } else {
            while (true) {
                setChanged();
                notifyObservers(command);
                clearChanged();
                if (socket.isClosed()) {
                    break;
                }
                try {
                    synchronized (readLock) {
                        read = readString();
                        command = ClientCommands.valueOf(read);
                    }
                } catch (IllegalArgumentException ia) {
                    setChanged();
                    notifyObservers(getHostAndPort() + "A authed client has used a non standard command: " + read);
                    clearChanged();
                    command = ClientCommands.SENDQUIT;
                }
            }
        }
    }

    /**
     * Parses the create directory request.
     */
    public void getCreateDirectoryRequest() {
        String path = readPath();
        if (hasWrite) {
            new File(rootPath + File.separatorChar + path).mkdirs();
        }
    }

    /**
     * Parses the delete request.
     */
    public void getDeleteRequest() {
        String path = readPath();
        if (hasWrite) {
            TransferITController.recursiveDelete(rootPath + File.separatorChar + path);
        }
    }

    /**
     * Parses the directory list request.
     */
    public void getDirectoryListingRequest() {
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        String path = readPath();
        for (File file : (new File(rootPath + File.separatorChar + path)).listFiles()) {
            String relPath = file.getAbsolutePath().replace(rootPath, "");
            fileInfos.add(new FileInfo(file.getName(), relPath, file.length(), file.isDirectory()));
        }
        writeObject(fileInfos);
    }

    /**
     * Parses the recursive directory list request.
     */
    public void getRecursiveDirectoryListingRequest() {
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        String path = readPath();
        addFilesToList(new File(rootPath + File.separatorChar + path), fileInfos);
        writeObject(fileInfos);
    }

    /**
     * Adds a files in a folder to a list.
     * @param folder the folder.
     * @param fileInfos the list.
     */
    private void addFilesToList(File folder, ArrayList<FileInfo> fileInfos) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFilesToList(file, fileInfos);
            } else {
                String relPath = file.getAbsolutePath().replace(rootPath, "");
                fileInfos.add(new FileInfo(file.getName(), relPath, file.length(), file.isDirectory()));
            }
        }
    }

    /** 
     * Gets a request for a file from the client, it then
     * provides the client with a File object of that file. 
     */
    public void getFileGetRequest() {
        writeFile(new File(rootPath + File.separatorChar + readPath()));
    }

    /**
     * Parses the file send request.
     */
    public void getFileSendRequest() {
        if (hasWrite) {
            String filePath = rootPath + readPath();
            new File(filePath).getParentFile().mkdirs();
            readFile(filePath);
        } else {
            sendQuit();
        }
    }

    /**
     * Reads a file.
     * 
     * @param path where to put the file.
     */
    protected void readFile(String path) {
        Thread readFolderListRequest = new Thread() {

            @Override
            public void run() {

                ClientCommands command = ClientCommands.NOVALUE;
                synchronized (readLock) {
                    while (!isClearToRead) {
                        try {
                            readLock.wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                    command = ClientCommands.valueOf(readString());
                    while (command == ClientCommands.CREATEDIR || command == ClientCommands.DELETE || command == ClientCommands.GETFILELIST || command == ClientCommands.ABORT) {
                        if (command == ClientCommands.ABORT) {
                            abort = true;
                            return;
                        }
                        setChanged();
                        notifyObservers(command);
                        isClearToRead = false;
                        readLock.notifyAll();
                        while (!isClearToRead) {
                            try {
                                readLock.wait();
                            } catch (InterruptedException ie) {
                            }
                        }
                        command = ClientCommands.valueOf(readString());
                    }
                    while (true) {
                        isClearToRead = false;
                        while (!isClearToRead) {
                            try {
                                readLock.wait();
                            } catch (InterruptedException ie) {
                            }
                        }
                        isClearToRead = false;
                        readLock.notifyAll();
                    }
                }
            }
        };
        TransferIT.getTransferITThreadFactory().dispatchNewThread(readFolderListRequest,"ServerConnection-" + socket.getInetAddress().getHostAddress() + "-ReadFile-ReadAsyncCliCommand");
        
        BufferedOutputStream fileOutputStream = null;

        try {
            File fileToWrite = new File(path);
            fileToWrite.getParentFile().mkdirs();
            fileToWrite.createNewFile();
            fileOutputStream = new BufferedOutputStream(new FileOutputStream(fileToWrite), STREAMBUFFERSIZE);
            int bufferlength = Integer.parseInt(readString());
            byte[] buffer = new byte[bufferlength];
            long length = Long.parseLong(readString());
            int id = 0;
            while (0 < length) {
                if (abort) {
                    abort = false;
                    return;
                }
                id = in.read();
                if (id == 1) {
                    synchronized (readLock) {
                        isClearToRead = true;
                        readLock.notifyAll();
                        while (isClearToRead) {
                            readLock.wait();
                        }
                    }
                } else if (id == 0) {
                    if (length < bufferlength) {
                        bufferlength = (int) length;
                        buffer = new byte[bufferlength];
                    }
                    in.readFully(buffer);
                    length -= bufferlength;
                    fileOutputStream.write(buffer);
                }
            }
            fileOutputStream.flush();
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException se) {
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        readFolderListRequest.interrupt();
    }

    /**
     * Writes a file.
     * 
     * @param fileToRead the file.
     */
    protected void writeFile(File fileToRead) {
        Thread readAsyncCommand = new Thread() {

            @Override
            public void run() {
                ClientCommands command = ClientCommands.NOVALUE;
                try {
                    command = ClientCommands.valueOf(readString());
                    while (command == ClientCommands.CREATEDIR || command == ClientCommands.DELETE || command == ClientCommands.GETFILELIST) {
                        if (command == ClientCommands.GETFILELIST) {
                            ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
                            String path = readPath();
                            for (File file : (new File(rootPath + File.separatorChar + path)).listFiles()) {
                                String relPath = file.getAbsolutePath().replace(rootPath, "");
                                fileInfos.add(new FileInfo(file.getName(), relPath, file.length(), file.isDirectory()));
                            }
                            objectToWrite = fileInfos;
                            isInCommandState = true;
                            synchronized (writeLock) {
                                while (isInCommandState) {
                                    try {
                                        writeLock.wait();
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(ServerConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        } else if (command == ClientCommands.ABORT) {
                            abort = true;
                            return;
                        } else {
                            setChanged();
                            notifyObservers(command);
                        }
                        command = ClientCommands.valueOf(readString());
                    }
                    isDead = true;
                    synchronized (readLock) {
                        readLock.notifyAll();
                    }
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                } catch (IllegalArgumentException iae) {
                }
            }
        };
        TransferIT.getTransferITThreadFactory().dispatchNewThread(readAsyncCommand,"ServerConnection-" + socket.getInetAddress().getHostAddress() + "-WriteFile-ReadAsyncCliCommand");
        DataInputStream fileInputStream = null;

        try {
            byte[] buffer = new byte[STREAMBUFFERSIZE];
            fileInputStream = new DataInputStream(new FileInputStream(fileToRead));
            long left = fileToRead.length();


            writeString(Integer.toString(STREAMBUFFERSIZE));
            writeString(Long.toString(left));


            while (0 < left) {
                if (abort) {
                    abort = false;
                    return;
                }
                if (isInCommandState) {
                    synchronized (writeLock) {
                        out.write(1);
                        writeObject(objectToWrite);
                        isInCommandState = false;
                        writeLock.notifyAll();
                    }
                } else {
                    if (left < buffer.length) {
                        buffer = new byte[(int) left];
                    }
                    out.write(0);
                    fileInputStream.readFully(buffer);
                    left -= buffer.length;
                    out.write(buffer);
                }
            }
            out.flush();

            synchronized (readLock) {
                while (!isDead) {
                    try {
                        readLock.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            isDead = false;
        } catch (SocketException se) {
        } catch (IOException ex) {
        } finally {
            try {

                fileInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Tells the client that it is authed.
     */
    public void sendAuthed() {
        writeString(ServerCommands.AUTHED.toString());
    }

    /**
     * Tells the client that it is not authed and quits the connection.
     */
    public void sendNotAuthed() {
        sendQuit();
    }

    /**
     * Tells the client that it has write access.
     */
    public void sendAuthWrite() {
        hasWrite = true;
        writeString(ServerCommands.AUTHWRITE.toString());
    }

    /**
     * Tells the client that it does not have write access.
     */
    public void sendNotAuthWrite() {
        hasWrite = false;
        writeString(ServerCommands.AUTHNOTWRITE.toString());
    }

    /**
     * Sends a message on OutputStream.
     * 
     * @param message the message to send.
     */
    @Override
    public void sendMessage(String message) {
        writeString(ServerCommands.SENDMSG.toString());
        writeString(message);
    }

    /**
     * Tells the client that the connection is closign and then closes the 
     * socket.
     */
    @Override
    public void sendQuit() {
        writeString(ServerCommands.SENDQUIT.toString());
        closeSocket();
    }

    /**
     * Returns the host and port in a IP:port format.
     * @return the IP and port.
     */
    public String getHostAndPort() {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " ";
    }
}
