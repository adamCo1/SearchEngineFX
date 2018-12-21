package Ranking;

import Structures.CorpusDocument;
import Structures.Pair;
import Structures.PostingDataStructure;
import Structures.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public interface ISearcher {

    void setRanker(IRanker ranker);
    void setOutPaths(String termsOutPath , String docOutPath);
    ArrayList<CorpusDocument> analyzeAndRank(String query);
    void setDictionaries(TreeMap<String, PostingDataStructure> termIdMap , HashMap<Integer, Pair> docPositions);
}
