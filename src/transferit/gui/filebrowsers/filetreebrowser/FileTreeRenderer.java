package transferit.gui.filebrowsers.filetreebrowser;

import transferit.gui.filebrowsers.*;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import static transferit.gui.filebrowsers.InfoContainer.*;

/**
 *
 * @author Mathias Söderberg
 */
public class FileTreeRenderer extends DefaultTreeCellRenderer {

    private String type;
    private File file;
    private FileInfo fileInfo;

    public FileTreeRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof TransferNode) {
            TransferNode node = (TransferNode) value;
            if (node.getFile() != null) {
                setNameAndIcon(node.getFile().getName(), node.getFile().isDirectory());
            } else if (node.getFileInfo() != null) {
                setNameAndIcon(node.getFileInfo().getFileName(), node.getFileInfo().isFolder());
            }
        }

        return this;
    }

    private void setNameAndIcon(String fileName, boolean directory) {
        String extension = null;
        File tmpFile = null;

        /* set the icon */
        if (fileName.contains(".") && !directory) {
            extension = fileName.substring(fileName.lastIndexOf("."));
            if (icons.containsKey(extension)) {
                setIcon(icons.get(extension));
            } else if (file != null && !icons.containsKey(extension)) {
                icons.put(extension, getSystemIcon(file));
                setIcon(icons.get(extension));
            } else {
                tmpFile = makeTempFile(fileName, directory);
                icons.put(extension, getSystemIcon(tmpFile));
                tmpFile.delete();
                setIcon(icons.get(extension));
            }
        } else if (file != null && directory) {
            if (!icons.containsKey("Directory")) {
                icons.put("Directory", getSystemIcon(file));
            }
            setIcon(icons.get("Directory"));
        } else {
            tmpFile = makeTempFile(fileName, directory);
            icons.put(extension, getSystemIcon(tmpFile));
            tmpFile.delete();
            setIcon(icons.get(extension));
        }

        /* set the "name" that will be displayed in the file browser */
        if (file != null) {
            setText(getSystemDisplayName(file));
        } else {
            setText(getSystemDisplayName(new File(fileName)));
        }
        setHorizontalAlignment(LEFT);
    }

    private void setFileSize(long l) {
        if (l == 0) {
            setText("");
        } else {
            setText(l + " B");
        }
    }

    private void setType(String fileName, boolean directory) {
        String extension = null;

        if (fileName.contains(".") && !directory) {
            extension = fileName.substring(fileName.lastIndexOf("."));
            if (description.containsKey(extension)) {
                setText(description.get(extension));
            } else {
                if (file != null) {
                    description.put(extension, getSystemDescription(file));
                } else {
                    File tmpFile = makeTempFile(fileName, directory);
                    description.put(extension, getSystemDescription(tmpFile));
                    tmpFile.delete();
                }
                setText(description.get(extension));
            }
        } else if (directory && fileName.equals("")) {
            // fixa en hashmap eller nÃ¥t fÃ¶r diskar/cd/diskett etc
            setText(getSystemDescription(file));
        } else if (directory) {
            if (description.containsKey("Directory")) {
                setText(description.get("Directory"));
            } else {
                description.put("Directory", getSystemDescription(file));
                setText(description.get("Directory"));
            }
        }

        /* set the alignemt to the left, like in Windows Explorer */
        setHorizontalAlignment(LEFT);
    }

    private File makeTempFile(String file, boolean dir) {
        File tmpFile = null;
        try {
            tmpFile = new File(file);
            if (dir) {
                tmpFile.mkdir();
            } else {
                tmpFile.createNewFile();
            }
        } catch (IOException ex) {
        }
        return tmpFile;
    }
}
