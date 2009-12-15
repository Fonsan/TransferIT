package transferit.gui.filebrowsers.filelistbrowser;

import java.io.File;
import javax.swing.table.AbstractTableModel;

class FileListModel extends AbstractTableModel {

    private String[] columnNames = {"Name",
        "Type",
        "Size"
    };
    private Object[][] data;

    public FileListModel() {
        super();
    }

    public FileListModel(Object[][] data) {
        super();
        this.data = data;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (data[row][col] instanceof File) {
            File file = (File) data[row][col];
            return file;
        }
        return data[row][col];
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
