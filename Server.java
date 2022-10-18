package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket serverSocket;
    private boolean done;
    private ExecutorService executorService;  // thread pool

    Server(){
        connections = new ArrayList<>();
        done = false;
    }

    //Server constantly listen to incoming request
    public void run(){
        try {
            serverSocket = new ServerSocket(9999);

            while(!done){
                Socket client = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                executorService = Executors.newCachedThreadPool();
                connections.add(handler);
                executorService.execute(handler);
            }

        } catch (IOException e) {
            try {
                shutdownServer();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // broadcast message
    void broadcast(String message){
        for(ConnectionHandler ch:connections){
            ch.sendMessage(message);
        }
    }

    void shutdownServer() throws IOException {
        done = true;
        if(!serverSocket.isClosed()){
            serverSocket.close();
        }

        for(ConnectionHandler ch : connections){
            ch.shutdownClient();
        }
    }

    //Handles client individual connection
    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        ConnectionHandler(Socket client) {
            this.client = client;
        }

        public void run(){
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter you nickname");
                nickname = in.readLine();

                broadcast(nickname+" joined the chat");
                String message;

                while((message = in.readLine()) != null){
                    String[] splitmessage = message.split(" ");
                    if(splitmessage[0].equals("/nick")){
                        if(splitmessage.length >= 2){
                            String newnickname = splitmessage[1];
                            broadcast(nickname +" changed nickname to "+newnickname);
                            nickname = newnickname;
                            out.println("nickname changed");
                        }else{
                            out.println("no nickname provided");
                        }
                    }else if(message.equals("/quit")){
                        // todo: quit
                        broadcast(nickname+" left the chat room");
                        shutdownClient();
                    }else{
                        broadcast(nickname+": "+message);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void shutdownClient(){
            try{
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        void sendMessage(String message){
            out.println(message);
        }

    }

    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }
}
