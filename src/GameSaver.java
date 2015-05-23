import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSaver
{
    private ArrayList<Save> savedGames;
    private int seqNo;
    private int cols, rows;
    
    public GameSaver()
    {
        seqNo = 1;
        savedGames = new ArrayList<>();
    }
    
    public void addSavedGame(int score, int difficulty, int lives, int[][] gameState, int timesRound, int cols, int rows, int gameType)
    {
        savedGames.add(new Save(seqNo, score, difficulty, lives, gameState, timesRound, gameType));
        this.cols = cols; this.rows = rows;
        seqNo++;
        writeToFile();
    }
    
    public ArrayList<Save> getSavedGames()
    {
        return savedGames;
    }
    
    public void writeToFile()
    {
        BufferedWriter writer = null;
        int count = 0;
        
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        
        
        for (Save z: savedGames)
        {
            try {
                String curDate =dateFormat.format(date);
                File logFile = new File(z.getScore()+"-"+z.getDifficulty()+"-"+z.getLives()+"_"+curDate+".brs");

                writer = new BufferedWriter(new FileWriter(logFile));
                
                writer.write(z.getScore() + "," + z.getLives() + "," + z.getDifficulty() + "," + z.getGameType() + "\r\n");
                for (int k = 1; k<rows; k++)
                {
                    for(int j = 1; j<cols; j++)
                    {
                        writer.write(z.getGameState()[k][j]+"");
                    }
                    writer.write("\r\n");
                }
                count++;
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            } 
            finally 
            {
                try {
                    // Close the writer regardless of what happens...
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
    }
    
    public Save getSaveFromFile(File file, int rows, int cols)
    {
        Save externalSave = null;
        int[][] arr = new int[rows+1][cols+1];
        int score = 0, difficulty = 0, lives = 0, gameType = 1;
        try
        {
            Scanner s = new Scanner(file);
            Scanner f;
            String head = "";
            
//            while(s.hasNextLine())
//            {
//                System.out.println(s.nextLine());
//            }
            head = s.nextLine();
            Scanner h = new Scanner(head);
            h.useDelimiter(",");
            score = h.nextInt();
            lives = h.nextInt();
            difficulty = h.nextInt();
            gameType = h.nextInt();
            System.out.println(score + "-" + lives + "-" + difficulty);
            
            // Game state
            int row = 1; int column = 1;
            
            System.out.println("Array");
            int count = 0;
            while (s.hasNextLine())
            {
                column = 1;
                String line = s.nextLine();
                f = new Scanner(line);
                f.useDelimiter("(?<=.)");
                //System.out.print(row+",");
                while (f.hasNext())
                {
                    int x = f.nextInt();
                    arr[row][column] = x;
                    column++;
                }
               row++; 
            }
            
        } 
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(GameSaver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (int i = 0; i<rows; i++)
        {
            for(int j = 0; j<cols; j++)
            {
                System.out.print(arr[i][j]+" ");
            }
            System.out.println();
        }
        externalSave = new Save(0,score,difficulty,lives,arr,0,gameType);
        return externalSave;
    }
}
