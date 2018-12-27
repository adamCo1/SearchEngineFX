package Indexer;

import Structures.Pair;
import java.util.HashMap;
import java.util.PriorityQueue;

public class DocuemntEntities {

    private HashMap<Integer, PriorityQueue<Pair<Integer,Integer>>> docToEntities;

    public DocuemntEntities(){
        this.docToEntities = new HashMap<>();
    }

    public void addEntry(int docID){

        if(this.docToEntities.get(docID) != null)
            return;

        this.docToEntities.put(docID,new PriorityQueue<>( ((o1, o2) -> {
            if(o1.getSecondValue() > o2.getSecondValue())
                return -1;
            else if(o1.getSecondValue() == o2.getSecondValue())
                return 0;

            return 1;
        })));

    }

    public void addEntityTf(int docID , int termID , int tf){
        this.docToEntities.get(docID).add(new Pair(termID,tf));
    }

    public int getBestEntityID(int docID){

        PriorityQueue<Pair<Integer,Integer>> q = this.docToEntities.get(docID);

        if(q == null || q.size() == 0)
            return -1;

        return q.poll().getFirstValue();//the id of the current strongest entity
    }

    public void removeEntry(int docID){
        this.docToEntities.remove(docID);
    }
}
