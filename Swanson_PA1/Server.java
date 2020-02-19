/* 
 * @author Gabriela Swanson, 2542788
 * CSCI4311, Spr 2020
 * Due Date: 20200228
 * Programming Assignment #1
 *
 * Please note: I worked extensively with Sheldon Guillory on this project.
 *
 * References used:
 * *Geeks for Geeks' "Introducing Threads in Socket Programming"
 * *Geeks for Geeks' "Multi-Threaded Chat Application in Java"
 * *Stack Overflow (various searches)
 * *Java8 API (various searches)
 * *Baeldung's "A Guide to Java Sockets"
 */

 /*
  * This is the server-side implementation of the multi-threaded chat app.
  * There are two classes: Server and ClientHandler
  */

import java.io.*;
import java.net.*;
import java.util.*;

//Define the Server class
public class Server{

//Create an ArrayList to maintain the clients currently using the app
public static ArrayList<ClientHandler> activeUser = new ArrayList<ClientHandler>();

//Keep count of the number of clients using the app
static int i = 0;

    //Set up the main method that will also throw exceptions as needed
    public static void main(String[] args) throws Exception{
        Server server = new Server(1775);        
    } //End method main

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
                // Need to change this out to accept a client's user name
                ClientHandler newClient = new ClientHandler(clientSocket, "Client " + i);

                // Starts this thread's run() method
                newClient.start();

                //Add the client to the active clients list
                activeUser.add(newClient);

                // Every time someone signs in, the entire list of users will print
                activeUser.forEach(activeUser ->{
                    System.out.println(activeUser.name);
                });

                //Increment i to handle next new client
                i++;
        } // End while loop
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
    
        //Overrides the start() method
        @Override
        public void run(){
            String incoming;
            try{
                dis = new BufferedReader(new InputStreamReader(clientHandlerSocket.getInputStream()));
                // dos.print("Enter your alphanumeric username: ");
                // name = dis.readLine();
                // dos.flush();
            }
            catch(Exception e){
                System.out.println("Server error: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
            
            try {
                dos.println("Enter your alphanumeric name: ");
                name = dis.readLine();
                if(!name.matches("^[a-zA-Z0-9]*$") || name.isEmpty()){
                    dos.println("Please enter an ALPHANUMERIC name: ");
                    name = dis.readLine();
                }
                //Shut the client out if they can't enter their name properly after two tries.
                if(!name.matches("^[a-zA-Z0-9]*$") || name.isEmpty()){
                    dos.println("You didn't enter an appropriate username. Your socket is being closed.");
                    activeUser.remove(this);
                    clientHandlerSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Server error: " + e.getMessage() + "\n");
                e.printStackTrace();
            }

            if(activeUser.contains(this)){
                for(ClientHandler nameList : activeUser){
                    nameList.dos.println("SERVER: Welcome, " + name);
                }
                System.out.println("SERVER: Welcome, " + name);
            }

            try{
                while(true){
                    //Receive the string
                    incoming = dis.readLine();

                    System.out.print("\n"+ name + ": " + incoming);
                    //Check whether the client wants to logout
                    if(incoming.toLowerCase().equals("allusers")){
                        dos.println("\nThe list of all users is:"); // EDIT MADE HERE; ADDED LN TO PRINT STATEMENT
                        activeUser.forEach(activeUser ->{
                            dos.println("\n" + activeUser.name);    // EDIT MADE HERE; ADDED LN TO PRINT STATEMENT
                        });
                        dos.println("\n \n");                       // EDIT MADE HERE; ADDED LN TO PRINT STATEMENT
                    }

                    else if(incoming.toLowerCase().equals("bye")){
                        dos.println("SERVER: Goodbye, " + this.name);         // EDIT MADE HERE; ADDED LN TO PRINT STATEMENT
                        this.loggedIn = false;
                        activeUser.remove(this);
                        this.clientHandlerSocket.close();
                        break;
                    } //End if statement

                    //Search for the recipient in the ArrayList.
                    for(ClientHandler msgCreator : activeUser){
                        //If recipient is there, write on their output stream
                        if(!msgCreator.name.equals(name) && msgCreator.loggedIn == true){
                            msgCreator.dos.println((this.name + ": " + incoming));
                        } //End if statement
                    } //End for statement
                } //End while(true) loop
            } //End try 
            catch (Exception e){
                System.out.println("Server error: " + e.getMessage() + "\n");
                // e.printStackTrace();
            } //End catch
            
            
            try{
                this.dis.close();
                this.dos.close();
            } //End try
            catch (Exception e){
                dos.print("Server error: " + e.getMessage() + "\n");
                e.printStackTrace();
            } //End catch
        } //End method run        
    } //End class ClientHandler
} //End class Server
