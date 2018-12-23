package Indexer;

import java.io.EOFException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

public class PostingBuffer {

    private String tempPostingPath;
    private int nextID ,index , blocksRead , blockSize;
    private byte[] buffer ;
    private boolean blockChanged , done , reachedEOF;

    public PostingBuffer(String path , int blockSize) {
        this.tempPostingPath = path;
        this.index = 0;
        this.nextID =-1;
        this.blockSize = blockSize;
        this.blocksRead = 0;
        this.buffer = new byte[blockSize];
        this.done = false;
        this.reachedEOF = false;
        try {
            fillBuffer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * check if all the numbers are already read
     * @return true if the index points to the length of the buffer
     */
    public boolean isEmpty(){
        return this.index >= buffer.length;
    }

    private byte getNextByte() throws Exception{

        if(this.index >= 4096) {
            fillBuffer();
        }

        return this.buffer[index++];
    }

    /**
     * read only untill the first negative byte
     * @param vb
     * @param wantedID
     * @return
     * @throws Exception
     */
    public int readTermID(VariableByteCode vb , int wantedID) throws Exception{

        int zeronum = 0 ;

        if(nextID != -1) {
            if(nextID == wantedID){
                int ans = nextID ;
                nextID = -1;
                return ans;
            }else
                return -1;
        }

        LinkedList<Byte> ans = new LinkedList<>();

        int currentID = 0 , i = 0;
        byte b = 1;
        boolean changed = false;

        do{
            if(index == buffer.length) {
                fillBuffer();
                b = buffer[index++];
                changed = true;
            }else
                b = buffer[index++];

            if(b == 0)
                zeronum++;
            if(zeronum >= 3)
                throw new EOFException();

            ans.addLast(b);

        }while(b >= 0);

        byte[] temp = new byte[ans.size()];
        for (byte t:
             ans) {
            temp[i++] = t;
        }
        currentID = vb.decode(temp).get(0).intValue();

        if(changed)
            nextID = currentID; // so we don't need to go back in the buffer

        if(currentID == wantedID) {
            nextID = -1;
            return currentID;
        }else
            nextID = currentID;

        return -1;
    }

    /**
     * raed to the end of the info about the current term. the end is represented by '00'
     * @return
     * @throws Exception
     */
    public LinkedList<Byte> readToEndOfTerm() throws Exception {
        LinkedList<Byte> ans = new LinkedList<>();


        int times = 0 , lastZeroIndex = 0;
        byte b ;

        while(times <= 2){
            b = getNextByte();
            if(b == 0){

                if(index - lastZeroIndex != 1) {
                    if(lastZeroIndex == 4096 && index == 1){
                        lastZeroIndex = index;
                        times++;
                        continue;
                    }
                    times = 0;
                    ans.addLast(b);
                }
                lastZeroIndex = index;
                times++;
                continue;
            }

            ans.addLast(b);

        }

        return ans;
    }

    /**
     * fill the buffer from the temp posting list
     */
    private void fillBuffer() throws Exception {

        RandomAccessFile in = new RandomAccessFile(this.tempPostingPath,"r");
        try {
            blockChanged = true;
            in.seek(blockSize * blocksRead++);
            in.readFully(this.buffer);
            this.index = 0;
            in.close();
        } catch (Exception e) {//close the file and throw

            if(this.reachedEOF) {
                in.close();
                throw e;
            }

            in.read(this.buffer);//this is the last block
            this.reachedEOF = true;
            this.index = 0 ;
            in.close();

            /**
             * in.close;
             * throw e;
             */
        }
    }

    public boolean isDone() {
        return done;
    }

    public String getTempPostingPath(){
        return this.tempPostingPath;
    }

    public String toString(){
        return ""+(index);
    }
}
