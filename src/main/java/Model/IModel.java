package Model;

import Engine.Engine;
import Structures.CorpusDocument;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.*;

public interface IModel {

    void setRankingParameters(double k,double b,double weightK ,double weightB,double weightBM,
                              double weigtPos,double weightTitle,double idfLower,double idfDelta);
    TreeMap runSample(String text,boolean stemmerOn);
    String LoadDictionaryToMemory()throws Exception;
    String deleteOutputFiles(File file);
    void setTargetPath(String path);
    void setCorpusPath(String path);
    void setEngine(Engine engine);
    ArrayList<CorpusDocument> runQueryOnEngine(String query, boolean stemmerStatus, HashSet<String> cities);
    void createResultFileForQueries(String queryFilePath,String resultsFilePath,boolean stemmerStatus,HashSet<String> cities);
    String runEngine(boolean stemmerStatus);
    TreeMap getDictionary();
    TreeSet getDocsLang();
    ObservableList<String> getEntitiesName(LinkedList<Integer> entitiesID);
}
