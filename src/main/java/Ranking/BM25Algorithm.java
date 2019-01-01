package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public class BM25Algorithm extends ARankingAlgorithm {

    private double k , b , avgDocLength , totalDocCount;

    public BM25Algorithm(double avgDocLength ,double totalDocCount, double weight , double b , double k){
        super(weight);
        this.totalDocCount = totalDocCount;
        this.avgDocLength = avgDocLength;
        this.b = b ;
        this.k = k ;
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {

        double docRank = 0;
        try {


            for (Term term : termList) {
                double tfInDoc = (Integer) term.getDocToDataMap().get(document.getDocID()).getFirst();
                docRank += (term.getIdf()) * ( 1 + (tfInDoc * (k + 1)) / (tfInDoc + k * (1 - b + b * (document.getLength()) / avgDocLength)));
            }
        }catch (NullPointerException e){//in case that the term is not in the given document
            docRank += 0;
        }

        return docRank * weight;
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

}
