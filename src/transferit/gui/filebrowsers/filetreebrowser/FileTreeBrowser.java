package transferit.gui.filebrowsers.filetreebrowser;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;

/**
 *
 * @author delusively
 */
public class FileTreeBrowser extends JPanel
        implements TreeWillExpandListener {

    private JTree tree;
    private HashMap<String, TransferNode> objectMap = new HashMap<String, TransferNode>();

    public FileTreeBrowser(List<File> receivedFileStructure) {
        super(new GridLayout(1, 0));

        TreeNode rootNode = createNodes(receivedFileStructure);
        tree = new JTree(rootNode);
        tree.setCellRenderer(new FileTreeRenderer());
        tree.addTreeWillExpandListener(this);

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);

        Dimension minimumSize = new Dimension(100, 50);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100);
        splitPane.setPreferredSize(new Dimension(300, 500));

        //Add the split pane to this panel.
        add(splitPane);
    }

    /**
     * Loads and shows files and folders in the root tree.
     * Each folder object is added to a HashMap with a unique key,
     * where the key is their unique path.
     *  
     * @return TreeNode root
     * Returns root node for the JTree being created at the start.
     */
    private TreeNode createNodes(List<File> receivedFileStructure) {
        //Array containing the content of the list. 
        Object[] contentListing = receivedFileStructure.toArray();

        //Nodes for the structure.
        TransferNode rootNode = new TransferNode(receivedFileStructure.get(0).getParentFile());
        TransferNode object = null;

        //Adds files and dirs in current folder to the tree
        for (Object currentFile : contentListing) {
            object = new TransferNode((File) currentFile);
            if (!object.isLeaf()) {
                objectMap.put(object.getFile().getAbsolutePath(), object);
            }
            rootNode.add(object);
        }
        return rootNode;
    }

    /**
     * Method is called when a folder is double-clicked.
     * It loads the folders file structure and presents it
     * accordingly. Same principle as createNodes-method.
     * @param subdirPath
     * subdirPath contains the absolute path to the folder being double clicked.
     */
    private void loadSubfolderContent(String subdirPath) {
        TransferNode object = null;
        File[] contentListing = (new File(subdirPath)).listFiles();

        for (File currentFile : contentListing) {
            object = new TransferNode(currentFile);
            objectMap.get(subdirPath).add(object);
            if (!object.isLeaf()) {
                objectMap.put(object.getFile().getAbsolutePath(), object);
            }
        }

    }

    /**
     * Executed when a folder node is being double clicked.
     * Simply saves the path to the folder being clicked
     * and passes it on as a parameter to the
     * loadSubfolderContent-method.
     * @param e
     * Contains information about the event. Such as path to folder
     * being clicked, which we take advantage of below.
     * @throws javax.swing.tree.ExpandVetoException
     */
    public void treeWillExpand(TreeExpansionEvent e)
            throws ExpandVetoException {
        String subdirectoryPath;
        Object object = e.getPath().getLastPathComponent();
        if (object instanceof TransferNode) {
            TransferNode tn = (TransferNode) object;
            subdirectoryPath = tn.getFile().getAbsolutePath();
            loadSubfolderContent(subdirectoryPath);
        }
    }
    //Required by TreeWillExpandListener interface.
    /**
     * Collapses the folder node and removes subfolders
     * from the HashMap 'objectMap'.
     * @param e
     */
    public void treeWillCollapse(TreeExpansionEvent e) {
    //Perhaps implement removal of objects in the HashMap objectMap?
    }
}
