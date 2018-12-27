package IO;

import Structures.CorpusDocument;
import Structures.IData;

import java.io.IOException;
import java.util.LinkedList;

public class DocBufferReader extends ABufferReader{


    public DocBufferReader(String path , int blockSize)throws IOException{
        super(path,blockSize);
    }

    @Override
    public IData getData(int positionInFile) throws IOException {
        this.blocksRead = 0 ;
        index = 0;
        initializeBuffer(positionInFile);

        return readAllDocData();
    }


    private CorpusDocument readAllDocData() throws IOException{

        CorpusDocument corpusDocument ;
        int docID , length , maxTF , uniqueTermsNumber ;
        String name , author , city, language , type;
        LinkedList<Integer> bestEntitiesID = new LinkedList<Integer>();

        docID = vb.decodeNumber(getSingleData());

        length = vb.decodeNumber(getSingleData());
        maxTF = vb.decodeNumber(getSingleData());
        uniqueTermsNumber = vb.decodeNumber(getSingleData());
        name = byteToString(getDataTillZero());
        author = byteToString(getDataTillZero());
        city = byteToString(getDataTillZero());
        language = byteToString(getDataTillZero());
        type = byteToString(getDataTillZero());
        //read the best 5 entities
        while(buffer[index] != 0) {//so there are more entities , max 5
            bestEntitiesID.addLast(vb.decodeNumber(getDataTillZero()));
        }
        corpusDocument = new CorpusDocument(docID,length,maxTF,uniqueTermsNumber,name,author,
                city,language,type , bestEntitiesID);

        return corpusDocument;
    }

    private String byteToString(LinkedList<Byte> stream){
        String ans = "";
        LinkedList<Integer> decoded = vb.decode(stream);
        char c ;

        for (Integer i:
                decoded) {
            c = (char)i.byteValue() ;
            ans += c;
        }

        return ans;
    }

}
