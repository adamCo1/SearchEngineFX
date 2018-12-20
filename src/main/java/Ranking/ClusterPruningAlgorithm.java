package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public class ClusterPruningAlgorithm extends ARankingAlgorithm{

    public ClusterPruningAlgorithm(double weight) {
        super(weight);
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {
        return 0;
    }
}
