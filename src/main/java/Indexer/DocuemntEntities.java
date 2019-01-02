package Indexer;

import Structures.Pair;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * stores all the document's entities found in a map where the key is the docID and the value is the
 * value is a queue of entities sorted by their importance in the document .
 */

public class DocuemntEntities {

    private HashMap<Integer, PriorityQueue<Pair<Integer,Integer>>> docToEntities;

    public DocuemntEntities(){
        this.docToEntities = new HashMap<>();
    }

    /**
     * adds an empty entry entry in the map for document with given docID
     * @param docID
     */
    public void addEntry(int docID){

        if(this.docToEntities.get(docID) != null)
            return;

        //new queue
        this.docToEntities.put(docID,new PriorityQueue<>( ((o1, o2) -> {
            if(o1.getSecondValue() > o2.getSecondValue())
                return -1;
            else if(o1.getSecondValue() == o2.getSecondValue())
                return 0;

            return 1;
        })));

    }


    /**
     * add a new entity to a document
     * @param docID id of document to add to
     * @param termID the id of the entity that is added
     * @param tf the tf in the given document
     */
    public void addEntityTf(int docID , int termID , int tf){
        this.docToEntities.get(docID).add(new Pair(termID,tf));
    }

    /**
     * get the best entity on a given document
     * @param docID if of document to get the best entity from
     * @return id of the best entity
     */
    public int getBestEntityID(int docID){

        PriorityQueue<Pair<Integer,Integer>> q = this.docToEntities.get(docID);

        if(q == null || q.size() == 0)
            return -1;

        return q.poll().getFirstValue();//the id of the current strongest entity
    }

    /**
     * removes an entry from the dictionary
     * @param docID id of documents of which to remove the entry
     */
    public void removeEntry(int docID){
        this.docToEntities.remove(docID);
    }
}
