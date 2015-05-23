import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JApplet;

// Director class for Brickles.
// Requires BrickWall, CollisionListener and CollisionEvent classes which
// are found in the Brickles library.

public class Director extends JPanel implements CollisionListener {

    private BrickWall wall;
    
    // Swing variables
    private JFrame frame, hsFrame;
    private JPanel gamePanel, hsPanel, slPanel;
    private UpperPanel upperPanel;
    private ControlPanel controlPanel;
    private JMenuBar menuBar;
    private JFrame pFrame;
    private JRadioButton pb1,pb2,pb3;
    private JCheckBoxMenuItem darkMenuItem, lightMenuItem;
    
    // Game score and save variables
    private Scorer scorer;
    private GameSaver saver;
    private int[][] savedGame;
    
    // Brick colour variables
    private ArrayList<Color> colors = new ArrayList<>();
    private Random random = new Random();
        
    // Gameplay variables
    private int lives, difficulty, score, rows, timesRound;
    private int livesQ;
    private int multiply1, gameType;

    // Constructor:
    // Sets up the main frame, high scores and preferences frame.
    // Sets up the game play variables based on user selection.
    public Director(JFrame window) {
        // User must specify the number of lives they wish to play with and 
        // the number of rows they wish to populate the board with (initially).
        showStartPanel(false);
        buildPreferences();
        //showPreferences();
        
        gameType = 1;
        difficulty = 1;
        score = 0;
        timesRound = 0;
        frame = window;
        
        frame.setTitle("Brickles");
        
        colors.add(Color.red); colors.add(Color.orange); colors.add(Color.blue); colors.add(Color.magenta);
        colors.add(Color.cyan);colors.add(Color.green); ;colors.add(Color.yellow);
        
        gamePanel = new JPanel();
        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(makeGamePanel());
        
        upperPanel = new UpperPanel();
        controlPanel = new ControlPanel(upperPanel);
        menuBar = new JMenuBar();
        
        frame.add(upperPanel,BorderLayout.NORTH);
        frame.add(controlPanel,BorderLayout.SOUTH);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.setJMenuBar(makeMenu());
        
        // High Scores Panel --
        scorer = new Scorer();
        saver = new GameSaver();
        
        makeHSFrame();
    }
    
    // Save the current state of the game board (and scores, lives etc.).
    // Checks each position on board and maps it in a 2D array.
    // Call to saver object bundles this saved state into a save object.
    public void saveGameState() throws IOException
    {
        int rows = wall.getRows()+1;
        int cols = wall.getColumns()+1;
        int[][] cells = new int[rows][cols];
        
        for (int i = 1; i<rows; i++)
        {
            for (int j = 1; j<cols; j++)
            {
                if (wall.getBrick(i, j)!= null)
                {
                    cells[i][j] = 1;
                }
                else
                {
                    cells[i][j] = 0;
                }
            }
        }
        
        // Print array of cells
//        for (int i = 1; i<rows; i++)
//        {
//            for(int j = 1; j<cols; j++)
//            {
//                System.out.print(cells[i][j]+" ");
//            }
//            System.out.println();
//        }
        
        savedGame = cells;
        saver.addSavedGame(score, difficulty, lives, savedGame, timesRound, cols, rows, gameType);
    }
    
    // Opens a previously saved game state.
    // Removes everything from the game panel, and adds a new board to the game
    // panel based on the information in the passed save object.
    public void openSavedState(Save s)
    {
        if (s.getGameState() != null)
        {
            gamePanel.removeAll();
            gamePanel.add(makeGamePanelFromSave(s.getGameState()), BorderLayout.CENTER);
            gamePanel.repaint();
            gamePanel.revalidate();
            controlPanel.setDifficulty(s.getDifficulty());
        }
    }
    
    // Update the high scores panel.
    // Called when the high scores frame is set to visible.
    // Retrieves the list of highscores from the scorer object and displays
    // the top 10 scores.
    public void updateHighScoresPanel()
    {
        slPanel.removeAll();
        ArrayList<HighScore> list = scorer.getList();
        String tab = ".&nbsp;&nbsp;&nbsp;&nbsp;";

        for (int i = 0; i<list.size(); i++)
        {
            if (i == 9)
                tab = ".&nbsp;&nbsp;";
            if (i < 10)
                slPanel.add(new JLabel("<html><font size=+1' color='white'>" + 
                                        "&nbsp;&nbsp;[" + (i+1) + "]" + tab + 
                                        " " + list.get(i).getName() + "&nbsp;(" + list.get(i).getScore() + ")" +
                                        "</font></html>"));
        }
        slPanel.repaint();
        slPanel.revalidate();
    }

    // Collision handler.
    // Handles the main game play within the game board. 
    // Processes each collision event appropriately, and constantly checks to
    // make sure there are bricks still left on the board.
    @Override
    public void collisionDetected(CollisionEvent e) {
        if (difficulty >= 3)
        {
            if (e.getTarget() == CollisionEvent.BRICK)
                wall.setBallColor(wall.getBrick(e.getRow(), e.getColumn()));
        }
        
        if (e.getTarget() == CollisionEvent.MISS)
        {
            lives--;
            upperPanel.repaint();
            
            if (lives == 0)
            {
                // Do highscore stuff.
                // If greater than the last in the list (needs adding).
                if (scorer.isEmpty() || scorer.isAHighScore(score))
                {
                    String newName = JOptionPane.showInputDialog(null,"Congratulations!\nYou got a new high score: "+score+"\nWhat is your name?","New High Score!!!!!", JOptionPane.QUESTION_MESSAGE);
                    if (newName != null)
                        scorer.addHighScore(newName,score);
                    else
                        scorer.addHighScore("",score);
                }
                
                // New game.
                int n = JOptionPane.showConfirmDialog(null, "Score: " + score + "\nWould you like to start a new game?","Game Over!",JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION)
                    restart(gameType);
                else
                    System.exit(0);
            }
        }
        
        wall.setBrick(e.getRow(), e.getColumn(), null);
        
        // Check to see how many bricks are still left on the board.
        if (e.getTarget() == CollisionEvent.BRICK)
        {
            controlPanel.cpCenter.updateScore(e.getRow());
            
            int c = 0;
            for (int i = 0; i<=wall.getRows(); i++)
            {
                for (int j = 0; j<=wall.getColumns(); j++)
                {
                    if (wall.getBrick(i, j)!= null)
                    {
                        c++;
                    }
                }
            }
            
            // If there are NO bricks left on the board, repopulate and add an
            // extra row of bricks.
            if (c == 0)
            {
                timesRound++;
                
                // Classic
                if (gameType == 1)
                {
                    int nextRow = 3;
                    Color nextColor = colors.get(0);
                    for (int i = 0; i<rows+timesRound; i++)
                    {
                        if (nextRow<4)
                            nextColor = colors.get(0);
                        else if (nextRow<5)
                            nextColor = colors.get(1);
                        else if (nextRow<6)
                            nextColor = colors.get(2);
                        else if (nextRow<7)
                            nextColor = colors.get(3);
                        else if (nextRow<8)
                            nextColor = colors.get(4);
                        else if (nextRow<9)
                            nextColor = colors.get(5);
                        else if (nextRow<10)
                            nextColor = colors.get(6);
                        else if (nextRow<11)
                            nextColor = colors.get(7);
                        else if (nextRow<12)
                            nextColor = colors.get(8);

                        wall.buildWall(nextRow, nextRow, 1, wall.getColumns(), nextColor);
                        wall.resetBall();
                        nextRow++;
                    }
                }
                
                // Ping pong
                else if (gameType == 2)
                {
                    for (int i = 4; i <= 16; i++)
                    {
                        for (int j = 4; j<=9; j++)
                        {
                            wall.buildWall(i, i, j, j, colors.get(random.nextInt((6 - 0)+1)+0));
                        }
                    }
                }
                
                // All in
                else if (gameType == 3)
                {
                    for (int i = 2; i <= 19; i++)
                    {
                        for (int j = 2; j<=11; j++)
                        {
                            if ((random.nextInt((1 - 0)+1)+0) == 1)
                                wall.buildWall(i, i, j, j, colors.get(random.nextInt((6 - 0)+1)+0));
                        }
                    }
                }
            }
        }
    }

    // Builds game board for ordinary game (game type 1).
    // Checks for the user specified rows and generates the corresponding
    // number of rows and colours.
    private JComponent makeGamePanel() {
        wall = new BrickWall();
        wall.addCollisionListener(this);
        wall.setReportMask(CollisionEvent.BRICK|CollisionEvent.MISS|CollisionEvent.WALL);
        wall.setBackground(new Color(160,160,190));
        
        int nextRow = 3;
        Color nextColor = colors.get(0);
        for (int i = 0; i<rows; i++)
        {
            if (nextRow<4)
                nextColor = colors.get(0);
            else if (nextRow<5)
                nextColor = colors.get(1);
            else if (nextRow<6)
                nextColor = colors.get(2);
            else if (nextRow<7)
                nextColor = colors.get(3);
            else if (nextRow<8)
                nextColor = colors.get(4);
            else if (nextRow<9)
                nextColor = colors.get(5);
            else if (nextRow<10)
                nextColor = colors.get(6);
            
            wall.buildWall(nextRow, nextRow, 1, wall.getColumns(), nextColor);
            nextRow++;
        }
        
        wall.setBallSpeed(5);
        return wall;
    }
    
    // Builds game board for ping pong (game type 2).
    // Generates random locations and colours for bricks within a specified range.
    private JComponent makePingPongGamePanel() {
        wall = new BrickWall();
        wall.addCollisionListener(this);
        wall.setReportMask(CollisionEvent.BRICK|CollisionEvent.MISS|CollisionEvent.WALL);
        wall.setBackground(new Color(160,160,190));
        // Ping pong bats
        wall.setWalls(true, true, false, false);
        wall.setBallReset(5, 2, 100);
        
        for (int i = 4; i <= 16; i++)
        {
            for (int j = 4; j<=9; j++)
            {
                wall.buildWall(i, i, j, j, colors.get(random.nextInt((6 - 0)+1)+0));
            }
        }
        
        controlPanel.setDifficulty(controlPanel.getDifficultySelected());
        return wall;
    }
 
    // Builds game board for all in (game type 3).
    // Generates random locations and colours for bricks within a specified range.
    private JComponent makeAllInGamePanel() {
        wall = new BrickWall();
        wall.addCollisionListener(this);
        wall.setReportMask(CollisionEvent.BRICK|CollisionEvent.MISS|CollisionEvent.WALL);
        wall.setBackground(new Color(160,160,190));
        // All 4 bats
        wall.setWalls(false, false, false, false);
        wall.setBallReset(4, 2, 140);
        
        for (int i = 2; i <= 19; i++)
        {
            for (int j = 2; j<=11; j++)
            {
                if ((random.nextInt((1 - 0)+1)+0) == 1)
                    wall.buildWall(i, i, j, j, colors.get(random.nextInt((6 - 0)+1)+0));
            }
        }
        
        controlPanel.setDifficulty(controlPanel.getDifficultySelected());
        return wall;
    }
    
    // Builds game panel based on the passed save state (2D array).
    // This method should be called in conjunction with the loadGame and openSavedState methods.
    private JComponent makeGamePanelFromSave(int[][] state) {
        wall = new BrickWall();
        wall.addCollisionListener(this);
        wall.setReportMask(CollisionEvent.BRICK|CollisionEvent.MISS|CollisionEvent.WALL);
        wall.setBackground(new Color(160,160,190));
        
        int[][] s = state;
        int nextRow = 0;
        Color nextColor = new Color(255,255,255);
        
        if (gameType == 1)
        {
            for (int i=0; i<=wall.getRows(); i++)
            {
                for (int j = 0; j <=wall.getColumns(); j++)
                {
                    if (nextRow<4)
                        nextColor = colors.get(0);
                    else if (nextRow<5)
                        nextColor = colors.get(1);
                    else if (nextRow<6)
                        nextColor = colors.get(2);
                    else if (nextRow<7)
                        nextColor = colors.get(3);
                    else if (nextRow<8)
                        nextColor = colors.get(4);
                    else if (nextRow<9)
                        nextColor = colors.get(5);
                    else if (nextRow<10)
                        nextColor = colors.get(6);

                    if (s[i][j] == 1)
                    {
                        wall.buildWall(i, i, j, j, nextColor);
                    }
                }
                nextRow++;
            }
            wall.setWalls(true, false, true, true);
            wall.setBallReset(18,6,15);
        }
        
        else if (gameType == 2)
        {
            for (int i=0; i<=wall.getRows(); i++)
            {
                for (int j = 0; j <=wall.getColumns(); j++)
                {
                    if (s[i][j] == 1)
                    {
                        wall.buildWall(i, i, j, j, colors.get(random.nextInt((6 - 0)+1)+0));
                    }
                }
            }
            wall.setWalls(true, true, false, false);
            wall.setBallReset(5, 2, 100);
        }
        
        else if (gameType == 3)
        {
            for (int i=0; i<=wall.getRows(); i++)
            {
                for (int j = 0; j <=wall.getColumns(); j++)
                {
                    if (s[i][j] == 1)
                    {
                        wall.buildWall(i, i, j, j, colors.get(random.nextInt((6 - 0)+1)+0));
                    }
                }
            }
            wall.setWalls(false, false, false, false);
            wall.setBallReset(4, 2, 140);
        }
        return wall;
    }
    
    // Initial setup for loading a saved game.
    // Variables and labels are set here and then control is passed to openSavedState method.
    private void loadGame(Save s)
    {
        difficulty = s.getDifficulty();
        lives = s.getLives();
        score = s.getScore();
        timesRound = s.getTimesRound();
        gameType = s.getGameType();
        
        controlPanel.cpCenter.scoreLabel.setText(score+"");
        controlPanel.cpCenter.toAddLabel.setText("");
        
        upperPanel.repaint();
        upperPanel.revalidate();
        openSavedState(s);
    }
    
    // Builds Menu.
    private JMenuBar makeMenu()
    {
        // Build menu bar.
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        
        JMenu settings = new JMenu("Settings");
        file.setMnemonic(KeyEvent.VK_S);
        
        // File Menu.
        JMenuItem newMenuItem = new JMenuItem("New Game");
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (gameType != 1)
                    restart(gameType);
                else
                {
                    showStartPanel(true);
                    restart(gameType);
                }
            }
        });
        
        JMenuItem oMenuItem = new JMenuItem("Open");
        oMenuItem.setMnemonic(KeyEvent.VK_O);
        oMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showSavedGamesPopup();
            }
        });
        
        JMenuItem ofMenuItem = new JMenuItem("Open from file");
        ofMenuItem.setMnemonic(KeyEvent.VK_O);
        ofMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                
                // Let user pick a saved game
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("BRS","brs");
                fileChooser.setFileFilter(filter);
                int result = fileChooser.showOpenDialog(null);
                
                File selectedFile = null;
                if (result == JFileChooser.APPROVE_OPTION) 
                {
                    selectedFile = fileChooser.getSelectedFile();
                    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                    loadGame(saver.getSaveFromFile(selectedFile, wall.getRows(), wall.getColumns()));
                } 
                
                
            }
        });
        
        JMenuItem sMenuItem = new JMenuItem("Save");
        sMenuItem.setMnemonic(KeyEvent.VK_S);
        sMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try
                {
                    saveGameState();
                } 
                catch (IOException ex)
                {
                    Logger.getLogger(Director.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        JMenuItem importMenuItem = new JMenuItem("Import High Scores");
        importMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                scorer.importScores();
            }
        });
        
        JMenuItem exportMenuItem = new JMenuItem("Export High Scores");
        exportMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                scorer.export();
            }
        });
        
        JMenuItem eMenuItem = new JMenuItem("Exit");
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        
        file.add(newMenuItem);
        file.addSeparator();
        file.add(oMenuItem);
        file.add(ofMenuItem);
        file.addSeparator();
        file.add(sMenuItem);
        file.addSeparator();
        file.add(importMenuItem);
        file.add(exportMenuItem);
        file.addSeparator();
        file.add(eMenuItem);
        
        // Settings menu.
        
        // Theme submenu.
        JMenu themeMenu = new JMenu("Theme");
        themeMenu.setMnemonic(KeyEvent.VK_P);
        themeMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.out.println("Theme");
            }
        });
        
        ThemeListener themeListener = new ThemeListener();
        darkMenuItem = new JCheckBoxMenuItem("Dark");
        lightMenuItem = new JCheckBoxMenuItem("Light");
        darkMenuItem.setSelected(true);
        darkMenuItem.setActionCommand("dark");
        lightMenuItem.setActionCommand("light");
        darkMenuItem.addActionListener(themeListener);
        lightMenuItem.addActionListener(themeListener);
        
        themeMenu.add(darkMenuItem);
        themeMenu.add(lightMenuItem);
        
        JMenuItem optMenuItem = new JMenuItem("Game Type");
        optMenuItem.setMnemonic(KeyEvent.VK_O);
        optMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showPreferences();
            }
        });
        
        settings.add(themeMenu);
        settings.add(optMenuItem);
        
        // Add to menu bar.
        menuBar.add(file);
        menuBar.add(settings);
        
        return menuBar;
    }
    
    class ThemeListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("dark"))
            {
                lightMenuItem.setSelected(false);
                wall.setBackground(new Color(160,160,190));
            }
            
            else if (e.getActionCommand().equals("light"))
            {
                darkMenuItem.setSelected(false);
                wall.setBackground(Color.white);
            }
        }
        
    }
    
    private BrickWall getWall()
    {
        return wall;
    }
    
    private void restart(int type)
    {
        lives = livesQ;
        score = 0;
        
        controlPanel.cpCenter.scoreLabel.setText(score+"");
        controlPanel.cpCenter.toAddLabel.setText("");
        
        gamePanel.removeAll();
        if (type == 1)
            gamePanel.add(makeGamePanel(), BorderLayout.CENTER);
        else if (type == 2)
            gamePanel.add(makePingPongGamePanel(), BorderLayout.CENTER);
        else if (type == 3)
            gamePanel.add(makeAllInGamePanel(), BorderLayout.CENTER);
        
        gamePanel.repaint();
        gamePanel.revalidate();
        controlPanel.setDifficulty(controlPanel.getDifficultySelected());
        
        upperPanel.repaint();
    }
    
    private void buildPreferences()
    {
        pFrame = new JFrame("Game Type");
        JPanel pp1 = new JPanel();
        JPanel pp2 = new JPanel();
        JPanel ip = new JPanel();
        pp1.setPreferredSize(new Dimension(200,100));
        pp2.setPreferredSize(new Dimension(100,50));
        
        JButton pOK = new JButton("Apply");
        JButton pCancel = new JButton("Cancel");
        
        pb1 = new JRadioButton("Classic");
        pb2 = new JRadioButton("Ping Pong");
        pb3 = new JRadioButton("All In");
        ButtonGroup group = new ButtonGroup();
        group.add(pb1);
        group.add(pb2);
        group.add(pb3);
        
        pb1.setOpaque(false);
        pb1.setContentAreaFilled(false);
        pb1.setBorderPainted(false);
        pb1.setForeground(Color.white);
        pb2.setOpaque(false);
        pb2.setContentAreaFilled(false);
        pb2.setBorderPainted(false);
        pb2.setForeground(Color.white);
        pb3.setOpaque(false);
        pb3.setContentAreaFilled(false);
        pb3.setBorderPainted(false);
        pb3.setForeground(Color.white);
        
        pp1.setLayout(new GridLayout());
        pp1.add(ip);
        pp1.setBackground(Color.gray);
        
        ip.setBackground(Color.darkGray);
        
        ip.add(pb1);
        ip.add(pb2);
        ip.add(pb3);
        ip.setLayout(new BoxLayout(ip, BoxLayout.PAGE_AXIS));
        pp1.setBorder(BorderFactory.createLineBorder(Color.gray, 10));
        ip.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        pp2.setLayout(new GridLayout());
        pOK.setBackground(new Color(102,102,102));
        pOK.setForeground(Color.white);
        pCancel.setForeground(Color.white);
        pCancel.setBackground(new Color(168,20,0));
        pOK.setBorderPainted(false);
        pCancel.setBorderPainted(false);
        
        pCancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e)
            {
                pFrame.setVisible(false);
            }
        });
        
        pOK.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int n = JOptionPane.showConfirmDialog(null, "Changing game type will reset the game. \nAre you sure?","Change game type", JOptionPane.YES_NO_OPTION);
                if (n == 0)
                {
                    if (pb1.isSelected())
                    {
                        gameType = 1;
                        restart(gameType);
                    }
                    else if (pb2.isSelected())
                    {
                        gameType = 2;
                        restart(gameType);
                    }
                    if (pb3.isSelected())
                    {
                        gameType = 3;
                        restart(gameType);
                    }
                    pFrame.setVisible(false);
                }
            }
        });
        
        pp2.add(pOK);
        pp2.add(pCancel);
        
        pFrame.setLayout(new BorderLayout());
        pFrame.add(pp1,BorderLayout.CENTER);
        pFrame.add(pp2,BorderLayout.SOUTH);
        
        pFrame.pack();
        //pFrame.setVisible(true);
        pFrame.setLocationRelativeTo(null);
        pFrame.setResizable(false);
    }
    
    public void showPreferences()
    {
        if (gameType == 1)
            pb1.setSelected(true);
        else if (gameType == 2)
            pb2.setSelected(true);
        else if (gameType == 3)
            pb3.setSelected(true);
        else
            pb1.setSelected(true);
        
        pFrame.setVisible(true);
    }
    
    private void makeHSFrame()
    {
        hsPanel = new JPanel();
        hsFrame = new JFrame();
        
        hsFrame.setPreferredSize(new Dimension(300,420));
        hsFrame.setResizable(false);
        hsFrame.setUndecorated(true);
        hsFrame.add(hsPanel);
        
        hsPanel.setLayout(new BorderLayout());
        hsPanel.setBackground(Color.gray);
        
        JLabel hsLabel = new JLabel("<html><h1>High Scores</h1></html>");
        hsLabel.setForeground(Color.white);
        hsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hsLabel.setBackground(Color.darkGray);
        hsLabel.setOpaque(true);
        
        slPanel = new JPanel();
        slPanel.setLayout(new BoxLayout(slPanel, BoxLayout.PAGE_AXIS));
        slPanel.setBackground(Color.darkGray);
        slPanel.setBorder(BorderFactory.createLineBorder(Color.gray, 20));
        
        JPanel hsBtnPanel = new JPanel();
        hsBtnPanel.setBackground(Color.yellow);
        hsBtnPanel.setPreferredSize(new Dimension(50,50));
        hsBtnPanel.setLayout(new GridLayout());
        
        JButton clearHs = new JButton("Clear");
        JButton closeHs = new JButton("Close");
        clearHs.setBackground(new Color(102,102,102));
        clearHs.setForeground(Color.white);
        clearHs.setBorderPainted(false);
        clearHs.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                scorer.clearAllScores();
                updateHighScoresPanel();
            }
        });
       
        closeHs.setBackground(new Color(168,20,0));
        closeHs.setForeground(Color.white);
        closeHs.setBorderPainted(false);
        closeHs.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                hsFrame.setVisible(false);
            }
        });
        
        hsPanel.add(hsLabel,BorderLayout.NORTH);
        hsPanel.add(slPanel,BorderLayout.CENTER);
        hsPanel.add(hsBtnPanel,BorderLayout.SOUTH);
        
        hsBtnPanel.add(clearHs);
        hsBtnPanel.add(closeHs);
        
        hsFrame.pack();
        hsFrame.setLocationRelativeTo(null);
    }
    
    private JPanel showStartPanel(boolean restart) {
        JPanel panel = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        
        String[] options = {"OK"};
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel livesLbl = new JLabel("Number of lives: ");
        JLabel rowsLbl = new JLabel("Number of rows: ");
        
        SpinnerNumberModel livesModel = new SpinnerNumberModel(4, 1, 10, 1);
        SpinnerNumberModel rowsModel = new SpinnerNumberModel(4, 1, 12, 1);
        JSpinner livesSpin = new JSpinner(livesModel);
        JSpinner rowsSpin = new JSpinner(rowsModel);
        
        panel2.add(livesLbl);
        panel2.add(livesSpin);
        
        panel3.add(rowsLbl);
        panel3.add(rowsSpin);
        
        panel.add(panel2);
        panel.add(panel3);
        
        int i = JOptionPane.showOptionDialog(null, panel, "New Game", JOptionPane.NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options , options[0]);
        if (i == JOptionPane.OK_OPTION)
        {
            livesQ = (Integer)livesSpin.getValue();
            lives = livesQ;
            rows = (Integer)rowsSpin.getValue();
        }
        return panel;
    }
    
    // Popup dialog for choosing a saved game.
    private void showSavedGamesPopup() 
    {
        JPanel savePanel = new JPanel();
        JFrame saveFrame = new JFrame();
        
        saveFrame.setPreferredSize(new Dimension(300,420));
        saveFrame.setResizable(false);
        saveFrame.setUndecorated(true);
        saveFrame.add(savePanel);
        
        savePanel.setLayout(new BorderLayout());
        savePanel.setBackground(Color.gray);
        
        JLabel saveLabel = new JLabel("<html><h1>Saved Games</h1></html>");
        saveLabel.setForeground(Color.white);
        saveLabel.setHorizontalAlignment(SwingConstants.CENTER);
        saveLabel.setBackground(Color.darkGray);
        saveLabel.setOpaque(true);
        
        JPanel saveListPanel = new JPanel();
//        saveListPanel.setLayout(new BoxLayout(slPanel, BoxLayout.PAGE_AXIS));
        saveListPanel.setBackground(Color.darkGray);
        saveListPanel.setBorder(BorderFactory.createLineBorder(Color.gray, 20));
        
        // NOT WORKING YET!!!!
        JList saveList = new JList(saver.getSavedGames().toArray());
        JScrollPane scroll = new JScrollPane(saveList);
        scroll.setPreferredSize(new Dimension(240, 265));
        saveList.setBackground(Color.lightGray);
        
        saveListPanel.add(scroll);
        
        JPanel saveBtnPanel = new JPanel();
        saveBtnPanel.setBackground(Color.yellow);
        saveBtnPanel.setPreferredSize(new Dimension(50,50));
        saveBtnPanel.setLayout(new GridLayout());
        
        JButton openSave = new JButton("Open");
        JButton closeSave = new JButton("Close");
        openSave.setBackground(new Color(102,102,102));
        openSave.setForeground(Color.white);
        openSave.setBorderPainted(false);
        openSave.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //load game
                loadGame(saver.getSavedGames().get(saveList.getSelectedIndex()));
                saveFrame.setVisible(false);
            }
        });
       
        closeSave.setBackground(new Color(168,20,0));
        closeSave.setForeground(Color.white);
        closeSave.setBorderPainted(false);
        closeSave.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveFrame.setVisible(false);
            }
        });
        
        savePanel.add(saveLabel,BorderLayout.NORTH);
        savePanel.add(saveListPanel,BorderLayout.CENTER);
        savePanel.add(saveBtnPanel,BorderLayout.SOUTH);
        
        saveBtnPanel.add(openSave);
        saveBtnPanel.add(closeSave);
        
        saveFrame.pack();
        saveFrame.setLocationRelativeTo(null);
        saveFrame.setVisible(true);
    }
    
    class UpperPanel extends JPanel
    {
        ImageIcon heart = new javax.swing.ImageIcon(getClass().getResource("/heart.png"));

        public UpperPanel()
        {
            setPreferredSize(new Dimension(10,30));
            setBackground(new Color(48,48,48));
        }

        @Override
        public void paintComponent(Graphics g)
        {
            int interval = 30;
            super.paintComponent(g);

            for (int i = 0; i<=lives; i++)
            {
                g.drawImage(heart.getImage(), this.getWidth()-(interval*i), 10, 20, 20, null);
            }
        }
    }

    class ControlPanel extends JPanel
    {
        CpLeft cpLeft;
        CpCenter cpCenter; 
        CpRight cpRight;

        public ControlPanel(UpperPanel upperPanel)
        {
            cpLeft = new CpLeft();
            cpCenter = new CpCenter();
            cpRight = new CpRight();
            add(cpLeft);
            add(cpCenter);
            add(cpRight);

            setPreferredSize(new Dimension(500,150));
            setBackground(new Color(48,48,48));
            setLayout(new GridLayout());
        }
        
        public void setDifficulty(int d)
        {
            switch(d)
            {
                case 1:
                    System.out.println("Difficulty: 1");
                    wall.setBatSize(20);
                    wall.setBallSpeed(5);
                    wall.setBallColor(Color.darkGray);
                    wall.setBallSize(3);
                    cpLeft.d1.setSelected(true);
                    difficulty = 1;
                    break;
                case 2:
                    System.out.println("Difficulty: 2");
                    wall.setBatSize(12);
                    wall.setBallSpeed(7);
                    wall.setBallColor(Color.darkGray);
                    wall.setBallSize(3);
                    cpLeft.d2.setSelected(true);
                    difficulty = 2;
                    break;
                case 3:
                    System.out.println("Difficulty: 3");
                    wall.setBatSize(8);
                    wall.setBallSpeed(9);
                    wall.setBallSize(2);
                    cpLeft.d3.setSelected(true);
                    difficulty = 3;
                    break;
                case 4:
                    System.out.println("Difficulty: 4");
                    wall.setBatSize(3);
                    wall.setBallSpeed(10);
                    wall.setBallSize(1);
                    cpLeft.d4.setSelected(true);
                    difficulty = 4;
                    break;
                default:
                    System.out.println("Difficulty: 1 (default)");
                    wall.setBatSize(20);
                    wall.setBallSpeed(5);
                    wall.setBallSize(3);
                    cpLeft.d1.setSelected(true);
                    difficulty = 1;
                    break;
            }
        }
        
        public int getDifficultySelected()
        {
            if (cpLeft.d1.isSelected())
                return 1;
            else if (cpLeft.d2.isSelected())
                return 2;
            else if (cpLeft.d3.isSelected())
                return 3;
            return 4;
        }


        // Sub Classes for different panels.
        class CpLeft extends JPanel
        {
            JLabel difficultyLabel;
            JRadioButton d1,d2,d3,d4;
            ButtonGroup group;
            RadioListener rl;

            public CpLeft()
            {
                setBackground(Color.darkGray);
                setBorder(BorderFactory.createLineBorder(new Color(48,48,48),5));
                setLayout(new BorderLayout());

                difficultyLabel = new JLabel("Difficulty");
                difficultyLabel.setForeground(Color.white);
                difficultyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                difficultyLabel.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
                add(difficultyLabel,BorderLayout.NORTH);

                d1 = new JRadioButton("Easy");
                d2 = new JRadioButton("Medium");
                d3 = new JRadioButton("Hard");
                d4 = new JRadioButton("Ultimate");
                rl = new RadioListener();

                group = new ButtonGroup();
                group.add(d1);
                group.add(d2);
                group.add(d3);
                group.add(d4);

                JPanel radioPanel = new JPanel(new GridLayout(0, 1));
                radioPanel.setBackground(new Color(48,48,48));
                radioPanel.add(d1);
                radioPanel.add(d2);
                radioPanel.add(d3);
                radioPanel.add(d4);

                d1.setBackground(Color.darkGray);
                d1.setForeground(Color.lightGray);
                d2.setBackground(Color.darkGray);
                d2.setForeground(Color.lightGray);
                d3.setBackground(Color.darkGray);
                d3.setForeground(Color.lightGray);
                d4.setBackground(Color.darkGray);
                d4.setForeground(Color.lightGray);

                add(radioPanel,BorderLayout.CENTER);

                d1.setSelected(true);
                d1.addActionListener(rl);
                d2.addActionListener(rl);
                d3.addActionListener(rl);
                d4.addActionListener(rl);
            }

            public class RadioListener implements ActionListener
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    JRadioButton source = (JRadioButton) e.getSource();
                    if (source == d1)
                    {
                        setDifficulty(1);
                    }
                    else if (source == d2)
                    {
                        setDifficulty(2);
                    }
                    else if (source == d3)
                    {
                        setDifficulty(3);
                    }
                    else
                    {
                        setDifficulty(4);
                    }
                }

            }

        }

        class CpCenter extends JPanel
        {
            JLabel sLabel;
            JLabel scoreLabel;
            JPanel sub;
            JLabel toAddLabel;

            public CpCenter()
            {
                setBackground(Color.darkGray);
                setBorder(BorderFactory.createLineBorder(new Color(48,48,48),5));
                setLayout(new BorderLayout());

                sub = new JPanel();
                sub.setBackground(Color.darkGray);
                toAddLabel = new JLabel("");
                toAddLabel.setFont(new Font("Arial", Font.ITALIC, 15));
                toAddLabel.setForeground(Color.white);
                toAddLabel.setHorizontalAlignment(SwingConstants.CENTER);
                toAddLabel.setPreferredSize(new Dimension(10,15));


                sLabel = new JLabel("Score");
                sLabel.setForeground(Color.white);
                sLabel.setHorizontalAlignment(SwingConstants.CENTER);
                sLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

                scoreLabel = new JLabel("0");
                scoreLabel.setForeground(Color.cyan);
                scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
                scoreLabel.setFont(new Font("Arial", Font.BOLD, 50));

                add(sLabel,BorderLayout.NORTH);
                add(sub, BorderLayout.CENTER);
                sub.setLayout(new BorderLayout());
                sub.add(toAddLabel,BorderLayout.SOUTH);
                sub.add(scoreLabel,BorderLayout.CENTER);
            }

            public void updateScore(int row)
            {
                int add = 0;
                if (row <= 3)
                    add = 10;
                else if (row > 3 && row <= 5)
                    add = 8;
                else if (row > 5 && row <= 9)
                    add = 7;
                else if (row > 9 && row <= 12)
                    add = 5;
                else if (row > 12)
                    add = 1;

                score += add + difficulty;    
                scoreLabel.setText(score+"");
                toAddLabel.setText("+ "+ add + " (bonus: " + difficulty + ")");
            }
        }

        class CpRight extends JPanel
        {
            JButton highScoresBtn, resetBtn, quitBtn;
            GridLayout lo;

            public CpRight()
            {
                setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

                highScoresBtn = new JButton();
                highScoresBtn.setText("High Scores");
                highScoresBtn.setBackground(new Color(102,102,102));
                highScoresBtn.setForeground(Color.white);
                highScoresBtn.setBorderPainted(false);

                resetBtn = new JButton();
                resetBtn.setText("Reset");
                resetBtn.setBackground(new Color(102,102,102));
                resetBtn.setForeground(Color.white);
                resetBtn.setBorderPainted(false);

                quitBtn = new JButton();
                quitBtn.setText("Quit");
                quitBtn.setBackground(new Color(168,20,0));
                quitBtn.setForeground(Color.white);
                quitBtn.setBorderPainted(false);

                lo = new GridLayout();
                lo.setRows(3);
                lo.setVgap(6);

                setLayout(lo);
                add(highScoresBtn);
                add(resetBtn);
                add(quitBtn);

                ButtonListener bl = new ButtonListener();
                highScoresBtn.addActionListener(bl);
                resetBtn.addActionListener(bl);
                quitBtn.addActionListener(bl);

                setBackground(new Color(48,48,48));

            }

            class ButtonListener implements ActionListener
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (e.getSource() == highScoresBtn)
                        {
                            updateHighScoresPanel();
                            hsFrame.setVisible(true);
                        }
                        else if (e.getSource() == resetBtn)
                        {
                            int n = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset?","Reset game",
                                    JOptionPane.YES_NO_OPTION);
                            if (n == 0)
                            {
                                restart(gameType);
                            }
                        }
                        else if (e.getSource() == quitBtn)
                        {
                            int n = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?","Quit game",
                                    JOptionPane.YES_NO_OPTION);
                            if (n == 0)
                                System.exit(0);
                        }

                    }

                }
        }
    }
}

