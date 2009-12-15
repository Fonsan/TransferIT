package transferit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.cybergarage.upnp.Action;
import transferit.gui.GUIView;
import transferit.gui.LocalFileBrowser;
import transferit.gui.RemoteFileBrowser;
import transferit.gui.SettingFrame;
import transferit.gui.UPNPGUI;
import transferit.gui.filebrowsers.FileInfo;
import transferit.gui.filebrowsers.filelistbrowser.RemoteFileListBrowser;
import transferit.net.PlatformIndenpendentInetAddress;
import transferit.net.Protocol.ClientCommands;
import transferit.net.Protocol.ServerCommands;
import transferit.net.client.ClientConnectionThread;
import transferit.net.multicast.MultiCaster;
import transferit.net.multicast.ObservablePacketContainer;
import transferit.net.server.ServerConnectionHandler;
import transferit.net.server.ServerConnectionThread;
import transferit.net.upnp.upnpFirewallPortOpener;

/**
 * This is the Controller.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class TransferITController implements Observer, ActionListener, ChangeListener {

    private TransferITModel transferITModel;
    private GUIView gUIView;
    private boolean serverIsAlive = false;
    private boolean serverFirewall = false;
    private boolean internetSharing = false;
    private Thread serverThread;
    private Timer firewallMultiCastTimeout;
    public final int DOUBLECLICK = 2;
    private final String NAME_COLUMN = "Name";
    private String multiCastLocal;
    private upnpFirewallPortOpener upnp;
    public static final int UPDATESPEED = 10;
    private Timer updateSpeedAndETA = new Timer(UPDATESPEED, new ActionListener() {

        public static final int LOGSIZE = 50;
        public static final int TIMEPASSED = UPDATESPEED * LOGSIZE;
        private long[] bytesTransferredLog = new long[LOGSIZE];
        private long bytesTransferred = 0;
        private int logIndex = -1;

        private int updateLogIndex() {
            return logIndex = logIndex == LOGSIZE - 1 ? 0 : logIndex + 1;
        }

        private void updateStatus(ClientConnectionThread cct) {
            final long bytesLeft = cct.getBytesLeftToTransferInQueue();
            final long max = cct.getBytesMaxInQueue();
            long tempBytes = (max - bytesLeft) - bytesTransferred;
            bytesTransferred += tempBytes;
            int index = updateLogIndex();
            bytesTransferredLog[index] = tempBytes;
            if (cct.getTimePassed() < TIMEPASSED || cct.getTimePassed() % TIMEPASSED != 0) {
                return;
            }
            long sumOfBytes = 0;
            for (long l : bytesTransferredLog) {
                sumOfBytes += l;
            }
            double result = 0;
            result = (sumOfBytes / TIMEPASSED);

            gUIView.getTransferSpeed().setText("Speed " + (long) result + " kb/s");

            result = (bytesLeft / result);
            long seconds = (long) result / 600;
            long minutes = (long) result / 600 / 60;
            if (TransferIT.DEBUG) {
                System.out.println("Res " + result);
                System.out.println("Min " + minutes);
                System.out.println("Sec " + seconds);
                System.out.println("Temp " + tempBytes);
            }
            gUIView.getETALabel().setText("Time left " + minutes + ':' + seconds);
        }

        private void updateProgressBars(ClientConnectionThread cct) {
            long transferedQueue = cct.getBytesMaxInQueue() - cct.getBytesLeftToTransferInQueue();
            if (transferedQueue == 0) {
                return;
            }
            double queueProgress = (double) transferedQueue / cct.getBytesMaxInQueue();
            queueProgress *= 1000000;
            gUIView.getProgressBarProcess().setValue((int) queueProgress);
            gUIView.getQueueProgressBarLabel().setText("File queue progress " + gUIView.getProgressBarProcess().getString());

            long transferedFile = cct.getBytesMaxInFile() - cct.getBytesLeftToTransferInFile();
            if (transferedFile == 0) {
                return;
            }
            double fileProgress = (double) transferedFile / cct.getBytesMaxInFile();
            fileProgress *= 1000000;
            gUIView.getProgressBarFile().setValue((int) fileProgress);
            gUIView.getFileProgressBarLabel().setText("File progress " + gUIView.getProgressBarFile().getString());
        }

        private void stopTimer() {
            updateSpeedAndETA.stop();
            bytesTransferred = 0;
            logIndex = -1;
            gUIView.getFileProgressBarLabel().setText("File progress 0%");
            gUIView.getQueueProgressBarLabel().setText("File queue progress 0%");
            gUIView.getTransferSpeed().setText("Speed 0 kb/s");
            gUIView.getETALabel().setText("Time left 0:00");
            gUIView.getProgressBarProcess().setValue(0);
            gUIView.getProgressBarFile().setValue(0);
        }

        public void actionPerformed(ActionEvent e) {
            RemoteFileBrowser rfb =
                    getRemoteFileBrowserFromTabComponent(gUIView.getRemotetJTabbedPane().getSelectedComponent());
            if (rfb == null) {
                stopTimer();
            } else {
                ClientConnectionThread cct = rfb.getClientConnectionThread();
                cct.updateTimePassed(10);
                updateProgressBars(cct);
                final long bytesLeft = cct.getBytesLeftToTransferInQueue();
                if (bytesLeft > 0) {
                    updateStatus(cct);
                } else {
                    stopTimer();
                }
            }
        }
    });
    private MouseAdapter localFileListListener = new MouseAdapter() {

        private final File BACK = new File("..");

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == DOUBLECLICK) {
                LocalFileBrowser lfb = gUIView.getLocalFileBrowser();
                /* get the table so that we are able to get some information */
                JTable target = (JTable) e.getSource();
                int getRow = target.getSelectedRow();
                int getColumn = target.getColumn(NAME_COLUMN).getModelIndex();

                Object value = target.getValueAt(getRow, getColumn);

                if (value.equals(BACK)) {
                    lfb.goUp();
                } else if (value instanceof File) {
                    File file = (File) value;
                    try {
                        if (file.isDirectory()) {
                            lfb.goTo(file.getAbsolutePath());
                        }
                    } catch (NullPointerException ex) {
                        // notify the GUI
                        //System.out.println("Can't open directory: " + file);
                    }
                } else if (value instanceof Object[]) {
                    lfb.goFromRoot(getRow);
                }
                gUIView.getLocalTextPath().setText(lfb.getCurrentDirectory());
            }
        }
    };
    private MouseAdapter remoteFileListListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == DOUBLECLICK) {
                RemoteFileBrowser rflb = getRemoteFileBrowserFromTabComponent(
                        gUIView.getRemotetJTabbedPane().getSelectedComponent());
                /* get the table so that we are able to get some information */
                JTable target = (JTable) e.getSource();

                int getRow = target.getSelectedRow();
                int getColumn = target.getColumn(NAME_COLUMN).getModelIndex();

                Object value = target.getValueAt(getRow, getColumn);

                if (value.equals(RemoteFileListBrowser.BACK)) {
                    rflb.goUp();
                } else if (value instanceof FileInfo) {
                    final FileInfo file = (FileInfo) value;
                    if (file.isFolder()) {
                        // adding the current path

                        /* when using .size(); to get the last element from an ArrayList you have to take minus 1 since 
                         * the ArrayList starts counting from 0, ex 1 element in an ArrayList equals size() = 1, but it is placed
                         * in place 0 in the list
                         */
                        String path = file.getFilePath();

                        rflb.goTo(path);
                        gUIView.getRemoteTextPath().setText(path);

                    }
                }
                gUIView.getRemoteTextPath().setText(rflb.getCurrentDirectory());
            }
        }
    };
    private KeyAdapter connectEnterListner = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                gUIView.getConnectButton().doClick();
            }
        }
    };
    private KeyAdapter localPathEnterListner = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                gUIView.getLocalTextPathButton().doClick();
            }
        }
    };
    private KeyAdapter remotePathEnterListner = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                gUIView.getRemoteTextPathButton().doClick();
            }
        }
    };
    private KeyAdapter remoteBrowserEnterListner = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                RemoteFileBrowser rflb = getRemoteFileBrowserFromTabComponent(
                        gUIView.getRemotetJTabbedPane().getSelectedComponent());
                for (FileInfo fileInfo : rflb.getSelectedFiles()) {
                    if (fileInfo.isFolder()) {
                        rflb.goTo(fileInfo.getFilePath());
                        break;
                    }
                }
            }
        }
    };
    private KeyAdapter localBrowserEnterListner = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                LocalFileBrowser lflb = gUIView.getLocalFileBrowser();
                for (File file : lflb.getSelectedFiles()) {
                    if (file.isDirectory()) {
                        lflb.goTo(file.getAbsolutePath());
                        break;
                    }
                }
            }
        }
    };

    public TransferITController(TransferITModel transferITModel) {
        this.transferITModel = transferITModel;
        try {
            multiCastLocal = PlatformIndenpendentInetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhe) {
        }

        firewallMultiCastTimeout = new Timer(Integer.parseInt(
                transferITModel.getProperty("net.multicast.timeout")), new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setMultiCastFirewall(false);
                firewallMultiCastTimeout.stop();
            }
        });
        upnp = new upnpFirewallPortOpener(transferITModel.getServerPort());
    }

    public KeyAdapter getLocalBrowserEnterListner() {
        return localBrowserEnterListner;
    }

    public KeyAdapter getLocalPathEnterListner() {
        return localPathEnterListner;
    }

    public KeyAdapter getRemoteBrowserEnterListner() {
        return remoteBrowserEnterListner;
    }

    public KeyAdapter getRemotePathEnterListner() {
        return remotePathEnterListner;
    }

    public boolean isServerFirewall() {
        return serverFirewall;
    }

    public void setServerFirewall(boolean serverFirewall) {
        this.serverFirewall = serverFirewall;
    }

    public boolean isServerIsAlive() {
        return serverIsAlive;
    }

    public MouseAdapter getLocalFileListListener() {
        return localFileListListener;
    }

    public MouseAdapter getRemoteFileListListener() {
        return remoteFileListListener;
    }

    public KeyAdapter getConnectEnterListner() {
        return connectEnterListner;
    }

    public void setServerIsAlive(boolean serverIsAlive) {
        this.serverIsAlive = serverIsAlive;
        if (serverIsAlive) {
            Thread testInterfaces = new Thread() {

                @Override
                public void run() {
                    testServerFirewall();
                    updateUPNP(Boolean.parseBoolean(transferITModel.getProperty("net.upnp.forward")));
                }
            };
            TransferIT.getTransferITThreadFactory().dispatchNewThread(testInterfaces,"TestInterfaces");
        }
    }
    private boolean multiCastFirewall = false;

    public boolean isMultiCastFirewall() {
        return multiCastFirewall;
    }

    public void setMultiCastFirewall(boolean multiCastFirewall) {
        this.multiCastFirewall = multiCastFirewall;
        gUIView.getAutoDetectIsAlive().setIcon(multiCastFirewall ? gUIView.TRUE_ICON : gUIView.FALSE_ICON);
    }

    public void setGUIView(GUIView gUIView) {
        this.gUIView = gUIView;
    }

    public GUIView getGUIView() {
        return gUIView;
    }

    public TransferITModel getTransferITModel() {
        return transferITModel;
    }

    public void init() {
        actionPerformed(new ActionEvent(gUIView.getCheckBoxAnonym(), (int) (Integer.MAX_VALUE * Math.random()), null));
        startUPNP();
        if (Boolean.parseBoolean(transferITModel.getProperty("net.server.isalive"))) {
            startServer();
        }
        if (Boolean.parseBoolean(transferITModel.getProperty("net.multicast.isalive"))) {
            startMultiCaster();
        }
    }

    private void startUPNP() {
        upnp.addObserver(this);
        upnp.start();
    }

    private void startServer() {
        int port = Integer.parseInt(transferITModel.getProperty("net.server.port"));
        if (!(new File(transferITModel.getProperty("net.server.rootpath"))).isDirectory()) {
            JOptionPane.showMessageDialog(gUIView, "Please choose a folder to share");
            TransferIT.checkRootDirectory();
        }
        ServerConnectionHandler serverConnectionHandler = new ServerConnectionHandler(port, transferITModel.getProperty("net.server.rootpath"));
        transferITModel.setServerConnectionHandler(serverConnectionHandler);
        serverConnectionHandler.addObserver(this);
        serverThread = new Thread(serverConnectionHandler);
        TransferIT.getTransferITThreadFactory().dispatchNewThread(serverThread,"Server");
    }

    private void testServerFirewall() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            Map<InetAddress, Boolean> netsconnected = new HashMap<InetAddress, Boolean>();
            while (nets.hasMoreElements()) {
                NetworkInterface net = nets.nextElement();
                Enumeration<InetAddress> inetAddresses = net.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inet = inetAddresses.nextElement();
                    if (!inet.getHostAddress().equals("127.0.0.1")) {
                        netsconnected.put(inet, false);
                    }
                }
            }
            int leftToConnect = netsconnected.size();
            while (leftToConnect != 0) {
                for (InetAddress inet : netsconnected.keySet()) {
                    Thread.sleep(100);
                    if (!netsconnected.get(inet)) {
                        if (testInetAddr(inet)) {
                            leftToConnect--;
                            netsconnected.put(inet, true);
                        }
                    }
                }
            }
            gUIView.setFirewallPassedIcon(gUIView.TRUE_ICON);
        } catch (SocketException ex) {
            Logger.getLogger(TransferITController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(TransferITController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean testInetAddr(InetAddress inet) {
        boolean returnValue = false;
        Socket socket = null;
        try {
            socket = new Socket(inet, Integer.parseInt(transferITModel.getProperty("net.server.port")));
            socket.setTcpNoDelay(true);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeUTF(ClientCommands.NOVALUE.toString());
            out.flush();

            ServerCommands command = ServerCommands.valueOf(in.readUTF());

            if (ServerCommands.SENDQUIT.equals(command)) {
                gUIView.appendToLog("Firewall settings sufficient for server on interface " +
                        inet.getHostAddress());
                socket.close();
                returnValue = true;
            }
        } catch (ConnectException soc) {
            returnValue = true;
        } catch (NoRouteToHostException soc) {
            returnValue = true;
        } catch (PortUnreachableException soc) {
            returnValue = true;
        } catch (SocketException soc) {
            gUIView.appendToLog("Firewall in place on interface " + inet.getHostAddress());
            returnValue = true;
        } catch (EOFException eof) {
            gUIView.appendToLog(eof.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(TransferITController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    private void startMultiCaster() {
        ObservablePacketContainer observablePacketContainer = new ObservablePacketContainer(
                Integer.parseInt(transferITModel.getProperty("net.multicast.timeout")));
        transferITModel.setObservablePacketContainer(observablePacketContainer);

        observablePacketContainer.addObserver(this);
        try {
            MultiCaster multiCaster = new MultiCaster(
                    Integer.parseInt(transferITModel.getProperty("net.multicast.port")),
                    Integer.parseInt(transferITModel.getProperty("net.multicast.interval")),
                    InetAddress.getByName(transferITModel.getProperty("net.multicast.inetgroup")),
                    transferITModel.getServerPort() + "",
                    observablePacketContainer);
            TransferIT.getTransferITThreadFactory().dispatchNewThread(multiCaster,"MultiCaster");
        } catch (UnknownHostException ex) {
            Logger.getLogger(TransferITController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startClientConnectionThread(String host, String username, String password) {
        int port;
        try {
            if (host.equals("")) {
                gUIView.appendToLog("Please supply a host");
                return;
            }
            if (host.contains(":")) {
                String url[] = host.split("\\:");
                host = url[0];
                port = Integer.parseInt(url[1]);
            } else {
                port = Integer.parseInt(transferITModel.getProperty("net.default.port"));
            }
            JCheckBox checkBox = gUIView.getCheckBoxAnonym();
            if (checkBox.isSelected()) {
                username = transferITModel.getProperty("net.default.username");
                password = transferITModel.getProperty("net.default.password");
            }
        } catch (NumberFormatException nex) {
            gUIView.appendToLog("Wrong url");
            return;
        }
        final String finHost = host;
        final int finPort = port;
        final String finusername = username;
        final String finpassword = password;
        gUIView.getUsernameInput().setText(finusername);
        gUIView.getPasswordInput().setText(finpassword);
        final TransferITController finTransferITController = this;
        Thread newClient = new Thread() {

            private ClientConnectionThread clientConnectionThread;

            @Override
            public void run() {
                InetAddress inetAddress;
                try {
                    inetAddress = InetAddress.getByName(finHost);
                } catch (UnknownHostException uex) {
                    gUIView.appendToLog("Unkown host " + finHost);
                    return;
                }
                try {
                    Socket socket = new Socket(inetAddress, finPort);
                    clientConnectionThread = new ClientConnectionThread(socket, finusername, finpassword);
                    clientConnectionThread.addObserver(finTransferITController);
                    transferITModel.addClientConnectionToSet(clientConnectionThread);
                    Thread client = new Thread(clientConnectionThread);
                    TransferIT.getTransferITThreadFactory().dispatchNewThread(client,"ClientConnection-" + socket.getInetAddress().getHostAddress());
                } catch (SocketException se) {
                    gUIView.appendToLog(se.getMessage());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        TransferIT.getTransferITThreadFactory().dispatchNewThread(newClient,"ClientStarter");
    }

    public RemoteFileBrowser getRemoteFileBrowserFromTabComponent(Component component) {
        for (RemoteFileBrowser browser : gUIView.tabs) {
            if (component instanceof JScrollPane) {
                JScrollPane jsp = (JScrollPane) component;
                JViewport jv = jsp.getViewport();
                if (browser.getBrowser() == jv.getView()) {
                    return browser;
                }
            }

        }
        return null;
    }

    public void updateServerStatus(String rootPath) {
        if (!new File(rootPath).isDirectory()) {
            throw new IllegalArgumentException(rootPath);
        }
        transferITModel.setRootPath(rootPath);
        ServerConnectionHandler sch = transferITModel.getServerConnectionHandler();
        if (sch == null) {
            startServer();
            return;
        }
        if (sch.isIsAlive()) {
            if (sch.getServerSocket() != null) {
                try {

                    sch.getServerSocket().close();
                } catch (IOException ioe) {
                }
                serverThread.interrupt();
                sch.setIsAlive(false);
                gUIView.setFirewallPassedIcon(gUIView.FALSE_ICON);
                gUIView.setServerIsAliveIcon(gUIView.FALSE_ICON);
            }
        } else {
            startServer();
        }
    }

    public void updateUPNP(boolean start) {
        if (start) {
            forwardUPNPPort();
        } else {
            removeUPNPPort();
        }
        transferITModel.setProperty("net.upnp.forward", Boolean.toString(start));
    }

    public void forwardUPNPPort() {
        gUIView.getInternetSharing().setIcon(gUIView.TRUE_ICON);
        upnp.doPortForwarding();

    }

    public void removeUPNPPort() {
        gUIView.getInternetSharing().setIcon(gUIView.FALSE_ICON);
        gUIView.setDefaultConnectText();
        upnp.removeForwarding();
    }

    public static void recursiveDelete(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            for (String subPath : file.list()) {
                File subFile = new File(file.getAbsolutePath() + File.separatorChar + subPath);
                if (subFile.isDirectory()) {
                    recursiveDelete(subFile.getAbsolutePath());
                }
                subFile.delete();
            }
            file.delete();
        } else {
            try {
                file.delete();
            } catch (SecurityException se) {
            }
        }
    }

    private void transferFiles(RemoteFileBrowser rflb) {
        ClientConnectionThread cct = rflb.getClientConnectionThread();
        synchronized (cct) {
            updateSpeedAndETA.start();
            while (!cct.getTransferList().isEmpty()) {
                Object o = cct.getTransferList().pop();
                if (o instanceof FileInfo) {
                    FileInfo fileInfo = (FileInfo) o;
                    cct.sendFileGetRequest(cct.getFilesToDownLoad().get(fileInfo), fileInfo.getFilePath());
                    cct.getFilesToDownLoad().remove(fileInfo);
                } else if (o instanceof File) {
                    File file = (File) o;
                    cct.sendFileSendRequest(file, cct.getFilesToUpload().get(file));
                    cct.getFilesToUpload().remove(file);
                }
                cct.getDefaultTableModel().removeRow(0);
            }
            cct.setBytesMaxInQueue(0);
            cct.setBytesLeftToTransferInQueue(0);
            gUIView.getLocalFileBrowser().update();
        }
    }

    private void addFilesToDownload(RemoteFileBrowser rflb) {
        ClientConnectionThread cct = rflb.getClientConnectionThread();
        String homePath = gUIView.getLocalFileBrowser().getCurrentDirectory();

        for (FileInfo fileInfo : rflb.getSelectedFiles()) {
            if (fileInfo.isFolder()) {
                new File(homePath + File.separatorChar + fileInfo.getFileName()).mkdirs();
                recursiveDownload(homePath, fileInfo, cct);
            } else {
                cct.addDownload(fileInfo, homePath);
            }
        }
    }

    private void recursiveDownload(String homePath, FileInfo folder, ClientConnectionThread cct) {
        homePath = homePath + File.separatorChar + folder.getFileName();
        String remotePath = folder.getFilePath();

        for (FileInfo fileInfo : cct.sendRecursiveDirectoryListingRequest(folder.getFilePath())) {
            String pathToSave = homePath + fileInfo.getFilePath().replace(remotePath, "");
            if (pathToSave.indexOf(File.separatorChar) != -1) {
                pathToSave = pathToSave.substring(0, pathToSave.lastIndexOf(File.separatorChar));
            }
            if (fileInfo.isFolder()) {
                recursiveDownload(pathToSave, fileInfo, cct);
            } else {
                cct.addDownload(fileInfo, pathToSave);
            }
        }

    }

    private void addFilesToUpload(RemoteFileBrowser rflb) {
        ClientConnectionThread cct = rflb.getClientConnectionThread();
        String remotePath = rflb.getCurrentDirectory();
        for (File file : gUIView.getLocalFileBrowser().getSelectedFiles()) {
            if (file.isDirectory()) {
                recursiveUpload(remotePath, file, cct);
            } else {
                cct.addUpload(file, remotePath);
            }
        }
    }

    private void recursiveUpload(String remotePath, File folder, ClientConnectionThread cct) {
        remotePath = remotePath + File.separatorChar + folder.getName();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                recursiveUpload(remotePath, file, cct);
            } else {
                cct.addUpload(file, remotePath);
            }
        }
    }

    private void interpretClientCommand(ServerConnectionThread serverConnectionThread, ClientCommands command) {
        switch (command) {
            case CREATEDIR:
                serverConnectionThread.getCreateDirectoryRequest();
                break;
            case DELETE:
                serverConnectionThread.getDeleteRequest();
                break;
            case GETFILE:
                serverConnectionThread.getFileGetRequest();
                break;
            case GETFILELIST:
                serverConnectionThread.getDirectoryListingRequest();
                break;
            case GETRECURSIVEFILELIST:
                serverConnectionThread.getRecursiveDirectoryListingRequest();
                break;
            case SENDAUTH:
                String clientusername = serverConnectionThread.readString();
                String clientpassword = serverConnectionThread.readString();
                boolean authed = false;
                for (String username : transferITModel.getUsers().keySet()) {
                    if (clientusername.equals(username)) {
                        try {
                            if (transferITModel.getUserAuth(username, clientpassword)) {
                                authed = true;
                                serverConnectionThread.sendAuthed();
                                gUIView.appendToLog(serverConnectionThread.getHostAndPort() +
                                        "Accepted new connection ");
                                if (transferITModel.getUserWriteAccess(username)) {
                                    serverConnectionThread.sendAuthWrite();
                                } else {
                                    serverConnectionThread.sendNotAuthWrite();
                                }
                            }
                        } catch (IllegalArgumentException e) {
                        }
                        break;
                    }
                }
                if (!authed) {
                    serverConnectionThread.sendNotAuthed();
                }
                break;
            case SENDFILE:
                serverConnectionThread.getFileSendRequest();
                break;
            case SENDMSG:
                gUIView.appendToLog(serverConnectionThread.getHostAndPort() +
                        "Sent message: " + serverConnectionThread.getMessage());
                break;
            case SENDQUIT:
                gUIView.appendToLog(serverConnectionThread.getHostAndPort() + "Has quit");
                serverConnectionThread.getQuit();
                break;
            case NOVALUE:
                serverConnectionThread.sendQuit();
                break;
            default:
                break;
        }
    }

    private void interpretServerCommand(ClientConnectionThread clientConnectionThread, ServerCommands command) {
        switch (command) {
            case AUTHED:
                gUIView.addNewRemoteTab(clientConnectionThread);
                gUIView.appendToLog(clientConnectionThread.getHost() + "Logged in");
                break;
            case SENDQUIT:
                gUIView.appendToLog(clientConnectionThread.getHost() + "Login failed");
                clientConnectionThread.getQuit();
                break;
            default:
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Object object = e.getSource();
        final ActionEvent ev = e;
        Thread interpret = new Thread() {

            @Override
            public void run() {
                if (object == gUIView.getCheckBoxAnonym()) {
                    boolean selected = gUIView.getCheckBoxAnonym().isSelected();
                    gUIView.getUsernameInput().setEditable(!selected);
                    gUIView.getPasswordInput().setEditable(!selected);
                    transferITModel.setProperty("gui.useanonymous", Boolean.toString(selected));
                } else if (object instanceof JButton) {
                    if (object == gUIView.getServerIsAlive()) {
                        updateServerStatus(transferITModel.getRootPath());
                    } else if (object == gUIView.getConnectButton()) {
                        transferITModel.setProperty("gui.host", gUIView.getHostInput().getText());
                        transferITModel.setProperty("gui.username", gUIView.getUsernameInput().getText());
                        startClientConnectionThread(gUIView.getHostInput().getText(),
                                gUIView.getUsernameInput().getText(),
                                String.valueOf(gUIView.getPasswordInput().getPassword()));
                        return;
                    } else if (object == gUIView.getAutoDetectIsAlive()) {
                        setMultiCastFirewall(false);
                    } else if (object == gUIView.getFirewallPassed()) {
                        gUIView.getFirewallPassed().setIcon(gUIView.FALSE_ICON);
                        testServerFirewall();
                    } else if (object == gUIView.getInternetSharing()) {
                        updateUPNP(gUIView.FALSE_ICON == gUIView.getInternetSharing().getIcon());
                    }

                    LocalFileBrowser lflb = gUIView.getLocalFileBrowser();

                    if (object == gUIView.getLocalBackButton()) {
                        lflb.goUp();
                        gUIView.getLocalTextPath().setText(lflb.getCurrentDirectory());
                    } else if (object == gUIView.getLocalDeleteButton()) {
                        int choice = JOptionPane.showConfirmDialog(gUIView, "Really delete files?",
                                "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                        if (choice == JOptionPane.OK_OPTION) {
                            for (String pathToDelete : lflb.getSelectedPaths()) {
                                TransferITController.recursiveDelete(pathToDelete);
                            }
                        }
                        lflb.update();
                    } else if (object == gUIView.getLocalRefreshButton()) {
                        lflb.update();
                    } else if (object == gUIView.getLocalCreateFolderButton()) {
                        File folder = null;
                        do {
                            String input = JOptionPane.showInputDialog(gUIView, "Choose folder name", "New folder");
                            if (input == null) {
                                return;
                            }
                            try {
                                folder = new File(lflb.getCurrentDirectory() +
                                        File.separatorChar + input);
                                folder.mkdir();
                            } catch (SecurityException se) {
                            }

                        } while (!folder.isDirectory());
                        lflb.update();
                    } else if (object == gUIView.getLocalTextPathButton()) {
                        gUIView.getLocalFileBrowser().goTo(gUIView.getLocalTextPath().getText());
                    } else if (object == gUIView.getLocalChangeViewButton()) {
                        gUIView.changeLocalFileBrowserType();
                    }

                    RemoteFileBrowser rflb = getRemoteFileBrowserFromTabComponent(
                            gUIView.getRemotetJTabbedPane().getSelectedComponent());
                    if (rflb == null) {
                        return;
                    }
                    if (!rflb.getClientConnectionThread().socketIsClosed()) {
                        int index = gUIView.getRemotetJTabbedPane().indexOfComponent(rflb.getBrowser());
                        transferITModel.removeClientConnectionFromSet(rflb.getClientConnectionThread());
                        if (index != -1) {
                            gUIView.getRemotetJTabbedPane().remove(index);
                        }
                        if (gUIView.getRemotetJTabbedPane().getTabCount() == 0) {
                            gUIView.setActivateTransferButtons(rflb.getClientConnectionThread().isHasWrite(), true);
                            gUIView.setRemoteBrowserButtonsActive(false);
                        }
                    }

                    if (object == gUIView.getTransferToClient()) {
                        addFilesToDownload(rflb);
                        transferFiles(rflb);
                    } else if (object == gUIView.getTransferToServer()) {
                        addFilesToUpload(rflb);
                        transferFiles(rflb);
                    } else if (object == gUIView.getRemoteBackButton()) {
                        rflb.goUp();
                        gUIView.getRemoteTextPath().setText(rflb.getCurrentDirectory());
                    } else if (object == gUIView.getRemoteDeleteButton()) {
                        int choice = JOptionPane.showConfirmDialog(gUIView, "Really delete files?",
                                "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                        if (choice == JOptionPane.OK_OPTION) {
                            rflb.getClientConnectionThread().sendDeleteRequest(rflb.getSelectedFiles());
                        }
                        rflb.update();
                    } else if (object == gUIView.getRemoteCreateFolderButton()) {
                        String input = JOptionPane.showInputDialog(gUIView, "Choose folder name", "New folder");
                        rflb.getClientConnectionThread().sendCreateDirectoryRequest(input);
                        rflb.update();
                    } else if (object == gUIView.getRemoteRefreshButton()) {
                        rflb.update();
                    } else if (object == gUIView.getRemoteTextPathButton()) {
                        rflb.goTo(gUIView.getRemoteTextPath().getText());
                    }
                } else if (object instanceof RemoteFileListBrowser) {
                    ClientConnectionThread cct = ((RemoteFileListBrowser) object).getClientConnectionThread();
                    cct.sendQuit();
                    gUIView.appendToLog(cct.getHost() + "Logged off");
                } else if (object instanceof JMenuItem) {
                    JMenuItem jMenuItem = (JMenuItem) object;
                    if (jMenuItem == gUIView.getMenuItemAbout()) {
                    } else if (jMenuItem == gUIView.getMenuItemManual()) {
                    } else if (jMenuItem == gUIView.getMenuItemQuit()) {
                        gUIView.windowClosing();
                    } else if (jMenuItem == gUIView.getMenuItemSettings()) {
                        new SettingFrame(transferITModel);
                    } else if (jMenuItem == gUIView.getMenuItemUPNP()) {
                        new UPNPGUI(transferITModel.getUpnpActions(), upnp);
                    } else {
                        throw new IllegalArgumentException(jMenuItem.toString());
                    }
                } else {
                    gUIView.appendToLog(((JButton) object).getText());
                }

            }
        };
        TransferIT.getTransferITThreadFactory().dispatchNewThread(interpret,"InterpretAction");
    }

    public void stateChanged(ChangeEvent e) {
        Object object = e.getSource();
        if (object instanceof JTabbedPane) {
            JTabbedPane jtp = (JTabbedPane) object;
            gUIView.pack();
            if (jtp.getTabCount() == 0) {
                //gUIView.getTransferTable().setModel(gUIView.getDefaultModel());
                return;
            }
            RemoteFileBrowser rflb =
                    getRemoteFileBrowserFromTabComponent(jtp.getSelectedComponent());
            if (rflb == null) {
                //gUIView.getTransferTable().setModel(gUIView.getDefaultModel());
                return;
            }
            ClientConnectionThread cct = rflb.getClientConnectionThread();
            if (!updateSpeedAndETA.isRunning()) {
                updateSpeedAndETA.start();
            }
            gUIView.getTransferTable().setModel(cct.getDefaultTableModel());
            gUIView.getTransferTable().getColumn(GUIView.transferTableColums[2]).setMaxWidth(40);
            gUIView.getTransferTable().getColumn(GUIView.transferTableColums[3]).setMaxWidth(30);
            gUIView.setActivateTransferButtons(true, cct.isHasWrite());
            gUIView.getRemoteCreateFolderButton().setEnabled(cct.isHasWrite());
            gUIView.getRemoteDeleteButton().setEnabled(cct.isHasWrite());
        }
    }

    public void update(Observable o, Object arg) {
        if (o instanceof ClientConnectionThread) {
            if (arg instanceof ServerCommands) {
                interpretServerCommand((ClientConnectionThread) o, (ServerCommands) arg);
            } else {
                throw new IllegalArgumentException();
            }
        } else if (o instanceof ObservablePacketContainer) {
            String host = "";

            if (arg instanceof String) {
                host = (String) arg;
            }

            int availableHosts = transferITModel.getObservablePacketContainer().getStrings().size();

            if (!host.equals(multiCastLocal + " " + transferITModel.getServerPort())) {
                transferITModel.getObservablePacketContainer().getHosts().put(host, null);
            } else {
                if (isMultiCastFirewall()) {
                    firewallMultiCastTimeout.restart();
                } else {
                    firewallMultiCastTimeout.start();
                    setMultiCastFirewall(true);
                    gUIView.appendToLog("Firewall settings are sufficient for MultiCaster");
                }
            }
            if (availableHosts != transferITModel.getObservablePacketContainer().getStrings().size()) {
                gUIView.appendToLog("Found new host " + host.replace(' ', ':'));
                gUIView.updateHostsTable();
            }
        } else if (o instanceof upnpFirewallPortOpener) {
            if (arg instanceof Boolean) {
                if ((Boolean) arg && !internetSharing) {
                    gUIView.appendToLog("Found upnp device");
                } else if (!(Boolean) arg) {
                    gUIView.appendToLog("Removed port forward");
                }
            } else if (arg instanceof String) {
                String s = (String) arg;
                gUIView.getInternetSharing().setIcon(gUIView.TRUE_ICON);
                gUIView.getConnectText().setText(s);
                gUIView.appendToLog("Forwarded port, connect address: " + s);
            } else if (arg instanceof ArrayList) {
                transferITModel.setUpnpActions((ArrayList<Action>) arg);
                gUIView.getMenuItemUPNP().setEnabled(true);
            }
        } else if (arg instanceof String) {
            gUIView.appendToLog((String) arg);
            return;
        } else if (o instanceof ServerConnectionThread) {
            if (arg instanceof ClientCommands) {
                interpretClientCommand((ServerConnectionThread) o, (ClientCommands) arg);
            }
        } else if (o instanceof ServerConnectionHandler) {
            if (arg instanceof ServerConnectionThread) {
                ((ServerConnectionThread) arg).addObserver(this);
            } else if (arg instanceof Boolean) {
                boolean isAlive = ((Boolean) arg);
                setServerIsAlive(isAlive);
                gUIView.setServerIsAliveIcon(isAlive ? gUIView.TRUE_ICON : gUIView.FALSE_ICON);
            }
        } else {
            gUIView.appendToLog(arg.toString());
        }

    }
}
