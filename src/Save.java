import java.util.ArrayList;

public class Save
{
    private int score, difficulty, lives, seqNo, timesRound, gameType;
    private int[][] gameState;
    private ArrayList<HighScore> highScores;

    public Save(int seqNo, int score, int difficulty, int lives, int[][] gameState, int timesRound, int gameType)
    {
        this.seqNo = seqNo;
        this.score = score;
        this.difficulty = difficulty;
        this.lives = lives;
        this.gameState = gameState;
        this.timesRound = timesRound;
        this.gameType = gameType;
    }

    public int getScore()
    {
        return score;
    }

    public int getDifficulty()
    {
        return difficulty;
    }

    public int getLives()
    {
        return lives;
    }
    
    public int getGameType()
    {
        return gameType;
    }

    public int[][] getGameState()
    {
        return gameState;
    }

    public ArrayList<HighScore> getHighScores()
    {
        return highScores;
    }
    
    public int getTimesRound()
    {
        return timesRound;
    }
    
    @Override
    public String toString()
    {
        return "Seq: " + seqNo +
                ", Score: " + score +
                ", Difficulty: " + difficulty +
                ", Lives: " + lives;
    }
    
}
