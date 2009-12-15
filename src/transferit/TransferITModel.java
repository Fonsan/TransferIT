package transferit;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.cybergarage.upnp.Action;
import transferit.gui.GUIView;
import transferit.net.client.ClientConnectionThread;
import transferit.net.multicast.ObservablePacketContainer;
import transferit.net.server.ServerConnectionHandler;
import transferit.net.server.ServerConnectionThread;

/**
 * This is the model.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias SÃ¶derberg and Henrik Wiberg
 * 
 */
public class TransferITModel extends Properties implements Serializable {

    private Set<ClientConnectionThread> clientConnectionSet = new HashSet<ClientConnectionThread>();
    private ServerConnectionHandler serverConnectionHandler;
    private Set<ServerConnectionThread> serverClientConnectionSet = new HashSet<ServerConnectionThread>();
    private ObservablePacketContainer observablePacketContainer;
    private HashMap<String, String> users = new HashMap<String, String>();
    private HashMap<String, Boolean> userRights = new HashMap<String, Boolean>();
    private HashSet<String> hostHistory = new HashSet<String>();
    private HashMap<String, String> usernameHistory = new HashMap<String, String>();
    private HashMap<String, String> passwordHistory = new HashMap<String, String>();
    private ArrayList<Action> upnpActions = new ArrayList();

    /**
     * Creates the model and loads the default settings.
     */
    public TransferITModel() {
        loadDefaultSettings();
    }

    public synchronized void addUser(String username, String password, boolean writeAccess) {
        users.put(username, password);
        userRights.put(username + password, writeAccess);
        TransferIT.saveSettings();
    }

    public synchronized void removeUser(String username) {
        userRights.remove(username + users.get(username));
        users.remove(username);
        TransferIT.saveSettings();
    }

    public synchronized boolean getUserAuth(String username, String password) {
        return password.equals(users.get(username));
    }

    public synchronized boolean getUserWriteAccess(String username) {
        return userRights.get(username + users.get(username));
    }

    public synchronized void addHostToHistory(String host, String username, String password) {
        hostHistory.add(host);
        usernameHistory.put(host, username);
        passwordHistory.put(host, password);
        TransferIT.saveSettings();
    }

    public synchronized void removeHostFromHistory(String host) {
        hostHistory.remove(host);
        usernameHistory.remove(host);
        passwordHistory.remove(host);
        TransferIT.saveSettings();
    }

    public synchronized String getHistoryPassowrd(String host) {
        return passwordHistory.get(host);
    }

    public synchronized String getHistoryUsername(String host) {
        return usernameHistory.get(host);
    }


    /**
     * Returns the ObservablePacketContainer used by this model.
     * 
     * @return the container.
     */
    public ObservablePacketContainer getObservablePacketContainer() {
        return observablePacketContainer;
    }

    /**
     * Changes the ObservablePacketContainer used by this model.
     * 
     * @param observablePacketContainer the new container.
     */
    public void setObservablePacketContainer(ObservablePacketContainer observablePacketContainer) {
        this.observablePacketContainer = observablePacketContainer;
    }

    /**
     * Returns a Set with the connected servers for the client.
     * 
     * @return the Set.
     */
    public synchronized Set<ClientConnectionThread> getClientConnectionSet() {
        return clientConnectionSet;
    }

    /**
     * Changes the Set with active connections for the client side.
     * 
     * @param clientConnectionSet the new Set.
     */
    public void setClientConnectionSet(Set<ClientConnectionThread> clientConnectionSet) {
        this.clientConnectionSet = clientConnectionSet;
    }

    /**
     * Adds a ClientConnectionThread to the clientConnectionSet.
     * 
     * @param clientConnectionThread the connection to add.
     */
    public synchronized void addClientConnectionToSet(ClientConnectionThread clientConnectionThread) {
        this.clientConnectionSet.add(clientConnectionThread);
    }

    /**
     * Removes a connection from the clientConnectionSet.
     * 
     * @param clientConnectionThread the connection to remove.
     * @return true if succeded, else false.
     */
    public boolean removeClientConnectionFromSet(ClientConnectionThread clientConnectionThread) {
        return this.clientConnectionSet.remove(clientConnectionThread);
    }

    /**
     * Returns a Set with the connections the server has open.
     * 
     * @return the Set.
     */
    public Set<ServerConnectionThread> getServerClientConnectionSet() {
        return serverClientConnectionSet;
    }

    public ArrayList<Action> getUpnpActions() {
        return upnpActions;
    }

    public void setUpnpActions(ArrayList<Action> upnpActions) {
        this.upnpActions = upnpActions;
    }
    
    

    /**
     * Sets a Set for this model.
     * 
     * @param serverClientConnectionSet the Set.
     */
    public void setServerClientConnectionSet(Set<ServerConnectionThread> serverClientConnectionSet) {
        this.serverClientConnectionSet = serverClientConnectionSet;
    }

    /**
     * Returns the connectionHandler used by this model.
     * 
     * @return the handler.
     */
    public ServerConnectionHandler getServerConnectionHandler() {
        return serverConnectionHandler;
    }

    /**
     * Sets the connectionHandler used by this model.
     * 
     * @param serverConnectionHandler
     */
    public void setServerConnectionHandler(ServerConnectionHandler serverConnectionHandler) {
        this.serverConnectionHandler = serverConnectionHandler;
    }

    /**
     * Loads default settings on startup
     */
    public synchronized void loadDefaultSettings() {

        /*
         * Here will all possible settings be placed
         * and their default value if any
         */
        int height = 750;
        int width = 750;
        put("test.setting", "test");
        put("gui.height", Integer.toString(height));
        put("gui.width", Integer.toString(width));
        put("gui.title", "TransferIT");
        put("gui.localfilebrowser.path", System.getProperty("user.home"));
        put("gui.useanonymous", "true");
        put("gui.usetree", "false");
        put("gui.host", "127.0.0.1");
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        put("gui.position.x", Integer.toString(((int) d.getWidth() - width) / 2));
        put("gui.position.y", Integer.toString(((int) d.getHeight() - height) / 2));
        put("gui.username", System.getProperty("user.name"));
        put("net.default.port", "62121");
        put("net.default.userwrite", "false");
        put("net.default.username","anon");
        put("net.default.password","anon");
        put("net.server.isalive", "true");
        put("net.server.port", "62121");
        put("net.server.rootpath", "");
        put("net.multicast.isalive", "true");
        put("net.multicast.interval", "1000");
        put("net.multicast.timeout", "5000");
        put("net.multicast.inetgroup", "239.21.21.21");
        put("net.multicast.port", "62121");
        put("net.upnp.forward", "false");
    }

    /**
     * Change a setting.
     * 
     * @param key the setting to change.
     * @param value the value of the setting.
     * @return
     */
    @Override
    public synchronized Object setProperty(String key, String value) {
        /*
         * A check that the setting is actually valid
         */
        Object o = super.setProperty(key, value);
        if (o == null) {
            throw new IllegalArgumentException(key);
        }
        return o;
    }

    /**
     * 
     * @param key
     * @return
     */
    @Override
    public String getProperty(String key) {
        /*
         * A check that the setting is actually valid
         */
        String str = super.getProperty(key);
        if (str == null) {
            throw new IllegalArgumentException(key);
        }
        return str;
    }

    /**
     * Set the path for the server to share.
     * 
     * @param rootPath the path.
     */
    public void setRootPath(String rootPath) {
        setProperty("net.server.rootpath", rootPath);
    }

    /**
     * Return the path that is shared.
     * 
     * @return the path.
     */
    public String getRootPath() {
        return getProperty("net.server.rootpath");
    }

    /**
     * Returns the anon setting. If we have anon login enabled.
     * 
     * @return the anon setting.
     */
    public boolean getAnonymousLogin() {
        return getUsers().keySet().contains("anon");
    }

    /**
     * Change the port the server operates on.
     * 
     * @param port the port.
     */
    public void setServerPort(String port) {
        setProperty("net.server.port", port);
    }

    /**
     * Return the port the server operates on.
     * 
     * @return the port.
     */
    public int getServerPort() {
        return Integer.parseInt(getProperty("net.server.port"));
    }

    /**
     * Sets the timeout of the multicaster
     * 
     * @param timeout the desired timeout in ms.
     */
    public void setTimeout(String timeout) {
        setProperty("net.multicast.timeout", timeout);
    }

    /**
     * Returns the multicaster timeout value.
     * 
     * @return the value.
     */
    public String getTimeout() {
        return getProperty("net.multicast.timeout");
    }

    /**
     * Returns what users are allowed access to the server.
     * 
     * @return the users in a String format.
     */
    public synchronized HashMap<String,String> getUsers() {
        return users;
    }

    /**
     * Adds a new user that is allowed access to the server.
     * 
     * @param user desired username.
     * @param password the users password.
     * @param write write permission.
     */
    public String getUserTree() {
        return getProperty("gui.usetree");
    }

    public void setUseTree(String useTree) {
        setProperty("gui.usetree", useTree);
    }

    public boolean getUseTree() {
        return Boolean.parseBoolean(getProperty("gui.usetree"));
    }

    public void addClienctConnection(String host, String user) {
        setProperty("net.client.connected", getProperty("net.client.connected") + host + " ");
        put("net.client.connected." + host, user);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.putAll(this);
        return properties;
    }

    public HashMap<String, String> getPasswordHistory() {
        return passwordHistory;
    }

    public HashMap<String, Boolean> getUserRights() {
        return userRights;
    }

    public HashMap<String, String> getUsernameHistory() {
        return usernameHistory;
    }

    public HashSet<String> getHostHistory() {
        return hostHistory;
    }

    public void setHostHistory(HashSet<String> hostHistory) {
        this.hostHistory = hostHistory;
    }

    public void setUsers(HashMap<String, String> users) {
        this.users = users;
    }

    public void setPasswordHistory(HashMap<String, String> passwordHistory) {
        this.passwordHistory = passwordHistory;
    }

    public void setUserRights(HashMap<String, Boolean> userRights) {
        this.userRights = userRights;
    }

    public void setUsernameHistory(HashMap<String, String> usernameHistory) {
        this.usernameHistory = usernameHistory;
    }
    
    
    
}

