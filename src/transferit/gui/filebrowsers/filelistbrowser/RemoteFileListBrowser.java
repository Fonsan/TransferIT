package transferit.gui.filebrowsers.filelistbrowser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import transferit.gui.filebrowsers.FileInfo;
import transferit.net.client.ClientConnectionThread;

/**
 *
 * @author Mathias SÃƒÂ¶derberg
 */
public class RemoteFileListBrowser extends FileListBrowser {

    private final int DOUBLECLICK = 2;
    /* needs to enable the user to traverse backwards */
    public static final FileInfo BACK = new FileInfo("..", "..", new Long(0), true);
    /* needed to get the column index for the column named "Name" */
    private final String NAME_COLUMN = "Name";
    /* the data that is to be used as table */
    private Object[][] data;
    /* counter for when adding things to data */
    private int current;
    private List<String> paths = new ArrayList<String>();
    private ClientConnectionThread clientConnectionThread;

    public RemoteFileListBrowser(ClientConnectionThread clientConnectionThread) {
        super();

        this.clientConnectionThread = clientConnectionThread;

        paths.add("");

        reDoTable(getDirectoryListing(""));
    }

    public ArrayList<FileInfo> getSelectedFiles() {
        int[] selected = getSelectedRows();
        int nameColumn = getColumn(NAME_COLUMN).getModelIndex();
        ArrayList<FileInfo> transfer = new ArrayList<FileInfo>();

        for (int i : selected) {
            Object object = getValueAt(i, nameColumn);

            if (object instanceof FileInfo) {
                transfer.add((FileInfo) object);
            }
        }

        return transfer;
    }

    public synchronized void reDoTable(ArrayList<FileInfo> files) {
        // get the size from the files array and add one, because of backwards traversing 
        int size = files.size() + 1;

        current = 0;

        data = new Object[size][];

        //add the functionality to traverse backwards
        addToData(BACK);

        //add the files to the TableModel and finally set the TableModel to the table 
        for (FileInfo info : files) {
            addToData(info);
        }

        setModel(new FileListModel(data));
    }

    public synchronized void updateTable() {
        if (paths.size() > 1) {
            reDoTable(getDirectoryListing(paths.get(paths.size() - 1)));
        } else {
            reDoTable(getDirectoryListing(""));
        }
    }

    private void addToData(FileInfo fileInfo) {
        Object[] fileData = {fileInfo, fileInfo, fileInfo.getSize()};
        data[current++] = fileData;
    }

    public ArrayList<FileInfo> getDirectoryListing(String path) {
        return clientConnectionThread.sendDirectoryListingRequest(path);
    }

    public String getCurrentDirectory() {
        String rtn = "";
        for(String s: paths)
        {
            rtn += File.separatorChar + s;
        }
        return rtn;
    }

    public ClientConnectionThread getClientConnectionThread() {
        return clientConnectionThread;
    }

    public synchronized void goUp() {
        if (paths.size() > 1) {
            reDoTable(getDirectoryListing(paths.get(paths.size() - 2)));
            paths.remove(paths.size() - 1);
        } else {
            reDoTable(getDirectoryListing(""));
            paths.clear();
        }
    }

    public synchronized void goTo(String path) {
        paths.clear();
        Scanner sc = new Scanner(path);
        sc.useDelimiter("\\" + File.separator);
        while (sc.hasNext()) {
            paths.add(sc.next());
        }
        reDoTable(getDirectoryListing(path));
    }

    public List<String> getPaths() {
        return paths;
    }
}
