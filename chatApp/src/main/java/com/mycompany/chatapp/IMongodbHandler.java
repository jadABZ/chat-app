package com.mycompany.chatapp;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.HashMap;

public interface IMongodbHandler {
//this interface is responsible for handling connection to our mongodb collection

    HashMap<String, String> messageProperties = new HashMap<>();

    MongoClient client = MongoClients.create("mongodb+srv://mongobjava:mongobjava1234@cluster0.qrqrx.mongodb.net/?retryWrites=true&w=majority");
    MongoDatabase db = client.getDatabase("chatAppDB");
    MongoCollection col = db.getCollection("chatAppCollection");

    default void uploadToMongodb() {
        Document messageDoc = new Document("_id", (int)(Math.random() * 10000));
        messageDoc.append("recClientID", messageProperties.get("recClientID"));
        messageDoc.append("sender", messageProperties.get("sender"));
        messageDoc.append("txtMessage", messageProperties.get("txtMessage"));
        messageDoc.append("time", messageProperties.get("time"));

        col.insertOne(messageDoc);
    }
}
