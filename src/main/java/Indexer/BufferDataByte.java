package Indexer;

/**
 * buffer for holding regular terms and their info on each docID seen
 */

public class BufferDataByte extends ABufferData {

    public BufferDataByte(int termID, byte[] docID , byte[] onTitle, byte[] info){
        super(termID,docID,onTitle,info);
    }
}
