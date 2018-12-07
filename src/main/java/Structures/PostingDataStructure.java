package Structures;

import java.io.Serializable;

public class PostingDataStructure implements Serializable {

    private int termID ;
    private byte[] tf , blockNum , index , out;

    public PostingDataStructure(int termID , byte[] tf , byte[] blockNum , byte[] index , byte[] out){
        this.termID = termID;
        this.tf = tf ;
        this.blockNum = blockNum;
        this.index = index;
        this.out = out;
    }

    public PostingDataStructure(PostingDataStructure other){
        this.termID = other.termID;
        copyArr(tf,other.tf);
        copyArr(blockNum,other.blockNum);
        copyArr(index,other.index);
        copyArr(out,other.out);
    }

    private void copyArr(byte[] toSave , byte[] toCopy){
        int i = 0;
        toSave = new byte[toCopy.length];

        while(i < toCopy.length)
            toSave[i] = toCopy[i++];
    }

    public int getTermID() {
        return termID;
    }

    public byte[] getTf() {
        return tf;
    }

    public byte[] getBlockNum() {
        return blockNum;
    }

    public byte[] getOut() {
        return out;
    }

    public byte[] getIndex() {
        return index;
    }

    public void setTermID(int termID) {
        this.termID = termID;
    }

    public void setTf(byte[] tf) {
        this.tf = tf;
    }

    public void setIndex(byte[] index) {
        this.index = index;
    }

    public void setBlockNum(byte[] blockNum) {
        this.blockNum = blockNum;
    }

    public void setOut(byte[] out) {
        this.out = out;
    }
}
