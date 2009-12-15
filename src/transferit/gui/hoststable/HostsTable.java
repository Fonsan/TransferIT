/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui.hoststable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import transferit.TransferITController;

/**
 *
 * @author distortion
 */
public class HostsTable extends JTable {

    private TransferITController controller;
    private HostsModel hostsModel = new HostsModel();
    private HostsRenderer hostsRenderer;
    private HostsEditor hostsEditor;
    private List<HostsObject> hostObjects = new ArrayList<HostsObject>();
    private List<HostsObject> tmpObjects = new ArrayList<HostsObject>();
    //  private Set<String> history;
    private Object[] columnNames = {"Host", "Multi", "Connect", "Remove"};
    private static final int HOST_COLUMN = 0;

    public HostsTable() {
        super();
    }

    public HostsTable(TransferITController controller) {
        this.controller = controller;

        hostsModel.setColumnIdentifiers(columnNames);
        setModel(hostsModel);
        
        Object[][] data = new Object[controller.getTransferITModel().getHostHistory().size()][];

        int current = 0;
        for (String hist : controller.getTransferITModel().getHostHistory()) {

            String username = controller.getTransferITModel().getHistoryUsername(hist);
            String password = controller.getTransferITModel().getHistoryPassowrd(hist);
            String[] userAndPass = {hist, username, password};
            HostsObject object = new HostsObject(hist, true, userAndPass);
            hostObjects.add(object);
            Object[] rowData = {hist, true, userAndPass, 1};
            hostsModel.addRow(rowData);
            data[current++] = rowData;

        }

        hostsRenderer = new HostsRenderer();
        hostsEditor = new HostsEditor(this.controller, this, hostsRenderer, data);

        setTableHeader(null);
        setFillsViewportHeight(true);
        setShowGrid(false);

        getColumn("Multi").setMaxWidth(24);
        getColumn("Multi").setMinWidth(24);
        getColumn("Connect").setMaxWidth(24);
        getColumn("Connect").setMinWidth(24);
        getColumn("Remove").setMaxWidth(24);
        getColumn("Remove").setMinWidth(24);
        setRowHeight(24);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return hostsRenderer;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (column == 2 || column == 3) {
            return hostsEditor;
        }
        return super.getCellEditor(row, column);
    }

    public HostsModel getHostsModel() {
        return hostsModel;
    }

    public void removeRow(int rowToRemove) {
        Object o = hostsModel.getValueAt(rowToRemove, HOST_COLUMN);
        hostsModel.removeRow(rowToRemove);
        controller.getTransferITModel().removeHostFromHistory((String) o);
    }

    public synchronized void updateHostsTable(Map<String, String> hostnames) {

        if (hostnames.size() > 0) {
            Set<String> available = new HashSet<String>(hostnames.keySet());

            for (String host : available) {
                String username = "anon";
                String password = "anon";
                String newHost = hostnames.containsKey(host) ? hostnames.get(host) : host;
                String[] userAndPass = {host, username, password};
                HostsObject hostsObject = new HostsObject(newHost, false, userAndPass);
                tmpObjects.add(hostsObject);
            }

            for (HostsObject object : tmpObjects) {
                if (!hostObjects.contains(object)) {
                    hostObjects.add(object);
                    Object[] data = {object.getHost(), false, object.getInfo(), 0};
                    hostsModel.addRow(data);
                }
            }
        }
    }
}



