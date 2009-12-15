package transferit.gui.filebrowsers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * Contains Maps with file descriptions and icons for specific files. 
 * E.g mp3
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class InfoContainer {

    public static Map<String, Icon> icons = new HashMap<String, Icon>();
    public static Map<String, String> description = new HashMap<String, String>();

    /**
     * Fetches the system default icon for the specified file extension.
     * 
     * @param file the file which icon we want
     * @return the icon.
     */
    public static Icon getSystemIcon(File file) {
        return FileSystemView.getFileSystemView().getSystemIcon(file);
    }

    /**
     * Fetches the system default extension description for specified file.
     * 
     * @param file the file which we want the description for.
     * @return the description.
     */
    public static String getSystemDescription(File file) {
        return FileSystemView.getFileSystemView().getSystemTypeDescription(file);
    }

    /**
     * Fetches how the system defaults file name display. E.g with or without
     * extension.
     * 
     * @param file the file which we want the description for.
     * @return the display name.
     */
    public static String getSystemDisplayName(File file) {
        return FileSystemView.getFileSystemView().getSystemDisplayName(file);
    }
}
