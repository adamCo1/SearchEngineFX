package Model;

import Controller.Controller;
import Engine.Engine;
import Structures.CorpusDocument;
import javafx.collections.ObservableList;
import oracle.jrockit.jfr.StringConstantPool;

import java.io.File;
import java.util.*;

public class Model implements IModel {

    private Controller controller;
    private Engine engine;

    public Model(Controller controller){
        this.controller = controller;
        this.engine = new Engine();
    }


    @Override
    public void setRankingParameters(double k, double b, double weightK, double weightB, double weightBM, double weightPos, double weightTitle, double idfLower, double idfDelta) {
        this.engine.setRankingParameters(k,b,weightK,weightB,weightBM,weightPos,weightTitle,idfLower,idfDelta);
    }

    @Override
    public TreeMap runSample(String text,boolean stemmerOn) {
        return this.engine.sampleRun(text,stemmerOn);
    }

    @Override
    public String LoadDictionaryToMemory()throws Exception {
       String out = this.engine.loadDictionaryToMemory();
        return out;
    }

    @Override
    public String deleteOutputFiles(File file) {
        String out = this.engine.deleteAllFiles(file);
        this.engine = new Engine();
        return out;
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
    public ArrayList<CorpusDocument> runQueryOnEngine(String query, boolean stemmerStatus, HashSet<String> cities) {
        return this.engine.runQuery(query,stemmerStatus,cities);
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

    @Override
    public ObservableList<String> getEntitiesName(LinkedList<Integer> entitiesID) {

        ObservableList<String> answer = this.engine.getEntitiesNames(entitiesID);

        return answer;
    }

    @Override
    public void createResultFileForQueries(String queryFilePath, String resultsFilePath, boolean stemmerStatus, HashSet<String> cities){
        engine.createResultFileForQueries(queryFilePath,resultsFilePath,stemmerStatus,cities);

    }
}
