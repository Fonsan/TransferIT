package transferit.gui.filebrowsers.filelistbrowser;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class FileListBrowser extends JTable {

    public FileListBrowser() {
        super();
        initFileListBrowser();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        String columnName = getColumnName(column);

        if (columnName.equals("Name")) {
            return new FileListRenderer("Name");
        } else if (columnName.equals("Type")) {
            return new FileListRenderer("Type");
        }

        return new FileListRenderer("Size");
    }

    private void initFileListBrowser() {
        setShowGrid(false);

        setFillsViewportHeight(true);
    }
}
