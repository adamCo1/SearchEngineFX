package Structures;

import java.io.Serializable;

/**
 *Class for holding data for the main dictionary
 *
 * holds all the encoded data on a term id
 */

public class PostingDataStructure implements Serializable {

    private int termID ;
    //private byte[] tf , blockNum , index ,out;
    private byte[] encodedData;

    public PostingDataStructure(int termID , byte[] encodedData){
        this.termID = termID;
        this.encodedData = encodedData;
    }

    public PostingDataStructure(PostingDataStructure other){
        this.termID = other.termID;
        this.encodedData = new byte[other.encodedData.length];
        copyArr(this.encodedData,other.encodedData);
    }

    /**
     * used for deep copying an array of bytes
     * @param toSave
     * @param toCopy
     */
    private void copyArr(byte[] toSave , byte[] toCopy){
        int i = 0;

        while(i < toCopy.length)
            toSave[i] = toCopy[i++];
    }

    public byte[] getEncodedData(){
        return this.encodedData;
    }

    public int getTermID() {
        return this.termID;
    }

    public void setTermID(int termID) {
        this.termID = termID;
    }

    /**
     * sets encoded data on the current term
     * @param encodedData
     */
    public void setEncodedData(byte[] encodedData) {
        this.encodedData = new byte[encodedData.length];
        copyArr(this.encodedData,encodedData);
    }
}
