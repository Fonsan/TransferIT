package transferit.net.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import transferit.TransferIT;

/**
 * The ServerConnectionHandler handles incoming connections on the server side.
 * It starts a new ServerConnectionThread for each new incoming connections.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class ServerConnectionHandler extends Observable implements Runnable,Serializable {

    private ServerSocket serverSocket;
    private int port;
    private String rootPath;
    private boolean isAlive = true;
    private Thread thread;

    /**
     * Starts the handler telling it to listen on a port and what directory the 
     * server is sharing.
     * 
     * @param port the port to listen on.
     * @param rootPath shared directory.
     */
    public ServerConnectionHandler(int port, String rootPath) {
        super();
        this.port = port;
        this.rootPath = rootPath;
    }

    /**
     * Returns if the server is online not.
     * 
     * @return is it is alive.
     */
    public boolean isIsAlive() {
        return isAlive;
    }

    /**
     * Sets the server online or not.
     * 
     * @param isAlive set true or false.
     */
    public void setIsAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    /**
     * Returns the serverSocket.
     * 
     * @return the socket.
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Starts a new thread for each incoming connection and then listens for
     * more incoming.
     */
    @Override
    public void run() {
        while (true) {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException ex) {
                setChanged();
                notifyObservers(false);
                setChanged();
                notifyObservers("Problem starting server " + ex.getLocalizedMessage());
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                    Logger.getLogger(ServerConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                continue;
            }

            setChanged();
            notifyObservers(true);
            setChanged();
            notifyObservers("Server has started listening on port: " + port);
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    ServerConnectionThread serverConnectionThread = new ServerConnectionThread(socket, rootPath);
                    setChanged();
                    notifyObservers(serverConnectionThread);
                    clearChanged();
                    Thread serverThread = new Thread(serverConnectionThread);
                    TransferIT.getTransferITThreadFactory().dispatchNewThread(serverConnectionThread,"ServerConnection-" + socket.getInetAddress().getHostAddress());
                }
            } catch (IOException ioe) {
                setChanged();
                notifyObservers(false);
                break;
            }

        }
    }
}
