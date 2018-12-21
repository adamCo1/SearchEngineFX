package IO;

import Indexer.VariableByteCode;
import Structures.IData;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

public abstract class ABufferReader {

    protected VariableByteCode vb ;
    protected RandomAccessFile randomAccessFile ;
    protected int blockSize , index , blocksRead;
    protected byte[] buffer;

    public ABufferReader(String path , int blockSize ) throws IOException {
        this.randomAccessFile = new RandomAccessFile(path,"r");
        this.blockSize = blockSize;
        this.blocksRead = 0;
        this.vb = new VariableByteCode();
        this.buffer = new byte[blockSize];
    }

    public abstract IData getData(int positionInFile) throws IOException;


    /**
     * fills the buffer according to the amount of blocks read and the current block size
     * @throws IOException
     */
    protected void fillBuffer() throws IOException{
        this.randomAccessFile.seek(blocksRead*blockSize);
        this.randomAccessFile.read(this.buffer);
        this.index = 0;
        this.blocksRead++ ;
    }

    protected void initializeBuffer(int positionInFile)throws IOException{

        this.randomAccessFile.seek(positionInFile);
        this.randomAccessFile.read(buffer);
    }

    /**
     * get data on the term . this function looks for single data ints like tf , doc-id and such
     * @return
     * @throws IOException
     */
    protected LinkedList<Byte> getSingleData() throws IOException{

        LinkedList<Byte> tempList = new LinkedList<>() ;
        byte current ;

        do{
            if(index >= buffer.length) {
                fillBuffer();
            }

            current = buffer[index++];
            tempList.addLast(current);
        }while(current > 0 );

        return tempList;
    }

    /**
     * this function returns linked list filled with all the positions . it stops only
     * at a 0.
     * @return
     * @throws IOException
     */
    protected LinkedList<Byte> getDataTillZero()throws IOException{

        LinkedList<Byte> tempList = new LinkedList<>();
        byte current = 0;
        do{
            if(index >= buffer.length)
                fillBuffer();

            current = buffer[index++];
            if(current == 0)
                break ;

            tempList.addLast(current);
        }while(current != 0);

        return tempList;
    }

    public void close() throws IOException{
        this.randomAccessFile.close();
    }
}
