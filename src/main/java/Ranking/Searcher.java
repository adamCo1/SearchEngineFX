package Ranking;

import IO.BufferReader;
import IO.DocBufferReader;
import Indexer.VariableByteCode;
import Parser.IParser;
import Structures.CorpusDocument;
import Structures.Pair;
import Structures.PostingDataStructure;
import Structures.Term;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Searcher implements ISearcher {

    private final int DOCS_RETURN_NUMBER = 50;
    private HashMap<Integer, Pair> docPositions ;
    private IRanker ranker ;
    private IParser parser;
    private String outTermPath , outDocPath ;
    private HashMap<String, PostingDataStructure> termIdMap;
    private VariableByteCode vb;
    private int blockSize;

    public Searcher(HashMap termIdMap , HashMap docPositions, IParser parser , String termOutPath , String docOutPath , int blockSize) {
        try {
            this.termIdMap = termIdMap;
            this.docPositions = docPositions;
            this.vb = new VariableByteCode();
            this.parser = parser;
            this.blockSize = blockSize;
            this.outDocPath = docOutPath;
            this.outTermPath = termOutPath;
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
            fillTermDataList(queryTerms,termList);
            this.ranker.rankByTerms(termList);

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("at searcher getDataOnQueryTerms");
        }
    }

    /**
     * fill the term's information from the terms posting list according to the query terms
     * @param queryTerms
     * @param termList
     * @throws IOException
     */
    private void fillTermDataList(ArrayList<String> queryTerms ,ArrayList<Term> termList)throws IOException {

        BufferReader bufferReader = new BufferReader(this.outTermPath,this.blockSize);

        for (String term :
                queryTerms) {
            byte[] data = this.termIdMap.get(term).getEncodedData();
            LinkedList<Integer> decodedData = vb.decode(data);
            termList.add(bufferReader.getData(decodedData.get(2) * blockSize +
                    decodedData.get(3)));
        }

    }

    /**
     * read doc data from the doc posting list and fill a buffer with it .
     */
    private void fillDocDataBuffer(ArrayList<Integer> relevantDocIDS) {

        try {
            DocBufferReader docBufferReader = new DocBufferReader(this.outDocPath, this.blockSize);

            docBufferReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<String> parse(String query){

        return null;
    }

    public void setRanker(IRanker ranker){
        this.ranker = ranker;
    }

}
