/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapp.Utils.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kennywang
 */
public class ChatServer {
    
    public static ServerSocket Server(InetAddress IPAddress, int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port, -1, IPAddress);
        
        final Executor pool = Executors.newCachedThreadPool();
        boolean loop = true;
        Set<ChatWorker> workers = Collections.synchronizedSet(new HashSet<ChatWorker>());
        
        System.out.println(String.format("Server started at %s:%d", serverSocket.getInetAddress().getHostAddress(), port));

        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                ChatWorker worker = null;
                
                while (loop) {
                    try {
                        socket = serverSocket.accept();
                        worker = new ChatWorker(socket, workers, pool);
                        } catch(IOException e) {

                    }

                    synchronized (workers) {
                        workers.add(worker);
                    }

                    pool.execute(worker);
                    
                }

                
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                return;
            }
        }).start();
        
        return serverSocket;
    }
    
}
