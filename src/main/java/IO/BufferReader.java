package IO;

import Structures.Term;
import java.io.IOException;
import java.util.LinkedList;

/**
 * class for reading info on terms from a posting list file
 */

public class BufferReader extends ABufferReader {


    public BufferReader(String path , int blockSize )throws IOException{
        super(path,blockSize);
    }

    public Term getData(int positionInFile) throws IOException{

        this.blocksRead = 0;
        this.randomAccessFile.seek(positionInFile);

        return readAllTermData();
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
        getSingleData();//blank read for the term id. we already know it .

        totalTF = vb.decodeNumber(getSingleData());
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

            docID = vb.decodeNumber(getSingleData());
            tf = vb.decodeNumber(getSingleData());
            onTitle = vb.decodeNumber(getSingleData());
            LinkedList<Integer> positions = vb.decode(getDataTillZero());
            numberOfZeroes++;

            term.addDocEntry(docID,tf,onTitle,positions);
        }

        return term;
    }

}
