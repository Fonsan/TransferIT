/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui.hoststable;

import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author distortion
 */
public class HostsRenderer extends DefaultTableCellRenderer {

    private final String path = ("/transferit/gui/images/");
    private final ImageIcon MULTICAST_ICON = new ImageIcon(this.getClass().getResource(path + "multicast.png"));
    private final ImageIcon CONNECT_ICON = new ImageIcon(this.getClass().getResource(path + "connect.png"));
    private final ImageIcon HISTORY_ICON = new ImageIcon(this.getClass().getResource(path + "history.png"));
    private final ImageIcon REMOVE_ICON = new ImageIcon(this.getClass().getResource(path + "cancel.png"));
    private List<JLabel> multiCastLabels = new LinkedList<JLabel>();
    private List<JLabel> connectLabels = new LinkedList<JLabel>();
    private List<JLabel> removeLabels = new LinkedList<JLabel>();

    

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Boolean) {
            Boolean isHistory = (Boolean) value;
            if (multiCastLabels.isEmpty()) {
                JLabel label = initLabel(new JLabel(), isHistory ? HISTORY_ICON : MULTICAST_ICON, "Remote from history");
                multiCastLabels.add(0, label);
                return label;
            } else if (multiCastLabels.size() > row) {
                return multiCastLabels.get(row);
            } else {
                JLabel label = initLabel(new JLabel(), isHistory ? HISTORY_ICON : MULTICAST_ICON, "Remote from history");
                multiCastLabels.add(row, label);
                return label;
            }
        } else if (value instanceof String[]) {
            if (connectLabels.isEmpty()) {
                JLabel label = initLabel(new JLabel(), CONNECT_ICON, "Connect to this remote");
                connectLabels.add(0, label);
                return label;
            } else if (connectLabels.size() > row) {
                return connectLabels.get(row);
            } else {
                JLabel label = initLabel(new JLabel(), CONNECT_ICON, "Connect to this remote");
                connectLabels.add(row, label);
                return label;
            }

        } else if (value instanceof String) {
            setText((String) value);
            setHorizontalAlignment(RIGHT);
            setHorizontalTextPosition(RIGHT);
            setSelected(this, table, isSelected);
        } else if (value instanceof Integer) {
            int intValue = (Integer) value;
            if (intValue == 0) {
                if (removeLabels.isEmpty()) {
                    JLabel label = initLabel(new JLabel(), null, "Remove this remote from history");
                    removeLabels.add(0, label);
                    return label;
                } else if (removeLabels.size() > row) {
                    return removeLabels.get(row);
                } else {
                    JLabel label = initLabel(new JLabel(), null, "Remove this remote from history");
                    removeLabels.add(row, label);
                    return label;
                }
            } else {
                if (removeLabels.isEmpty()) {
                    JLabel label = initLabel(new JLabel(), REMOVE_ICON, "Remove this remote from history");
                    removeLabels.add(0, label);
                    return label;
                } else if (removeLabels.size() > row) {
                    return removeLabels.get(row);
                } else {
                    JLabel label = initLabel(new JLabel(), REMOVE_ICON, "Remove this remote from history");
                    removeLabels.add(row, label);
                    return label;
                }
            }
        }

        return this;
    }

    private JLabel initLabel(JLabel label, ImageIcon icon, String toolTip) {
        label.setHorizontalAlignment(CENTER);
        label.setOpaque(false);
        //     label.setBorder(BorderFactory.createEtchedBorder());
        label.setIcon(icon);
        return label;
    }

    private JLabel setSelected(JLabel label, JTable table, boolean isSelected) {
        if (isSelected) {
            label.setForeground(table.getSelectionForeground());
            label.setBackground(table.getSelectionBackground());
        } else {
            label.setForeground(table.getForeground());
            label.setBackground(table.getBackground());
        }
        return label;
    }

    public void removeRow(int row) {
        multiCastLabels.remove(row);
        connectLabels.remove(row);
        removeLabels.remove(row);
    }
}
