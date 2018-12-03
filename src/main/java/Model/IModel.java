package Model;

import Engine.Engine;

import java.io.File;
import java.util.TreeMap;

public interface IModel {

    void deleteOutputFiles(File file);
    void setTargetPath(String path);
    void setCorpusPath(String path);
    void setEngine(Engine engine);
    void runEngine(boolean stemmerStatus);
    TreeMap getDictionary();
}
