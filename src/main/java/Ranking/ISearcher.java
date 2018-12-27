package Ranking;

import Structures.CorpusDocument;
import Structures.Pair;
import Structures.PostingDataStructure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public interface ISearcher {

    void setRankingParameters(double k, double b, double weightK, double weightB, double weightBM, double weightPos, double weightTitle, double idfLower, double idfDelta);

    void setRanker(IRanker ranker);

    void setAttributes(String termsOutPath , String docOutPath , double avgDocLength);

    ArrayList<CorpusDocument> analyzeAndRank(String query);

    void setDictionaries(TreeMap<String, PostingDataStructure> termIdMap , HashMap<Integer, Pair> docPositions);
}
