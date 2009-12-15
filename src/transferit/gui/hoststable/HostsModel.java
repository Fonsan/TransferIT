/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui.hoststable;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author distortion
 */
public class HostsModel extends DefaultTableModel {

    public HostsModel() {
    }

    public HostsModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0 || column == 1) {
            return false;
        }
        return true;
    }

 

    @Override
    public void setValueAt(Object aValue, int aRow, int aCol) {
        Vector dataRow = (Vector) dataVector.elementAt(aRow);
        dataRow.setElementAt(aValue, aCol);
        fireTableCellUpdated(aRow, aCol);
    }
}
