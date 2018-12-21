package Ranking;

import IO.DocBufferReader;
import Structures.CorpusDocument;
import Structures.Pair;
import Structures.Term;

import java.util.*;

public class Ranker implements IRanker {

    private final double BM_25_B = 1.2 , BM_25_K = 0.75;
    private final double TITLE_WEIGHT = 0.4 , POSITIONS_WEIGHT = 0.3 , BM25_WEIGHT = 0.3;
    private String termOutPath,docOutPath,cityOutPath;
    private int blockSize;
    private HashMap<Integer,DocRank> docRanks ;
    private ArrayList<CorpusDocument> docBuffer;
    private HashMap<Integer, Pair> docPos;

    public Ranker(HashMap docPos,int blockSize){
        this.termOutPath = termOutPath;
        this.docOutPath = docOutPath;
        this.cityOutPath = cityOutPath;
        this.blockSize = blockSize;
        this.docPos = docPos;
        this.docRanks = new HashMap<>();
        this.docBuffer = new ArrayList<>();
    }


    @Override
    public void rankByTerms(ArrayList<Term> termList) {
        try {
            PriorityQueue<DocRank> rankStack = new PriorityQueue<>(new DocRankComparator());
            docBuffer = new ArrayList<>();

            ArrayList<Integer> relevantDocIDS = getRelevantDocIDs(termList);
            initializeRankMap(relevantDocIDS);
            fillDocDataBuffer(relevantDocIDS);

            Thread titleAndPositionThread = new Thread(() -> rankByTitleAndPosition(termList));
          //  Thread readDocPostingsThread = new Thread(() -> readDocsPostings(getRelevantDocIDs(termList)));

            titleAndPositionThread.start();
            //readDocPostingsThread.start();

            titleAndPositionThread.join();
            //readDocPostingsThread.join();
            //when the 2 threads are done we have all the documents . now we can calculate BM25

           // Thread bm25Thread = new Thread(() -> rankByBM25(termList));
            //Thread clusterPrunningThread = new Thread(() -> rankByPruning(termList));
            long t1 = System.currentTimeMillis();
           // bm25Thread.start();
            rankByBM25(termList);
          //  bm25Thread.join();
            System.out.println("query process time : " + (System.currentTimeMillis()-t1));

            System.out.println("Ranked " + this.docRanks.size() + " Documents : ");
            Iterator iterator = this.docRanks.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<Integer,DocRank> entry = (Map.Entry)iterator.next();
                System.out.println("DOC ID : " + entry.getKey());
                System.out.println("RANK : " + entry.getValue());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * initalizes the ranking map . this map holds an entry for each document that is
     * relevant to the query
     * @param relevantDocIDS
     */
    private void initializeRankMap(ArrayList<Integer> relevantDocIDS){

        for (Integer i :
             relevantDocIDS) {
            this.docRanks.put(i,new DocRank());
        }
    }

    @Override
    public void setPaths(String termsPath, String docsPath) {
        this.termOutPath = termsPath;
        this.docOutPath = docsPath;
    }

    @Override
    public void setDictionaries(HashMap<Integer, Pair> docPositions) {
        this.docPos = docPositions;
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
    private void fillDocDataBuffer(ArrayList<Integer> relevantDocIDS) {


        try {
            DocBufferReader docBufferReader = new DocBufferReader(this.docOutPath, this.blockSize);
            Arrays.toString(relevantDocIDS.toArray());
            for (Integer docID:
                 relevantDocIDS) {
                Pair p = docPos.get(docID);
                if(p == null)
                    continue;
                int pos = (Integer)p.getFirstValue()*this.blockSize + (Integer)p.getSecondValue();
                docBuffer.add((CorpusDocument)docBufferReader.getData(pos));
            }

            docBufferReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void rankByBM25(ArrayList<Term> termList){

        BM25Algorithm bm25Algorithm = new BM25Algorithm(700,BM25_WEIGHT,BM_25_B,BM_25_K);
        DocRank docRank ;
        try{

            for (CorpusDocument doc :
                    this.docBuffer) {
                double rank = bm25Algorithm.rank(doc,termList);
                docRank = this.docRanks.get(doc.getDocID());
                if(docRank != null)//till we solve the EOF bug
                    docRank.addRank(rank);
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
                DocRank docRank = this.docRanks.get(doc.getDocID());
                docRank.addRank(titleAlgorithm.rank(doc,termList));
                docRank.addRank(positionsAlgorithm.rank(doc,termList));
            }

        }catch (Exception e){

        }
    }


}