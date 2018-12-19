package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public class BM25Algorithm extends ARankingAlgorithm {

    private int avgDocLength ;

    public BM25Algorithm(int avgDocLength){
        super();
        this.avgDocLength = avgDocLength;
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {
        return 0;
    }
}
