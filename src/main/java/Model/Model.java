package Model;

import Controller.Controller;
import Engine.Engine;
import java.util.TreeMap;

public class Model implements IModel {

    private Controller controller;
    private Engine engine;

    public Model(Controller controller){
        this.controller = controller;
        this.engine = new Engine();
    }


    @Override
    public void setTargetPath(String path) {
        this.engine.setTargetPath(path);
    }

    @Override
    public void setCorpusPath(String path) {
        this.engine.setCorpusPath(path);
    }

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void runEngine(boolean stemmerStatus) {
        this.engine.run(stemmerStatus);
    }

    @Override
    public TreeMap<String,Integer> getDictionary() {
        return this.engine.getTermIDDictionary();
    }
}
