package Model;

import Engine.Engine;
import java.util.HashMap;
import java.util.TreeMap;

public interface IModel {

    void setEngine(Engine engine);
    void runEngine(String corpusPath,String targetPath,boolean stemmerStatus);
    TreeMap getDictionary();
}
