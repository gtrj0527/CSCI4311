/* 
 * @author	Gabriela Swanson, 2542788
 * CSCI4311, Spr 2020
 * Due Date: 20200331
 * Programming Assignment #2
 * 
 * References/resources used:
 * B. Jaeger, Tester
 * Baeldung's "A Guide to Java Sockets"
 * Geeks for Geeks' "Multi-Threaded Chat Application in Java"
 * Java APIs (various searches)
 * StackOverflow (various searches)
 * YouTube tutorials (various searches)
 *  
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/* Class TicTacToe contains sub-class Painter
 * Together, they:
 * 	Establish the server-client connection
 * 	Create the board
 * 	Allow game play and win/loss messaging
 */
public class TicTacToe implements Runnable {
	// Variables for the connection
	private String ip = "localhost";
	private int port = 1775;
	private ServerSocket serverSocket;
	private Socket socket;

	// Variables for writing to screen and receiving inputs
	private Scanner scanner = new Scanner(System.in);
	private DataOutputStream dos;
	private DataInputStream dis;

	private Thread thread;

	// Variables for the board and "pieces"
	private BufferedImage board;
	private BufferedImage redX;
	private BufferedImage blueX;
	private BufferedImage redCircle;
	private BufferedImage blueCircle;
	private final int WIDTH = 506;
	private final int HEIGHT = 527;
	private JFrame frame;
	private Painter painter;
	private String[] spaces = new String[9];	// 9 = total no. spaces on the board
	private int lengthOfSpace = 160;
	private int errors = 0;

	// Variables for the beginning and end of the "win" line
	private int firstSpot = -1;
	private int secondSpot = -1;

	// Variables to keep the turns sorted
	private boolean yourTurn = false;
	private boolean circle = true;

	// Variables for connection with opponent
	private boolean accepted = false;
	private boolean unableToCommunicateWithOpponent = false;

	// Variables for results
	private boolean won = false;
	private boolean enemyWon = false;
	private boolean tie = false;

	// Set up the font
	private Font font = new Font("Georgia", Font.BOLD, 32);
	private Font smallerFont = new Font("Georgia", Font.BOLD, 20);
	private Font largerFont = new Font("Georgia", Font.BOLD, 50);

	// Strings to communicate with the players
	private String waitingString = "Waiting for another player";
	private String unableToCommunicateWithOpponentString = "Can't communicate with opponent.";
	private String wonString = "You won!";
	private String enemyWonString = "Opponent won!";
	private String tieString = "Nobody won.";

	/**
	  * Variable for the possible ways to win.
	  * Board "looks" like this.
	  * 0, 1, 2 
	  * 3, 4, 5 
  	  * 6, 7, 8
	  */
	private int[][] wins = new int[][] { { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, // vertical wins
										 { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, // horizontal wins
										 { 0, 4, 8 }, { 2, 4, 6 } };			// diagonal wins

	//  Constructor
	public TicTacToe() {
		System.out.println("Please input the IP: ");
		ip = scanner.nextLine();
		System.out.println("Please input the port: ");
		port = scanner.nextInt();
		while (port < 1 || port > 65535) {
			System.out.println("The port you entered was invalid; please input a valid one: ");
			port = scanner.nextInt();
		}

		loadImages();

		painter = new Painter();
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		if (!connect()) initializeServer();

		frame = new JFrame();
		frame.setTitle("Tic-Tac-Toe");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		// Threads allow multi-players over the TCP connection
		thread = new Thread(this, "TicTacToe");
		thread.start();
	}

	// Method to actually run the game
	public void run() {
		while (true) {
			tick();
			painter.repaint();

			// Set the server up to listen for clients
			if (!circle && !accepted) {
				listenForServerRequest();
			}
		}
	}

	// Set up the graphics for the game
	private void render(Graphics g) {
		g.drawImage(board, 0, 0, null);
		// What happens if players can't communicate
		if (unableToCommunicateWithOpponent) {
			g.setColor(Color.RED);
			g.setFont(smallerFont);
			Graphics2D g2 = (Graphics2D) g;
			// Removes text pixelation by smoothing it out
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// Tell how long the string is in the font used so we can...
			int stringWidth = g2.getFontMetrics().stringWidth(unableToCommunicateWithOpponentString);
			// Perfectly center the string
			g.drawString(unableToCommunicateWithOpponentString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			return;
		}

		// What happens when players *can* communicate
		if (accepted) {
			for (int i = 0; i < spaces.length; i++) {
				if (spaces[i] != null) {
					// Set up the players' symbols on the board grid
					if (spaces[i].equals("X")) {
						if (circle) {
							// Render the symbol in the correct box with the correct symbols
							// 10 is the width of the vertical and horizontal grids 
							// Cast to an integer and set the result of the % to the floor value
							// This places the symbol in the correct position within the correct box
							g.drawImage(redX, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(blueX, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						}
					} else if (spaces[i].equals("O")) {
						if (circle) {
							g.drawImage(blueCircle, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(redCircle, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						}
					}
				}
			}
			// When someone gets three symbols in a row, draw a line connecting the three boxes
			if (won || enemyWon) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(10));		// Width of the "win" line
				g.setColor(Color.BLACK);
				// Center the line within the three boxes it crosses
				g.drawLine(firstSpot % 3 * lengthOfSpace + 10 * firstSpot % 3 + lengthOfSpace / 2, (int) (firstSpot / 3) * lengthOfSpace + 10 * (int) (firstSpot / 3) + lengthOfSpace / 2, secondSpot % 3 * lengthOfSpace + 10 * secondSpot % 3 + lengthOfSpace / 2, (int) (secondSpot / 3) * lengthOfSpace + 10 * (int) (secondSpot / 3) + lengthOfSpace / 2);
				
				// Set up font for result strings "You won" or "Opponent won"
				// Position the string so it looks good on the board 
				g.setColor(Color.DARK_GRAY);
				g.setFont(largerFont);
				if (won) {
					int stringWidth = g2.getFontMetrics().stringWidth(wonString);
					g.drawString(wonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				} else if (enemyWon) {
					int stringWidth = g2.getFontMetrics().stringWidth(enemyWonString);
					g.drawString(enemyWonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				}
			}
			// If there's a tie, send both players a message
			else if (tie) {
				Graphics2D g2 = (Graphics2D) g;
				g.setColor(Color.DARK_GRAY);
				g.setFont(largerFont);
				int stringWidth = g2.getFontMetrics().stringWidth(tieString);
				g.drawString(tieString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			}
		} 
		// If another player hasn't joined the game, print the wait string
		else {
			g.setColor(Color.RED);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(waitingString);
			g.drawString(waitingString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}
	}

	// Handles adding the tick marks to the board
	private void tick() {
		if (errors >= 10){
			unableToCommunicateWithOpponent = true;
		}
		// Set up the turns
		if (!yourTurn && !unableToCommunicateWithOpponent) {
			try {
				int space = dis.readInt();
				if (circle){
					spaces[space] = "X";
				} 
				else{
					spaces[space] = "O";
				} 
				checkForEnemyWin();
				checkForTie();
				yourTurn = true;
			} catch (IOException e) {
				e.printStackTrace();
				errors++;
			}
		}
	}

	// Check if there are three ticks in the defined win sequences
	// If so, set the variables for the beginning and end of the win line
	private boolean checkForWin() {
		for (int i = 0; i < wins.length; i++) {
			if (circle) {
				if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O") {
					firstSpot = wins[i][0];
					secondSpot = wins[i][2];
					won = true;
				}
			} else {
				if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X") {
					firstSpot = wins[i][0];
					secondSpot = wins[i][2];
					won = true;
				}
			}
		}
		return won;
	}

	// Check if there are three ticks in the win sequences for the opponent
	private void checkForEnemyWin() {
		for (int i = 0; i < wins.length; i++) {
			if (circle) {
				if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X") {
					firstSpot = wins[i][0];
					secondSpot = wins[i][2];
					enemyWon = true;
				}
			} else {
				if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O") {
					firstSpot = wins[i][0];
					secondSpot = wins[i][2];
					enemyWon = true;
				}
			}
		}
	}

	// A tie is when all spots are filled && there are *not* three of the same ticks in a row
	private void checkForTie() {
		for (int i = 0; i < spaces.length; i++) {
			if (spaces[i] == null) {
				return;
			}
		}
		tie = true;
	}

	// Server is listening for new client requests
	private void listenForServerRequest() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
			System.out.println("CLIENT REQUESTED TO JOIN; SERVER ACCEPTED");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Connect the server and client
	private boolean connect() {
		try {
			socket = new Socket(ip, port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
		} catch (IOException e) {
			System.out.println("Unable to connect to the address: " + ip + ":" + port + " | Starting a server");
			return false;
		}
		System.out.println("Successfully connected to the server.");
		return true;
	}

	// Set server up
	private void initializeServer() {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		} catch (Exception e) {
			e.printStackTrace();
		}
		yourTurn = true;
		circle = false;
	}

	// Load images for the game
	private void loadImages() {
		try {
			board = ImageIO.read(getClass().getResourceAsStream("/board.png"));
			redX = ImageIO.read(getClass().getResourceAsStream("/redX.png"));
			redCircle = ImageIO.read(getClass().getResourceAsStream("/redCircle.png"));
			blueX = ImageIO.read(getClass().getResourceAsStream("/blueX.png"));
			blueCircle = ImageIO.read(getClass().getResourceAsStream("/blueCircle.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Run the game
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		TicTacToe ticTacToe = new TicTacToe();
	}

	// Separate class that allows players to click on the board for their moves
	private class Painter extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;

		// Constructor
		public Painter() {
			setFocusable(true);				// Receive key events
			requestFocus();					// Get the input
			setBackground(Color.WHITE);
			addMouseListener(this);			// Allows game play via mouse click instead of keyboard entry
		}

		// Render the graphics
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}

		// Set up mouse click events
		@Override
		public void mouseClicked(MouseEvent e) {
			if (accepted) {
				if (yourTurn && !unableToCommunicateWithOpponent && !won && !enemyWon) {
					int x = e.getX() / lengthOfSpace;
					int y = e.getY() / lengthOfSpace;
					y *= 3;
					int position = x + y;

					if (spaces[position] == null) {
						if (!circle) spaces[position] = "X";
						else spaces[position] = "O";
						yourTurn = false;
						repaint();
						Toolkit.getDefaultToolkit().sync();

						try {
							dos.writeInt(position);
							dos.flush();
						} catch (IOException e1) {
							errors++;
							e1.printStackTrace();
						}

						System.out.println("DATA WAS SENT");
						checkForWin();
						checkForTie();

					}
				}
			}
		}

		// Mandatory overrides for MouseListener
		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
	}
}