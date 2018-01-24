/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapp.Utils.Client;

/**
 *
 * @author kennywang
 */
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class ThreadedOutputStream {
    private static Socket socket;
    private static DataOutputStream outputStream;
    private static CountDownLatch signal;

    public ThreadedOutputStream(Socket socket, CountDownLatch signal, String username) throws IOException {
        this.socket = socket;
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.signal = signal;
        
        createUsername(username);
    }
    
    public static void sendMessage(String message) throws IOException {
        outputStream.writeUTF(message);
    } 

//    @Override
//    public void run() {
//        Scanner input = new Scanner(System.in);
//        boolean running = true;
//
//        while (running) {
//            String request = input.nextLine();
//
//            try {
//                outputStream.writeUTF(request);
//
//                if (request.equals("/stop")) {
//                    outputStream.close();
//                    signal.countDown();
//                    return;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                break;
//            }
//        }
//    }
    
    private void createUsername(String username) throws IOException {
        outputStream.writeUTF(String.format("/name %s", username));
    }
}
