/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit.net.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
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

/**
 *
 * @author fonsan
 */
public class ClientConnectionThreadTest {

    public ClientConnectionThreadTest() {
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
     * Test of run method, of class ClientConnectionThread.
     */
    @Test
    public void run() {
        try {
            System.out.println("run");
            ServerSocket srvSocket = new ServerSocket(62121);
            Socket socket = srvSocket.accept();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println(readString(in));
            System.out.println(readString(in));
            System.out.println(readString(in));
            out.writeUTF("AUTHED");
            out.flush();
            out.writeUTF("AUTHWRITE");
            out.flush();

            while (true) {
                String input = readString(in);
                if (input.equals(ClientCommands.GETFILE.toString())) {
                    getReadFile(in, out);
                } else if (input.equals("SENDFILE")) {
                    getSendFile(in, out);
                } else if (input.equals(ClientCommands.GETFILELIST.toString())) {
                    readString(in);
                    ArrayList<FileInfo> files = new ArrayList<FileInfo>();
                    files.add(new FileInfo("fff", "ad", 14L, false));
                    out.writeUnshared(files);
                    out.reset();
                    out.flush();
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ClientConnectionThreadTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private String readString(ObjectInputStream in) throws IOException
    {
        String input = in.readUTF();
        System.out.println("read: " + input);
        return input;
    }

    private void getSendFile(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String file = readString(in);
        String buffersize = readString(in);
        String length = readString(in);
        Long bytesToRead = Long.parseLong(length);
        int bufferLength = Integer.parseInt(buffersize);
        byte[] buffer = new byte[bufferLength];
        int x =0;
        while (bytesToRead > 0) {
            System.out.println(in.read());
            x++;
            System.out.println(x);
            if (bytesToRead < bufferLength) {
                bufferLength = new Long(bytesToRead).intValue();
                buffer = new byte[bufferLength];
            }
            in.readFully(buffer);
        }

    }

    private void getReadFile(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String file = readString(in);
        int buffersize = 50000;
        long hundredMB = 1024 * 1024 * 30;
        out.writeUTF(Integer.toString(buffersize));
        out.flush();
        out.writeUTF(Long.toString(hundredMB));
        out.flush();
        byte [] buffer = new byte[buffersize];
        
        while(hundredMB >0)
        {
            out.write(0);
            if(hundredMB < buffersize)
            {
                buffersize = new Long(hundredMB).intValue();
                buffer =  new byte[buffersize];
                Arrays.fill(buffer,(byte)21);
            }
            out.write(buffer);
            hundredMB -= buffersize;
        }
        readString(in);
        System.out.println("done");
        
    }

    public static void main(String[] args) {
        new ClientConnectionThreadTest().run();
    }
}