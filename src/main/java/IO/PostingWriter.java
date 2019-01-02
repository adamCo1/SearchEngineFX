package IO;

import java.io.IOException;
import java.io.PrintStream;

/**
 * class for writing buffers of bytes of size 4KB to the disk
 */

public class PostingWriter {

    private PrintStream out ;
    private int position ;

    public PostingWriter(){

    }

    public void write(byte[] block) throws IOException{
        this.out.write(block);
        this.out.flush();
        this.position += 4096 ;
    }

    public void setPath(String path) throws IOException {
        this.out = new PrintStream(path);
    }

    public void flush(){
        this.out.flush();
    }

    public void close(){
        this.out.close();
    }
}
