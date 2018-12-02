package Model;

import Controller.Controller;
import Engine.Engine;

import java.util.HashMap;
import java.util.TreeMap;

public class Model implements IModel {

    private Controller controller;
    private Engine engine;

    public Model(Controller controller){
        this.controller = controller;
    }

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void runEngine(String corpusPath, String targetPath, boolean stemmerStatus) {
        this.engine.run(corpusPath,targetPath,stemmerStatus);
    }

    @Override
    public TreeMap<String,Integer> getDictionary() {
        return this.engine.getSortedTermDictionary();
    }
}
