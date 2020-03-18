import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.util.Random;

public class Gameplay extends JPanel implements KeyListener, ActionListener{
    // Variables for maintaining the length of the snake as it moves and gets longer.
    // Logic will be: Shift previous length into next array. 
    private int[] snakeXLength = new int[750];
    private int[] snakeYLength = new int[750];

    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;

    private ImageIcon leftMouth;
    private ImageIcon rightMouth;
    private ImageIcon upMouth;
    private ImageIcon downMouth;
    private ImageIcon snakeImage;

    // arrays to hold the "enemy" position on the board
    // keeps going out of bounds in the upward position
    private int[] enemyXPosition = { 75, 100, 125, 150, 175, 200, 
                                    225, 250, 275, 300, 325, 350, 
                                    375, 400, 425, 450, 475, 500, 
                                    525, 550, 575, 600, 625, 650, 
                                    675, 700, 725, 750, 775, 800};
    private int[] enemyYPosition = { 75, 100, 125, 150, 175, 200, 
                                    225, 250, 275, 300, 325, 350, 
                                    375, 400, 425, 450, 475, 500, 
                                    525, 550}; 
    private ImageIcon enemyImage;
    private Random random = new Random();      
    private int xPos = random.nextInt(30); // total number of x positions
    private int yPos = random.nextInt(20); // total number of y positions         
    
    private int score = 0;

    private int moves = 0; 
    private int lengthOfSnake = 3;

    private Timer timer;
    private int delay = 100;

    
    private ImageIcon titleImage;
    
    // Constructor
    public Gameplay(){
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    // Paints the snake
    public void paint (Graphics g){
        
        // Sets initial snake position and length
        if(moves == 0){
            snakeXLength[2] = 50;
            snakeXLength[1] = 75;
            snakeXLength[0] = 100;

            snakeYLength[2] = 100;
            snakeYLength[1] = 100;
            snakeYLength[0] = 100;
        }
        
        // Draw title image border 
        g.setColor(Color.white);
        g.drawRect(24, 10, 851, 55);

        // Draw the title image
        titleImage = new ImageIcon("snaketitle.jpg");
        titleImage.paintIcon(this, g, 25, 11);

        // Draw playing area border
        g.setColor(Color.WHITE);
        g.drawRect(24, 74, 851, 600);

        // Draw game background
        g.setColor(Color.black);
        g.fillRect(25, 75, 850, 598);

        // Score board: show the score
        g.setColor(Color.white);
        g.setFont(new Font("georgia", Font.PLAIN, 14));
        g.drawString("Score: " + score, 750, 30);

        // Score board: show the snake length
        g.setColor(Color.white);
        g.setFont(new Font("georgia", Font.PLAIN, 14));
        g.drawString("Snake Length: " + lengthOfSnake, 750, 50);

        // Draw the snake
        rightMouth = new ImageIcon("rightmouth.png");
        rightMouth.paintIcon(this, g, snakeXLength[0], snakeYLength[0]);

        // Set the snake head direction
        for(int a = 0; a < lengthOfSnake; a++){
            if(a == 0 && right){
                rightMouth = new ImageIcon("rightmouth.png");
                rightMouth.paintIcon(this, g, snakeXLength[a], snakeYLength[a]);
            }
            if(a == 0 && left){
                leftMouth = new ImageIcon("leftmouth.png");
                leftMouth.paintIcon(this, g, snakeXLength[a], snakeYLength[a]);
            }
            if(a == 0 && down){
                downMouth = new ImageIcon("downmouth.png");
                downMouth.paintIcon(this, g, snakeXLength[a], snakeYLength[a]);
            }
            if(a == 0 && up){
                upMouth = new ImageIcon("upmouth.png");
                upMouth.paintIcon(this, g, snakeXLength[a], snakeYLength[a]);
            }
            if(a != 0){
                snakeImage = new ImageIcon("snakeimage.png");
                snakeImage.paintIcon(this, g, snakeXLength[a], snakeYLength[a]);
            }
        }

        // Check if snake head is colliding with the "enemy" (the ball); if so, increase snake length
        enemyImage = new ImageIcon("enemy.png");
        
        if((enemyXPosition[xPos] == snakeXLength[0]) && (enemyYPosition[yPos] == snakeYLength[0])){
            score++;
            lengthOfSnake++;
            xPos = random.nextInt(30);
            yPos = random.nextInt(20);
        }

        enemyImage.paintIcon(this, g, enemyXPosition[xPos], enemyYPosition[yPos]);

        // If the snake touches itself, the game is over
        for(int b = 1; b < lengthOfSnake; b++){
            if(snakeXLength [b] == snakeXLength[0] && snakeYLength[b] == snakeYLength[0]){
                right = false;
                left = false;
                up = false;
                down = false;

                g.setColor(Color.white);
                g.setFont(new Font("georgia", Font.BOLD, 50));
                g.drawString("GAME OVER", 300, 300);

                g.setFont(new Font("georgia", Font.BOLD, 20));
                g.drawString("Press spacebar to RESTART", 325, 340);
            }
        }
        // Disposes graphics context and releases system resources
        g.dispose();
    }

    // This method handles the snake activity and increases the length of the snake
    // depending on the direction the snake is moving
    @Override
    public void actionPerformed(ActionEvent e){
        timer.start();
        if(right){
            for (int r = lengthOfSnake - 1; r >= 0; r--){
                snakeYLength[r+1] = snakeYLength[r];
            }
            for(int r = lengthOfSnake; r >= 0; r--){
                if(r == 0) {
                    snakeXLength[r] = snakeXLength[r] + 25;
                }
                else{
                    snakeXLength[r] = snakeXLength[r - 1];
                }
                // Check the position of the head; if it runs out of room, then it'll
                // loop around to come out on the opposite side.
                if(snakeXLength[r] > 850){
                    snakeXLength[r] = 25;
                }
            }
            repaint();
        }
        if(left){
            for (int r = lengthOfSnake - 1; r >= 0; r--){
                snakeYLength[r+1] = snakeYLength[r];
            }
            for(int r = lengthOfSnake; r >= 0; r--){
                if(r == 0) {
                    snakeXLength[r] = snakeXLength[r] - 25;
                }
                else{
                    snakeXLength[r] = snakeXLength[r - 1];
                }
                // Check the position of the head; if it runs out of room, then it'll
                // loop around to come out on the opposite side.
                if(snakeXLength[r] < 25){
                    snakeXLength[r] = 850;
                }
            }
            repaint();
        }
        if(up){
            for (int r = lengthOfSnake - 1; r >= 0; r--){
                snakeXLength[r+1] = snakeXLength[r];
            }
            for(int r = lengthOfSnake; r >= 0; r--){
                if(r == 0) {
                    snakeYLength[r] = snakeYLength[r] - 25;
                }
                else{
                    snakeYLength[r] = snakeYLength[r - 1];
                }
                // Check the position of the head; if it runs out of room, then it'll
                // loop around to come out on the opposite side.
                if(snakeYLength[r] < 75){
                    snakeYLength[r] = 625;
                }
            }
            repaint(); 
        }
        if(down){
            for (int r = lengthOfSnake - 1; r >= 0; r--){
                snakeXLength[r+1] = snakeXLength[r];
            }
            for(int r = lengthOfSnake; r >= 0; r--){
                if(r == 0) {
                    snakeYLength[r] = snakeYLength[r] + 25;
                }
                else{
                    snakeYLength[r] = snakeYLength[r - 1];
                }
                // Check the position of the head; if it runs out of room, then it'll
                // loop around to come out on the opposite side.
                if(snakeYLength[r] > 625){
                    snakeYLength[r] = 75;
                }
            }
            repaint();
        }
    }

    // This override MUST be here because of the KeyListener class.
    @Override
    public void keyTyped(KeyEvent e){}
    // If player wants to quit game, they can hit "Escape"
    // If player wants to pause game, hit "Shift"
    /*  // Couldn't get this to work
    @Override
    public void keyTyped(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }
        if(e.getKeyCode() == KeyEvent.VK_SHIFT){
            left = false;
            right = false;
            up = false; 
            down = false;
        }
    }
    */

    // Method that defines what happens when keys are pressed
    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_SPACE){
            moves = 0;
            score = 0;
            lengthOfSnake = 3;
            repaint();
        }
        if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            moves++;
            right = true;
            if(!left){
                right = true;
            }
            else{
                right = false;
                left = true;
            }
            up = false;
            down = false;
        }
        if(e.getKeyCode() == KeyEvent.VK_LEFT){
            moves++;
            left = true;
            if(!right){
                left = true;
            }
            else{
                left = false;
                right = true;
            }
            up = false;
            down = false;
        }
        if(e.getKeyCode() == KeyEvent.VK_UP){
            moves++;
            up = true;
            if(!down){
                up = true;
            }
            else{
                up = false;
                down = true;
            }
            left = false;
            right = false;
        }
        if(e.getKeyCode() == KeyEvent.VK_DOWN){
            moves++;
            down = true;
            if(!up){
                down = true;
            }
            else{
                down = false;
                up = true;
            }
            left = false;
            right = false;
        }
        
    }

    /*
    // Turned this on for easier, faster testing
    // When player releases up, down, right, left, then snake stops moving
    @Override
    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_LEFT){
            left = false;
        }
        if(key == KeyEvent.VK_RIGHT){
            right = false;
        }
        if(key == KeyEvent.VK_UP){
            up = false;
        }
        if(key == KeyEvent.VK_DOWN){
            down = false;
        }
    }
    */
    // This override MUST be here because of the KeyListener class.
    @Override
    public void keyReleased(KeyEvent e){}
}
