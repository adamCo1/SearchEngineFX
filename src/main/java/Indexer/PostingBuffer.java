package Indexer;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.LinkedList;

public class PostingBuffer {

    private SecureRandom secureRandom;
    private FileChannel fileChannel;
    private String tempPostingPath;
    private int nextID ,index , blocksRead , blockSize;
    private byte[] buffer ;
    private boolean blockChanged;

    public PostingBuffer(String path , int blockSize) {
        this.tempPostingPath = path;
        this.index = 0;
        this.nextID =-1;
        this.blockSize = blockSize;
        this.blocksRead = 0;
        this.secureRandom = new SecureRandom();
        this.buffer = new byte[blockSize];

        try {
        //    this.file = new RandomAccessFile(path,"R");
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

    public int readTermID(VariableByteCode vb , int wantedID) throws Exception{

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

    public LinkedList<Byte> readToEndOfTerm() throws Exception {
        LinkedList<Byte> ans = new LinkedList<>();


        int times = 0 , lastZeroIndex = 0;
        byte b ;

        while(times <= 2){
            b = getNextByte();
            if(b == 0){
                if(index - lastZeroIndex != 1) {
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
            //in = new RandomAccessFile(this.tempPostingPath, "r");
            //FileChannel channel = in.getChannel();
          //  MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,0,this.buffer.length);
            in.seek(blockSize * blocksRead++);
            in.readFully(this.buffer);
            this.index = 0;
            //buffer.force();
            //channel.close();
            in.close();
        } catch (Exception e) {//close the file and throw

            in.close();
            throw e;
        }
    }
    public String getTempPostingPath(){
        return this.tempPostingPath;
    }

    public String toString(){
        return ""+(index);
    }
}
