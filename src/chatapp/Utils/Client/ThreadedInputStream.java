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
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import javax.swing.JTextArea;

public class ThreadedInputStream extends Thread {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final CountDownLatch signal;
    
    private final JTextArea chatTA;

    public ThreadedInputStream(Socket socket, CountDownLatch signal, JTextArea chatTA) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.signal = signal;
        
        this.chatTA = chatTA;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String response = inputStream.readUTF();
//                System.out.println(response);
                chatTA.append(response + "\n\r"); //update chat
            } catch (EOFException e) {
                break;
            } catch (IOException e) {
                break;
            }
        }

        try {
            this.inputStream.close();
            // signal.countDown();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}

