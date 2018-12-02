package Engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import Structures.Doc;
import IO.ReadFile;
import Indexer.SpimiInverter;

    /**
     * Class for the engine itself . holds all the parts needed for a search engine to work
     */

    public class Engine {

        private String corpusPath ;
        private boolean stemmerOn ;
        private HashMap<Integer,String> idTermMap; // ID - TERM map
        private HashMap<String,Integer> termIdMap;
        private Parser parser ;
        private SpimiInverter spimi;
        private ReadFile reader;
        private DocController docController;
        private Thread readThread , indexThread , parseThread;

        public Engine(boolean stemmerOn , String corpusPath) {
            this.corpusPath = corpusPath;
            this.termIdMap = new HashMap<>();
            this.idTermMap = new HashMap<>();
            this.stemmerOn = stemmerOn;
            this.spimi = new SpimiInverter(termIdMap, idTermMap);
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
            this.spimi = new SpimiInverter(idTermMap, termIdMap);
            this.parser = new Parser();
        }

        public Engine(Parser parser){
            this.parser = parser;
        }

        public void run(String corpusPath,String targetPath,boolean stemerStatus) {

            int maxsize = 40 * 1000;

            this.parser.initializeStopWordsTree(corpusPath);
            this.spimi.setStemOn(stemerStatus);
            //initialize target path for spimi

            String stemmerStatus = "OFF";
            if (stemmerOn)
                stemmerStatus = "ON";

            this.spimi.setParser(this.parser);

            try {

                readThread = new Thread(() -> this.reader.read(this.corpusPath));
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

                System.out.println("Total time in seconds : " + ((System.nanoTime() - t1) / (1000 * 1000 * 1000)));
                System.out.println("Number of unique terms found : " + this.termIdMap.size());
                System.out.println("Stemmer status : " + stemmerStatus);
            }catch (Exception e){
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

    }

