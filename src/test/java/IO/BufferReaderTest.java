package IO;

import Structures.Term;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

class BufferReaderTest {

    @Test
    public void testReading() throws Exception{

        byte[] arr = new byte[]{-126,2,-127,-125,-127,-127,-5,-3,-4,-5,-1,-2,-4,-3,0,-120,-127,-126,
        -127,-127,-127,-110,4,2,-127,0,0,0};

        PrintStream stream = new PrintStream("file");
        stream.write(arr);

        BufferReader reader = new BufferReader("file",arr.length);
        //Term term = reader.getTermData(0);

        System.out.println("done");
    }

}