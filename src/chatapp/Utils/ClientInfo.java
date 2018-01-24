/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapp.Utils;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author kennywang
 */
public class ClientInfo {
    private Socket socket;
    private CountDownLatch doneSignal;
    private String username;
    
    public ClientInfo(Socket socket, CountDownLatch doneSignal, String username) {
        this.socket = socket;
        this.doneSignal = doneSignal;
        this.username = username;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public CountDownLatch getDoneSignal() {
        return doneSignal;
    }
    
    public String getUsername() {
        return username;
    }
}
