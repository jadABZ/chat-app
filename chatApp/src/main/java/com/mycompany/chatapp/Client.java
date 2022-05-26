package com.mycompany.chatapp;

package com.mycompany.chatproject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// A client sends messages to the server, the server spawns a thread to communicate with the client.
// Each communication with a client is added to an array list so any message sent gets sent to every other client
// by looping through it.
public class Client implements ICloseable {

    // A client has a socket to connect to the server and a reader and writer to receive and send messages respectively.
    private int clientID;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client() {
        try {
            this.socket = new Socket("localhost", 1234);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything();
        }
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    // Sending a message isn't blocking and can be done without spawning a thread, unlike waiting for a message.
    public void sendMessage() {
        try {
            // Create a scanner for user input.
            Scanner scanner = new Scanner(System.in);

            // While there is still a connection with the server, continue to scan the terminal and then send the message.
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                if (messageToSend.contains("@")) {
                    bufferedWriter.write("Client " + this.clientID + ": " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } else {
                    System.out.println("Message was not sent, message should contain @");
                }

            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Listening for a message is blocking so need a separate thread for that.
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                // While there is still a connection with the server, continue to listen for messages on a separate thread.
                boolean hasRecvdID = false;
                while (!socket.isClosed()) {
                    try {
                        //first, recieve the client ID from the client handler
                        if (!hasRecvdID) {
                            int rclientID = Integer.parseInt(bufferedReader.readLine());
                            setClientID(rclientID);
                            hasRecvdID = true;
                            System.out.println("Welcome! your ID is " + rclientID);
                            System.out.println("Make sure to follow the messaging protocol: message@id1,id2,...,idn or message@all to send a broadcast to all online clients");
                        }
                        // then get the messages sent from other users and print it to the console.
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        closeEverything();
                        throw new ArithmeticException("Connection to the server was lost");
                    }
                }
            }
        }).start();
    }

    @Override
    public void closeEverything() {
        try {
            if (this.bufferedReader != null) {
                this.bufferedReader.close();
            }
            if (this.bufferedWriter != null) {
                this.bufferedWriter.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
        }
    }

// Run the program.
    public static void main(String[] args) {

        // Get a username for the user and a socket connection.
        //      Scanner scanner = new Scanner(System.in);
        ///      System.out.print("Enter your username for the group chat: ");
        //      String username = scanner.nextLine();
        // Create a socket to connect to the server.
        // Pass the socket and give the client a username.
        // Infinite loop to read and send messages.
        Client client = new Client();

        client.listenForMessage();
        client.sendMessage();
    }
}
