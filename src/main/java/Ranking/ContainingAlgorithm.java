package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * checks if a term is an entity of the given document .
 */

public class ContainingAlgorithm extends ARankingAlgorithm {

    public ContainingAlgorithm(double weight) {
        super(weight);
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {

        double rank = 0;
        LinkedList<Integer> positions;
        int inDoc = 0 ,numTerms = 0;

        for (Term t : termList) {
            positions = t.getPositions(document.getDocID());
            if (positions != null) {

                //check if its an entity
                if (document.isEntity(t.getTermName()))
                    rank += 0.3;
            }
        }

                return rank*weight;
    }
}
