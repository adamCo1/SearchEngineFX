package Ranking;

/**
 * hold a document's rank
 */

public class DocRank {

    private double rank ;

    public DocRank(){
        this.rank = 0;
    }

    public double getRank() {
        return rank;
    }

    public void addRank(double rank){
        this.rank += rank;
    }
}
