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
import java.util.*;

public class Searcher implements ISearcher {

    private final int DOCS_RETURN_NUMBER = 50;
    private HashMap<Integer, Pair> docPositions ;
    private IRanker ranker ;
    private IParser parser;
    private String outTermPath , outDocPath ;
    private TreeMap<String, PostingDataStructure> termIdMap;
    private VariableByteCode vb;
    private int blockSize;

    public Searcher(IParser parser , String termOutPath , String docOutPath , int blockSize) {
        try {
            this.ranker = new Ranker(docPositions,blockSize);
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
    public ArrayList<String> analyzeAndRank(String query) {

        try {
            ArrayList<String> queryTermList = parser.parse(query);
            ArrayList<Term> terms = new ArrayList<>();

            ArrayList<String> ans = getDataOnQueryTerms(queryTermList, terms);

            System.out.println("Best documents found : ");
            System.out.println(Arrays.toString(ans.toArray()));
            return ans;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null ;
    }

    @Override
    public void setDictionaries(TreeMap<String, PostingDataStructure> termIdMap, HashMap<Integer, Pair> docPositions) {
        this.docPositions = docPositions;
        this.termIdMap = termIdMap;
        this.ranker.setDictionaries(docPositions);
    }

    private ArrayList<String> getDataOnQueryTerms(ArrayList<String> queryTerms, ArrayList<Term> termList){

        try {
            fillTermDataList(queryTerms,termList);
            this.ranker.setPaths(outTermPath,outDocPath);
            return this.ranker.rankByTerms(termList);

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("at searcher getDataOnQueryTerms");
        }

        return null ;
    }

    /**
     * fill the term's information from the terms posting list according to the query terms
     * @param queryTerms
     * @param termList
     * @throws IOException
     */
    private void fillTermDataList(ArrayList<String> queryTerms ,ArrayList<Term> termList)throws IOException {

        BufferReader bufferReader = new BufferReader(this.outTermPath,this.blockSize);
        PostingDataStructure posting ;
        for (String term :
                queryTerms) {
            posting = this.termIdMap.get(term);
            if(posting == null)//so there is no record of this term , go to the next one
                continue;
            byte[] data = posting.getEncodedData();
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


    public void setRanker(IRanker ranker){
        this.ranker = ranker;
    }

    public IRanker getRanker(){return this.ranker;}

    @Override
    public void setOutPaths(String termsOutPath, String docOutPath) {
        this.outTermPath = termsOutPath;
        this.outDocPath = docOutPath;
    }

}
