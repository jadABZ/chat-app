package com.mycompany.chatapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private final String[] recClientID;
    private final String sender;
    private final String txtMessage;
    private final String time;

    Message(String[] recClientID, String req) {
        this.recClientID = recClientID;
         //req = "Client 1: hi" so we split on : this message to fill the sender id and the txt message
        String tmp[] = req.split(": ");
        this.sender = tmp[0];
        this.txtMessage = tmp[1];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        this.time = dtf.format(now);
    }


    public String getRecClientID() {
        String recClientIdInOne = "";
        for (String s : this.recClientID) {

            if (s.equals(this.recClientID[this.recClientID.length - 1])) {
                recClientIdInOne += s;

            } else {
                recClientIdInOne += s + " - ";
            }
        }
        return recClientIdInOne;
    }

    public String getSenderClientID() {
        return this.sender;
    }

    public String getTxtMessage() {
        return this.txtMessage;
    }

    public String getTime() {
        return this.time;
    }
}
