package com.mycompany.chatapp;

interface ICloseable {

    void closeEverything(); //Server, ClientHandler and Client will have to implement this function
                            //to close their socket, buffered reader and writer
}
