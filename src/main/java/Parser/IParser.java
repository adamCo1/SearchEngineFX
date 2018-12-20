package Parser;

import Structures.Doc;
import Structures.TokensStructure;

import java.util.ArrayList;


public interface IParser {

    TokensStructure getBuffer();
    void parse(Doc doc);
    ArrayList<String> parse(String text);
    void setDone(boolean done);
    boolean isDone();
    void initializeStopWordsTreeAndStrategies(String path);
    void reset();
}
