package Ranking;

import Structures.CorpusDocument;

import java.util.ArrayList;

public interface ISearcher {

    void setRanker(IRanker ranker);
    void setOutPaths(String termsOutPath , String docOutPath);
    ArrayList<CorpusDocument> analyzeAndRank(String query);
}
