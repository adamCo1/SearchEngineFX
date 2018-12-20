package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public class BM25Algorithm extends ARankingAlgorithm {

    private double k , b , avgDocLength , totalDocCount;

    public BM25Algorithm(double avgDocLength , double weight , double b , double k){
        super(weight);
        this.totalDocCount = 472500;
        this.avgDocLength = avgDocLength;
        this.b = b ;
        this.k = k ;
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {

        double docRank = 0;

        for (Term term:termList) {
            double tfInDoc =  (Integer)term.getDocToDataMap().get(document.getDocID()).getFirst();
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
