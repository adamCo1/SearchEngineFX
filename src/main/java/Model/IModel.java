package Model;

import Engine.Engine;

import java.io.File;
import java.util.TreeMap;

public interface IModel {

    TreeMap runSample(String text,boolean stemmerOn);
    void LoadDictionaryToMemory()throws Exception;
    void deleteOutputFiles(File file);
    void setTargetPath(String path);
    void setCorpusPath(String path);
    void setEngine(Engine engine);
    void runEngine(boolean stemmerStatus);
    TreeMap getDictionary();
}
