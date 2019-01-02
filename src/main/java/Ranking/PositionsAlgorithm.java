package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * an algorithm to rank by positions in the document. this algorithm checks for consecutive terms in the
 * documents and if terms are the the head of the document .
 */

public class PositionsAlgorithm extends ARankingAlgorithm {

    private final int POS_PERCENT = 15 ;

    public PositionsAlgorithm(double weight) {
        super(weight);
    }


        public double rank(CorpusDocument document , ArrayList<Term> termList){

            if (document == null || termList == null || termList.size() == 1)
                return 0;

            /**
             * check if terms are the the head of the file
             */
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

            /**
             * check for consecutive terms in the given document
             */
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

        //normalize the weights
        for(int i = 0 ; i < logWeights.length ; i++){
            summedWeights += logWeights[i];
        }

        rank = weight  * (summedWeights + termListRankInThisDoc);

        return rank*weight;
    }
}





