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
                    return (document.getLength()/tmpSum)/document.getLength();
                continue;

            }
            else{
                tmpSum = Integer.MAX_VALUE;
                continue;

            }


        }


        return (document.getLength()/minDistance)/document.getLength();



//
//        int [] currTp;
//        currTp = positionsQueue.poll();
//        if(currTp[0] == currSeq) {
//            if(currSeq == 0) {
//                tmpSum = 0;
//                tmpPos = currTp[1];
//            }
//
//
//
//            else if(currTp[0] < numberOfTermsInQuery-1){
//                tmpSum += (currTp[1]-tmpPos);
//
//            }
//
//
//            if(currTp[0]==numberOfTermsInQuery-1)
//                minDistanceBetweenWords=Math.min(minDistanceBetweenWords,)
//        }
//        else{
//            currSeq=0;
//            tmpSum=Integer.MAX_VALUE;
//        }
//



//        while(!done){
//            for(int i = 0 ; i < numberOfTermsInQuery ; i++){
//                if(currPositionOfEachTerm[i] >= totalPositionsOfEachTerm[i]) {
//                    done = true;
//                    break;
//                }
//                int currTermPositionInDoc = termList.get(i).getPositions(docId).get(currPositionOfEachTerm[i]);
//                int lastTermPositionInDoc = termList.get((i-1)%numberOfTermsInQuery).getPositions(docId).get(currPositionOfEachTerm[i]);
//                while()
//            }
//        }
//
//
//        int currTermByOrderInQuestion = 0;
//
//        while (totalPositionsChecked < totalPositions) {
//
//
//            if (currPositionOfEachTerm[currTermByOrderInQuestion] < totalPositionsOfEachTerm[currTermByOrderInQuestion]) {
//                for (int i = 0; i <= 300; i++) {
//                    int[] tp = new int[2];
//                    tp[0] = currTermByOrderInQuestion;
//                    tp[1] = termList.get(currTermByOrderInQuestion).getPositions(document.getDocID()).get(currPositionOfEachTerm[currTermByOrderInQuestion]);
//                    positionsQueue.add(tp);
//                    currPositionOfEachTerm[currTermByOrderInQuestion]++;
//                }
//            }
//
//
//            //got positions of all terms now stop and start find gaps
//            if (currTermByOrderInQuestion == numberOfTermsInQuery - 1) {
//                // enqueue the positions
//                int[] positionsGap = new int[numberOfTermsInQuery];
//                while (!positionsQueue.isEmpty()) {
//                    int[] currtp = positionsQueue.poll();
//
//
//                }
//
//            }
//
//            currTermByOrderInQuestion = (currTermByOrderInQuestion + 1) % numberOfTermsInQuery;
//
//        }
////
////
////    }


    }}

