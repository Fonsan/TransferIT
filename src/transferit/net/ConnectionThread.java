package transferit.net;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import transferit.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;

/**
 * A ConnectionThread contains things necessary to keep a connection between
 * client and host. Methods to read and write commands on the socket.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public abstract class ConnectionThread extends Observable implements Runnable, Protocol, Serializable {

    protected ObjectOutputStream out;
    protected ObjectInputStream in;
    protected Socket socket;
    protected boolean hasWrite = false;
    public final int STREAMBUFFERSIZE = 1000;

    public abstract void sendMessage(String message);

    public abstract void sendQuit();

    protected abstract void writeFile(File fileToWrite);

    protected abstract void readFile(String relPath);

    /**
     * Returns wether the user has write permission for this ConnectionThread.
     * 
     * @return users write access.
     */
    public boolean isHasWrite() {
        return hasWrite;
    }

    /**
     * Sets write permission for this ConnectionThread.
     * 
     * @param hasWrite users write access.
     */
    public void setHasWrite(boolean hasWrite) {
        this.hasWrite = hasWrite;
    }

    /**
     * Closes the socket for this ConnectionThread.
     */
    public void closeSocket() {
        try {
            socket.close();
        } catch (SocketException se) {
        } catch (IOException ex) {
        }
    }

    /**
     * Reads a path expected from input the InputStream.
     * 
     * @return the read path.
     */
    public String readPath() {
        //Sanitizes user input to prevent directory traversal
        String input = readString();
        input = input.replace(':', File.separatorChar);
        input = input.replace("..", "");
        return input;
    }

    /**
     * Writes a path on the OutputStream.
     * 
     * @param path the path to write.
     */
    public void writePath(String path) {
        writeString(path.replace(File.separatorChar, ':'));
    }

    /**
     * Reads a string from the InputStream.
     * 
     * @return the read value.
     */
    public String readString() {
        String string = "";
        try {
            string = in.readUTF();
            if (TransferIT.DEBUG) {
                System.out.println(this.getClass().getName() + " in " + string);
            }
        } catch (SocketException se) {
        } catch (IOException ex) {
        }
        return string;
    }

    /**
     * Writes a string on the OutputStream.
     * 
     * @param string the value to write.
     */
    public void writeString(String string) {
        try {
            out.writeUTF(string);
            out.flush();
            if (TransferIT.DEBUG) {
                System.out.println(this.getClass().getName() + " out " + string);
            }
        } catch (SocketException se) {
        } catch (IOException ex) {
        }
    }

    /**
     * Reads an object from the InputStream.
     * 
     * @return the read object.
     */
    public Object readObject() {
        Object object = null;
        try {
            object = in.readUnshared();
            if (TransferIT.DEBUG) {
                System.out.println(this.getClass().getName() + " in obj " + object);
            }
        } catch (SocketException se) {
        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    /**
     * Writes an object on the OutputStream.
     * 
     * @param object the object to write.
     */
    public void writeObject(Object object) {
        try {
            out.writeUnshared(object);
            out.flush();
            if (TransferIT.DEBUG) {
                System.out.println(this.getClass().getName() + " out obj " + object);
            }
            out.reset();
        } catch (SocketException se) {
        } catch (IOException ex) {
        }
    }

    /**
     * Reads a message from the InputStream.
     * 
     * @return the read message.
     */
    public String getMessage() {
        return readString();
    }

    /**
     * Closes the socket.
     */
    public void getQuit() {
        closeSocket();
    }

    public boolean socketIsClosed() {
        return socket.isClosed();
    }
}
