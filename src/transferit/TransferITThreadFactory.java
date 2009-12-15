/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transferit;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author fonsan
 */
public class TransferITThreadFactory implements ThreadFactory {

    public void dispatchNewThread(Runnable r, String name) {
        Thread thread = newThread(r);
        thread.setName("TransferITThread-" + name + "-" + thread.getName());
        thread.setDaemon(true);
        thread.start();
    }

    public Thread newThread(Runnable r) {
        return Executors.defaultThreadFactory().newThread(r);
    }
}
