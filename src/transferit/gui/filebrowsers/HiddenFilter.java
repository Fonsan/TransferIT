package transferit.gui.filebrowsers;

import java.io.File;
import java.io.FileFilter;

/**
 * Show files that is shown by the system default.
 * 
 * @author Erik Fonseilus, Hannes Gustafsson, Mathias Söderberg and Henrik Wiberg
 * 
 */
public class HiddenFilter implements FileFilter {

    public boolean accept(File file) {
        return !file.isHidden();
    }
}
