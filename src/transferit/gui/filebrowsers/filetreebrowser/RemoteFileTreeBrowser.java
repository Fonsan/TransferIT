package transferit.gui.filebrowsers.filetreebrowser;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import transferit.gui.filebrowsers.FileInfo;
import transferit.gui.filebrowsers.HiddenFilter;
import transferit.net.client.ClientConnectionThread;

/**
 *
 * @author delusive
 */
public class RemoteFileTreeBrowser extends JPanel implements TreeWillExpandListener, TreeModelListener {

    static private JTree tree;
    private HashMap<String, TransferNode> objectMap = new HashMap<String, TransferNode>();
    private String currentPath;
    private FileFilter hiddenFilter = new HiddenFilter();
    private TransferNode root;
    private TreeModel treeModel;
    private ClientConnectionThread clientConnectionThread;
    private boolean treeIsExpanding;

    public RemoteFileTreeBrowser(ClientConnectionThread clientConnectionThread) {
        super(new GridLayout(1, 0));
        this.currentPath = "";
        this.clientConnectionThread = clientConnectionThread;

        //Creates a tree model..
        root = new TransferNode("");
        treeModel = new DefaultTreeModel(root);
        treeModel.addTreeModelListener(this);
        FileTreeRenderer fileTreeRenderer = new FileTreeRenderer();


        //Try because if path is invalid nasty exception is thrown.
        try {
            /* addNodes creates a File array of the content in the
             * current path. It creates node-objects out of the Files
             * and adds them to the root node initiated above. Doing this
             * before adding the node to the tree allows us to
             * load the tree filled with desired nodes directly. */
            addNodes(getDirectoryListing(""));
        } catch (NullPointerException e) {
            System.out.println("Current path does not exist.");
        }

        tree = new JTree(treeModel); //Takes a TreeNode as parameter and sets it as root node
        tree.addTreeWillExpandListener(this);
        tree.setRootVisible(false); //Hides the ugly root node..

        //Dynamically changing shit.. TreeModel stuffies
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(fileTreeRenderer);

        //Needed to show the tree in the jframe..
        JScrollPane treeView = new JScrollPane(tree);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        Dimension minimumSize = new Dimension(100, 50);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100);
        splitPane.setPreferredSize(new Dimension(300, 500));
        //Add the split pane to this panel.
        add(splitPane);

    }

    public String getCurrentDirectory() {
        return currentPath;
    }

    public void addNodes(ArrayList<FileInfo> fileInfos) {

        TransferNode object = null;

        for (FileInfo fileInfo : fileInfos) {
            object = new TransferNode(fileInfo);

            if (treeIsExpanding) {
                objectMap.get(currentPath).add(object);
            }

            if (fileInfo.isFolder()) {
                objectMap.put(fileInfo.getFilePath(), object);
            }

            if (!treeIsExpanding) {
                root.add(object);
            }
        }

        treeIsExpanding = false;
    }

    /**
     * @param args the command line arguments
     */
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        Object object = event.getPath().getLastPathComponent();

        if (object instanceof TransferNode) {
            TransferNode tn = (TransferNode) object;
            currentPath = tn.getFileInfo().getFilePath();
            treeIsExpanding = true;
            addNodes(getDirectoryListing(currentPath));
        }
    }

    public ArrayList<FileInfo> getDirectoryListing(String path) {
        return clientConnectionThread.sendDirectoryListingRequest(path);
    }

    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        Object object = event.getPath().getLastPathComponent();

        if (object instanceof TransferNode) {
            TransferNode tn = (TransferNode) object;
            currentPath = tn.getFileInfo().getFilePath();
        }
    }

    public void goTo(String path) {

    }

    public ArrayList<FileInfo> getSelectedFiles() {
        ArrayList<FileInfo> transfer = new ArrayList<FileInfo>();
        TreePath[] selected = tree.getSelectionPaths();

        for (TreePath path : selected) {
            if (path.getLastPathComponent() instanceof TransferNode) {
                TransferNode temp = (TransferNode) path.getLastPathComponent();
                transfer.add(new FileInfo(temp.getFile().getName(),
                        temp.getFile().getAbsolutePath(), temp.getFile().length(),
                        temp.getFile().isDirectory()));
            }
        }
        return transfer;
    }

    public void treeNodesChanged(TreeModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void treeNodesInserted(TreeModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void treeStructureChanged(TreeModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ClientConnectionThread getClientConnectionThread() {
        return clientConnectionThread;
    }

    private static void createAndShowGUI() {
    /*try {
    UIManager.setLookAndFeel(
    UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    System.err.println("Couldn't use system look and feel.");
    }
    //Create and set up the window.
    JFrame frame = new JFrame("TreeDemo");
    frame.setPreferredSize(new Dimension(400, 600));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //Add content to the window.
    frame.add(new RemoteFileTreeBrowser("C:\\"));
    //Display the window.
    frame.pack();
    frame.setVisible(true);*/
    }

    public static void main(String[] args) {
        createAndShowGUI();
    //Schedule a job for the event dispatch thread:
    //creating and showing this application's GUI.
        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
    public void run() {
    createAndShowGUI();
    }
    });*/
    }
}
