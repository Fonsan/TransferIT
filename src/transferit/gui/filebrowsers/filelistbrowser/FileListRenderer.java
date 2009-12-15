/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui.filebrowsers.filelistbrowser;

import transferit.gui.filebrowsers.*;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import static transferit.gui.filebrowsers.InfoContainer.*;

/**
 *
 * @author Mathias SÃ¶derberg
 */
public class FileListRenderer extends DefaultTableCellRenderer {

    private String type;
    private File file;
    private FileInfo fileInfo;
    private static final String TYPE = "Type";
    private static final String NAME = "Name";
    private static final String SIZE = "Size";

    public FileListRenderer(String type) {
        this.type = type;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof FileInfo) {
            fileInfo = (FileInfo) value;

            if (type.equals(NAME)) {
                setNameAndIcon(fileInfo.getFileName(), fileInfo.isFolder());
            } else if (type.equals(TYPE)) {
                setType(fileInfo.getFileName(), fileInfo.isFolder());
            } else if (type.equals(SIZE)) {
                setText(setFileSize((Long) value));
            }
        } else if (value instanceof File) {
            file = (File) value;

            if (type.equals(NAME)) {
                setNameAndIcon(file.getName(), file.isDirectory());
            } else if (type.equals(TYPE)) {
                setType(file.getName(), file.isDirectory());
            } else if (type.equals(SIZE)) {
                setText(setFileSize((Long) file.length()));
            }
        } else {
            if (value instanceof Object[]) {
                Object[] values = (Object[]) value;
                setIcon((Icon) values[0]);
                setText((String) values[1]);
            } else if (value instanceof String) {
                setText((String) value);
            } else if (value instanceof Long) {
                setText(setFileSize((Long) value));
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

    public static String setFileSize(long l) {
        if (l == 0) {
            return "";
        } else {
            int count = 0;
            double result = l;
            if (result < 1024) {
                return String.format("%.5s%s", (int) result, " B");
            } else {

                while (true) {
                    result = result / 1024;
                    if (result < 1024) {
                        break;
                    }
                    count++;
                }

                switch (count) {
                    case 0:
                        return String.format("%.5s%s", (int) result, " KB"); //kb
                    case 1:
                        return String.format("%.5s%s", (int) result, " MB"); //mb
                    case 2:
                        return String.format("%.5s%s", (int) result, " GB"); //gb
                    default:
                        return "";
                }
            }
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
