package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;
import java.util.LinkedList;

public class PositionsAlgorithm extends ARankingAlgorithm {


    private final int POS_PERCENT = 15 ;

    public PositionsAlgorithm(double weight) {
        super(weight);
    }

/**
    public double rank(CorpusDocument document, ArrayList<Term> termList) {

        if (document == null || termList == null || termList.size() == 1)
            return 0;

        LinkedList<Integer> positions = new LinkedList<>();

        double termListRankInThisDoc = 0;
        int docLen = document.getLength();
        //this is the first part of the doc text by % which is refferd as important (for example 10% from the first)
        int FromStartInPercent = 10;

        //this is the last position of the important section of the document
        int lastImportentPosition = (docLen * POS_PERCENT) / 100;


        for (Term t : termList) {
            positions = t.getPositions(document.getDocID());
            if(positions != null){
            for (Integer p : t.getPositions(document.getDocID())) {
                if (p <= lastImportentPosition)
                    termListRankInThisDoc += 1;
                }
            }

        }
        termListRankInThisDoc = (termListRankInThisDoc / docLen) * weight;
        return termListRankInThisDoc;
    }
}

**/
        public double rank(CorpusDocument document , ArrayList<Term> termList){

            if (document == null || termList == null || termList.size() == 1)
                return 0;

            double rank = 0;
            int docID = document.getDocID();
            LinkedList<Integer> positions = new LinkedList<>();

            double termListRankInThisDoc = 0;
            int docLen = document.getLength();

            //this is the last position of the important section of the document
            int lastImportentPosition = (docLen * POS_PERCENT) / 100;

            for (Term t : termList) {
                positions = t.getPositions(docID);
                if (positions != null) {

                    for (Integer p : t.getPositions(document.getDocID())) {
                        if (p <= lastImportentPosition)
                            termListRankInThisDoc += 0.2;
                        else
                            continue;
                    }
                }
            }

            int[] foundInSeq = new int[termList.size()];
        double[] logWeights = new double[termList.size()];
        double  summedWeights = 0;
        int index = 0 ,consecutive = 0 ;
        Term next , current ;

        while(index < termList.size()-1){
            try {
                current = termList.get(index);
                next = termList.get(index + 1);

                for (Integer position :
                        current.getPositions(docID)) {
                    if (next.consecutivePos(docID, position)) {
                        consecutive++;
                        foundInSeq[index] = 1;
                        foundInSeq[index + 1] = 1;
                        logWeights[index] = (Math.log10((double)(current.getTotalTF()/current.getTF(docID))));
                        if (logWeights[index + 1] == 0)
                            logWeights[index + 1] = Math.log10((double) (next.getTotalTF()/next.getTF(docID)));
                        continue;
                    }
                }
            }catch (Exception e){

            }
            index++;
        }//end while

        for(int i = 0 ; i < logWeights.length ; i++){
            summedWeights += logWeights[i];
        }

        rank = weight  * (summedWeights + termListRankInThisDoc);

        return rank*weight;
    }
}





