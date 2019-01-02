package Ranking;

import Structures.CorpusDocument;
import Structures.Pair;
import Structures.PostingDataStructure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * contract for being a Searcher class
 */

public interface ISearcher {

    void setRanker(IRanker ranker);

    /**
     * set the needed parameters for the searcher to function
     * @param termsOutPath the path to the terms posting list
     * @param docOutPath the path to the docs posting list
     * @param avgDocLength average document length in the corpus
     */
    void setAttributes(String termsOutPath , String docOutPath , double avgDocLength);

    /**
     * given a query , analyze it and rank documents by it's terms .
     * @param query query with all its fields
     * @param cities list fo cities to filter by
     * @return a list of documents ranked from the best to the worst according to the query
     */
    ArrayList<CorpusDocument> analyzeAndRank(String query, HashSet<String> cities);

    /**
     * set the stemming status : on/off
     * @param stemmerStatus
     */
    void setStemmerStatus(boolean stemmerStatus);

    /**
     * set the dictionaries created by the indexer .
     * @param termIdMap dictioanry from term to its data
     * @param docPositions map for positions in the file
     */
    void setDictionaries(TreeMap<String, PostingDataStructure> termIdMap , HashMap<Integer, Pair> docPositions);
}
