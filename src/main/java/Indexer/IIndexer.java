package Indexer;

import Parser.IParser;

public interface IIndexer {

    void index(int maxSize);
    void setParser(IParser parser);
    void setStemOn(boolean stemOn);
    void setTargetPath(String path);
    void reset();
}
