package Indexer;

import java.util.Arrays;
import java.util.LinkedList;

/**
 *an abstract class for data buffers.
 *this class count its size in bytes for use in case the buffers needs to be capped in a max size .
 *
 */

public abstract class ABufferData {

    protected LinkedList<byte[]> data ;
    protected int termID , size , deltaSize;

    public ABufferData(){}

    /**
     * initialize a buffer with first term seen term .
     * @param termID id of the term
     * @param docID id of the doc the term is at
     * @param onTitle is the term on the doc's title or not
     * @param info all the info saved on this term in this document . this info can be anything as long as its
     *             in byte format
     */
    public ABufferData(int termID, byte[] docID , byte[] onTitle, byte[] info){
        this.data = new LinkedList<>();
        data = new LinkedList<>();
        data.addFirst(docID);
        data.addLast(onTitle);
        data.addLast(info);
        this.size = (12 + docID.length + onTitle.length + info.length +8*3);
        this.termID = termID;
        this.deltaSize = this.size;
    }

    /**
     * get the total size of the buffer
     * @return the size in bytes of the buffer
     */
    public int getSize(){
        return this.size;
    }

    /**
     *
     * @return the difference of last size
     */
    public int getDeltaSize(){
        return this.deltaSize;
    }

    /**
     * add more info on the current term and add the size to the total size variable
     * @param docid new doc the term seen at
     * @param onTitle is the term on the title or not
     * @param info all of the other info on this doc
     */
    public void addInfo(byte[] docid ,byte[] onTitle, byte[] info){
        this.data.addLast(docid);
        this.data.addLast(onTitle);
        this.data.add(info);
        this.deltaSize = size;
        this.size += (docid.length + onTitle.length + info.length + 8*3);
        this.deltaSize = size-deltaSize;
    }

    public byte[] getInfo(){
        byte[] ans = this.data.getFirst();
        this.data.removeFirst();
        return ans;
    }

    /**
     *
     * @return true if the buffer is not empty
     */
    public boolean hasMore(){
        return this.data.size() != 0;
    }

    public String toString(){
        String ans = "";

        for (byte[] b:
                this.data) {
            ans += (Arrays.toString(b));
        }

        return ans ;
    }

}
