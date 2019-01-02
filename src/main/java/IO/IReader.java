package IO;

/**
 * contract for being a reader
 */

public interface IReader {

    /**
     * start the reading process on a given path
     * @param path path to the files to be read
     */
    void read(String path);

    /**
     *
     * @return if there are no more files to be read
     */
    boolean getDone();

    /**
     * free the memory of the structures inside
     */
    void reset();
}
