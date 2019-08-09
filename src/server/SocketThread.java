package server;

import cars.dto.Request;
import response.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class SocketThread implements Runnable
{
    private Socket clientConnection;
    private Protocol protocol;

    public SocketThread(Socket clientConnection, Protocol protocol) {
        this.protocol = protocol;
        this.clientConnection = clientConnection;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream ois = new ObjectInputStream(clientConnection.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(clientConnection.getOutputStream());
        ){
            Request request = null;
           while (true){
               try {
                   request = (Request) ois.readObject();
               } catch (ClassNotFoundException e) {
                   throw new RuntimeException(e);
               }
               if (request == null) break;
               oos.writeObject(protocol.getResponse(request));
           }
        } catch (SocketException e) {
            System.out.println("client disconnect");
        } catch (IOException e) { }
    }
}
