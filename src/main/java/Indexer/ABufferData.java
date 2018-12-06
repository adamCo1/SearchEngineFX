package Indexer;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class ABufferData {

    protected LinkedList<byte[]> data ;
    protected int termID;

    public ABufferData(){}

    public ABufferData(int termID, byte[] docID , byte[] onTitle, byte[] info){
        this.data = new LinkedList<>();
        data = new LinkedList<>();
        data.addFirst(docID);
        data.addLast(onTitle);
        data.addLast(info);
        this.termID = termID;
    }

    public void addInfo(byte[] docid ,byte[] onTitle, byte[] info){
        this.data.addLast(docid);
        this.data.addLast(onTitle);
        this.data.add(info);
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
