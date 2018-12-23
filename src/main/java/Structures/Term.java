package Structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Term implements IData {

    private HashMap<Integer,Triplet> docToDataMap;
    private HashSet<Integer> champions ;
    private int totalTF , id;
    private double idf ;

    public Term(int totalTF , int id){
        this.id = id;
        this.champions = new HashSet<>();
        this.totalTF = totalTF;
        this.docToDataMap = new HashMap<>();
    }

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
        return (LinkedList<Integer>)this.docToDataMap.get(docID).getThird();
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

    public int getId() {
        return id;
    }
}
