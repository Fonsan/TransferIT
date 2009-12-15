package transferit.net.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import transferit.net.ConnectionThread;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import transferit.gui.GUIView;
import transferit.gui.filebrowsers.FileInfo;
import transferit.net.Protocol.ServerCommands;

/**
 * A ClientConnectionThread is a ConnectionThread implementaion on the client 
 * side. 
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class ClientConnectionThread extends ConnectionThread {

    private String username;
    private String password;
    private Object readLock = new Object();
    private Object writeLock = new Object();
    private volatile boolean isTransferring = false;
    private Object objectToRead = null;
    private Object objectToWrite = null;
    private boolean haveSomeThingToWrite = false;
    private boolean isWriting = false;
    private boolean isClearToWrite = false;
    private DefaultTableModel defaultTableModel = new DefaultTableModel();
    private volatile HashMap<FileInfo, String> filesToDownLoad = new HashMap<FileInfo, String>();
    private volatile LinkedList<Object> transferList = new LinkedList<Object>();
    private volatile HashMap<File, String> filesToUpload = new HashMap<File, String>();
    private volatile long bytesMaxInQueue = 0;
    private volatile long bytesLeftToTransferInQueue = 0;
    private volatile long bytesMaxInFile = 0;
    private volatile long bytesLeftToTransferInFile = 0;
    private long timePassed = 0;

    /**
     * Constructor that initializes a connection. All ConnectionThreads are 
     * connected to a socket which you specifiy here. You also tell what user
     * logged in and started the thread.
     * 
     * @param socket the socket to use.
     * @param finusername user that starts the thread.
     * @param finpassword the users password.
     */
    public ClientConnectionThread(Socket socket, String finusername, String finpassword) {
        for (String s : GUIView.transferTableColums) {
            defaultTableModel.addColumn(s);
        }
        try {
            this.socket = socket;
            socket.setTcpNoDelay(true);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.username = finusername;
            this.password = finpassword;
        } catch (IOException ex) {
            Logger.getLogger(ClientConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addBytesToQueue(long bytes) {
        this.bytesMaxInQueue += bytes;
        this.bytesLeftToTransferInQueue += bytes;
    }

    public void addDownload(FileInfo fileInfo, String homePath) {
        addBytesToQueue(fileInfo.getSize());
        filesToDownLoad.put(fileInfo, homePath);
        transferList.addLast(fileInfo);
        Object[] row = new Object[4];
        row[0] = filesToDownLoad.get(fileInfo);
        row[1] = fileInfo.getFilePath();
        row[2] = Long.toString(fileInfo.getSize());
        row[3] = true;
        defaultTableModel.addRow(row);
    }

    public void addUpload(File file, String remotePath) {
        addBytesToQueue(file.length());
        filesToUpload.put(file, remotePath);
        transferList.addLast(file);
        Object[] row = new Object[4];
        row[0] = file.getAbsolutePath();
        row[1] = filesToUpload.get(file);
        row[2] = Long.toString(file.length());
        row[3] = false;
        defaultTableModel.addRow(row);
    }

    public void removeUpload(File file) {
        filesToUpload.remove(file);
        bytesLeftToTransferInQueue -= file.length();
        bytesMaxInQueue -= file.length();
    }

    public void removeDownload(FileInfo fileInfo) {
        filesToDownLoad.remove(fileInfo);
        bytesLeftToTransferInQueue -= fileInfo.getSize();
        bytesMaxInQueue -= fileInfo.getSize();
    }

    public void removeTransfer(int row) {
        if (row == 0) {
            sendAbort();
        } else {
            Object o = transferList.remove(row);
            if (o instanceof FileInfo) {
                removeDownload((FileInfo) o);
            } else if (o instanceof File) {
                removeUpload((File) o);
            }
            defaultTableModel.removeRow(row);
        }
    }

    public DefaultTableModel getDefaultTableModel() {
        return defaultTableModel;
    }

    public long getTimePassed() {
        return timePassed;
    }

    /**
     * Listens for commands from the server on the other side of the socket.
     */
    @Override
    public void run() {
        sendAuthRequest();
        ServerCommands command = ServerCommands.NOVALUE;
        try {
            command = ServerCommands.valueOf(readString());
        } catch (IllegalArgumentException iae) {
            closeSocket();
            return;
        }
        if (command.equals(ServerCommands.AUTHED)) {
            command = ServerCommands.valueOf(readString());
            if (command.equals(ServerCommands.AUTHWRITE)) {
                hasWrite = true;
            } else if (command.equals(ServerCommands.AUTHNOTWRITE)) {
                hasWrite = false;
            } else {
                throw new IllegalArgumentException(command.toString());
            }
            setChanged();
            notifyObservers(ServerCommands.AUTHED);
        } else if (command.equals(ServerCommands.SENDQUIT)) {
            setChanged();
            notifyObservers(command);
        } else {
            throw new IllegalArgumentException(command.toString());
        }
    }

    /**
     * Sends username and password to a server to request authorization.
     */
    public void sendAuthRequest() {
        writeString(ClientCommands.SENDAUTH.toString());
        writeString(username);
        writeString(password);
    }

    /**
     * Sends a path that specifies where you want to create a new directory 
     * on the serve.
     * 
     * @param path the path to directory.
     */
    public void sendCreateDirectoryRequest(String path) {
        haveSomeThingToWrite = true;
        synchronized (writeLock) {
            writeString(ClientCommands.CREATEDIR.toString());
            writePath(path);
        }
    }

    /**
     * Sends request to delete files.
     * 
     * @param filesToDelete list with the files.
     */
    public void sendDeleteRequest(ArrayList<FileInfo> filesToDelete) {
        for (FileInfo fileInfo : filesToDelete) {
            haveSomeThingToWrite = true;
            synchronized (writeLock) {
                writeString(ClientCommands.DELETE.toString());
                writePath(fileInfo.getFilePath());
            }
        }
    }

    /**
     * Sends a request to list the files in a directory on the server.
     * 
     * @param path path to the directory.
     * @return list with the files.
     */
    public ArrayList<FileInfo> sendDirectoryListingRequest(String path) {
        Object returnObject = null;
        ArrayList<FileInfo> directoryList = null;
        if (isTransferring) {
            if (isWriting) {
                haveSomeThingToWrite = true;
                synchronized (writeLock) {
                    while (!isClearToWrite) {
                        try {
                            writeLock.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClientConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    writeString(ClientCommands.GETFILELIST.toString());
                    writePath(path);
                    returnObject = readObject();
                    haveSomeThingToWrite = false;
                    writeLock.notifyAll();
                }

            } else {
                synchronized (readLock) {
                    writeString(ClientCommands.GETFILELIST.toString());
                    writePath(path);
                    while (objectToRead == null) {
                        try {
                            readLock.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClientConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                returnObject = objectToRead;
                objectToRead = null;
            }

        } else {

            writeString(ClientCommands.GETFILELIST.toString());
            writePath(path);

            returnObject = readObject();
        }
        if (returnObject instanceof ArrayList) {
            directoryList = (ArrayList<FileInfo>) returnObject;
        }
        return directoryList;
    }

    /**
     * Sends a request to list the files recursively in a directory on the 
     * server.
     * 
     * @param filePath path to the directory.
     * @return list with files.
     */
    public ArrayList<FileInfo> sendRecursiveDirectoryListingRequest(String filePath) {
        ArrayList<FileInfo> directoryList = new ArrayList<FileInfo>();

        synchronized (writeLock) {
            writeString(ClientCommands.GETRECURSIVEFILELIST.toString());
            writePath(filePath);
        }
        Object returnObject = null;
        if (isTransferring) {
            return directoryList;
        } else {
            returnObject = readObject();
        }

        if (returnObject instanceof ArrayList) {
            directoryList.addAll((ArrayList<FileInfo>) returnObject);
        }
        return directoryList;
    }

    /**
     * Sends request to download a file.
     * 
     * @param homePath where to put the file locally.
     * @param relPath path to file on server.
     */
    public void sendFileGetRequest(String homePath, String relPath) {
        if (isTransferring) {
            return;
        }
        synchronized (readLock) {
            writeString(ClientCommands.GETFILE.toString());
            writePath(relPath);
        }
        if (relPath.indexOf(File.separatorChar) != -1) {
            relPath = relPath.substring(relPath.lastIndexOf(File.separatorChar) + 1);
        }
        readFile(homePath + File.separatorChar + relPath);
    }

    /**
     * Sends request to upload a file.
     * 
     * @param file the file to upload.
     * @param relPath path to file on server.
     */
    public void sendFileSendRequest(File file, String relPath) {
        if (isTransferring) {
            return;
        }
        synchronized (readLock) {
            writeString(ClientCommands.SENDFILE.toString());
            writePath(relPath + File.separatorChar + file.getName());
        }
        writeFile(file);
    }

    /**
     * Writes a file on OutputStream.
     * 
     * @param fileToRead the file to write.
     */
    protected void writeFile(File fileToRead) {
        isTransferring = true;
        isWriting = true;
        DataInputStream fileInputStream = null;

        try {
            byte[] buffer = new byte[STREAMBUFFERSIZE];
            fileInputStream = new DataInputStream(new FileInputStream(fileToRead));
            bytesMaxInFile = fileToRead.length();
            bytesLeftToTransferInFile = bytesMaxInFile;
            synchronized (writeLock) {
                writeString(Integer.toString(STREAMBUFFERSIZE));
                writeString(Long.toString(bytesMaxInFile));
            }

            while (0 < bytesLeftToTransferInFile) {
                if (haveSomeThingToWrite) {
                    out.write(1);
                    synchronized (writeLock) {
                        isClearToWrite = true;
                        writeLock.notifyAll();
                        while (haveSomeThingToWrite) {
                            try {
                                writeLock.wait();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ClientConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        isClearToWrite = false;
                    }

                }
                synchronized (writeLock) {
                    out.write(0);
                    if (bytesLeftToTransferInFile < STREAMBUFFERSIZE) {
                        buffer = new byte[(int) bytesLeftToTransferInFile];
                    }
                    fileInputStream.readFully(buffer);
                    bytesLeftToTransferInFile -= buffer.length;
                    bytesLeftToTransferInQueue -= buffer.length;
                    out.write(buffer);

                }
            }
            isWriting = false;
            isTransferring = false;
            out.flush();
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
     * Reads a file from InputStream.
     * 
     * @param path where to put the file.
     */
    protected synchronized void readFile(String path) {
        isTransferring = true;
        final FileOutputStream fileOutputStream;
        try {
            File fileToWrite = new File(path);
            fileToWrite.getParentFile().mkdirs();
            fileToWrite.createNewFile();
            fileOutputStream = new FileOutputStream(fileToWrite);

            int bufferlength = 0;
            synchronized (readLock) {
                bufferlength = Integer.parseInt(readString());
                bytesMaxInFile = Long.parseLong(readString());
            }
            bytesLeftToTransferInFile = bytesMaxInFile;
            byte[] buffer = new byte[bufferlength];


            int id = 0;
            while (0 < bytesLeftToTransferInFile) {
                id = in.read();
                if (id == 1) {
                    synchronized (readLock) {
                        objectToRead = readObject();
                        readLock.notifyAll();
                    }
                } else if (id == 0) {
                    if (bytesLeftToTransferInFile < bufferlength) {
                        bufferlength = (int) bytesLeftToTransferInFile;
                        buffer = new byte[bufferlength];
                    }
                    /*in.readFully(buffer,0,buffer.length/2);
                    in.readFully(buffer, buffer.length/2, buffer.length/2);
                    int bytesRead = 0;
                    int bytesWritten = 0;
                    while (0 < (bytesRead = in.read(buffer, bytesWritten, bufferlength - bytesWritten -1))) {
                    fileOutputStream.write(buffer, bytesWritten, bytesRead);
                    bytesWritten += bytesRead;
                    }*/
                    in.readFully(buffer);
                    fileOutputStream.write(buffer);
                    bytesLeftToTransferInFile -= bufferlength;
                    bytesLeftToTransferInQueue -= bufferlength;
                }
            }
            writeString(ClientCommands.NOVALUE.toString());
            isTransferring = false;
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (SocketException se) {
        } catch (IOException ex) {
        }
    }

    /**
     * Sends a message to server.
     * 
     * @param message the message to send.
     */
    @Override
    public void sendMessage(String message) {
        synchronized (readLock) {
            writeString(message);
        }
    }

    /**
     * Sends SENDQUIT command and closes the socket.
     */
    @Override
    public void sendQuit() {
        writeString(ClientCommands.SENDQUIT.toString());
        closeSocket();
    }

    public void sendAbort() {
        if (isTransferring) {
            if (isWriting) {
                haveSomeThingToWrite = true;
                synchronized (writeLock) {
                    while (!isClearToWrite) {
                        try {
                            writeLock.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClientConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    writeString(ClientCommands.ABORT.toString());
                    haveSomeThingToWrite = false;
                    writeLock.notifyAll();
                }

            } else {
                writeString(ClientCommands.ABORT.toString());
            }

        } else {
            writeString(ClientCommands.ABORT.toString());
        }
    }

    /**
     * Returns the host IP.
     * 
     * @return the IP.
     */
    public String getHost() {
        return socket.getInetAddress().getHostAddress() + ' ';
    }

    /**
     * Returns the host port.
     * 
     * @return the port.
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * Returns the password for this ConnectionThread.
     * 
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * 
     * @param password the password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the username for this ConnectionThread.
     * 
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * 
     * @param username the username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public LinkedList<Object> getTransferList() {
        return transferList;
    }

    public HashMap<FileInfo, String> getFilesToDownLoad() {
        return filesToDownLoad;
    }

    public HashMap<File, String> getFilesToUpload() {
        return filesToUpload;
    }

    public long getBytesLeftToTransferInFile() {
        return bytesLeftToTransferInFile;
    }

    public long getBytesLeftToTransferInQueue() {
        return bytesLeftToTransferInQueue;
    }

    public long getBytesMaxInFile() {
        return bytesMaxInFile;
    }

    public long getBytesMaxInQueue() {
        return bytesMaxInQueue;
    }

    public void setBytesLeftToTransferInQueue(long bytesLeftToTransferInQueue) {
        this.bytesLeftToTransferInQueue = bytesLeftToTransferInQueue;
    }

    public void setBytesMaxInQueue(long bytesMaxInQueue) {
        this.bytesMaxInQueue = bytesMaxInQueue;
    }
    
    

    public long updateTimePassed(long toAdd) {
        return timePassed += toAdd;
    }

    public void setTimePassed(long timePassed) {
        this.timePassed = timePassed;
    }

    public boolean hasTransfersLeft() {
        return transferList.isEmpty();
    }
}
