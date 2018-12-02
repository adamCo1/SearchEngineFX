package Model;

import Engine.Engine;
import java.util.TreeMap;

public interface IModel {

    void setTargetPath(String path);
    void setCorpusPath(String path);
    void setEngine(Engine engine);
    void runEngine(boolean stemmerStatus);
    TreeMap getDictionary();
}
