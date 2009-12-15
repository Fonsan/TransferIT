/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fonsan
 */
public class LocalFileBrowserTest {

    public LocalFileBrowserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getBrowser method, of class LocalFileBrowser.
     */
    @Test
    public void testGetBrowser() {
        main(null);
    }

    public static void main(String[] a) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LocalFileBrowserTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(LocalFileBrowserTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LocalFileBrowserTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(LocalFileBrowserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        JFileChooser fileChooser = new JFileChooser(".");
        FileView view = new JavaFileView();
        fileChooser.setFileView(view);
        int status = fileChooser.showOpenDialog(null);
        if (status == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println(selectedFile.getParent());
            System.out.println(selectedFile.getName());
        } else if (status == JFileChooser.CANCEL_OPTION) {
            System.out.println("JFileChooser.CANCEL_OPTION");
        }
    }
}

class JavaFileView extends FileView {
    
    

    Icon javaIcon = new MyIcon(Color.BLUE);
    Icon classIcon = new MyIcon(Color.GREEN);
    Icon htmlIcon = new MyIcon(Color.RED);
    Icon jarIcon = new MyIcon(Color.PINK);

    public String getName(File file) {
        String filename = file.getName();
        if (filename.endsWith(".java")) {
            String name = filename + " : " + file.length();
            return name;
        }
        return null;
    }

    public String getTypeDescription(File file) {
        String typeDescription = null;
        String filename = file.getName().toLowerCase();

        if (filename.endsWith(".java")) {
            typeDescription = "Java Source";
        } else if (filename.endsWith(".class")) {
            typeDescription = "Java Class File";
        } else if (filename.endsWith(".jar")) {
            typeDescription = "Java Archive";
        } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            typeDescription = "Applet Loader";
        }
        return typeDescription;
    }

    public Icon getIcon(File file) {
        if (file.isDirectory()) {
            return null;
        }
        Icon icon = null;
        String filename = file.getName().toLowerCase();
        if (filename.endsWith(".java")) {
            icon = javaIcon;
        } else if (filename.endsWith(".class")) {
            icon = classIcon;
        } else if (filename.endsWith(".jar")) {
            icon = jarIcon;
        } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            icon = htmlIcon;
        }
        return icon;
    }
}

class MyIcon implements Icon {

    Color myColor;

    public MyIcon(Color myColor) {
        this.myColor = myColor;
    }

    public int getIconWidth() {
        return 16;
    }

    public int getIconHeight() {
        return 16;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(myColor);
        g.drawRect(0, 0, 16, 16);
    }
}

