/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import transferit.net.upnp.upnpFirewallPortOpener;

/**
 *
 * @author fonsan
 */
public class UPNPGUI extends JFrame implements ActionListener {

    private ArrayList<Action> actions;
    private upnpFirewallPortOpener firewallPortOpener;

    public UPNPGUI(ArrayList<Action> actions, upnpFirewallPortOpener firewallPortOpener) {
        this.actions = actions;
        this.firewallPortOpener = firewallPortOpener;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        for (Action a : actions) {
            buttonPanel.add(new ActionButton(a, this));
        }
        JScrollPane jsp = new JScrollPane(buttonPanel);
        jsp.setPreferredSize(new Dimension(0, 200));
        add(jsp);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(((int) dim.getWidth() - 200) / 2, ((int) dim.getHeight() - 200) / 2);
        setTitle("UPNP controller");
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        ActionDialog actionDialog = new ActionDialog(this, ((ActionButton) e.getSource()).getUPNPAction());
    }

    class ActionButton extends JButton {

        private Action action;

        public ActionButton(Action action, ActionListener actionListener) {
            this.addActionListener(actionListener);
            setText(action.getName());
            this.action = action;
        }

        public Action getUPNPAction() {
            return action;
        }
    }

    class ActionDialog extends JDialog implements ActionListener {

        private Action action;
        private JButton okButton;
        private JButton cancelButton;
        private boolean result;
        private ArgumentList inArgList;
        private Vector inArgFieldList;

        public ActionDialog(Frame frame, Action action) {
            super(frame, true);
            getContentPane().setLayout(new BorderLayout());

            this.action = action;

            inArgList = new ArgumentList();
            inArgFieldList = new Vector();

            JPanel argListPane = new JPanel();
            argListPane.setLayout(new GridLayout(0, 2));
            getContentPane().add(argListPane, BorderLayout.CENTER);

            ArgumentList argList = action.getArgumentList();
            int nArgs = argList.size();
            for (int n = 0; n < nArgs; n++) {
                Argument arg = argList.getArgument(n);
                if (arg.isInDirection() == false) {
                    continue;
                }
                JLabel argLabel = new JLabel(arg.getName());
                JTextField argField = new JTextField();

                inArgFieldList.add(argField);
                argListPane.add(argLabel);
                argListPane.add(argField);

                Argument inArg = new Argument();
                inArg.setName(arg.getName());
                inArgList.add(inArg);
            }

            okButton = new JButton("OK");
            okButton.addActionListener(this);
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);
            JPanel buttonPane = new JPanel();
            buttonPane.add(okButton);
            buttonPane.add(cancelButton);
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            setTitle("Input arguments for " + action.getName());
            pack();
            Dimension size = getSize();
            Point fpos = frame.getLocationOnScreen();
            Dimension fsize = frame.getSize();
            setLocation(fpos.x + (fsize.width - size.width) / 2, fpos.y + (fsize.height - size.height) / 2);
            setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okButton) {
                result = true;
                int fieldCnt = inArgFieldList.size();
                for (int n = 0; n < fieldCnt; n++) {
                    JTextField field = (JTextField) inArgFieldList.get(n);
                    String value = field.getText();
                    Argument arg = inArgList.getArgument(n);
                    arg.setValue(value);
                }
                ArgumentList out = firewallPortOpener.doAction(action.getName(), inArgList);
                String textResult = "";
                for (int x = 0; x < out.size(); x++) {
                    Argument arg = out.getArgument(x);
                    textResult += arg.getName() + " = " + arg.getValue() + "\n";
                }
                JOptionPane.showMessageDialog(UPNPGUI.this, textResult);
                dispose();
            }
            if (e.getSource() == cancelButton) {
                result = false;
                dispose();
            }
        }
    }
}
