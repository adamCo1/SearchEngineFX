package Parser;

import Structures.Doc;
import Structures.TokensStructure;


public interface IParser {

    TokensStructure getBuffer();
    void parse(Doc doc);
    void parse(String text);
    void setDone(boolean done);
    boolean isDone();
    void initializeStopWordsTreeAndStrategies(String path);
    void reset();
}
