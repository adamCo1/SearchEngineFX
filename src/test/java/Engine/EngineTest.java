package Engine;

import IO.RandomPostingWriter;
import Indexer.VariableByteCode;
import org.junit.jupiter.api.Test;

import java.io.RandomAccessFile;
import java.util.Random;

class EngineTest {

    @Test
    public void testEngine() throws Exception {

        RandomAccessFile randomAccessFile = new RandomAccessFile("test", "rw");
        randomAccessFile.write(5);
        randomAccessFile.close();
        RandomAccessFile randomAccessFile12 = new RandomAccessFile("test", "r");
        while (true) {
            byte[] arr = new byte[10];

            randomAccessFile12.read(arr);
            System.out.println(arr[1]);
        }
    }

}