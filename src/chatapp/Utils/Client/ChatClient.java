/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapp.Utils.Client;

import chatapp.Utils.ClientInfo;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kennywang
 */
public class ChatClient {
    private String IPAddress;
    private int port;
    private String username;

    public ChatClient(String IPAddress, int port, String username) {
        this.IPAddress = IPAddress;
        this.port = port;
        this.username = username;
    }
    
//    @Override
//    public void run() {
//        try {
//            instantiate(IPAddress, port, username);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//    }
    
    public ClientInfo instantiate() throws IOException {

        final Socket socket = new Socket(IPAddress, port);
        final CountDownLatch doneSignal = new CountDownLatch(1);

        System.out.println(String.format("Connected to the server"));
        
        return new ClientInfo(socket, doneSignal, username);

        // start input and output thread
//        final ThreadedInputStream inputStream = new ThreadedInputStream(socket, doneSignal);
//        final ThreadedOutputStream outputStream = new ThreadedOutputStream(socket, doneSignal, username);

//        ExecutorService pool = Executors.newFixedThreadPool(1);
//        pool.submit(inputStream);
//        pool.submit(outputStream);
        



        //while (inputStream.isAlive() && outputStream.isAlive());
//        try {
//            System.out.println("await0");
//            doneSignal.await();
//            System.out.println("await1");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Disconnected from server");
//        socket.close();
    }
}
