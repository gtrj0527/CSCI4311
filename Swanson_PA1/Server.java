/* 
 * @author Gabriela Swanson, 2542788
 * CSCI4311, Spr 2020
 * Due Date: 20200228
 * Programming Assignment #1
 *
 * Please note: I talked with Sheldon Guillory extensively about this project
 *
 * References used:
 * *Geeks for Geeks' "Introducting Threads in Socket Programming"
 * *Geeks for Geeks' "Multi-Threaded Chat Application in Java"
 * *Stack Overflow (various searches)
 * *Java8 API (various searches)
 * *Baeldung's "A Guide to Java Sockets"
 */

 /*
  * This is the server-side implementation of the multi-threaded chat app.
  * There are two classes: Server and ClientHandler
  * The Server class establishes the connection between the server and the client(s).
  */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Math;


//Define the Server class
public class Server{

//Create an ArrayList to maintain the clients currently using the app
public static ArrayList<ClientHandler> activeUser = new ArrayList<ClientHandler>();

//Keep count of the number of clients using the app
static int i = 0;

    //Set up the main method that will also throw exceptions as needed
    public static void main(String[] args) throws Exception{
        Server server = new Server();        
    } //End method main

    public Server() throws Exception{
        ServerSocket serverSocket = new ServerSocket(1775);
        //Set up a socket that will be used for input later
        Socket clientSocket;
        //Set up a while loop that will wait for client requests
            while(true){
                clientSocket = serverSocket.accept();
                
                //Create a ClientHandler object to handle the request
                // Need to change this out to accept a client's user name
                ClientHandler newClient = new ClientHandler(clientSocket, " client " + Math.random());
                
                // Starts this thread's run() method
                newClient.start();
                
                //Add the client to the active clients list
                activeUser.add(newClient);
                /* // Test code to make sure the ArrayList was working
                activeUser.forEach(activeUser ->{
                    System.out.println(activeUser.name);
                });
                 */

                //Increment i to handle next new client
                i++;
            } // End while loop
        // serverSocket.close();
    } //end constructor class Server

    class ClientHandler extends Thread{
        Socket clientHandlerSocket;    
        String name;
        boolean loggedIn;
        private BufferedReader dis;
        private PrintWriter dos;
    
        //Constructor for the Client Handler
        public ClientHandler(Socket clientHandlerSocket, String name) throws Exception{
            this.clientHandlerSocket = clientHandlerSocket;
            this.name = name;
            this.loggedIn = true;
            this.dis = dis;
            this.dos = new PrintWriter(clientHandlerSocket.getOutputStream(),true);
        } //Close constructor method
    
        //Handle the exceptions that may occur when main method is run
        @Override
        public void run(){
            String incoming;
            try{
                dis = new BufferedReader(new InputStreamReader(clientHandlerSocket.getInputStream()));
            }
            catch(Exception e){
                e.printStackTrace();
            }
            while(true){
                try{
                    //Receive the string
                    incoming = dis.readLine();
                    System.out.println(incoming);
                    //Check whether the client wants to logout
                    if(incoming.equals("logout")){
                        this.loggedIn = false;
                        this.clientHandlerSocket.close();
                        System.out.println("You are now logged out.");
                        break;
                    } //End if statement
    
                    //Search for the recipient in the ArrayList.
                    for( ClientHandler msgCreator : activeUser){
                        //If recipient is there, write on their output stream
                        if(!msgCreator.name.equals(name) && msgCreator.loggedIn==true){
                            msgCreator.dos.println((this.name + ": " + incoming));
                        } //End if statement
                    } //End for statement
    
                } //End try
                catch ( IOException e){
                    e.printStackTrace();
                } //End catch
            } //End while(true) loop
            try{
                this.dis.close();
                this.dos.close();
            } //End try
            catch ( IOException e){
                e.printStackTrace();
            } //End catch
        } //End method run        
    } //End class ClientHandler
} //End class Server