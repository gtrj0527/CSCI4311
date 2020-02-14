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
        BufferedReader dis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter dos = new PrintWriter(clientSocket.getOutputStream(), true);

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
                        e.printStackTrace();
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
                        e.printStackTrace();
                    } //End catch
                } //End while loop
            } //End method run
        }); //End Thread for reading
        readMsg.start();
        sendMsg.start();

    } //End method main
    
  } //End class Client