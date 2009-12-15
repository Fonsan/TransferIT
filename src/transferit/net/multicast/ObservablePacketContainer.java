package transferit.net.multicast;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import javax.swing.Timer;

/**
 * Contains the hosts that the MultiCasterListener has found. Maps a Timer to 
 * each found host to remove it from the list of available hosts if we stop
 * receiving packets from it.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class ObservablePacketContainer extends Observable implements ActionListener,Serializable {

    private int timeout;
    private Map<String, Timer> hosts = new HashMap<String, Timer>();
    private Map<String, String> hostnames = new HashMap<String, String>();

    /**
     * Initializes a packet with a specified timeout.
     * 
     * @param timeout the timeout in ms.
     */
    public ObservablePacketContainer(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the hosts.
     * 
     * @return a Map with the hosts.
     */
    public Map<String, Timer> getHosts() {
        return hosts;
    }

    /**
     * Returns the found hostnames.
     * 
     * @return a Map with the hostnames.
     */
    public synchronized Map<String, String> getHostnames() {
        return hostnames;
    }

    /**
     * Adds a found host.
     * 
     * @param host the host.
     */
    public void add(String host) {
        if (hosts.containsKey(host)) {
            hosts.get(host).restart();
        } else {
            addToHostnames(host);
            setChanged();
            notifyObservers(host);
            clearChanged();
            if (hosts.containsKey(host)) {
                TimeoutTimer timer = new TimeoutTimer(timeout, this, host);
                timer.start();
                hosts.put(host, timer);
            }
        }
    }

    /**
     * Tries to resolve a hostname.
     * 
     * @param host the hostname.
     */
    private synchronized void addToHostnames(String host) {
        String[] ipAndPort = host.split(" ");
        if (!hostnames.containsKey(ipAndPort[0])) {
            try {
                hostnames.put(ipAndPort[0], InetAddress.getByName(ipAndPort[0]).getHostName());
            } catch (UnknownHostException uhe) {
                hostnames.put(host, "Unkown");
            }
        }
    }

    /**
     * Removes a found host.
     * 
     * @param host host to remove.
     * @return if successfully removed
     */
    public synchronized boolean removeFromHostnames(String host) {
        return hostnames.keySet().remove(host);
    }

    /**
     * Returns found hosts.
     * 
     * @return the hosts.
     */
    public Set<String> getStrings() {
        return hosts.keySet();
    }

    /**
     * Triggers when a Timer sets off.
     * 
     * @param e the triggering timer.
     */
    public void actionPerformed(ActionEvent e) {
        Object object = e.getSource();
        if (object instanceof TimeoutTimer) {
            TimeoutTimer timer = (TimeoutTimer) object;
            hosts.remove(timer.getHost());
            timer.stop();

            setChanged();
            notifyObservers();
            clearChanged();
        }
    }

    /**
     * 
     */
    class TimeoutTimer extends Timer {

        private String host;

        /**
         * Creates a Timer with a delay, adds a listener to it and adds a host.
         * 
         * @param delay the delay.
         * @param listener the listener.
         * @param host the host.
         */
        public TimeoutTimer(int delay, ActionListener listener, String host) {
            super(delay, listener);
            this.host = host;
        }

        /**
         * Returns the host.
         * 
         * @return the host.
         */
        public String getHost() {
            return host;
        }
    }
}
