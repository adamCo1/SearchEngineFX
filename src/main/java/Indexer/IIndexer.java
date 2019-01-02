package Indexer;

import Parser.IParser;

public interface IIndexer {

    /**
     * the main index function of the indexer
     * @param maxSize the maximum size that a buffer can reach in the memory
     */
    void index(int maxSize);

    /**
     * set a parser
     * @param parser
     */
    void setParser(IParser parser);

    /**
     * set the stemming status - with or without
     * @param stemOn
     */
    void setStemOn(boolean stemOn);

    /**
     * set the path to write the files to
     * @param path
     */
    void setTargetPath(String path);

    /**
     * reset the values in the current indexer
     */
    void reset();
}
