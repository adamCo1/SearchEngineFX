package Structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * class for a term in the corpus .
 * a term has all of its saved data on the posting file .
 *
 * has mainly getters and setters
 */

public class Term implements IData {

    private HashMap<Integer,Triplet> docToDataMap; // a map from docID to all of the term's data on it
    private HashSet<Integer> champions ;
    private int totalTF , id;
    private double idf ;
    private String termName ;

    public Term(int totalTF , int id){
        this.id = id;
        this.champions = new HashSet<>();
        this.totalTF = totalTF;
        this.docToDataMap = new HashMap<>();
    }

    /**
     * add a new entry for a given document id
     * @param docID new document id
     * @param tf tf in the document
     * @param onTitle is the term on the document's title or not
     * @param positions the positions of the term in this document
     */
    public void addDocEntry(int docID , int tf, int onTitle, LinkedList<Integer> positions){
        this.docToDataMap.put(docID,new Triplet(tf,onTitle,positions));
    }

    public void setIDF(double idf){
        this.idf = idf;
    }

    public double getIdf(){
        return this.idf;
    }

    public int getTotalTF(){
        return this.totalTF;
    }

    public int getOnTitle(int docID){
        return (int)this.docToDataMap.get(docID).getSecond();
    }

    public int getTF(int docID){
        return (int)this.docToDataMap.get(docID).getFirst();
    }

    public LinkedList<Integer> getPositions(int docID){
        try {
            return (LinkedList<Integer>)this.docToDataMap.get(docID).getThird();
        }catch (Exception e){
            return null ;
        }
    }

    /**
     * check if this term has a consecutive position in a given document to other term position
     * @param docID
     * @param position
     * @return
     */
    public boolean consecutivePos(int docID , int position){

        boolean ans = false;

        for (Integer pos:
             getPositions(docID)) {
            if(pos > position+1)
                break;
            if(pos == position+1)
                ans = true;
        }

        return ans ;
    }

    public HashMap<Integer, Triplet> getDocToDataMap() {
        return docToDataMap;
    }

    public void addChampion(int docID){
        this.champions.add(docID);
    }

    public boolean isChampion(int docID){
        return this.champions.contains(docID);
    }

    public boolean equals(Object o){

        if(!(o instanceof Term))
            return false;

        return ((Term)o).getId() == this.id;
    }

    public void setTermName(String name){
        this.termName = name ;
    }

    public String getTermName() {
        return termName;
    }

    public int getId() {
        return id;
    }
}
