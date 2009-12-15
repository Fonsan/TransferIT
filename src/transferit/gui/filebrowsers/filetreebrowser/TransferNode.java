package transferit.gui.filebrowsers.filetreebrowser;

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import transferit.gui.filebrowsers.FileInfo;

/**
 *
 * @author delusive
 */
public class TransferNode extends DefaultMutableTreeNode {

    private String nodeName;
    private File file;
    private FileInfo fileInfo;

    public TransferNode(String nodeName) {
        super(nodeName, true);
        this.nodeName = nodeName;

    }

    public TransferNode(File file) {
        this.file = file;
        this.nodeName = file.getName();
        this.fileInfo = new FileInfo(file.getName(), file.getPath(), file.length(), file.isDirectory());
    }

    public TransferNode(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        this.nodeName = fileInfo.getFileName();
    }

    @Override
    public boolean isLeaf() {
        if (file != null) {
            return !file.isDirectory();
        } else if (fileInfo != null) {
            return !fileInfo.isFolder();
        }
        return false;
    }

    @Override
    public String toString() {
        if (file != null) {
            return file.getName();
        }
        return this.nodeName;
    }

    public File getFile() {
        return this.file;
    }

    public FileInfo getFileInfo() {
        if (fileInfo != null) {
            return fileInfo;
        }
        return null;
    }
}
