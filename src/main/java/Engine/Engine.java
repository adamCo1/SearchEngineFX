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
        private TreeMap<String,Integer[]> termIdTreeMap;
        private HashMap<String, Integer[]> termIdMap;// 0 - TF , 1 - ID , 2 - blockNumber , 3-index in block , 4 - out path id to be used with the out paths dictionary
        private IParser parser ;
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

        public Engine(SpimiInverter spimi , IParser parser){
            this.parser = parser;
            this.spimi = spimi;
            this.docController = new DocController();
            this.reader = new ReadFile(docController);


        }

        public Engine(){
            this.parser = new Parser(corpusPath);
            this.termIdMap = new HashMap<>();
            this.idTermMap = new HashMap<>();
            this.docController = new DocController();
            this.reader = new ReadFile(docController);
            this.spimi = new SpimiInverter(termIdMap, idTermMap);
        }


        public void loadDictionaryToMemory() throws Exception{


            FileInputStream in = new FileInputStream(targetPath+"\\"+TERM_ID_MAP_PATH);
            ObjectInputStream stream = new ObjectInputStream(in);
            TreeMap dict = (TreeMap)stream.readObject();
            this.termIdTreeMap = dict;
            stream.close();
            in.close();
        }


        public TreeMap sampleRun(String text , boolean stemmerOn){

            int maxsize = 40*1000;
            this.targetPath = "D:\\sample";
            this.parser.initializeStopWordsTree("C:\\Users\\adam\\Corpus2");
            this.spimi.setStemOn(stemmerOn);
            this.spimi.setTargetPath(this.targetPath);
            this.spimi.setParser(this.parser);

            String status = "OFF";
            if (stemmerOn)
                status = "ON";

            try {

                indexThread = new Thread(() -> this.spimi.index(maxsize));
                parseThread = new Thread(() -> parser.parse(text));
                System.out.println("Starting Sample");
                long t1 = System.nanoTime();

                indexThread.start();
                parseThread.start();
                parseThread.join();
                Thread.sleep(100);
                this.parser.setDone(true);
                indexThread.join();

               convertTermIdToTreeMap();
               // storeDictionariesOnDisk();

                System.out.println("Total time in seconds : " + ((System.nanoTime() - t1) / (1000 * 1000 * 1000)));
                System.out.println("Number of unique terms found : " + this.termIdMap.size());
                System.out.println("Stemmer status : " + status);

            }catch (Exception e){
                e.printStackTrace();
            }
            return this.termIdTreeMap;
        }

        public void run(boolean stemmerStatus) {

            int maxsize = 40 * 1000;
            this.spimi.setParser(this.parser);
            this.spimi.setStemOn(stemmerStatus);
            this.spimi.setTargetPath(this.targetPath);
            //initialize target path for spimi

            String status = "OFF";
            if (stemmerStatus)
                status = "ON";

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

            return this.termIdTreeMap;
        }

        private void storeDictionariesOnDisk(){
            try{
                FileOutputStream out = new FileOutputStream(targetPath+"\\"+TERM_ID_MAP_PATH);
                ObjectOutputStream stream = new ObjectOutputStream(out);
                stream.writeObject(this.termIdTreeMap);
                out.close();
                stream.close();

                out = new FileOutputStream(targetPath+"\\"+ID_TERM_MAP_PATH);
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
                Integer[] data =(Integer[]) entry.getValue();
                this.termIdTreeMap.put((String)entry.getKey(),new Integer[]{data[0],data[1],data[2],data[3],data[4]});
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

