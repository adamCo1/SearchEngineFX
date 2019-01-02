package IO;

import Structures.Term;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * class for reading info on terms from a posting list file
 */

public class BufferReader extends ABufferReader {

    /**
     *
     * @param path path to the posting file
     * @param blockSize the size to read
     * @throws IOException
     */
    public BufferReader(String path , int blockSize )throws IOException{
        super(path,blockSize);
    }

    public Term getData(int positionInFile) throws IOException{

        this.blocksRead = 0;
        index = 0 ;
        initializeBuffer(positionInFile);
        return readAllTermData();
    }

    /**
     * read all the data on a given term's position in a posting list
     * @return Term - object of the term filled with all of it's data
     * @throws IOException
     */
    private Term readAllTermData() throws IOException{

        byte numberOfZeroes = 0 ;
        int id , docID  , tf , totalTF , onTitle , champ;
        Term term ;

        id = vb.decodeNumber(getSingleData());//blank read for the term id. we already know it .

        totalTF = vb.decodeNumber(getSingleData());
        term = new Term(totalTF,id);
      //  int times = 0 ;
        while(true){
        //    times++;
            if(index >= buffer.length)
                fillBuffer();

            if(buffer[index] == 0){
                index++;
                numberOfZeroes++;
                if(numberOfZeroes == 3)
                    break;
                continue;
            }

            numberOfZeroes = 0;
            //get the  doc-id , tf , on-title and positions

            docID = vb.decodeNumber(getSingleData());
            tf = vb.decodeNumber(getSingleData());
            champ = vb.decodeNumber(getSingleData());//is this doc a champ of the term?
            onTitle = vb.decodeNumber(getSingleData());

            //LinkedList<Integer> positions = vb.decode(getDataTillZero());
            LinkedList<Integer> positions = openGaps(vb.decode(getDataTillZero()));
            numberOfZeroes++;

            if(champ == 1 ) {//this doc is in the champion list of this term
                term.addChampion(docID);
            }

            term.addDocEntry(docID,tf,onTitle,positions);
        }

        //System.out.println(times);
        return term;
    }

    /**
     * restore the original term's positions from the encoded form
     * @param gapedPositions list of positions by gaps
     * @return
     */
    private LinkedList<Integer> openGaps(LinkedList<Integer> gapedPositions){

        LinkedList<Integer> ans = new LinkedList<Integer>();
        int last = 0 , current;

        while(gapedPositions.size() != 0){
            current = gapedPositions.poll();
            ans.addLast(current + last);
            last = current;
        }

        return ans;
    }
}
