package Ranking;

import IO.BufferReader;
import Indexer.VariableByteCode;
import Parser.IParser;
import Structures.CorpusDocument;
import Structures.PostingDataStructure;
import Structures.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Searcher implements ISearcher {

    private final int DOCS_RETURN_NUMBER = 50;
    private IRanker ranker ;
    private IParser parser;
    private BufferReader termReader ;
    private HashMap<String, PostingDataStructure> termIdMap;
    private VariableByteCode vb;
    private int blockSize;

    public Searcher(IParser parser , String termOutPath , String docOutPath , int blockSize) {
        try {
            this.vb = new VariableByteCode();
            this.parser = parser;
            this.blockSize = blockSize;
            this.termReader = new BufferReader(termOutPath, blockSize);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<CorpusDocument> analyzeAndRank(String query) {
        ArrayList<String> queryTermList = parse(query);
        ArrayList<Term> terms = new ArrayList<>();

        getDataOnQueryTerms(queryTermList,terms);
        

        return null ;
    }

    private void getDataOnQueryTerms(ArrayList<String> queryTerms, ArrayList<Term> termList){

        try {
            for (String term :
                    queryTerms) {
                byte[] data = this.termIdMap.get(term).getEncodedData();
                LinkedList<Integer> decodedData = vb.decode(data);
                termList.add(this.termReader.getTermData(decodedData.get(2) * blockSize +
                        decodedData.get(3)));
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("at searcher");
        }
    }

    private ArrayList<String> parse(String query){

        return null;
    }

    public void setRanker(IRanker ranker){
        this.ranker = ranker;
    }

}
