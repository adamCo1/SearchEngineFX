package Engine;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import Structures.Doc;
import IO.ReadFile;
import Indexer.SpimiInverter;

    /**
     * Class for the engine itself . holds all the parts needed for a search engine to work
     */

    public class Engine {

        private final String TERM_ID_MAP_PATH = "term_id.data" , ID_TERM_MAP_PATH = "id_term.data";
        private String corpusPath , targetPath ;
        private boolean stemmerOn ;
        private HashMap<Integer,String> idTermMap; // ID - TERM map
        private TreeMap<String,Integer> termIdTreeMap;
        private HashMap<String,Integer> termIdMap;
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

        public void run(boolean stemerStatus) {

            int maxsize = 40 * 1000;

            this.parser.initializeStopWordsTree(corpusPath);
            this.spimi.setStemOn(stemerStatus);
            //initialize target path for spimi

            String stemmerStatus = "OFF";
            if (stemmerOn)
                stemmerStatus = "ON";

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
                System.out.println("Stemmer status : " + stemmerStatus);
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

        /**
         *
         * @return sorted term dictionary
         */
        public TreeMap<String, Integer> getSortedTermDictionary(){
            return null;
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
                this.termIdTreeMap.put((String)entry.getKey(),(Integer)entry.getValue());
            }
        }
    }

