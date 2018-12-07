package Parser;

import Structures.Doc;

import java.util.ArrayList;

public interface IParser {

    ArrayList<String> getBuffer();
    void parse(Doc doc);
    void parse(String text);
    void setDone(boolean done);
    boolean isDone();
    void initializeStopWordsTree(String path);
}
