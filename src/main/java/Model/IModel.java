package Model;

import Engine.Engine;

import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

public interface IModel {

    TreeMap runSample(String text,boolean stemmerOn);
    String LoadDictionaryToMemory()throws Exception;
    String deleteOutputFiles(File file);
    void setTargetPath(String path);
    void setCorpusPath(String path);
    void setEngine(Engine engine);
    String runEngine(boolean stemmerStatus);
    TreeMap getDictionary();
    TreeSet getDocsLang();
}
