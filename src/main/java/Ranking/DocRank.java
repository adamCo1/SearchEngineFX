package Ranking;

/**
 * hold a document's rank
 */

public class DocRank {

    private double rank ;
    private String name ;

    public DocRank(){
        this.name = "";
        this.rank = 0;
    }

    public double getRank() {
        return rank;
    }

    public void addRank(double rank){
        this.rank += rank;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean hasName(){
        return !this.name.equals("");
    }

    public String toString(){
        return ""+this.rank;
    }
}
