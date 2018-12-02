package Indexer;

public class BufferDataDoc {

    private byte[] maxTF , uniqueNumber ;

    public BufferDataDoc(byte[] maxTF , byte[] uniqueNumber){
        this.maxTF = maxTF;
        this.uniqueNumber = uniqueNumber;
    }

    public byte[] getMaxTF(){
        return this.maxTF;
    }

    public byte[] getUniqueNumber(){
        return this.uniqueNumber;
    }
}
