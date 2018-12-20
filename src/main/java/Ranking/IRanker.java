package Ranking;

import Structures.Term;

import java.util.ArrayList;

public interface IRanker {

    void rankByTerms(ArrayList<Term> termList);
    void setPaths(String termsPath , String docsPath);
}
