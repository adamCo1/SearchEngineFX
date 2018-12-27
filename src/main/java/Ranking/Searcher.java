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
    private double avgDocLength;
    private int blockSize ;

    public Searcher(IParser parser , String termOutPath , String docOutPath , int blockSize ) {
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
    public ArrayList<CorpusDocument> analyzeAndRank(String query) {

        try {
            ArrayList<String> queryTermList = parser.parse(query);
            checkForUpperCase(queryTermList);

            ArrayList<Term> terms = new ArrayList<>();
            long t1 = System.currentTimeMillis();
            ArrayList<CorpusDocument> ans = getDataOnQueryTerms(queryTermList, terms);
            System.out.println("query process time : " + (System.currentTimeMillis()-t1));
            System.out.println("Best documents found : " + ans.size());
            System.out.println(Arrays.toString(ans.toArray()));
            return ans;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null ;
    }

    private void checkForUpperCase(ArrayList<String> qTerms){

        ArrayList<String> finalAns = new ArrayList<>();

        char c;
        for (String term:
             qTerms) {

            try{
                String upString = term.toUpperCase();
                c = term.charAt(0);
                //if(c >= 65 && c < 91)//upper case
                    if(termIdMap.get(upString) != null) {
                        finalAns.add(upString);
                    }else
                        finalAns.add(term);
             //   else
              //      finalAns.add(term);
            }catch (Exception e){

            }

        }

        qTerms = finalAns;
    }

    @Override
    public void setDictionaries(TreeMap<String, PostingDataStructure> termIdMap, HashMap<Integer, Pair> docPositions) {
        this.docPositions = docPositions;
        this.termIdMap = termIdMap;
        this.ranker.setDictionaries(docPositions);
    }

    private ArrayList<CorpusDocument> getDataOnQueryTerms(ArrayList<String> queryTerms, ArrayList<Term> termList){

        try {
            fillTermDataList(queryTerms,termList);
            this.ranker.setAttributes(outTermPath,outDocPath,avgDocLength);
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


    @Override
    public void setRankingParameters(double k, double b, double weightK, double weightB, double weightBM, double weightPos, double weightTitle, double idfLower, double idfDelta) {
        this.ranker.setRankingParameters(k,b,weightK,weightB,weightBM,weightPos,weightTitle,idfLower,idfDelta);
    }

    public void setRanker(IRanker ranker){
        this.ranker = ranker;
    }

    public IRanker getRanker(){return this.ranker;}

    @Override
    public void setAttributes(String termsOutPath, String docOutPath,double avgDocLength) {
        this.avgDocLength = avgDocLength;
        this.outTermPath = termsOutPath;
        this.outDocPath = docOutPath;
    }

}
