package Indexer;

public class BufferDataDoc {

    private byte[] maxTF , uniqueNumber ;
    private int size;

    public BufferDataDoc(byte[] maxTF , byte[] uniqueNumber){
        this.maxTF = maxTF;
        this.uniqueNumber = uniqueNumber;
        this.size += maxTF.length + uniqueNumber.length;
    }

    public int getSize(){
        return this.size;
    }

    public byte[] getMaxTF(){
        return this.maxTF;
    }

    public byte[] getUniqueNumber(){
        return this.uniqueNumber;
    }
}
