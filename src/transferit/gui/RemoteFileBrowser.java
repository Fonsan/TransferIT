package transferit.gui;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JTable;
import transferit.gui.filebrowsers.FileInfo;
import transferit.gui.filebrowsers.filelistbrowser.RemoteFileListBrowser;
import transferit.gui.filebrowsers.filetreebrowser.RemoteFileTreeBrowser;
import transferit.net.client.ClientConnectionThread;

/**
 * This is the file browser for remote servers showing files in a list or tree.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class RemoteFileBrowser {

    private Component browser = null;
    private boolean isTree;
    private String path = "";

    /**
     * Creates a remote browser with either a tree view or list view browser
     * component.
     * 
     * @param browser the file or list browser.
     */
    public RemoteFileBrowser(Component browser) {
        this.browser = browser;
        this.isTree = browser instanceof RemoteFileTreeBrowser;
    }

    /**
     * Tells what kind of browser is being used.
     * 
     * @return the used browser.
     */
    public Component getBrowser() {
        return browser;
    }

    /**
     * Returns a list with the FileInfo-objects selected in the browser.
     * 
     * @return the list.
     */
    public synchronized ArrayList<FileInfo> getSelectedFiles() {
        return isTree ? new ArrayList<FileInfo>() : ((RemoteFileListBrowser) browser).getSelectedFiles();
    }

    public JTable getJTable() {
        if (!isTree) {
            return (RemoteFileListBrowser) browser;
        }
        return null;
    }

    /**
     * Changes the current working directory to  specified path.
     * 
     * @param path the path to change to.
     */
    public synchronized void goTo(String path) {
        if (isTree) {
        } else {
            ((RemoteFileListBrowser) browser).goTo(path);
            this.path = path;
        }
    }

    /**
     * Move upwards in the structure.
     */
    public synchronized void goUp() {
        if (isTree) {

        } else {
            ((RemoteFileListBrowser) browser).goUp();
            path = ((RemoteFileListBrowser)browser).getCurrentDirectory();
        }
    }

    public ClientConnectionThread getClientConnectionThread() {
        return isTree ? ((RemoteFileTreeBrowser) browser).getClientConnectionThread() : ((RemoteFileListBrowser) browser).getClientConnectionThread();
    }

    /**
     * Returns the current working directory.
     * 
     * @return the path to the directory.
     */
    public synchronized String getCurrentDirectory() {
        return isTree ? ((RemoteFileTreeBrowser) browser).getCurrentDirectory() : ((RemoteFileListBrowser) browser).getCurrentDirectory();
    }

    /**
     * Refreshes the browser and checks for newly added or removed files.
     */
    public synchronized void update() {
        if (isTree) {

        } else {
            goTo(path);
        }
    }
}
