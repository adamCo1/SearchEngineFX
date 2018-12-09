package Model;

import Controller.Controller;
import Engine.Engine;

import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

public class Model implements IModel {

    private Controller controller;
    private Engine engine;

    public Model(Controller controller){
        this.controller = controller;
        this.engine = new Engine();
    }


    @Override
    public TreeMap runSample(String text,boolean stemmerOn) {
        return this.engine.sampleRun(text,stemmerOn);
    }

    @Override
    public void LoadDictionaryToMemory()throws Exception {
        this.engine.loadDictionaryToMemory();
    }

    @Override
    public void deleteOutputFiles(File file) {
        this.engine.deleteAllFiles(file);
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
    public String runEngine(boolean stemmerStatus) {
        try {
            return this.engine.run(stemmerStatus);
        }catch (Exception e){
            return "Error running Engine";
        }
    }

    @Override
    public TreeMap<String,Integer> getDictionary() {
        return this.engine.getTermIDDictionary();
    }

    @Override
    public TreeSet<String> getDocsLang(){return this.engine.getDocsLang();}
}
