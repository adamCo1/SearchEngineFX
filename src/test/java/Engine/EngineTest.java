package Engine;

import IO.BufferReader;
import Structures.Term;
import org.junit.jupiter.api.Test;
import java.io.RandomAccessFile;

class EngineTest {

    @Test
    public void testEngine() throws Exception {


        RandomAccessFile randomAccessFile = new RandomAccessFile("test", "rw");
        randomAccessFile.write(new byte[]{-127,-120,-120,-126,-126,-127,-126,4,0,-124,-123,0,0,0});
        BufferReader bufferReader = new BufferReader("test",4096);

        Term term = bufferReader.getData(0);


    }

}