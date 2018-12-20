package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public class BM25Algorithm extends ARankingAlgorithm {

    private double avgDocLength ;

    public BM25Algorithm(double avgDocLength , double weight){
        super(weight);
        this.avgDocLength = avgDocLength;
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {
        return 0;
    }
}
