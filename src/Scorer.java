import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Scorer
{
    private ArrayList<HighScore> highScores;
    
    public Scorer()
    {
        highScores = new ArrayList<>();
        for (int i = 0; i<10; i++)
        {
            //highScores.add(new)
        }
    }
    
    public void addHighScore(String n, int s)
    {
        highScores.add(new HighScore(n,s));
        Collections.sort(highScores);
        System.out.println(highScores);
    }
    
    public void populateFromFile()
    {
        
    }
    
    public void export()
    {
        if (!highScores.isEmpty())
        {
            Collections.sort(highScores);
            
            BufferedWriter writer = null;
            // Export to file
            try 
            {
                File file = new File("highscores.txt");
                writer = new BufferedWriter(new FileWriter(file));
                for (HighScore hs: highScores)
                {
                    writer.write(hs.getName()+","+hs.getScore()+"\r\n");
                }
            } 
            catch (Exception e) 
            {
                // Something went wrong!
                e.printStackTrace();
            } 
            finally 
            {
                try 
                {
                    // Close the writer regardless of what happens...
                    writer.close();
                } 
                catch (Exception e) 
                {
                    // Something went wrong closing the file writer.
                }
            }
        }
    }
    
    public void importScores()
    {
        highScores.clear();
        try
        {
            // Read in scores from file.
            Scanner scan = new Scanner(new File("highscores.txt"));
            Scanner lineScan;
            while (scan.hasNextLine())
            {
                String s = scan.nextLine();
                lineScan = new Scanner(s);
                lineScan.useDelimiter(",");
                
                highScores.add(new HighScore(lineScan.next(),Integer.parseInt(lineScan.next())));
            }
            
        } 
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(Scorer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setScoresFromSave(ArrayList<HighScore> h)
    {
        highScores.clear();
        for (HighScore score: h)
        {
            highScores.add(score);
        }
    }
    
    public boolean isAHighScore(int s)
    {
        if (s >= highScores.get(highScores.size()-1).getScore())
            return true;
        else
            return false;
    }
    
    public ArrayList<HighScore> getList()
    {
        return highScores;
    }
    
    public void testPopulate()
    {
        for (int i = 0; i<10; i++)
        {
            highScores.add(new HighScore("rob",60));
        }
    }
    
    public boolean isEmpty()
    {
        return highScores.isEmpty();
    }
    
    public void clearAllScores()
    {
        highScores.clear();
    }
}
