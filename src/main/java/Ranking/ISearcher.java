package Ranking;

import Structures.CorpusDocument;

import java.util.ArrayList;

public interface ISearcher {

    void setRanker(IRanker ranker);
    ArrayList<CorpusDocument> analyzeAndRank(String query);
}
