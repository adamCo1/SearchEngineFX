package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;

public abstract class ARankingAlgorithm {

    protected double weight;

    public ARankingAlgorithm(double weight){
        this.weight = weight;
    }

    /**
     * rank a document according to a term list given from a query
     * @param document
     * @param termList
     * @return the ranking value of the algorithm
     */
    public abstract double rank(CorpusDocument document, ArrayList<Term> termList);



}
