/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.net.upnp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ActionList;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import transferit.TransferIT;
import transferit.net.PlatformIndenpendentInetAddress;

/**
 *
 * @author fonsan
 */
public class upnpFirewallPortOpener extends Observable implements NotifyListener, EventListener, SearchResponseListener {

    private boolean alive = false;
    private String externalIPAddress = null;
    private boolean foundDevice = false;
    private ControlPoint controlPoint = null;
    private int portToOpen;
    private InetAddress lanAddr = null;
    public final String GETGENERICPORT = "GetGenericPortMappingEntry";
    public final String ADDPORTMAPPING = "AddPortMapping";
    public final String GETEXTERNALIPADDR = "GetExternalIPAddress";
    public final String DELETEPORTMAPPING = "DeletePortMapping";
    private int portOpened = -1;
    private HashMap<Integer, Integer> forwards = new HashMap<Integer, Integer>();

    public upnpFirewallPortOpener(int portToOpen) {
        controlPoint = new ControlPoint();
        controlPoint.addNotifyListener(this);
        controlPoint.addSearchResponseListener(this);
        controlPoint.addEventListener(new org.cybergarage.upnp.event.EventListener() {

            public void eventNotifyReceived(String uuid, long seq, String varName, String value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        this.portToOpen = portToOpen;

    }

    public void setExternalIPAddress(String externalIPAddress) {
        this.externalIPAddress = externalIPAddress;
        try {
            lanAddr = PlatformIndenpendentInetAddress.getLocalHost();
        } catch (Exception e) {
        }
    }

    public void setFoundDevice(boolean foundDevice) {
        this.foundDevice = foundDevice;
        if (foundDevice && externalIPAddress == null) {
            setChanged();
            notifyObservers(foundDevice);
            setChanged();
            notifyObservers(getAllActions(controlPoint.getDeviceList()));
            if (TransferIT.DEBUG) {
                for (Action a : getAllActions(controlPoint.getDeviceList())) {
                    System.out.println(a.getName());
                }
            }
        }

    }

    public boolean isAlive() {
        return alive;
    }

    public void start() {
        alive = true;
        controlPoint.start();
    }

    public void deviceNotifyReceived(SSDPPacket ssdpPacket) {
        //System.out.println(new String(ssdpPacket.getData()));
        setFoundDevice(checkForRootDev(controlPoint.getDeviceList()));
    }

    public void deviceSearchResponseReceived(SSDPPacket ssdpPacket) {
        //System.out.println(new String(ssdpPacket.getData()));
        setFoundDevice(checkForRootDev(controlPoint.getDeviceList()));
    }

    public boolean checkForRootDev(DeviceList dl) {
        Iterator it = dl.iterator();
        while (it.hasNext()) {
            if (((Device) it.next()).isRootDevice()) {
                return true;
            }
        }
        return false;
    }

    public String getFristOutputArg(ArgumentList outArgList) {
        Argument arg = outArgList.getArgument(0);
        String value = arg.getValue();
        return value;
    }

    public ArgumentList doAction(String actionName) {
        return doAction(actionName, null);
    }

    public ArgumentList doAction(String actionName, ArgumentList args) {
        Action action = getActionFromList(actionName, getAllActions(controlPoint.getDeviceList()));
        if (args != null) {
            action.setArgumentValues(args);
        }
        boolean ctrlRes = action.postControlAction();
        if (ctrlRes == true) {
            for (int x = 0; x < action.getOutputArgumentList().size(); x++) {
                Argument a = action.getOutputArgumentList().getArgument(x);
            }
            return action.getOutputArgumentList();
        } else {
            return new ArgumentList();
        }
    }

    public Action getAction(String name) {
        return getActionFromList(name, getAllActions(controlPoint.getDeviceList()));
    }

    public Action getActionFromList(String actionName, ArrayList<Action> actions) {
        for (Action action : getAllActions(controlPoint.getDeviceList())) {
            if (action.getName().equals(actionName)) {
                return action;
            }
        }
        throw new IllegalArgumentException(actionName);
    }

    public ArrayList<Action> getAllActions(DeviceList deviceList) {
        ArrayList<Action> actions = new ArrayList<Action>();
        for (Object d : deviceList) {
            actions.addAll(getAllActions(((Device) d).getDeviceList()));
            for (Object s : ((Device) d).getServiceList()) {
                Service service = (Service) s;
                ActionList actionList = service.getActionList();
                for (Object a : actionList) {
                    Action action = (Action) a;
                    actions.add(action);
                }
            }
        }
        return actions;
    }

    private Boolean checkForwards() {
        ArgumentList in = getAction(GETEXTERNALIPADDR).getArgumentList();

        boolean rtnval = false;
        for (int x = 0; true; x++) {
            in.set(0, new Argument(in.getArgument(0).getName(), x + ""));
            ArgumentList out = doAction(GETGENERICPORT, in);
            if (out.size() == 0) {
                break;
            }
            int internal = 0;
            int external = 0;
            int enabled = 0;
            Boolean checkForward = null;
            for (int y = 0; y < out.size(); y++) {
                Argument arg = out.getArgument(y);

                
                if (arg.getName().equals("NewExternalPort")) {
                    external = Integer.parseInt(arg.getValue());
                } else if (arg.getName().equals("NewInternalPort")) {
                    internal = Integer.parseInt(arg.getValue());
                } else if (arg.getName().equals("NewInternalClient")) {
                    System.out.println(arg.getName() + " " + arg.getValue());
                    System.out.println(lanAddr.getHostAddress());
                    if (arg.getValue().equals(lanAddr.getHostAddress())) {
                        checkForward = true;
                    }
                } else if (arg.getName().equals("NewEnabled")) {
                    enabled = Integer.parseInt(arg.getValue());
                }
            }
            forwards.put(external, internal);
            if (checkForward && internal == portToOpen) {
                rtnval = true;
            }
        }
        return rtnval;
    }

    public void removeForwarding() {
        if (portOpened == -1) {
            return;
        }
        Action action = getAction(DELETEPORTMAPPING);
        ArgumentList in = action.getArgumentList();
        for (int y = 0; y < in.size(); y++) {
            Argument arg = in.getArgument(y);
            if (arg.getName().equals("NewExternalPort")) {
                arg.setValue(portOpened);
            } else if (arg.getName().equals("NewProtocol")) {
                arg.setValue("TCP");
            }
        }
        ArgumentList out = doAction(DELETEPORTMAPPING, in);
        if (TransferIT.DEBUG) {
            Enumeration e = out.elements();
            while (e.hasMoreElements()) {
                Argument arg = (Argument) e.nextElement();
                System.out.println(arg.getName() + "=" + arg.getValue());
            }
        }
        setChanged();
        notifyObservers(false);        
    }

    public void doPortForwarding() {
        setExternalIPAddress(getFristOutputArg(doAction(GETEXTERNALIPADDR)));
        if (checkForwards()) {
            portOpened = portToOpen;
        } else if (openPort(portToOpen)) {
            portOpened = portToOpen;
        } else {
            int forwardPort = getValidForwardPort();
            openPort(forwardPort);
            portOpened = forwardPort;
        }
        String connectAddr = externalIPAddress + ":" + portOpened;
        setChanged();
        notifyObservers(connectAddr);
    }

    private boolean openPort(int portToOpen) {
        Action action = getAction(ADDPORTMAPPING);
        ArgumentList in = action.getArgumentList();
        for (int y = 0; y < in.size(); y++) {
            Argument arg = in.getArgument(y);

            if (arg.getName().equals("NewExternalPort")) {
                arg.setValue(portToOpen);
            } else if (arg.getName().equals("NewInternalPort")) {
                arg.setValue(this.portToOpen);
            } else if (arg.getName().equals("NewInternalClient")) {
                arg.setValue(lanAddr.getHostAddress());
            } else if (arg.getName().equals("NewEnabled")) {
                arg.setValue(1);
            } else if (arg.getName().equals("NewProtocol")) {
                arg.setValue("TCP");
            } else if (arg.getName().equals("NewPortMappingDescription")) {
                arg.setValue("TransferIT_" + lanAddr.getHostAddress());
            }
        }
        ArgumentList out = doAction(ADDPORTMAPPING, in);
        if (TransferIT.DEBUG) {
            Enumeration e = out.elements();
            while (e.hasMoreElements()) {
                Argument arg = (Argument) e.nextElement();
                System.out.println(arg.getName() + "=" + arg.getValue());
            }
        }
        return true;
    }

    public int getValidForwardPort() {
        while (true) {
            int randPort = getRandomForwardPort();
            if (!forwards.keySet().contains(randPort)) {
                return randPort;
            }
        }

    }

    public int getRandomForwardPort() {
        int offset = 10000;
        int max = 60000;
        return offset + new Random().nextInt(max - offset);
    }

    public static void main(String args[]) {
        try {
            new upnpFirewallPortOpener(62121);
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(upnpFirewallPortOpener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

