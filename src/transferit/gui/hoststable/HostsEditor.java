/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui.hoststable;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.TableCellEditor;
import transferit.TransferITController;

/**
 *
 * @author distortion
 */
public class HostsEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private List<JButton> connectButtons = new LinkedList<JButton>();
    private List<JButton> removeButtons = new LinkedList<JButton>();
    private final ImageIcon CONNECT_ICON = new ImageIcon(this.getClass().getResource("/transferit/gui/images/connect.png"));
    private final ImageIcon REMOVE_ICON = new ImageIcon(this.getClass().getResource("/transferit/gui/images/cancel.png"));
    private TransferITController controller;
    private HostsTable table;
    private Object currentValue;
    private HostsRenderer hostsRenderer;

    public HostsEditor(TransferITController controller, HostsTable table, HostsRenderer hostsRenderer, Object[][] data) {
        this.controller = controller;
        this.table = table;
        this.hostsRenderer = hostsRenderer;

        for (Object[] rowData : data) {
            //String host = (String) rowData[0];
            boolean history = (Boolean) rowData[1];
            //String[] userAndPass = (String[]) rowData[2];
            //int showRemove = (Integer) rowData[3];

            JButton button = initButton(new JButton(), history ? REMOVE_ICON : null, "Remove this remote from history");
            removeButtons.add(button);
            button = initButton(new JButton(), CONNECT_ICON, "Connect to this");
            connectButtons.add(button);
        }
    }

    public Object getCellEditorValue() {
        return currentValue;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentValue = value;

        int intValue = 0;

        if (value instanceof Integer) {
            intValue = (Integer) value;

            if (intValue == 0) {
                return returnButton(removeButtons, row, null, "");
            } else if (intValue == 1) {
                return returnButton(removeButtons, row, REMOVE_ICON, "Remove this remote from history");
            }
        }

        if (value instanceof String[]) {
            return returnButton(connectButtons, row, CONNECT_ICON, "Connect to this remote");
        }

        return removeButtons.get(row); //dummy
    }

    private JButton initButton(JButton button, ImageIcon icon, String toolTip) {
        button.setIcon(icon);
        button.setToolTipText(toolTip);
        //Make the button looks the same for all Laf's
        button.setUI(new BasicButtonUI());
        //Make it transparent
        button.setContentAreaFilled(false);
        //No need to be focusable
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEtchedBorder());
        button.setBorderPainted(false);
        //Making nice rollover effect
        button.setRolloverEnabled(true);
        button.addActionListener(this);
        return button;
    }

    public void actionPerformed(ActionEvent e) {
        this.fireEditingStopped();
        JButton tmp = (JButton) e.getSource();
        Point p = new Point(tmp.getX(), tmp.getY());
        int column = table.columnAtPoint(p);
        int row = table.rowAtPoint(p);
        if (column == 2) {
            Object object = table.getValueAt(row, column);
            if (object instanceof String[]) {
                String[] info = (String[]) object;
                controller.startClientConnectionThread(info[0], info[1], info[2]);
            }
        } else if (column == 3) {
            table.removeRow(row);
            removeButtons.remove(row);
            connectButtons.remove(row);
            hostsRenderer.removeRow(row);
        }
    }

    private JButton returnButton(List<JButton> buttons, int row, ImageIcon icon, String toolTip) {
        if (buttons.isEmpty()) {
            JButton button = initButton(new JButton(), icon, toolTip);
            if (icon == null) {
                button.setEnabled(false);
            }
            buttons.add(button);
            return button;
        } else if (buttons.size() > row) {
            return buttons.get(row);
        }

        JButton button = initButton(new JButton(), icon, toolTip);
        if (icon == null) {
            button.setEnabled(false);
        }
        buttons.add(button);
        return button;
    }
}
