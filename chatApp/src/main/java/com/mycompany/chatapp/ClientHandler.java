package com.mycompany.chatapp;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class ClientHandler extends Thread implements ICloseable, IMongodbHandler { //inner class 

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Integer clientID;
    private String clientIP;

    protected static ArrayList<ClientHandler> clientsConnected = new ArrayList<>();

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientID = Server.newClientID;
            this.clientIP = socket.getRemoteSocketAddress().toString();
        } catch (IOException e) {
            closeEverything();
        }
    }

    @Override
    public void run() {

        while (!socket.isClosed()) {

            System.out.println("Connection of a client '" + this.clientID + "', IP: " + this.clientIP);

            try {
                int[] s = {this.clientID};
                sendResponse(clientID.toString(), s);
                sendResponse("SERVER: Client " + this.clientID + " has joined the chat!", 0);

            } catch (IOException e) {
                closeEverything();
            }

            String req;
            try {
                while ((req = bufferedReader.readLine()) != null) {

                    String[] tmp = req.split("@");
                    String txtMessage = tmp[0];
                    String[] recIds = tmp[1].split(",");

                    if ("all".equals(recIds[0])) { // if the message is a broadcast
                        sendResponse(txtMessage + "(broadcast)", this.clientID);

                    } else { // if the message is destined to specific clients (1 or more)
                        int[] recipientIDs = new int[recIds.length];
                        for (int i = 0; i < recIds.length; i++) {
                            recipientIDs[i] = Integer.parseInt(recIds[i]);
                        }
                        sendResponse(txtMessage, recipientIDs);
                    }
                        //upload to mongodb here
                        Message message = new Message(recIds, txtMessage);
                        // from interface IJsonable:
                        messageProperties.put("recClientID", message.getRecClientID());
                        messageProperties.put("sender", message.getSenderClientID());
                        messageProperties.put("txtMessage", message.getTxtMessage());
                        messageProperties.put("time", message.getTime());
                        uploadToMongodb();
                        System.out.println("message was sent: " + messageProperties);

                }

            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public int getClientID() {
        return this.clientID;
    }

//without recipientIDs parameter we send response to all clients connected (broadcast)
    public void sendResponse(String message, int senderID) throws IOException {
       //senderID is passed to make sure not to send broadcast to the sender
        //senderID = 0 means server is broadcasting to all clients
        for (ClientHandler c : clientsConnected) {

            try {
                if (c.clientID != senderID) {
                    c.bufferedWriter.write(message);
                    c.bufferedWriter.newLine();
                    c.bufferedWriter.flush();
                }

            } catch (IOException e) {
                closeEverything();
            }
        }
    }

//overloading sendResponse when we want to send a message to specific clients
    public void sendResponse(String message, int[] recipientIDs) throws IOException {
        boolean connectionExists;

        for (ClientHandler c : clientsConnected) {
            connectionExists = false;
            for (int i = 0; i < recipientIDs.length; i++) {
                if (c.getClientID()==recipientIDs[i]) {
                    connectionExists = true;
                    break;
                }
            }

            if (connectionExists) {
                try {
                    c.bufferedWriter.write(message);
                    c.bufferedWriter.newLine();
                    c.bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything();
                }
            }

        }
    }

    @Override
    public void closeEverything() {
        removeClientHandler();
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
        } catch (IOException e) { }
    }

    public void removeClientHandler() {
        try {
            clientsConnected.remove(this);
            System.out.println("Disconnection of a client '" + this.clientID + "', IP: " + this.clientIP);
            sendResponse("SERVER: Client " + this.clientID + " has left the chat!", 0);
        } catch (IOException e) {e.printStackTrace();}
    }
}
