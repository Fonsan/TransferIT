/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.net.server;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import transferit.gui.filebrowsers.FileInfo;
import transferit.net.Protocol.ClientCommands;
import static org.junit.Assert.*;

/**
 *
 * @author fonsan
 */
public class ServerConnectionThreadTest {

    public ServerConnectionThreadTest() {
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
     * Test of run method, of class ServerConnectionThread.
     */
    @Test
    public void run() throws IOException, ClassNotFoundException {

        System.out.println("run");
        Socket socket = new Socket("82.182.113.155", 62121);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeUTF(ClientCommands.SENDAUTH.toString());
        out.flush();
        out.writeUTF("Fonsan");
        out.flush();
        out.writeUTF("1234");
        out.flush();
        assertEquals("AUTHED", in.readUTF());
        System.out.println("Logged On");
        assertEquals("AUTHWRITE", in.readUTF());
        System.out.println("Got write access");

        out.writeUTF(ClientCommands.GETFILELIST.toString());
        out.flush();
        out.writeUTF("");
        out.flush();
        ArrayList<FileInfo> arrayList = (ArrayList<FileInfo>) in.readObject();
        for (FileInfo fileInfo : arrayList) {
            System.out.println(fileInfo.getFileName());
            System.out.println(fileInfo.getFilePath());
            System.out.println(fileInfo.getSize());
            System.out.println(fileInfo.isFolder());
        }


        long length;
        int bufferlength;
        bufferlength = 50000;
        length = 1024 * 1024 * 1024;
        byte[] buffer = new byte[bufferlength];
        Arrays.fill(buffer, (byte) 2);

        out.writeUTF(ClientCommands.SENDFILE.toString());
        out.flush();
        out.writeUTF("testfile.jpg".replace(File.separatorChar, ':'));
        out.flush();
        out.writeUTF(Long.toString(bufferlength));
        out.flush();
        out.writeUTF(Long.toString(length));
        out.flush();

        while (0 < length) {
            if (length < bufferlength) {
                bufferlength = (int) length;
                buffer = new byte[bufferlength];
                Arrays.fill(buffer, (byte) 1);
            }
            out.write(0);
            out.write(buffer);
            length -= bufferlength;
        }
        out.flush();

        out.writeObject(ClientCommands.SENDMSG.toString());

        out.writeObject("Testing");
        out.flush();
    // TODO review the generated test code and remove the default call to fail.

    }
}
