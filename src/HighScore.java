public class HighScore implements Comparable<HighScore>
{
    private String name = "";
    private int score = 0;
    
    public HighScore(String n, int s)
    {
        if (!n.equals(""))
            name = n;
        else
            name = "noname";
        score = s;
    }
    
    public int getScore()
    {
        return score;
    }
    
    public String getName()
    {
        return name;
    }

    @Override
    public int compareTo(HighScore o)
    {
        if (this.score != o.getScore())
            return o.getScore() - this.score;
        else
            return this.name.compareToIgnoreCase(o.getName());
    }
    
    @Override
    public String toString()
    {
        return "Name: " + name + ", Score: " + score;
    }
}
