/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui.hoststable;

import java.util.List;

/**
 * This contains a host for the hosts-box. Each host has a IP, port and a 
 * setting to autoconnect it or not.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class HostsObject {

    private String host;
    private boolean history;
    private String[] userAndPass;

    /**
     * Creates a new host and tells whether we should autoconnect or not.
     * 
     * @param host the host.
     * @param autoConnect tells if we should autoconnect or not.
     * @param info 
     */
    public HostsObject(String host, boolean history, String[] userAndPass) {
        this.host = host.replaceAll(" ", "");
        this.history = history;
        this.userAndPass = userAndPass;
    }

    /**
     * Returns if we should autoconnect to this host.
     * 
     * @return true or false.
     */
    public boolean isHistory() {
        return history;
    }

    /**
     * Set if we should autoconnect to this host.
     * 
     * @param autoConnect true or false.
     */
    public void setHistory(boolean history) {
        this.history = history;
    }

    /**
     * Returns the host IP and port.
     * 
     * @return the host IP and port.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host IP and port.
     * 
     * @param host the host IP and port.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the username and password for this current HostsObject
     * 
     * @return the list.
     */
    public String[] getInfo() {
        return userAndPass;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof HostsObject) {
            HostsObject other = (HostsObject) object;
            if (this.host.equals(other.getHost())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 59 * hash + (this.history ? 1 : 0);
        hash = 59 * hash + (this.userAndPass != null ? this.userAndPass.hashCode() : 0);
        return hash;
    }
}
