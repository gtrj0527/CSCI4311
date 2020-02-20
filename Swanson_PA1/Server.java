/* 
 * @author Gabriela Swanson, 2542788
 * CSCI4311, Spr 2020
 * Due Date: 20200228
 * Programming Assignment #1
 *
 *
 * References used:
 * *Baeldung's "A Guide to Java Sockets"
 * *Geeks for Geeks' "Introducing Threads in Socket Programming"
 * *Geeks for Geeks' "Multi-Threaded Chat Application in Java"
 * *Java8 API (various searches)
 * *Sheldon Guillory, classmate
 * *Stack Overflow (various searches)
 * 
 */

 /*
  * This is the server-side implementation of the multi-threaded chat app.
  * There are two classes: Server and ClientHandler
  */

import java.io.*;
import java.net.*;
import java.util.*;

// Define the Server class
public class Server{

// Create an ArrayList to maintain the clients currently using the app
public static ArrayList<ClientHandler> activeUser = new ArrayList<ClientHandler>();

// Keep count of the number of clients using the app
static int i = 1;

    //Set up the main method that will also throw exceptions as needed
    public static void main(String[] args) throws Exception{
        Server server = new Server(1775);        
    } // End method main

    // Constructor class to define what the server does
    public Server(int port) throws Exception{
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started.");
        System.out.println("Waiting for client.");
        
        //Set up a socket that will be used for input later
        Socket clientSocket;

        //Set up a while loop that will wait for client requests 
            while(true){
                clientSocket = serverSocket.accept();
                System.out.println("\n\nClient accepted.");
                
                //Create a ClientHandler object to handle the request
                ClientHandler newClient = new ClientHandler(clientSocket, "Client " + i);

                // Starts this thread's run() method
                newClient.start();

                //Add the client to the active clients list
                activeUser.add(newClient);

                // Every time someone signs in, the entire list of users will print on the server
                activeUser.forEach(activeUser ->{
                    System.out.println(activeUser.name);
                });

                //Increment i to handle next new client
                i++;
        } // End while loop
    } // End constructor class Server

    class ClientHandler extends Thread{
        Socket clientHandlerSocket;    
        String name;
        boolean loggedIn;
        private BufferedReader br;
        private PrintWriter pw;
    
        // Constructor for the Client Handler
        public ClientHandler(Socket clientHandlerSocket, String name) throws Exception{
            this.clientHandlerSocket = clientHandlerSocket;
            this.name = name;
            this.loggedIn = true;
            this.br = new BufferedReader(new InputStreamReader(clientHandlerSocket.getInputStream()));
            this.pw = new PrintWriter(clientHandlerSocket.getOutputStream(),true);
        } // Close constructor method
    
        // Overrides the start() method
        @Override
        public void run(){
            String incoming;
            
            // A series of try-catch blocks to ensure incoming strings behave in an expected way
            // Try-catch: Is the username alphanumeric? If not, log them out after the second try.
            try {
                pw.println("Enter your alphanumeric name: ");
                name = br.readLine();
                if(!name.matches("^[a-zA-Z0-9]*$") || name.isEmpty()){
                    pw.println("Please enter an ALPHANUMERIC name: ");
                    name = br.readLine();
                } // End if statement
                if(!name.matches("^[a-zA-Z0-9]*$") || name.isEmpty()){
                    pw.println("You didn't enter an appropriate username. Your socket is being closed.");
                    activeUser.remove(this);
                    clientHandlerSocket.close();
                } // End if statement
            } // End try 
            catch (Exception e) {
                System.out.println("Server error: " + e.getMessage() + "\n");
                e.printStackTrace();
            } // End catch

            // If the username was satisfactory, print a welcome message
            if(activeUser.contains(this)){
                for(ClientHandler nameList : activeUser){
                    nameList.pw.println("SERVER: Welcome, " + name);
                } // End for statement
                System.out.println("SERVER: Welcome, " + name);
            } // End if statement

            // Try-catch: Print the clients' incoming messages
            // If incoming message is "allusers", print a list of all users currently active
            // If incoming messages is "bye", disconnect the user and remove them from the ArrayList
            try{
                while(true){
                    
                    // Receive the string and print it to the server console
                    incoming = br.readLine();
                    System.out.print("\n"+ name + ": " + incoming);
                    
                    // Print the list of all users
                    if(incoming.toLowerCase().equals("allusers")){
                        pw.println("\nThe list of all users is:"); 
                        activeUser.forEach(activeUser ->{
                            pw.println("\n" + activeUser.name);    
                        });
                        pw.println("\n");                       
                    } // End if statement

                    // Disconnect the user from the server and remove them from the ArrayList
                    else if(incoming.toLowerCase().equals("bye")){
                        pw.println("SERVER: Goodbye, " + this.name);         
                        this.loggedIn = false;
                        activeUser.remove(this);
                        this.clientHandlerSocket.close();
                        break;
                    } // End if statement

                    // Print the client messages to each other's consoles
                    for(ClientHandler msgCreator : activeUser){
                        // If recipient is there, write on their output stream
                        if(!msgCreator.name.equals(name) && msgCreator.loggedIn == true){
                            msgCreator.pw.println((this.name + ": " + incoming));
                        } // End if statement
                    } // End for statement
                } // End while(true) loop
            } // End try 
            catch (Exception e){
                System.out.println("Server error: " + e.getMessage() + "\n");
                e.printStackTrace();
            } // End catch
            
            try{
                this.br.close();
                this.pw.close();
            } // End try
            catch (Exception e){
                pw.print("Server error: " + e.getMessage() + "\n");
                e.printStackTrace();
            } // End catch
        } // End method run        
    } // End sub-class ClientHandler
} // End class Server
