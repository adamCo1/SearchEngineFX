package Ranking;

import Structures.Pair;
import Structures.PostingDataStructure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public interface ISearcher {

    void setRanker(IRanker ranker);
    void setOutPaths(String termsOutPath , String docOutPath);
    ArrayList<String> analyzeAndRank(String query);
    void setDictionaries(TreeMap<String, PostingDataStructure> termIdMap , HashMap<Integer, Pair> docPositions);
}
