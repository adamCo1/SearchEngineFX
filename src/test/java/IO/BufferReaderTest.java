package IO;

import Structures.Term;
import org.junit.jupiter.api.Test;


class BufferReaderTest {

    @Test
    public void testReading() throws Exception{

        byte[] b = new byte[1000*1000*1000];
        int i = 0;
        int len = b.length;
        long t1 = System.currentTimeMillis();
        while(i < len)
            b[i++] = 1;

        System.out.println("took" + " " + (System.currentTimeMillis()-t1));
    }

}