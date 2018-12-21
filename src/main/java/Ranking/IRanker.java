package Ranking;

import Structures.Pair;
import Structures.Term;

import java.util.ArrayList;
import java.util.HashMap;

public interface IRanker {

    /**
     * the main ranking function
     * @param termList a list of the query terms after parsing
     */
    ArrayList<String> rankByTerms(ArrayList<Term> termList);

    /**
     * set the paths to the postings lists
     * @param termsPath
     * @param docsPath
     */
    void setPaths(String termsPath , String docsPath);

    /**
     * set the dictionary . usefull when the dictionaries are loaded from memory
     * @param docPositions dictinary containing doc's positions in the posting list
     */
    void setDictionaries(HashMap<Integer, Pair> docPositions);
}
