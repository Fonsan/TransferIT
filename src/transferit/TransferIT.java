package transferit;

import transferit.gui.GUIView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * This is where the main() method is kept.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class TransferIT {

    private static final File settingsfile = new File(
            System.getProperty("user.home") + File.separatorChar + ".tfrit");
    private static TransferITController transferITController;
    private static TransferITModel transferITModel;
    private static GUIView gUIView;
    private static TransferITThreadFactory transferITThreadFactory; 
    public static final boolean DEBUG = true;

    /**
     * Sets look and feel to system default and inits settings.
     * 
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        transferITThreadFactory = new TransferITThreadFactory();
        transferITModel = new TransferITModel();
        initSettings(transferITModel);

        transferITController = new TransferITController(transferITModel);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                gUIView = new GUIView(transferITModel, transferITController);
            }
        });

    /*gUIView.appendToLog("Thread count " + Thread.activeCount());
    gUIView.appendToLog("wid " + gUIView.getTransferToServer().getBounds().getWidth());*/
    }

    /**
     * Checks if the shared directory is valid.
     */
    public static void checkRootDirectory() {
        File root = new File(transferITModel.getProperty("net.server.rootpath"));
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        while (true) {
            try {
                if (root.isDirectory()) {
                    transferITModel.setProperty("net.server.rootpath", root.getPath());
                    break;
                }
                int returnVal = jFileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    root = jFileChooser.getSelectedFile();
                }
            } catch (SecurityException se) {
            }

        }
    }

    /**
     * Creates a model and initializes the user settings, if any, otherwise 
     * the defaults.
     * 
     * @return the model.
     */
    private static void initSettings(TransferITModel model) {
        if (settingsfile.exists()) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(settingsfile));
                Object[] readObjects = new Object[6];
                for (int x = 0; x < readObjects.length; x++) {
                    readObjects[x] = objectInputStream.readUnshared();
                    if (readObjects[x] == null) {
                        return;
                    }
                }
                model.putAll((Properties) readObjects[0]);

                model.setHostHistory((HashSet<String>) readObjects[1]);

                model.setUsernameHistory((HashMap<String, String>) readObjects[2]);

                model.setPasswordHistory((HashMap<String, String>) readObjects[3]);

                model.setUsers((HashMap<String, String>) readObjects[4]);

                model.setUserRights((HashMap<String, Boolean>) readObjects[5]);



            } catch (IOException ex) {
                Logger.getLogger(TransferIT.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TransferIT.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            model.addUser("anon", "anon", false);
        }
    }

    /**
     * Saves the settings to the settingsfile.
     */
    public synchronized static void saveSettings() {
        try {
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(new FileOutputStream(settingsfile));
                objectOutputStream.writeUnshared(transferITModel.getProperties());
                objectOutputStream.reset();
                objectOutputStream.writeUnshared(transferITModel.getHostHistory());
                objectOutputStream.reset();
                objectOutputStream.writeUnshared(transferITModel.getUsernameHistory());
                objectOutputStream.reset();
                objectOutputStream.writeUnshared(transferITModel.getPasswordHistory());
                objectOutputStream.reset();
                objectOutputStream.writeUnshared(transferITModel.getUsers());
                objectOutputStream.reset();
                objectOutputStream.writeUnshared(transferITModel.getUserRights());
                objectOutputStream.reset();
                objectOutputStream.flush();
            } catch (IOException ex1) {
                Logger.getLogger(TransferIT.class.getName()).log(Level.SEVERE, null, ex1);
            }
            objectOutputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(TransferIT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the GUIView
     * @return the GUIView
     */
    public static GUIView getGUIView() {
        return gUIView;
    }

    public static TransferITThreadFactory getTransferITThreadFactory() {
        return transferITThreadFactory;
    }

    public static void setTransferITThreadFactory(TransferITThreadFactory transferITThreadFactory) {
        TransferIT.transferITThreadFactory = transferITThreadFactory;
    }
    
    

    /**
     * Returns the settins file.
     * 
     * @return the file.
     */
    public static File getSettingsfile() {
        return settingsfile;
    }

    /**
     * Returns the controller
     * 
     * @return the controller
     */
    public static TransferITController getTransferITController() {
        return transferITController;
    }

    /**
     * Sets the model.
     * 
     * @param transferITModel the model
     */
    public static void setTransferITModel(TransferITModel transferITModel) {
        TransferIT.transferITModel = transferITModel;
    }

    /**
     * Return the model
     * 
     * @return the model
     */
    public static TransferITModel getTransferITModel() {
        return transferITModel;
    }
}


