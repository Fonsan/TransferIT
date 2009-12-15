package transferit.gui.filebrowsers.filelistbrowser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTable;
import transferit.gui.filebrowsers.HiddenFilter;
import transferit.gui.filebrowsers.InfoContainer;

/**
 *
 * @author project group 20
 */
public class LocalFileListBrowser extends FileListBrowser {

    private File parentFile;
    private File currentFile;
    private final int DOUBLECLICK = 2;
    /* needs to enable the user to traverse backwards */
    private final File BACK = new File("..");
    /* needed to get the column index for the column named "Name" */
    private final String NAME_COLUMN = "Name";
    /* the data that is to be used as table */
    private Object[][] data;
    /* counter for when adding things to data */
    private int current;
    private static List<Object[]> roots = new ArrayList<Object[]>();
    private static FileFilter hiddenFilter = new HiddenFilter();

    public LocalFileListBrowser(String path) {
        super();


        listRoots();
        data = new Object[0][];

        //add the functionality to traverse backwards 

        if (path.equals("")) {

            currentFile = new File("");
        } else {

            File init = new File(path);
            currentFile = init;
            parentFile = init.getParentFile();
            reDoTable(Arrays.asList(init.listFiles()));
        }
    }

    public ArrayList<File> getSelectedFiles() {
        int[] selected = getSelectedRows();
        int nameColumn = getColumn(NAME_COLUMN).getModelIndex();
        ArrayList<File> transfer = new ArrayList<File>();

        for (int i : selected) {
            Object object = getValueAt(i, nameColumn);

            if (object instanceof File) {
                transfer.add((File) object);
            }
        }

        return transfer;
    }

    public synchronized void reDoTable(List<File> files) {
        // get the size from the files array and add one, because of backwards traversing 
        int size = files.size() + 1;

        current = 0;

        data = new Object[size][];

        //add the functionality to traverse backwards 
        addToData(BACK);

        //add the files to the TableModel and finally set the TableModel to the table 
        for (File file : files) {
            addToData(file);
        }

        setModel(new FileListModel(data));
    }

    public synchronized void updateTable() {
        reDoTable(Arrays.asList(currentFile.listFiles(hiddenFilter)));
    }

    private void addToData(Object[] objects) {
        Object[] test = {objects[0], objects[1]};
        Object[] fileData = {test, objects[2], new Long(0)};
        data[current++] = fileData;
    }

    private void addToData(File file) {
        Object[] fileData = {file, file, file.length()};
        data[current++] = fileData;
    }

    public String getCurrentDirectory() {
        return currentFile.getAbsolutePath();
    }

    public void listRoots() {
        if (roots.isEmpty()) {
            File[] rootsList = File.listRoots();
            for (File f : rootsList) {
                Object[] objects = {InfoContainer.getSystemIcon(f), InfoContainer.getSystemDisplayName(f), InfoContainer.getSystemDescription(f), f.getAbsolutePath()};
                roots.add(objects);
            }
        }

        data = new Object[roots.size()][];

        current = 0;

        for (Object[] objects : roots) {
            addToData(objects);
        }


        setModel(new FileListModel(data));
    }

    public void goUp() {
        if (parentFile == null) {
            listRoots();

        } else {
            // redo the table with the parentFile's content
            reDoTable(Arrays.asList(parentFile.listFiles(hiddenFilter)));
            currentFile = parentFile;
            parentFile = parentFile.getParentFile();
        }
    }

    public void goTo(String path) {
        try {
            File folder = new File(path);
            if (folder.isDirectory()) {
                // redo the table with the parentFile's content
                reDoTable(Arrays.asList((folder).listFiles(hiddenFilter)));
                currentFile = folder;
                parentFile = folder.getParentFile();
            }
        } catch (SecurityException se) {
        }
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public File getParentFile() {
        return parentFile;
    }

    public void setParentFile(File parentFile) {
        this.parentFile = parentFile;
    }

    public static FileFilter getHiddenFilter() {
        return hiddenFilter;
    }

    public static List<Object[]> getRoots() {
        return roots;
    }
}
