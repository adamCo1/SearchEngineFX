package Ranking;

import Structures.CorpusDocument;
import Structures.Pair;
import Structures.Term;

import java.util.ArrayList;
import java.util.HashMap;

public interface IRanker {

    /**
     * the main ranking function
     * @param termList a list of the query terms after parsing
     */
    ArrayList<CorpusDocument> rankByTerms(ArrayList<Term> termList);

    /**
     * set the paths to the postings lists
     * @param termsPath
     * @param docsPath
     */
    void setAttributes(String termsPath , String docsPath,double docLength);

    /**
     * set the dictionary . usefull when the dictionaries are loaded from memory
     * @param docPositions dictinary containing doc's positions in the posting list
     */
    void setDictionaries(HashMap<Integer, Pair> docPositions);

    public void setRankingParameters(double k, double b, double weightK, double weightB, double weightBM, double weightPos, double weightTitle, double idfLower, double idfDelta);
}
