package IO;

import Indexer.VariableByteCode;
import Structures.CorpusDocument;
import Structures.Term;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

/**
 * class for reading info on terms from a posting list file
 */

public class BufferReader {

    private VariableByteCode vb ;
    private RandomAccessFile randomAccessFile ;
    private int blockSize , index , blocksRead;
    private byte[] buffer;

    public BufferReader(String path , int blockSize ) throws IOException{
        this.randomAccessFile = new RandomAccessFile(path,"r");
        this.blockSize = blockSize;
        this.blocksRead = 0;
        this.vb = new VariableByteCode();
        this.buffer = new byte[blockSize];
    }

    public Term getTermData(int positionInFile) throws IOException{

        this.blocksRead = 0;
        this.randomAccessFile.seek(positionInFile);

        return readAllTermData();
    }

    public CorpusDocument getDocData(int positionInFile)throws IOException{

        this.blocksRead = 0 ;
        this.randomAccessFile.seek(positionInFile);

        return readAllDocData();
    }

    private CorpusDocument readAllDocData() throws IOException{

        CorpusDocument corpusDocument ;
        int docID , length , maxTF , uniqueTermsNumber ;
        String name , author , city, language , type;

        docID = vb.decodeNumber(getData());
        length = vb.decodeNumber(getData());
        maxTF = vb.decodeNumber(getData());
        uniqueTermsNumber = vb.decodeNumber(getData());
        name = byteToString(getPosData());
        author = byteToString(getPosData());
        city = byteToString(getPosData());
        language = byteToString(getPosData());
        type = byteToString(getPosData());

        corpusDocument = new CorpusDocument(docID,length,maxTF,uniqueTermsNumber,name,author,
                city,language,type);

        return corpusDocument;
    }

    private String byteToString(LinkedList<Byte> stream){
        String ans = "";

        for (byte b:
             stream) {
            ans += (char)b;
        }

        return ans;
    }

    /**
     * read all the data on a given term's position in a posting list
     * @return Term - object of the term filled with all of it's data
     * @throws IOException
     */
    private Term readAllTermData() throws IOException{

        byte numberOfZeroes = 0 ;
        int docID  , tf , totalTF , onTitle;
        Term term ;

        fillBuffer();
        getData();//blank read for the term id. we already know it .

        totalTF = vb.decodeNumber(getData());
        term = new Term(totalTF);

        while(true){

            if(numberOfZeroes == 3)
                break;

            if(index >= buffer.length)
                fillBuffer();

            if(buffer[index] == 0){
                index++;
                numberOfZeroes++;
                continue;
            }

            numberOfZeroes = 0;
            //get the  doc-id , tf , on-title and positions

            docID = vb.decodeNumber(getData());
            tf = vb.decodeNumber(getData());
            onTitle = vb.decodeNumber(getData());
            LinkedList<Integer> positions = vb.decode(getPosData());
            numberOfZeroes++;

            term.addDocEntry(docID,tf,onTitle,positions);
        }

        return term;
    }

    /**
     * fills the buffer according to the amount of blocks read and the current block size
     * @throws IOException
     */
    private void fillBuffer() throws IOException{
        this.randomAccessFile.seek(blocksRead*blockSize);
        this.randomAccessFile.read(this.buffer);
        this.index = 0;
    }

    /**
     * this function returns linked list filled with all the positions . it stops only
     * at a 0.
     * @return
     * @throws IOException
     */
    private LinkedList<Byte> getPosData()throws IOException{

        LinkedList<Byte> tempList = new LinkedList<>();
        byte current = 0;
        do{
            if(index >= buffer.length)
                fillBuffer();

            current = buffer[index++];
            tempList.addLast(current);
        }while(current != 0);

        return tempList;
    }

    /**
     * get data on the term . this function looks for single data ints like tf , doc-id and such
     * @return
     * @throws IOException
     */
    private LinkedList<Byte> getData() throws IOException{

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


    public void close() throws IOException{
        this.randomAccessFile.close();
    }
}
