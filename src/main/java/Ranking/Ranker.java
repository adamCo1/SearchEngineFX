package Ranking;

import IO.DocBufferReader;
import Structures.CorpusDocument;
import Structures.Pair;
import Structures.Term;
import sun.awt.Mutex;
import java.util.*;

/**
 * ranker class . gets a list of terms and ranks all relevant documents by them by the weights set below
 */

public class Ranker implements IRanker {

    private  double BM_25_B = 1.22 , BM_25_K = 0.77 , CONTAINING_WEIGHT = 0.4;
    private  double IDF_DELTA = 1, TITLE_WEIGHT = 0.45 , POSITIONS_WEIGHT = 0.08 , BM25_WEIGHT = 0.25 , IDF_LOWER_BOUND = 2;
    private  int DOCUMENT_RETRIEVE_COUNT = 50 ;
    private Mutex rankMutex;
    private String termOutPath,docOutPath,cityOutPath;
    private int blockSize;
    private double avgDocLength ;
    private ArrayList<CorpusDocument> docRanks;
    private ArrayList<CorpusDocument> docBuffer;
    private HashMap<Integer, Pair> docPos;

    public Ranker(HashMap docPos,int blockSize){
        this.avgDocLength = 233;
        this.termOutPath = termOutPath;
        this.docOutPath = docOutPath;
        this.cityOutPath = cityOutPath;
        this.blockSize = blockSize;
        this.docPos = docPos;
        this.docBuffer = new ArrayList<>();
        this.rankMutex = new Mutex();
        this.docRanks = new ArrayList<>();

    }


    @Override
    public ArrayList<CorpusDocument> rankByTerms(ArrayList<Term> termList,HashSet<String> cities) {

        PriorityQueue<CorpusDocument> rankStack = new PriorityQueue<>(new DocRankComparator());
        docBuffer = new ArrayList<>();
        docRanks = new ArrayList<>();


        for (Term term:
             termList) {
            setIDF(term);
        }
        if(termList.size() > 1)
            dropTermsByIDF(termList);


        try {
            ArrayList<Integer> relevantDocIDS = getRelevantDocIDs(termList);
            //dropDocsByTFIDF(relevantDocIDS,termList);
            //calculateAndSetIDF(termList);
            fillDocDataBuffer(relevantDocIDS,cities);
            initializeRankMap(relevantDocIDS,termList);

            Thread titleAndPositions = new Thread(()->rankByTitleAndPosition(termList));
            Thread otherRanks = new Thread(()->rankByTerms(termList));

            otherRanks.start();
            titleAndPositions.start();

            otherRanks.join();
            titleAndPositions.join();

            for (CorpusDocument doc:
                 this.docBuffer) {
                rankStack.add(doc);
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        System.out.println("Ranked " + rankStack.size() + " Documents : ");
        return retrieveBestDocuments(rankStack);

    }

    /**
     * get the best scores and return an array filled with the document's names from the best
     * to the worst
     * @param rankQ
     * @return
     */
    private ArrayList<CorpusDocument> retrieveBestDocuments(PriorityQueue<CorpusDocument> rankQ){

        int i = 0 ;
        ArrayList<CorpusDocument> bestDocumentsByOrder = new ArrayList<>();

        while(i < DOCUMENT_RETRIEVE_COUNT && rankQ.size() > 0){
            bestDocumentsByOrder.add(i,rankQ.poll());
            i++;
        }

        return bestDocumentsByOrder;
    }

    /**
     * if a term has a low idf , dont rank documents by it
     * @param termList
     */
    private void dropTermsByIDF(ArrayList<Term> termList){

        int numberOfLowerTerms = 0 ;
        ArrayList<Term> remove = new ArrayList<>();

        for (Term term:
             termList) {
            if(term.getIdf() < IDF_LOWER_BOUND) {//we dont want to consider this term
                remove.add(term);
                numberOfLowerTerms ++ ;
            }
        }

        if(remove.size() == termList.size()){
            for (Term term:
                 remove) {
                for (Term term2:
                     remove) {
                    if(!term.equals(term2) && (term.getIdf() - term2.getIdf()) > IDF_DELTA)
                        termList.remove(term2);
                }

            }
        }else
            termList.remove(remove);

    }

    /**
     * checks if an idf of a term is lower than the set bound
     * @param idf
     * @return true if its idf is higher than the bound
     */
    private boolean idfMoreThanBound(double idf){

        if(idf < IDF_LOWER_BOUND)
            return false;
        return true;
    }

    /**
     * initalizes the ranking map . this map holds an entry for each document that is
     * relevant to the query
     * @param relevantDocIDS
     */
    private void initializeRankMap(ArrayList<Integer> relevantDocIDS , ArrayList<Term> termList){

        for (CorpusDocument doc :
             this.docBuffer) {

        }
    }

    @Override
    public void setAttributes(String termsPath, String docsPath , double docaAvgLength) {
        this.avgDocLength = avgDocLength;
        this.termOutPath = termsPath;
        this.docOutPath = docsPath;
    }

    @Override
    public void setDictionaries(HashMap<Integer, Pair> docPositions) {
        this.docPos = docPositions;
        this.avgDocLength = (double)docPositions.get(-1).getSecondValue();
    }

    private void readDocsPostings(ArrayList<Integer> docIDS){

        try {
            DocBufferReader docBufferReader = new DocBufferReader(this.docOutPath, this.blockSize);

            docBufferReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * calculate the total number of documents needed to be ranked
     * @param termList
     * @return number of documents
     */
    private ArrayList<Integer> getRelevantDocIDs(ArrayList<Term> termList){

        HashSet<Integer> docIDSet = new HashSet<>();

        for (Term term:
             termList) {
            HashMap docMap = term.getDocToDataMap();
            Iterator iterator = docMap.keySet().iterator();
            while(iterator.hasNext()){
                Integer docID = (Integer)iterator.next();
                docIDSet.add(docID);
            }
        }

        ArrayList<Integer> docIDList = new ArrayList<>();
        docIDList.addAll(docIDSet);
        return docIDList;
    }

    /**
     * read doc data from the doc posting list and fill a buffer with it .
     */
    private void fillDocDataBuffer(ArrayList<Integer> relevantDocIDS,HashSet<String> cities) {


        try {
            DocBufferReader docBufferReader = new DocBufferReader(this.docOutPath, this.blockSize);
            Arrays.toString(relevantDocIDS.toArray());
            for (Integer docID:
                 relevantDocIDS) {
                Pair p = docPos.get(docID);

                if(p == null)
                    continue;

                int pos = (Integer)p.getFirstValue()*this.blockSize + (Integer)p.getSecondValue();
                CorpusDocument currentDocument = (CorpusDocument)docBufferReader.getData(pos);

                if(cities.size() != 0 && !(cities.contains(currentDocument.getCity())))//filter by chosen cities
                    continue;

                docBuffer.add((CorpusDocument)docBufferReader.getData(pos));
            }

            docBufferReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void rankByTerms(ArrayList<Term> termList){

        ContainingAlgorithm containingAlgorithm = new ContainingAlgorithm(CONTAINING_WEIGHT);
        BM25Algorithm bm25Algorithm = new BM25Algorithm(avgDocLength,this.docPos.size()-1,BM25_WEIGHT,BM_25_B,BM_25_K);
      //  uniqueAlgorithm uniqueAlgorithm = new uniqueAlgorithm(UNIQUE_WEIGHT);

        try{

            for (CorpusDocument doc:
                    this.docBuffer) {
               // addRank(doc,uniqueAlgorithm.rank(doc,termList));
                addRank(doc,bm25Algorithm.rank(doc,termList));
                addRank(doc,containingAlgorithm.rank(doc,termList));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * add the ranking part of the title and positions to each doc
     * @param termList
     */
    private void rankByTitleAndPosition(ArrayList<Term> termList){

        TitleAlgorithm titleAlgorithm = new TitleAlgorithm(this.TITLE_WEIGHT);
        PositionsAlgorithm positionsAlgorithm = new PositionsAlgorithm(this.POSITIONS_WEIGHT);

        try{

            for (CorpusDocument doc:
                 this.docBuffer) {
                addRank(doc,titleAlgorithm.rank(doc,termList));
                addRank(doc,positionsAlgorithm.rank(doc,termList));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * add rank to a document
     * @param doc the document to add rank to
     * @param rank rank to be added
     */
    private void addRank(CorpusDocument doc , double rank ){
        //this.rankMutex.lock();
        try{
            doc.addRank(rank);
        }catch (Exception e){
            //
        }
      //  this.rankMutex.unlock();
    }

    /**
     * set idf to terms
     * @param term
     */
    private void setIDF(Term term){

        double ni = term.getDocToDataMap().size()+0.5;//number of docs containing this term
        double numerator = this.docPos.size() - ni + 0.5 ;
        double denominator = ni ;
        double idf = Math.log10(numerator/denominator);

        term.setIDF(idf);
    }
}
