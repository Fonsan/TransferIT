package transferit.net.multicast;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import transferit.net.PlatformIndenpendentInetAddress;

/**
 * Multicaster has a Listener and a Client that sends and recevies packets with
 * IP and port of local and remote hosts. When a host is found it is added to 
 * the container.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class MultiCaster extends Thread implements Serializable{

    private int port;
    private InetAddress group;
    private MulticastSocket multicastSocket;
    private ObservablePacketContainer observablePacketContainer;
    private DatagramPacket packetToSend;
    private Timer client;

    /**
     * Starts the MultiCaster.
     * 
     * @param port the port to listen on.
     * @param beaconInterval the interval to use when sending.
     * @param group your IP
     * @param portToSend
     * @param observablePacketContainer 
     */
    public MultiCaster(int port, int beaconInterval, InetAddress group, String portToSend, ObservablePacketContainer observablePacketContainer) {
        this.port = port;
        this.group = group;
        this.observablePacketContainer = observablePacketContainer;
        String inetadr = "";
        try {
            inetadr = PlatformIndenpendentInetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {   
        }
        byte[] bytesToSend = (inetadr + " " + portToSend).getBytes();
        packetToSend = new DatagramPacket(bytesToSend, bytesToSend.length, group, port);
        client = new Timer(beaconInterval, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    multicastSocket.send(packetToSend);
                } catch (SocketException se) {
                    client.stop();
                } catch (IOException ex) {
                }
            }
        });
    }

    /**
     * Starts the Listener and Client and initializes the container.
     */
    @Override
    public void run() {
        try {
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(group);
            client.start();
            try {
                byte[] buffer = "255.255.255.255 65555".getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                String sbuf;
                while (true) {
                    multicastSocket.receive(packet);
                    observablePacketContainer.add(new String(buffer, 0, packet.getLength()));
                }
            } catch (IOException ex) {
            }
        } catch (IOException ex) {
            Logger.getLogger(MultiCaster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
