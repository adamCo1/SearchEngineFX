package Ranking;

import Structures.CorpusDocument;
import Structures.Term;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PositionsAlgorithm extends ARankingAlgorithm {


    public PositionsAlgorithm(double weight) {
        super(weight);
    }


    public double rank(CorpusDocument document , ArrayList<Term> termList){

        if(document == null || termList == null || termList.size() == 1)
            return 0;

        int[] foundInSeq = new int[termList.size()];
        double[] logWeights = new double[termList.size()];
        double rank = 0 , summedWeights = 0;
        int docID = document.getDocID(), index = 0 ,consecutive = 0 ;
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
                        logWeights[index] = Math.log10(current.getTotalTF() / current.getTF(docID));
                        if (logWeights[index + 1] == 0)
                            logWeights[index + 1] = Math.log10(next.getTotalTF() / next.getTF(docID));
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

        rank = (weight * consecutive) * summedWeights;
        return rank;
    }
/**
    @Override
    public double rank(CorpusDocument document, ArrayList<Term> termList) {

        int numberOfTermsInQuery = termList.size();


        double minDistance = numberOfTermsInQuery - 1;

        int[] currPositionOfEachTerm = new int[numberOfTermsInQuery];

        int[] totalPositionsOfEachTerm = new int[numberOfTermsInQuery];

        int totalPositionsChecked = 0;

        int docId = document.getDocID();

        int totalPositions = 0;
        boolean done = false;
        int minDistanceBetweenWords = Integer.MAX_VALUE;


        for (int i = 0; i < numberOfTermsInQuery; i++) {
            int termPositionCount = termList.get(i).getPositions(document.getDocID()).size();
            totalPositionsOfEachTerm[i] = termList.get(i).getPositions(document.getDocID()).size();
            totalPositions += termPositionCount;
        }



        //int array of [position in query of the term][position in doc of the term]
        PriorityQueue<int[]> positionsQueue = new PriorityQueue<>(totalPositions+20, Comparator.comparingInt(t -> t[1]));

        int currTermByOrderInQuestion=0;
        for(Term term:termList){
            for(int i = 0 ; i < term.getPositions(docId).size();i++){

                int[] tp = new int[2];
                tp[0] = currTermByOrderInQuestion;
                tp[1] = term.getPositions(docId).get(i);
                positionsQueue.add(tp);

            }
            currTermByOrderInQuestion++;
        }
        int currSeq = 0;
        int tmpSum = Integer.MAX_VALUE;
        int tmpTermPos = 0;
        minDistance = tmpSum;
        int[] currTp = null;
        int [] lastTp = null;
        while(!positionsQueue.isEmpty()){
            if(currTp!=null)
                lastTp = currTp;
            currTp = positionsQueue.poll();
            if(currTp[0] == 0){
                lastTp = currTp;
                tmpSum = 0;
                continue;
            }
            else if(currTp[0] < numberOfTermsInQuery-1 && currTp[0] == lastTp[0]+1){
                tmpSum+=currTp[1]-lastTp[1];
                continue;
            }
            else if(currTp[0] == numberOfTermsInQuery-1 ){
                tmpSum+=currTp[1]-lastTp[1];
                minDistance=Math.min(minDistance,tmpSum);
                if(tmpSum == numberOfTermsInQuery-1)
                    return (document.getLength()/tmpSum)*weight;
                continue;

            }
            else{
                tmpSum = Integer.MAX_VALUE;
                continue;

            }


        }


        return (document.getLength()/minDistance)*weight;

**/


    }

