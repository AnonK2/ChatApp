package chatapp.Utils.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ChatWorker implements Runnable {
    private final Socket socket;
    private final Set<ChatWorker> workers;
    private final Executor pool;
    private final Queue<String> requests;
    private final Queue<String> responses;
    private String name;

    public ChatWorker(final Socket socket, final Set<ChatWorker> workers, final Executor pool) throws IOException {
        this.socket = socket;
        this.workers = workers;
        this.pool = pool;
        this.name = "unknown";

        this.requests = new ConcurrentLinkedQueue<String>();
        this.responses = new ConcurrentLinkedQueue<String>();
    }

    @Override
    public void run() {
        System.out.println(String.format("Client connected: %s", this.socket.getRemoteSocketAddress()));
        
        try {
            if(!inputOutputStream())
                return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    boolean inputOutputStream() throws IOException {
        boolean running = true;

        final Runnable input = new InputThread(this.socket, this.requests);
        final Runnable output = new OutputThread(this.socket, this.responses);
        
        pool.execute(input);
        pool.execute(output);
        
        while (running) {
            // get request
            String request = null;
            synchronized (requests) {
                request = requests.poll();
            }
            
            if (request != null) {
                //Change name
                if (request.startsWith("/name")) {
                    String name = request.split(" ").length > 1 ? request.split(" ")[1] : "unknown";
                    this.name = name;
                } else if (request.startsWith("/client/") || request.startsWith("@")) {
                    String[] requestArgs = Stream.of(request.split(" ")).map(arg -> arg.trim())
                            .toArray(size -> new String[size]);//OR .toArray(String[]::new);
                    
                    // System.out.println(requestArgs.length); //MUST BE AT LEAST 3(/to-client, targeterName, message)
                    
                    if (requestArgs.length < 2) {
                        broadcastPublicExceptUs(String.format("%s: %s", this.name, request));
//                        return running; //This will lead to freeze, before returning... we MUST "requests.wait();".
                    } else {
                        String targeterName = null;
                        
                        if (request.startsWith("/client/")) {
                            targeterName = requestArgs[0].replace("/client/", "");
                        } else {
                            targeterName = requestArgs[0].replace("@", "");
                        }

                        int messageFirstIndex = 1;
                        int messageLength = requestArgs.length;
                        String message = String.join(" ", Arrays.copyOfRange(requestArgs, messageFirstIndex, messageLength));

                        System.out.println(requestArgs.length);

                        boolean res = broadcastPrivateToClient(targeterName, String.format("%s: %s", this.name, message));

                        if (!res) {
                            broadcastPublicExceptUs(String.format("%s: %s", this.name, request));
//                            return running; //This will lead to freeze, before returning... we MUST "requests.wait();".
                        }
                    }
                } else {
                    switch (request) {
                        case "/stop":
                            getMessage("bye-bye");
                            running = false;
                            broadcastDisconnectMessageToOtherUsers();
                            // addMessage("HI"); //will throw "java.lang.IllegalMonitorStateException"
                            //because "this" worker is already got removed from "broadcastDisconnectMessageToOtherUsers()".
                            break;
                        case "/time":
                            getMessage(new Date().toString());
                            break;
                        case "/clients":
                            getMessage(workers.stream()
                                    .map(x -> x.name)
                                    .collect(Collectors.joining(", ")));
                            break;
                        case "/memory":
                            getMessage(String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                                    .freeMemory()));
                            break;
                        default:
                            if (!request.trim().equals("")) {
                                broadcastPublicExceptUs(String.format("%s: %s", this.name, request));
                            }
                            break;
                    }
                }
                
            }
            
            //This step is A MUST!
            synchronized (requests) {
                try {
                    requests.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }

            
        }

        return running;
    }

    void broadcastDisconnectMessageToOtherUsers() {
        System.out.println(String.format("Client disconnects: %s", this.socket.getRemoteSocketAddress()));
        synchronized (workers) {
            workers.remove(this);
            broadcastPublicExceptUs(String.format("%s has left", this.name));
        }
    }

    //Broadcast to public except us
    public void broadcastPublicExceptUs(String message) {
        synchronized (workers) {
            workers.stream().filter(worker -> worker != this).forEach(worker -> {
                worker.getMessage(String.format(message));
            });
        }
    }

    //Broadcast to public
    public void broadcastPublic(String message) {
        synchronized (workers) {
            workers.stream().forEach(worker -> {
                worker.getMessage(String.format(message));
            });
        }
    }

    //Broadcast to private
    public boolean broadcastPrivateToClient(String clientName, String message) {
        synchronized (workers) {
            //Find the targeter
            ChatWorker targeter = workers.stream().filter(worker -> clientName.equals(worker.name))
                .findAny()
                .orElse(null);

                if (targeter == null)
                    return false;

            //Send to targeter
            targeter.getMessage(message);
            
            return true;
        }
    }

    public void getMessage(String message) {
        synchronized (responses) {
            responses.offer(message); //will not return false if Queue is full, but "add()" will throw an Exception.
            responses.notify();
        }
    }
}


