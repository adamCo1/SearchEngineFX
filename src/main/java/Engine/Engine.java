package Engine;

import IO.ReadFile;
import Indexer.SpimiInverter;
import Structures.Doc;
import Structures.Pair;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
     * Class for the engine itself . holds all the parts needed for a search engine to work
     */

    public class Engine {

        private final String TERM_ID_MAP_PATH = "term_id.data" , ID_TERM_MAP_PATH = "id_term.data";
        private String corpusPath , targetPath ;
        private boolean stemmerOn ;
        private HashMap<Integer,String> idTermMap; // ID - TERM map
        private TreeMap<String,Pair<Integer,Integer>> termIdTreeMap;
        private HashMap<String, Pair<Integer,Integer>> termIdMap;
        private Parser parser ;
        private SpimiInverter spimi;
        private ReadFile reader;
        private DocController docController;
        private Thread readThread , indexThread , parseThread;

        public Engine(boolean stemmerOn , String corpusPath) {

            this.termIdMap = new HashMap<>();
            this.idTermMap = new HashMap<>();
            this.docController = new DocController();
            this.reader = new ReadFile(docController);
        }

        public Engine(SpimiInverter spimi , Parser parser){
            this.parser = parser;
            this.spimi = spimi;
            this.docController = new DocController();
            this.reader = new ReadFile(docController);

        }

        public Engine(){
            this.parser = new Parser();
            this.termIdMap = new HashMap<>();
            this.idTermMap = new HashMap<>();
            this.docController = new DocController();
            this.reader = new ReadFile(docController);
            this.spimi = new SpimiInverter(termIdMap, idTermMap);
        }

        public Engine(Parser parser){
            this.parser = parser;
        }

        public void run(boolean stemmerStatus) {

            int maxsize = 40 * 1000;

            this.parser.initializeStopWordsTree(corpusPath);
            this.spimi.setStemOn(stemmerStatus);
            this.spimi.setTargetPath(this.targetPath);
            //initialize target path for spimi

            String status = "OFF";
            if (stemmerStatus)
                status = "ON";

            this.spimi.setParser(this.parser);

            try {

                readThread = new Thread(() -> this.reader.read(corpusPath));
                indexThread = new Thread(() -> this.spimi.index(maxsize));
                parseThread = new Thread(() -> parse());
                System.out.println("Starting");
                long t1 = System.nanoTime();
                readThread.start();
                indexThread.start();
                parseThread.start();

                readThread.join();
                parseThread.join();
                indexThread.join();

                convertTermIdToTreeMap();
                storeDictionariesOnDisk();

                System.out.println("Total time in seconds : " + ((System.nanoTime() - t1) / (1000 * 1000 * 1000)));
                System.out.println("Number of unique terms found : " + this.termIdMap.size());
                System.out.println("Stemmer status : " + status);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public TreeMap getTermIDDictionary(){
            try{
                FileInputStream in = new FileInputStream(targetPath+"\\"+TERM_ID_MAP_PATH);
                ObjectInputStream stream = new ObjectInputStream(in);
                TreeMap dict = (TreeMap) stream.readObject();

                in.close();
                stream.close();
                return dict;
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        private void storeDictionariesOnDisk(){
            try{
                FileOutputStream out = new FileOutputStream(targetPath+"\\"+TERM_ID_MAP_PATH);
                ObjectOutputStream stream = new ObjectOutputStream(out);
                stream.writeObject(this.termIdTreeMap);
                out.close();
                stream.close();

                out = new FileOutputStream(ID_TERM_MAP_PATH);
                stream = new ObjectOutputStream(out);
                stream.writeObject(this.idTermMap);
                out.close();
                stream.close();

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        private void parse(){
            Doc temp = null;
            int id = 1;

            try {
                while (!this.reader.getDone()) {
                    temp = docController.takeDoc();
                    this.parser.parse(temp);
                    id++;
                }
                System.out.println("Total number of documents parsed and indexed : " + id);
                this.parser.setDone(true);
            }catch (Exception e){
                System.out.println(temp);
            }
        }

        public void setParser(Parser parser){
            this.parser = parser;
        }

        public void setCorpusPath(String path){
            this.corpusPath = path;
        }

        public void setTargetPath(String path){
            this.targetPath = path;
        }

        private void convertTermIdToTreeMap(){
            this.termIdTreeMap = new TreeMap<>();
            Iterator iterator = this.termIdMap.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry)iterator.next();
                this.termIdTreeMap.put((String)entry.getKey(),new Pair(((Pair)entry.getValue()).getFirstValue(),((Pair)entry.getValue()).getSecondValue()));
            }
        }

        public void deleteAllFiles(File file){
            try{
                FileUtils.cleanDirectory(file);

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

