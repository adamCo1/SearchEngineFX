package Structures;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.HashMap;
import java.util.LinkedList;

public class Term implements IData {

    private HashMap<Integer,Triplet> docToDataMap;
    private int totalTF;

    public Term(int totalTF){
        this.totalTF = totalTF;
        this.docToDataMap = new HashMap<>();
    }

    public void addDocEntry(int docID , int tf, int onTitle, LinkedList<Integer> positions){
        this.docToDataMap.put(docID,new Triplet(tf,onTitle,positions));
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
}
