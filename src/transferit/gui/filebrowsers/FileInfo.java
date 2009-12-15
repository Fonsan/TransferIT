package transferit.gui.filebrowsers;

import java.io.File;
import java.io.Serializable;

/**
 * FileInfo is a wrapper for files in the remote browser. It contains vital parts
 * like file size and path to show the file correctly in the browser.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class FileInfo implements Serializable {

    private String fileName;
    private String filePath;
    private Long size;
    private boolean folder;

    /**
     * Creates a new FileInfo with file name, path, size and if it is a folder.
     * 
     * @param fileName the name.
     * @param filePath the path.
     * @param size the size.
     * @param folder is it is a folder.
     */
    public FileInfo(String fileName, String filePath, Long size, boolean folder) {
        this.fileName = fileName;
        this.filePath = filePath.replace(File.separatorChar, ':');
        //this.filePath = filePath;
        this.size = size;
        this.folder = folder;
    }

    /**
     * Returns the file name.
     * 
     * @return the name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     * 
     * @param fileName the name.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the file path.
     * 
     * @return the path.
     */
    public String getFilePath() {
        return (filePath.replace(':', File.separatorChar));
    }

    /**
     * Sets the path of the file.
     * 
     * @param filePath the path.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath.replace(File.separatorChar, ':');
    }

    /**
     * Returns if it is a folder.
     * 
     * @return true or false.
     */
    public boolean isFolder() {
        return folder;
    }

    /**
     * Sets if the FileInfo is a folder.
     * 
     * @param folder true or false.
     */
    public void setFolder(boolean folder) {
        this.folder = folder;
    }
    
    /**
     * Returns the file size.
     * 
     * @return the size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the file size.
     * 
     * @param size the size.
     */
    public void setSize(Long size) {
        this.size = size;
    }
}
    
