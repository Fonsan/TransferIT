/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui;

import transferit.gui.SettingFrame;
import java.awt.Container;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author distortion
 */
public class SettingsFrameListSelectionListener implements ListSelectionListener {

    private static JList settingsFrameList;

    public SettingsFrameListSelectionListener(JList settingsFrameList) {
        SettingsFrameListSelectionListener.settingsFrameList = settingsFrameList;
    }

    public void valueChanged(ListSelectionEvent e) {
        try {

            int index = settingsFrameList.getSelectedIndex();

            Container c = settingsFrameList.getParent();
            while (!(c instanceof SettingFrame)) {
                c = c.getParent();
            }
            SettingFrame frame = (SettingFrame) c;
            frame.notifyText((String) settingsFrameList.getModel().getElementAt(index));

        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }
}
