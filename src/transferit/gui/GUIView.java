package transferit.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseEvent;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import transferit.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import transferit.gui.filebrowsers.filelistbrowser.LocalFileListBrowser;
import transferit.gui.filebrowsers.filelistbrowser.RemoteFileListBrowser;
import transferit.gui.filebrowsers.filetreebrowser.LocalFileTreeBrowser;
import transferit.gui.filebrowsers.filetreebrowser.RemoteFileTreeBrowser;
import transferit.gui.hoststable.HostsModel;
import transferit.gui.hoststable.HostsTable;
import transferit.net.client.ClientConnectionThread;
import javax.swing.table.DefaultTableCellRenderer;
import transferit.gui.filebrowsers.filelistbrowser.FileListRenderer;
import transferit.net.PlatformIndenpendentInetAddress;

/**
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class GUIView extends JFrame {

    private TransferITModel transferITModel;
    private TransferITController transferITController;
    //Menu
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu();
    private JMenu aboutMenu = new JMenu();
    private JMenuItem menuItemSettings = new JMenuItem();
    private JMenuItem menuItemManual = new JMenuItem();
    private JMenuItem menuItemQuit = new JMenuItem();
    private JMenuItem menuItemAbout = new JMenuItem();
    private JMenuItem menuItemUPNP = new JMenuItem();
    //Qconnect
    private JPanel quickConnectPanel = new JPanel();
    private JLabel connectTxt = new JLabel();
    private JLabel usernameTxt = new JLabel();
    private JLabel passwordTxt = new JLabel();
    private JTextField hostInput = new JTextField();
    private JTextField usernameInput = new JTextField();
    private JPasswordField passwordInput = new JPasswordField();
    private JCheckBox checkBoxAnonym;
    private JButton connectButton = new JButton();
    private JPanel northPanel = new JPanel();
    private JPanel statusPanel = new JPanel();
    private JTextArea loggArea = new JTextArea();
    private JScrollPane loggerScrollPane = new JScrollPane(loggArea);
    private JPanel hostsPanel = new JPanel();
    private JLabel hostsPanelLabel = new JLabel();
    private JPanel transferPanel = new JPanel();
    private HostsTable hostsTable;
    private JTabbedPane remoteJTabbedPane = new JTabbedPane();
    private JButton remoteTextPathButton = new JButton();
    private JTextField remoteTextPath = new JTextField();
    private JPanel remoteFileBrowserPanel = new JPanel();
    private JPanel remoteNavigatePanel = new JPanel();
    private JButton remoteDeleteButton = new JButton();
    private JButton remoteRefreshButton = new JButton();
    private JButton remoteCreateFolderButton = new JButton();
    private JButton remoteBackButton = new JButton();
    private JButton remoteChangeViewButton = new JButton();
    private LocalFileBrowser localFileBrowser;
    private JPanel navigatePanel = new JPanel();
    private JTextField localTextPath = new JTextField();
    private JButton localTextPathButton = new JButton();
    private JPanel localFileBrowserPanel = new JPanel();
    private JPanel localNavigatePanel = new JPanel();
    private JButton localDeleteButton = new JButton();
    private JButton localRefreshButton = new JButton();
    private JButton localBackButton = new JButton();
    private JButton localCreateFolderButton = new JButton();
    private JButton localChangeViewButton = new JButton();
    private JScrollPane localFileBrowserScroll;
    private JPanel transferButtonPanel = new JPanel();
    private JButton transferToServer = new JButton();
    private JButton transferToClient = new JButton();
    private JProgressBar progressBarFile = new JProgressBar();
    private JProgressBar progressBarProcess = new JProgressBar();
    private JPanel statusBox = new JPanel();
    private JPanel queueTransferPanel = new JPanel();
    private JPanel fileStatusPanel = new JPanel();
    private JButton serverIsAlive = new JButton();
    private JButton autoDetectIsAlive;
    private JButton firewallPassed;
    private JButton internetSharing;
    private JTextField connectText;
    private JLabel ETALabel = new JLabel();
    private JLabel transferSpeed = new JLabel();
    private JLabel fileProgressBarLabel = new JLabel();
    private JLabel queueProgressBarLabel = new JLabel();
    private JPanel transferFilePanel = new JPanel();
    private JTable transferTable;
    private DefaultTableModel defaultModel;
    private final int LOGG_AREA_HEIGHT = 9;
    private final int INPUT_AREA_WIDTH = 15;
    private boolean useTreeBrowser;
    public final ImageIcon FAVOURITE_ICON = new ImageIcon(this.getClass().getResource("images/favourite.png"));
    public final ImageIcon READ_ICON = new ImageIcon(this.getClass().getResource("images/read.png"));
    public final ImageIcon WRITE_ICON = new ImageIcon(this.getClass().getResource("images/write.png"));
    public final ImageIcon FALSE_ICON = new ImageIcon(this.getClass().getResource("images/false.png"));
    public final ImageIcon TRUE_ICON = new ImageIcon(this.getClass().getResource("images/accept.png"));
    public final ImageIcon RIGHT_ICON = new ImageIcon(this.getClass().getResource("images/arrow_right.png"));
    public final ImageIcon LEFT_ICON = new ImageIcon(this.getClass().getResource("images/arrow_left.png"));
    public final ImageIcon CANCEL_ICON = new ImageIcon(this.getClass().getResource("images/cancel.png"));
    public final ImageIcon UPDATE_ICON = new ImageIcon(this.getClass().getResource("images/arrow_refresh.png"));
    public final ImageIcon CREATE_FOLDER_ICON = new ImageIcon(this.getClass().getResource("images/folder_add.png"));
    public final ImageIcon DELETE_FILE_ICON = new ImageIcon(this.getClass().getResource("images/table_delete.png"));
    public final ImageIcon ARROW_UP_ICON = new ImageIcon(this.getClass().getResource("images/arrow_up.png"));
    public final ImageIcon ARROW_DOWN_ICON = new ImageIcon(this.getClass().getResource("images/arrow_down.png"));
    public final ImageIcon TRANSFERIT_ICON = new ImageIcon(this.getClass().getResource("images/icon.gif"));
    public final ImageIcon FOLDER_GO_ICON = new ImageIcon(this.getClass().getResource("images/folder_go.png"));
    public final ImageIcon FOLDER_CHANGE_ICON = new ImageIcon(this.getClass().getResource("images/folder_wrench.png"));
    public static final String[] transferTableColums = {
        "Home path",
        "Remote path",
        "Size",
        "U/D"
    };
    public Set<RemoteFileBrowser> tabs = new HashSet<RemoteFileBrowser>();
    private TrayIcon trayIcon;
    private MenuItem exitItem;
    private MenuItem settingsItem;
    private MenuItem showItem;
    private MouseListener buttonMouseListener = new MouseAdapter() {

        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
    private MouseAdapter mouseListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            Object o = e.getSource();
            if (16 == e.getModifiers()) {
                setVisible(!isVisible());
            }
        }
    };
    private ActionListener trayButtonListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o == showItem) {
                setVisible(!isVisible());
            } else if (o == settingsItem) {
                new SettingFrame(transferITModel);
            } else if (o == exitItem) {
                windowClosing();
            }
        }
    };
    private KeyAdapter guiBindings = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W) {
                RemoteFileBrowser rfb =
                        transferITController.getRemoteFileBrowserFromTabComponent(
                        remoteJTabbedPane.getSelectedComponent());
                if (rfb == null) {
                    return;
                }
                ButtonTabComponent btc =
                        (ButtonTabComponent) remoteJTabbedPane.getTabComponentAt(
                        remoteJTabbedPane.indexOfComponent(rfb.getBrowser()));
                btc.getCloseButton().doClick();
            }
        }
    };

    public GUIView(final TransferITModel transferITModel, final TransferITController transferITController) {
        this.transferITModel = transferITModel;
        this.transferITController = transferITController;
        this.transferITController.setGUIView(this);

        addKeyListener(guiBindings);
        setFocusable(true);

        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            Image image = TRANSFERIT_ICON.getImage();

            PopupMenu popup = new PopupMenu();
            showItem = new MenuItem("Show/Hide");
            showItem.addActionListener(trayButtonListener);
            popup.add(showItem);

            settingsItem = new MenuItem("Settings");
            settingsItem.addActionListener(trayButtonListener);
            popup.add(settingsItem);

            exitItem = new MenuItem("Exit");
            exitItem.addActionListener(trayButtonListener);
            popup.add(exitItem);

            trayIcon = new TrayIcon(image, "TransferIT", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(mouseListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
            }
        }

        setTitle(transferITModel.getProperty("gui.title"));

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));


        setIconImage(TRANSFERIT_ICON.getImage());



        //Menu
        menuItemAbout.setText("About");
        menuItemManual.setText("Manual");
        menuItemQuit.setText("Quit");
        menuItemSettings.setText("Settings");
        menuItemUPNP.setText("UPNP controller");

        menuItemAbout.addActionListener(transferITController);
        menuItemManual.addActionListener(transferITController);
        menuItemQuit.addActionListener(transferITController);
        menuItemSettings.addActionListener(transferITController);
        menuItemUPNP.addActionListener(transferITController);

        menuItemUPNP.setEnabled(false);

        fileMenu.setText("File");
        fileMenu.add(menuItemSettings);
        fileMenu.add(menuItemUPNP);
        fileMenu.add(menuItemQuit);

        aboutMenu.setText("About");
        aboutMenu.add(menuItemManual);
        aboutMenu.add(menuItemAbout);

        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);

        //quickconnectpanel setup
        quickConnectPanel.setLayout(
                new BoxLayout(quickConnectPanel, BoxLayout.X_AXIS));

        connectTxt.setText(" Host[:port] ");
        usernameTxt.setText(" Username ");
        passwordTxt.setText(" Password ");
        boolean isanon = Boolean.parseBoolean(transferITModel.getProperty("gui.useanonymous"));
        checkBoxAnonym = new JCheckBox(" Use anonymous ", isanon);
        checkBoxAnonym.addActionListener(transferITController);
        checkBoxAnonym.addKeyListener(transferITController.getConnectEnterListner());

        hostInput.addKeyListener(transferITController.getConnectEnterListner());
        hostInput.setColumns(INPUT_AREA_WIDTH);
        hostInput.setText(transferITModel.getProperty("gui.host"));
        usernameInput.setText(transferITModel.getProperty("gui.username"));
        usernameInput.addKeyListener(transferITController.getConnectEnterListner());
        usernameInput.setColumns(INPUT_AREA_WIDTH);
        usernameInput.setSize(new Dimension(100, 0));
        passwordInput.addKeyListener(transferITController.getConnectEnterListner());
        passwordInput.setColumns(INPUT_AREA_WIDTH);
        connectButton.setText("Connect");
        connectButton.addActionListener(transferITController);


        quickConnectPanel.add(connectTxt);
        quickConnectPanel.add(hostInput);
        quickConnectPanel.add(usernameTxt);
        quickConnectPanel.add(usernameInput);
        quickConnectPanel.add(passwordTxt);
        quickConnectPanel.add(passwordInput);
        quickConnectPanel.add(checkBoxAnonym);
        quickConnectPanel.add(connectButton);

        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        //Loggerbox
        loggArea.setEditable(false);
        loggArea.setRows(LOGG_AREA_HEIGHT);
        loggArea.setEditable(false);
        loggArea.setLineWrap(true);

        statusBox.setLayout(new BoxLayout(statusBox, BoxLayout.Y_AXIS));

        serverIsAlive = new JButton("Server status");
        initButton(serverIsAlive, FALSE_ICON, "Start/Stop server");
        //serverIsAlive.setHorizontalTextPosition(SwingConstants.LEFT);
        statusBox.add(serverIsAlive);

        statusBox.add(Box.createRigidArea(new Dimension(5, 0)));

        firewallPassed = new JButton("Firewall settings");
        initButton(firewallPassed, FALSE_ICON, "Recheck firewall settings manually");
        //firewallPassed.setHorizontalTextPosition(SwingConstants.LEFT);
        statusBox.add(firewallPassed);

        autoDetectIsAlive = new JButton("LAN Autodetect");
        initButton(autoDetectIsAlive, FALSE_ICON, "Recheck firewall settings for autodetect manually");
        //autoDetectIsAlive.setHorizontalTextPosition(SwingConstants.LEFT);
        statusBox.add(autoDetectIsAlive);

        internetSharing = new JButton("Internet sharing");
        initButton(internetSharing, FALSE_ICON, "Open up port in router via UPNP. " +
                "If port is already forwarded to another computer on the network it will \n" +
                "try and forward a new port on the external WAN interface of the router, note " +
                "that the local port will still be the same within the network");
        //internetSharing.setHorizontalTextPosition(SwingConstants.LEFT);
        statusBox.add(internetSharing);


        JLabel connectTextLabel = new JLabel("Your connect url");
        connectTextLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        statusBox.add(connectTextLabel);
        connectText = new JTextField();
        connectText.setToolTipText("Your connect url, click on the text and it will copy itself onto the clipboard ");
        connectText.setColumns(INPUT_AREA_WIDTH);
        connectText.setBackground(Color.WHITE);
        connectText.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                connectText.selectAll();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(connectText.getText()), null);
            }
        });
        setDefaultConnectText();
        connectText.setEditable(false);
        statusBox.add(connectText);
        statusBox.add(Box.createRigidArea(new Dimension(0, 3)));

        transferSpeed.setText("Speed 0 kb/s");
        statusBox.add(transferSpeed);
        statusBox.add(Box.createRigidArea(new Dimension(0, 3)));

        ETALabel.setText("Time left 0:00");
        statusBox.add(ETALabel);

        queueProgressBarLabel.setText("File queue progress 0% ");

        progressBarFile.setMaximum(1000000);
        progressBarProcess.setMaximum(1000000);

        hostsTable = new HostsTable(transferITController);
        hostsPanelLabel.setText("Local network");
        hostsPanel.setLayout(new BoxLayout(hostsPanel, BoxLayout.Y_AXIS));
        hostsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        hostsPanel.add(hostsPanelLabel);
        hostsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        hostsPanel.add(new JScrollPane(hostsTable));
        hostsPanel.setPreferredSize(new Dimension(140, 0));

        defaultModel = new DefaultTableModel();

        for (String s : transferTableColums) {
            defaultModel.addColumn(s);
        }

        transferTable = new JTable(defaultModel) {

            private DefaultTableCellRenderer arrowRenderer = new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel arrowLabel = ((Boolean) value) ? new JLabel(ARROW_DOWN_ICON) : new JLabel(ARROW_UP_ICON);
                    arrowLabel.setBackground(row == 0 ? Color.blue : arrowLabel.getBackground());
                    return arrowLabel;
                }
            };
            private DefaultTableCellRenderer sizeRenderer = new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    return new JLabel(FileListRenderer.setFileSize(Long.parseLong((String) value)));
                }
            };

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 3) {
                    return arrowRenderer;
                } else if (column == 2) {
                    return sizeRenderer;
                }
                return super.getCellRenderer(row, column);
            }
        };
        transferTable.getColumn(transferTableColums[2]).setMaxWidth(40);
        transferTable.getColumn(transferTableColums[3]).setMaxWidth(30);
        transferTable.setFillsViewportHeight(true);
        transferTable.setShowGrid(false);
        transferTable.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (KeyEvent.VK_DELETE == e.getKeyCode()) {
                    ClientConnectionThread cct = transferITController.getRemoteFileBrowserFromTabComponent(remoteJTabbedPane.getSelectedComponent()).
                            getClientConnectionThread();
                    DefaultTableModel dtm = cct.getDefaultTableModel();
                    int[] rows = transferTable.getSelectedRows();
                    String message = "Really delete these transfers?\n";

                    for (int x : rows) {
                        String rowTxt = "";
                        for (int y = 0; y < dtm.getRowCount(); y++) {
                            Object o = dtm.getValueAt(x, y);
                            if (!(o instanceof Component)) {
                                rowTxt += o + " ";
                            }
                        }
                        message += rowTxt + "\n";
                    }
                    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(GUIView.this, message)) {
                        for (int x : rows) {
                            cct.removeTransfer(x);
                        }
                    }
                }
            }
        });

        transferFilePanel.setLayout(new BoxLayout(transferFilePanel, BoxLayout.Y_AXIS));
        transferFilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        transferFilePanel.add(new JLabel("Transfer queue"));
        transferFilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        transferFilePanel.add(new JScrollPane(transferTable));

        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));

        statusPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        statusPanel.add(statusBox);
        statusPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        statusPanel.add(transferFilePanel);
        statusPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        statusPanel.add(hostsPanel);

        northPanel.add(quickConnectPanel);
        northPanel.add(statusPanel);

        localNavigatePanel.setLayout(new BoxLayout(localNavigatePanel, BoxLayout.X_AXIS));
        initButton(localRefreshButton, UPDATE_ICON, "Refresh files");
        initButton(localBackButton, ARROW_UP_ICON, "Up");
        initButton(localCreateFolderButton, CREATE_FOLDER_ICON, "Create folder");
        initButton(localDeleteButton, DELETE_FILE_ICON, "Delete");
        initButton(localTextPathButton, FOLDER_GO_ICON, "Go");
        initButton(localChangeViewButton, FOLDER_CHANGE_ICON, "Change view (Tree/Table)");
        localTextPathButton.addKeyListener(transferITController.getLocalPathEnterListner());

        localNavigatePanel.add(localTextPath);
        localNavigatePanel.add(localTextPathButton);
        localNavigatePanel.add(localRefreshButton);
        localNavigatePanel.add(localBackButton);
        localNavigatePanel.add(localCreateFolderButton);
        localNavigatePanel.add(localDeleteButton);
        localNavigatePanel.add(localChangeViewButton);

        remoteNavigatePanel.setLayout(new BoxLayout(remoteNavigatePanel, BoxLayout.X_AXIS));
        initButton(remoteRefreshButton, UPDATE_ICON, "Refresh files");
        initButton(remoteBackButton, ARROW_UP_ICON, "Up");
        initButton(remoteCreateFolderButton, CREATE_FOLDER_ICON, "Create folder");
        initButton(remoteDeleteButton, DELETE_FILE_ICON, "Delete");
        initButton(remoteTextPathButton, FOLDER_GO_ICON, "Go");
        initButton(remoteChangeViewButton, FOLDER_CHANGE_ICON, "Change view (Tree/Table)");
        remoteTextPathButton.addKeyListener(transferITController.getRemotePathEnterListner());

        remoteNavigatePanel.add(remoteTextPath);
        remoteNavigatePanel.add(remoteTextPathButton);
        remoteNavigatePanel.add(remoteRefreshButton);
        remoteNavigatePanel.add(remoteBackButton);
        remoteNavigatePanel.add(remoteCreateFolderButton);
        remoteNavigatePanel.add(remoteDeleteButton);
        remoteNavigatePanel.add(remoteChangeViewButton);

        navigatePanel.setLayout(new BoxLayout(navigatePanel, BoxLayout.X_AXIS));
        localNavigatePanel.setMaximumSize(new Dimension(340, 20));
        navigatePanel.add(localNavigatePanel);
        navigatePanel.add(Box.createRigidArea(new Dimension(60, 0)));
        remoteNavigatePanel.setMaximumSize(new Dimension(340, 20));
        navigatePanel.add(remoteNavigatePanel);

        //Transferpanel
        transferPanel.setLayout(new BoxLayout(transferPanel, BoxLayout.X_AXIS));
        transferToServer = new JButton(RIGHT_ICON);
        transferToClient = new JButton(LEFT_ICON);
        transferToClient.addActionListener(transferITController);
        transferToServer.addActionListener(transferITController);
        transferButtonPanel.setLayout(new BoxLayout(transferButtonPanel, BoxLayout.PAGE_AXIS));

        transferButtonPanel.add(transferToServer);
        transferButtonPanel.add(Box.createRigidArea(new Dimension(0, 200)));
        transferButtonPanel.add(transferToClient);


        useTreeBrowser = transferITModel.getUseTree();
        localFileBrowserPanel.setLayout(new BoxLayout(localFileBrowserPanel, BoxLayout.Y_AXIS));
        localFileBrowser = new LocalFileBrowser(transferITModel.getProperty("gui.localfilebrowser.path"),
                useTreeBrowser, transferITController.getLocalFileListListener());

        localTextPath.addKeyListener(transferITController.getLocalBrowserEnterListner());
        
        if (transferITModel.getProperty("gui.localfilebrowser.path").equals("") || useTreeBrowser) {
            localTextPath.setText("");
        } else {
            localTextPath.setText(localFileBrowser.getCurrentDirectory());
        }
        localFileBrowserScroll = new JScrollPane(localFileBrowser.getBrowser());

        localFileBrowserPanel.add(localFileBrowserScroll);

        remoteFileBrowserPanel.setLayout(new BoxLayout(remoteFileBrowserPanel, BoxLayout.Y_AXIS));
        remoteFileBrowserPanel.add(remoteJTabbedPane);



        transferPanel.add(localFileBrowserPanel);
        transferPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        transferPanel.add(transferButtonPanel);
        transferPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        transferPanel.add(remoteFileBrowserPanel);


        setPreferredSize(new Dimension(Integer.parseInt(transferITModel.getProperty("gui.width")),
                Integer.parseInt(transferITModel.getProperty("gui.height"))));
        //statusPanel.setPreferredSize(new Dimension(0, 2000));
        transferPanel.setPreferredSize(new Dimension(0, 9000));
        loggerScrollPane.setPreferredSize(new Dimension(0, 5000));
        quickConnectPanel.setPreferredSize(new Dimension(0, 20));



        remoteFileBrowserPanel.setPreferredSize(new Dimension(200, 0));
        localFileBrowserScroll.setPreferredSize(new Dimension(200, 6000));
        localFileBrowserPanel.setPreferredSize(new Dimension(200, 0));


        fileProgressBarLabel.setText("File progress 0%");
        fileProgressBarLabel.setHorizontalAlignment(SwingConstants.LEFT);
        fileProgressBarLabel.setPreferredSize(new Dimension(140, 0));

        fileStatusPanel.setLayout(new BoxLayout(fileStatusPanel, BoxLayout.X_AXIS));
        fileStatusPanel.add(Box.createRigidArea(new Dimension(3, 0)));

        fileStatusPanel.add(progressBarFile);
        fileStatusPanel.add(Box.createRigidArea(new Dimension(3, 0)));

        fileStatusPanel.add(fileProgressBarLabel);


        queueTransferPanel.setLayout(new BoxLayout(queueTransferPanel, BoxLayout.X_AXIS));
        queueTransferPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        queueTransferPanel.add(progressBarProcess);
        queueTransferPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        queueTransferPanel.add(queueProgressBarLabel);
        queueProgressBarLabel.setPreferredSize(new Dimension(140, 0));


        //add(quickConnectPanel, BorderLayout.NORTH);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(northPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(fileStatusPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(queueTransferPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(navigatePanel);
        add(transferPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        add(loggerScrollPane);


        pack();

        setLocation(Integer.parseInt(transferITModel.getProperty("gui.position.x")),
                Integer.parseInt(transferITModel.getProperty("gui.position.y")));
        remoteJTabbedPane.addChangeListener(transferITController);
        addWindowListener(new ExitWindowListener());
        setActivateTransferButtons(false, false);
        setRemoteBrowserButtonsActive(false);
        setVisible(true);
        requestFocus();

        this.transferITController.init();
    }

    private void initButton(JButton button, ImageIcon icon, String toolTip) {
        button.setIcon(icon);
        button.setToolTipText(toolTip);
        //Make the button looks the same for all Laf's
        button.setUI(new BasicButtonUI());
        //Make it transparent
        button.setContentAreaFilled(false);
        //No need to be focusable
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEtchedBorder());
        button.setBorderPainted(false);
        //Making nice rollover effect
        //we use the same listener for all buttons
        button.addMouseListener(buttonMouseListener);
        button.setRolloverEnabled(true);
        //Close the proper tab by clicking the button
        button.addActionListener(transferITController);
    }

    public void addNewRemoteTab(ClientConnectionThread cct) {
        RemoteFileBrowser remoteFileBrowser = new RemoteFileBrowser(useTreeBrowser ? new RemoteFileTreeBrowser(cct) : new RemoteFileListBrowser(cct));
        remoteFileBrowser.getBrowser().addMouseListener(transferITController.getRemoteFileListListener());
        remoteFileBrowser.getBrowser().addKeyListener(transferITController.getRemoteBrowserEnterListner());
        JScrollPane toAdd = new JScrollPane(remoteFileBrowser.getBrowser());

        remoteJTabbedPane.addTab(cct.getHost(), toAdd);
        tabs.add(remoteFileBrowser);
        remoteJTabbedPane.setTabComponentAt(remoteJTabbedPane.indexOfComponent(toAdd),
                new ButtonTabComponent(remoteJTabbedPane));
        remoteJTabbedPane.setSelectedComponent(toAdd);
        transferTable.setModel(cct.getDefaultTableModel());


        transferTable.getColumn(transferTableColums[2]).setMaxWidth(40);
        transferTable.getColumn(transferTableColums[3]).setMaxWidth(30);
        setActivateTransferButtons(true, cct.isHasWrite());
        setRemoteBrowserButtonsActive(true);
        remoteCreateFolderButton.setEnabled(cct.isHasWrite());
        remoteDeleteButton.setEnabled(cct.isHasWrite());
    }

    public void setActivateTransferButtons(boolean active, boolean hasWrite) {
        getTransferToClient().setEnabled(active);
        getTransferToServer().setEnabled(hasWrite);
        if (remoteJTabbedPane.getTabCount() == 0) {
            return;
        }
    }

    public void updateHostsTable() {
        hostsTable.updateHostsTable(transferITModel.getObservablePacketContainer().getHostnames());
    }

    public void setHistory() {
        HostsModel hostsModel = hostsTable.getHostsModel();
        //int historyColumn = 1;
        int infoColumn = 2;
        for (int i = 0; i < hostsModel.getRowCount(); i++) {
            //  Boolean isHistory = (Boolean) hostsModel.getValueAt(i, historyColumn);
            //if (isHistory) {
            String[] userAndPass = (String[]) hostsModel.getValueAt(i, infoColumn);
            transferITModel.addHostToHistory(userAndPass[0], userAndPass[1], userAndPass[2]);
        //}
        }
    }

    public synchronized void appendToLog(String message) {
        loggArea.append(message + System.getProperty("line.separator"));
        loggArea.setCaretPosition(loggArea.getText().length());
    }

    public void setRemoteBrowserButtonsActive(boolean active) {
        remoteBackButton.setEnabled(active);
        remoteRefreshButton.setEnabled(active);
        remoteDeleteButton.setEnabled(active);
        remoteTextPathButton.setEnabled(active);
        remoteCreateFolderButton.setEnabled(active);
        remoteChangeViewButton.setEnabled(active);
    }

    public void changeLocalFileBrowserType() {
        localFileBrowser.changeBrowserType();
        localFileBrowserScroll.getViewport().setView(localFileBrowser.getBrowser());
    }

    public void setDefaultConnectText() {
        try {
            connectText.setText(PlatformIndenpendentInetAddress.getLocalHost().getHostAddress() + ":" + transferITModel.getProperty("net.server.port"));
        } catch (UnknownHostException ex) {
            Logger.getLogger(GUIView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JButton getLocalTextPathButton() {
        return localTextPathButton;
    }

    public JLabel getFileProgressBarLabel() {
        return fileProgressBarLabel;
    }

    public JLabel getETALabel() {
        return ETALabel;
    }

    public JLabel getQueueProgressBarLabel() {
        return queueProgressBarLabel;
    }

    public JButton getAutoDetectIsAlive() {
        return autoDetectIsAlive;
    }

    public JLabel getTransferSpeed() {
        return transferSpeed;
    }

    public JTextField getLocalTextPath() {
        return localTextPath;
    }

    public JButton getFirewallPassed() {
        return firewallPassed;
    }

    public JTextField getRemoteTextPath() {
        return remoteTextPath;
    }

    public JButton getLocalChangeViewButton() {
        return localChangeViewButton;
    }

    public JButton getRemoteChangeViewButton() {
        return remoteChangeViewButton;
    }

    public JButton getRemoteTextPathButton() {
        return remoteTextPathButton;
    }

    public JTable getTransferTable() {
        return transferTable;
    }

    public JButton getConnectButton() {
        return connectButton;
    }

    public JButton getLocalBackButton() {
        return localBackButton;
    }

    public JButton getLocalCreateFolderButton() {
        return localCreateFolderButton;
    }

    public JButton getLocalDeleteButton() {
        return localDeleteButton;
    }

    public JButton getLocalRefreshButton() {
        return localRefreshButton;
    }

    public JButton getRemoteBackButton() {
        return remoteBackButton;
    }

    public JButton getRemoteCreateFolderButton() {
        return remoteCreateFolderButton;
    }

    public JButton getRemoteDeleteButton() {
        return remoteDeleteButton;
    }

    public JButton getRemoteRefreshButton() {
        return remoteRefreshButton;
    }

    public JMenuItem getMenuItemAbout() {
        return menuItemAbout;
    }

    public JMenuItem getMenuItemManual() {
        return menuItemManual;
    }

    public JMenuItem getMenuItemQuit() {
        return menuItemQuit;
    }

    public JMenuItem getMenuItemSettings() {
        return menuItemSettings;
    }

    public JButton getTransferToClient() {
        return transferToClient;
    }

    public JTextField getConnectText() {
        return connectText;
    }

    public JMenuItem getMenuItemUPNP() {
        return menuItemUPNP;
    }

    public JButton getInternetSharing() {
        return internetSharing;
    }

    public JButton getTransferToServer() {
        return transferToServer;
    }

    public JTextField getHostInput() {
        return hostInput;
    }

    public JPasswordField getPasswordInput() {
        return passwordInput;
    }

    public JButton getServerIsAlive() {
        return serverIsAlive;
    }

    public JTextField getUsernameInput() {
        return usernameInput;
    }

    public LocalFileBrowser getLocalFileBrowser() {
        return localFileBrowser;
    }

    public JCheckBox getCheckBoxAnonym() {
        return checkBoxAnonym;
    }

    public JTabbedPane getRemotetJTabbedPane() {
        return remoteJTabbedPane;
    }

    public JScrollPane getLoggerScrollPane() {
        return loggerScrollPane;
    }

    public JProgressBar getProgressBarFile() {
        return progressBarFile;
    }

    public void setProgressBarFile(JProgressBar progressBarFile) {
        this.progressBarFile = progressBarFile;
    }

    public JProgressBar getProgressBarProcess() {
        return progressBarProcess;
    }

    public void setProgressBarProcess(JProgressBar progressBarProcess) {
        this.progressBarProcess = progressBarProcess;
    }

    public void setServerIsAliveIcon(Icon icon) {
        serverIsAlive.setIcon(icon);
    }

    public void setFirewallPassedIcon(Icon icon) {
        firewallPassed.setIcon(icon);
    }

    public void windowClosing() {
        transferITModel.setProperty("gui.host", hostInput.getText());
        transferITModel.setProperty("gui.username", usernameInput.getText());
        transferITModel.setProperty("gui.position.x", Integer.toString(getX()));
        transferITModel.setProperty("gui.position.y", Integer.toString(getY()));
        transferITModel.setProperty("gui.localfilebrowser.path", localFileBrowser.getCurrentDirectory());
        transferITModel.setProperty("net.server.isalive", Boolean.toString(TRUE_ICON.equals(serverIsAlive.getIcon())));
        setHistory();
        TransferIT.saveSettings();
        dispose();
        System.exit(0);
    }

    class ButtonTabComponent extends JPanel {

        private final JTabbedPane pane;
        private JButton closeButton = new JButton();

        public ButtonTabComponent(final JTabbedPane pane) {
            //unset default FlowLayout' gaps
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));

            this.pane = pane;
            setOpaque(false);

            //make JLabel read titles from JTabbedPane
            JLabel label = new JLabel() {

                @Override
                public String getText() {
                    int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (i != -1) {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }
            };

            add(label);


            closeButton.setIcon(CANCEL_ICON);
            closeButton.setToolTipText("Close this connection");
            //Make the button looks the same for all Laf's
            closeButton.setUI(new BasicButtonUI());
            //Make it transparent
            closeButton.setContentAreaFilled(false);
            //No need to be focusable
            closeButton.setFocusable(false);
            closeButton.setBorder(BorderFactory.createEtchedBorder());
            closeButton.setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            closeButton.addMouseListener(buttonMouseListener);
            closeButton.setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            closeButton.addActionListener(buttonActionListener);
            add(closeButton);
        }

        public JButton getCloseButton() {
            return closeButton;
        }
        private ActionListener buttonActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final Object object = e.getSource();

                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                ClientConnectionThread cct = null;
                RemoteFileBrowser rflb = transferITController.getRemoteFileBrowserFromTabComponent(pane.getComponentAt(i));
                cct = rflb.getClientConnectionThread();
                if (object == ButtonTabComponent.this.getCloseButton()) {
                    if (!cct.hasTransfersLeft()) {
                        cct.sendAbort();
                        cct.sendQuit();
                    } else {
                        cct.sendQuit();
                    }

                    transferITModel.removeClientConnectionFromSet(cct);
                    if (i != -1) {
                        pane.remove(i);
                    }
                    if (pane.getTabCount() == 0) {
                        setActivateTransferButtons(false, false);
                        setRemoteBrowserButtonsActive(false);
                    }
                }

            }
        };
    }

    class ExitWindowListener extends WindowAdapter {

        /**
         * Called when the user clicks the close button on the window.
         * (the X-button in the upper right corner in Windows)
         */
        @Override
        public void windowClosing(WindowEvent e) {
            transferITModel.setProperty("gui.host", hostInput.getText());
            transferITModel.setProperty("gui.username", usernameInput.getText());
            transferITModel.setProperty("gui.position.x", Integer.toString(getX()));
            transferITModel.setProperty("gui.position.y", Integer.toString(getY()));
            transferITModel.setProperty("gui.localfilebrowser.path", localFileBrowser.getCurrentDirectory());
            transferITModel.setProperty("net.server.isalive", Boolean.toString(TRUE_ICON.equals(serverIsAlive.getIcon())));
            setHistory();
            TransferIT.saveSettings();
            System.exit(0);
        }
    }
}
/**
 * This class will close the window on user request
 */

