package IO;

import java.io.IOException;
import java.io.PrintStream;

public class PostingWriter {

    private PrintStream out ;

    public PostingWriter(){

    }

    public void write(byte[] block) throws IOException{
        this.out.write(block);

    }

    public void setPath(String path) throws IOException {
        this.out = new PrintStream(path);
    }

    public void close(){
        this.out.close();
    }
}
