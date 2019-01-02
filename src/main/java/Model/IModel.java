package Model;

import Engine.Engine;
import IO.Query;
import Structures.CorpusDocument;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.*;

/**
 * contract for the model
 */

public interface IModel {

    /**
     * run a sample test . only for debugging purposes
     * @param text free text from the user
     * @param stemmerOn status of stemmer
     * @return
     */
    TreeMap runSample(String text,boolean stemmerOn);

    /**
     * load dictionaries to the main memory from the disk
     * @return the result as text
     * @throws Exception
     */
    String LoadDictionaryToMemory()throws Exception;

    /**
     * delete all the files in the output directory
     * @param file
     * @return
     */
    String deleteOutputFiles(File file);

    /**
     * set the target path that the files will be written to
     * @param path directory path
     */
    void setTargetPath(String path);

    /**
     * set the path to the corpus
     * @param path
     */
    void setCorpusPath(String path);

    /**
     * set an engine
     * @param engine
     */
    void setEngine(Engine engine);

    /**
     * run a query on the eninge and get the results
     * @param query the query from the user
     * @param stemmerStatus on/off
     * @param cities list of cities to filter documents by
     * @return ranked documents from best to worse
     */
    ArrayList<CorpusDocument> runQueryOnEngine(Query query, boolean stemmerStatus, HashSet<String> cities)throws Exception;

    /**
     * given a path to a query file , run the queries in it and create a result file
     * @param queryFilePath path to query file
     * @param resultsFilePath path to the result file
     * @param stemmerStatus on/off
     * @param cities list of cities to filter by
     */
    void createResultFileForQueries(String queryFilePath,String resultsFilePath,boolean stemmerStatus,HashSet<String> cities)throws Exception;

    /**
     * run the engine , create the indexes
     * @param stemmerStatus on/off
     * @return the result as text
     */
    String runEngine(boolean stemmerStatus);

    /**
     * get the main term dictionary from the engine
     * @return the term dictionary
     */
    TreeMap getDictionary();

    /**
     * get the language's list from the engine , found in the documents it had parsed
     * @return set of the languages found
     */
    TreeSet getDocsLang();

    ObservableList<String> getEntitiesName(LinkedList<Integer> entitiesID);
}
