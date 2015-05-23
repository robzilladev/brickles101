import javax.swing.JFrame;
import javax.swing.JApplet;

/**
 * A starter class for a Director-pattern Swing application program.
 */

public class go extends JApplet{
    /**
     * Runs the program by creating a Director and passing it the
     * command-line arguments
     */
    public static void main(String args[]) {
        JFrame window = new JFrame();
        Director director = new Director(window);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
        window.setLocationRelativeTo(null);
    }   
    
    @Override
    public void init()
    {
        JFrame window = new JFrame();
        Director director = new Director(window);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
        window.setLocationRelativeTo(null);
    }
}




