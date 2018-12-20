package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public class BM25Algorithm extends ARankingAlgorithm {

    private double avgDocLength ;
    private double totalDocCount ;
    private double k;
    private double b;

    public BM25Algorithm(double avgDocLength , double weight){
        super(weight);
        this.avgDocLength = avgDocLength;
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {
        double docRank = 0;
        for (Term term:termList) {
            double tfInDoc = (double)term.getDocToDataMap().get(document.getDocID()).getFirst();
            docRank+=(getTermIdf(term)*tfInDoc*(k+1))/(tfInDoc+k*(1-b+b*(document.getLength()/avgDocLength)));
        }
        return docRank;
    }


    public void setTotalDocCount(int docCount){
        this.totalDocCount = docCount;
    }

    public void setK(double k){
        this.k = k;
    }

    public void setb(double b){
        this.b = b;
    }

    private double getTermIdf(Term term ){
        int termTotalOccurencesInDocs = term.getDocToDataMap().size();
        double ans = Math.log((totalDocCount-termTotalOccurencesInDocs+0.5)/(termTotalOccurencesInDocs+0.5));
        return ans;
    }
}
