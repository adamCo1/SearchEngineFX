package Ranking;

import IO.DocBufferReader;
import Structures.CorpusDocument;
import Structures.Pair;
import Structures.Term;

import java.util.*;

public class Ranker implements IRanker {

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

            fillDocDataBuffer(getRelevantDocIDs(termList));

            Thread titleAndPositionThread = new Thread(() -> rankByTitleAndPosition(termList));
          //  Thread readDocPostingsThread = new Thread(() -> readDocsPostings(getRelevantDocIDs(termList)));

            titleAndPositionThread.start();
            //readDocPostingsThread.start();

            titleAndPositionThread.join();
            //readDocPostingsThread.join();
            //when the 2 threads are done we have all the documents . now we can calculate BM25

            Thread bm25Thread = new Thread(() -> rankByBM25(termList));
            //Thread clusterPrunningThread = new Thread(() -> rankByPruning(termList));

            bm25Thread.start();

            bm25Thread.join();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setPaths(String termsPath, String docsPath) {
        this.termOutPath = termsPath;
        this.docOutPath = docsPath;
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
                int pos = (Integer)p.getFirstValue()*this.blockSize + (Integer)p.getSecondValue();
                docBuffer.add((CorpusDocument)docBufferReader.getData(pos));
            }

            docBufferReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void rankByBM25(ArrayList<Term> termList){

        BM25Algorithm bm25Algorithm = new BM25Algorithm(3,BM25_WEIGHT);

        try{

            for (CorpusDocument doc :
                    this.docBuffer) {
                double rank = bm25Algorithm.rank(doc,termList);
                this.docRanks.get(doc.getDocID()).addRank(rank);
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
