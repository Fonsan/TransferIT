/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import transferit.gui.filebrowsers.filelistbrowser.LocalFileListBrowser;
import transferit.gui.filebrowsers.filetreebrowser.LocalFileTreeBrowser;

/**
 *
 * @author fonsan
 */
public class LocalFileBrowser {

    private boolean useTreeBrowser;
    private LocalFileTreeBrowser treeBrowser = null;
    private LocalFileListBrowser listBrowser = null;

    LocalFileBrowser(String path, boolean useTreeBrowser, MouseAdapter localFileListListener) {
        treeBrowser = new LocalFileTreeBrowser(path);
        listBrowser = new LocalFileListBrowser(path);
        listBrowser.addMouseListener(localFileListListener);
        this.useTreeBrowser = useTreeBrowser;
    }

    public Component getBrowser() {
        return useTreeBrowser ? treeBrowser : listBrowser;
    }

    public synchronized ArrayList<File> getSelectedFiles() {
        return useTreeBrowser ? treeBrowser.getSelectedFiles() : listBrowser.getSelectedFiles();
    }

    public void changeBrowserType() {
        useTreeBrowser = !useTreeBrowser;
    }

    public synchronized ArrayList<String> getSelectedPaths() {
        ArrayList<String> rtr = new ArrayList<String>();
        for (File file : getSelectedFiles()) {
            rtr.add(file.getAbsolutePath());
        }
        return rtr;
    }

    public synchronized void goFromRoot(int getRow) {
        if (!useTreeBrowser) {
            Object[] objects = LocalFileListBrowser.getRoots().get(getRow);
            listBrowser.reDoTable(Arrays.asList(new File((String) objects[3]).listFiles(LocalFileListBrowser.getHiddenFilter())));
            listBrowser.setCurrentFile(new File((String) objects[3]));
        }
    }

    public synchronized void goTo(String path) {
        if (useTreeBrowser) {
            treeBrowser.goTo(path);
        } else {
            listBrowser.goTo(path);
        }
    }

    public synchronized void goUp() {
        if (useTreeBrowser) {
        } else {
            listBrowser.goUp();
        }
    }

    public synchronized String getCurrentDirectory() {
        return useTreeBrowser ? treeBrowser.getCurrentPath() : listBrowser.getCurrentDirectory();
    }

    public synchronized void update() {
        if (useTreeBrowser) {
        } else {
            listBrowser.updateTable();
        }
    }
}
