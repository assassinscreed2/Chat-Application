package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client{

    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private Socket client;

    Client(){
        done = false;
    }

    public void run(){
        try {
            client = new Socket("127.0.0.1",9999);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(),true);

            InputHandler handler = new InputHandler();

            Thread thread = new Thread(handler);
            thread.start();

            String inMessage;
            while((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void shutdown() throws IOException {
        done = true;
        in.close();
        out.close();
        if(!client.isClosed()){
            client.close();
        }
    }

    class InputHandler implements Runnable{
        public void run(){
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String message = reader.readLine();
                    if(message.equals("/quit")){
                        out.println(message);
                        reader.close();
                        shutdown();
                    }else{
                        out.println(message);
                    }
                }
            }catch(IOException e){
                try {
                    shutdown();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

}