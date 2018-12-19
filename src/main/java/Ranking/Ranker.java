package Ranking;

public class Ranker implements IRanker {

    private String termOutPath,docOutPath,cityOutPath;

    public Ranker(String termOutPath , String docOutPath , String cityOutPath){
        this.termOutPath = termOutPath;
        this.docOutPath = docOutPath;
        this.cityOutPath = cityOutPath;
    }



}
