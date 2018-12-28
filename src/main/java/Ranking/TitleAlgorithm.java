package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public class TitleAlgorithm extends ARankingAlgorithm {

    public TitleAlgorithm(double weight) {
        super(weight);
    }

    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {

        double rank = 0;

        for (Term term:
             termList) {
            try {
                if (term.getOnTitle(document.getDocID()) == 1)
                   // totalAtTitle++;
                    rank += weight;
            }catch (Exception e){

            }
        }

        return rank;

    }
}
