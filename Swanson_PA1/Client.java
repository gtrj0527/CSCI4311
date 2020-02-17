/* 
 * @author Gabriela Swanson, 2542788
 * CSCI4311, Spr 2020
 * Due Date: 20200228
 * Programming Assignment #1
 *
 * Please note: I worked extensively with Sheldon Guillory on this project.
 *
 * References used:
 * *Baeldung's "A Guide to Java Sockets"
 * *Geeks for Geeks' "Introducing Threads in Socket Programming"
 * *Geeks for Geeks' "Multi-Threaded Chat Application in Java"
 * *Java8 API (various searches)
 * *Stack Overflow (various searches)
 *  
 */

 /*
  * This is the client-side implementation of the multi-threaded chat app.
  * The Client class:
  *     Establishes socket connectivity
  *     Actually communicates
  */

  import java.io.*;
  import java.net.*;
  import java.util.Scanner;

  public class Client{
    final static int ServerPort = 1775;
    
    public static void main(String args[]) throws UnknownHostException, IOException{
        Scanner scanner = new Scanner(System.in);

        //Obtain host's local IP
        InetAddress ip = InetAddress.getByName("localhost");

        //Connect
        Socket clientSocket = new Socket(ip, ServerPort);

        //Create in/output streams
        BufferedReader dis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //Reads from other clients
        PrintWriter dos = new PrintWriter(clientSocket.getOutputStream(), true);    // Sends to server & other clients

        //dos.println("TEST");

        //Set up a thread to send the message
        Thread sendMsg = new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    //Read the msg to deliver
                    String msg = scanner.nextLine();

                    try{
                        //Write the msg on an output stream
                        dos.println(msg);
                    } //End try
                    catch(Exception e){
                        System.out.println("Client error: " + e.getMessage() + "\n");
                    } //End catch
                } //End while loop
            } //End method run
        }); //End Thread for sending

        //Set up a thread to read the message
        Thread readMsg = new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    try{
                        //Read the message sent to the client
                        String msg = dis.readLine();
                        System.out.println(msg);
                    } //End try
                    catch(Exception e){
                        System.out.println("Client error: " + e.getMessage() + "\n");
                    } //End catch
                } //End while loop
            } //End method run
        }); //End Thread for reading

        // Receive and send the messages
        readMsg.start();
        sendMsg.start();

    } //End method main

  } //End class Client
