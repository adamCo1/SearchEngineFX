package Indexer;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class ABufferData {

    protected LinkedList<byte[]> data ;
    protected int termID , size , deltaSize;

    public ABufferData(){}

    public ABufferData(int termID, byte[] docID , byte[] onTitle, byte[] info){
        this.data = new LinkedList<>();
        data = new LinkedList<>();
        data.addFirst(docID);
        data.addLast(onTitle);
        data.addLast(info);
        this.size = (4 + docID.length + onTitle.length + info.length);
        this.termID = termID;
        this.deltaSize = this.size;
    }

    public int getSize(){
        return this.size;
    }

    public int getDeltaSize(){
        return this.deltaSize;
    }

    public void addInfo(byte[] docid ,byte[] onTitle, byte[] info){
        this.data.addLast(docid);
        this.data.addLast(onTitle);
        this.data.add(info);
        this.deltaSize = size;
        this.size += (docid.length + onTitle.length + info.length);
        this.deltaSize = size-deltaSize;
    }

    public byte[] getInfo(){
        byte[] ans = this.data.getFirst();
        this.data.removeFirst();
        return ans;
    }

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
