package com.mycompany.chatapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread implements ICloseable {

    public static final int PORT = 1234; // server socket port number

    public static int newClientID = 0; //id will be incremented and given to the most recently joined client
    private final ServerSocket serverSocket;
    private Socket socket;

    Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            //  ss = new ServerSocket(PORT); //open a server socket on port 1234 to listen for connection
            System.out.println("The chat server is up and running on port: " + PORT);

            while (!serverSocket.isClosed()) {
                socket = serverSocket.accept(); //accept the connection
                ++newClientID;

                ClientHandler c = new ClientHandler(socket); //create a ClienHandler object which spawns a thread that is responsible for the communication with a client
                ClientHandler.clientsConnected.add(c); //add the ClientHandler to the array list
                c.start(); //starts the run() method of the thread of ClientHandler
            }
        } catch (IOException e) {
            if (serverSocket != null) {
                closeEverything();
            }
        }
    }

    @Override
    public void closeEverything() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) throws IOException {
        new Server(new ServerSocket(PORT)).start();
    }
}
