package IO;

import Indexer.VariableByteCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class PostingReader {

    private InputStream in;
    private VariableByteCode vb ;
    private boolean isDone = false;
    private final int EOF = -1;

    public PostingReader(InputStream in , VariableByteCode vb){
        this.in = in;
        this.vb = vb;
    }

    public LinkedList<Byte> readNumber() throws IOException{

        LinkedList<Byte> list = new LinkedList<>();
        byte t = (byte)(in.read() & 0xff);
        while(((t >> 7) & 1) == 0){
            list.addLast(t);
        }
        return list;
    }

    public LinkedList<Integer> readLine() throws IOException{

        LinkedList<Integer> ans = new LinkedList<>();
        LinkedList<Byte> termID = new LinkedList<>() , length = new LinkedList<>() , docID = new LinkedList<>() , info = new LinkedList<>();
        LinkedList<Integer> lengthList = new LinkedList<>() , decodedInfo;

        byte t = 0;
        int len = 10000 ;
        int index = 0 ;

        readBytesTllEnd(termID);
        readBytesTllEnd(length);
        readBytesTllEnd(docID);


        in.read();//skip the new line tokens

        return ans ;
    }


    public boolean isDone() throws IOException{

        if((byte)in.read() == -1)
            return true;

        return false;
    }

    private void readLineTillEnd(LinkedList<Byte> list , int len) throws IOException{

        int index = 0 ;
        byte t = 0;

        while(index < len) {
            t = (byte) (in.read() & 0xff);
            list.add(t);
            index += 1;
        }

    }

    private void readBytesTllEnd(LinkedList<Byte> list) throws IOException{

        byte t = (byte)(in.read() & 0xff);
        if(((t >> 7) & 1) == 0) {
            while (((t >> 7) & 1) == 0) {
                list.addLast(t);
                t = (byte) (in.read() & 0xff);
            }
            list.addLast(t);
        }else{
            list.addLast(t);
        }
    }
}
