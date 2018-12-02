package Engine;

import java.io.IOException;
import java.util.HashMap;
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
            this.spimi = new SpimiInverter(termIdMap, idTermMap,stemmerOn);
            this.docController = new DocController();
            this.reader = new ReadFile(docController);
            try {
                this.parser = new Parser(corpusPath);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        public Engine(SpimiInverter spimi , Parser parser){
            this.parser = parser;
            this.spimi = spimi;
            this.docController = new DocController();
            this.reader = new ReadFile(docController);

        }

        public Engine(Parser parser){
            this.parser = parser;
        }

        public void run() throws Exception{

            int maxsize = 40*1000;
            String stemmerStatus = "OFF";
            if(stemmerOn)
                stemmerStatus = "ON";

            this.spimi.setParser(this.parser);

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

            System.out.println("Total time in seconds : " +((System.nanoTime()-t1)/(1000*1000*1000)));
            System.out.println("Number of unique terms found : " + this.termIdMap.size());
            System.out.println("Stemmer status : " + stemmerStatus );
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
        public HashMap<String, Integer> getSortedTermMap(){
            return this.termIdMap;
        }

        public void setParser(Parser parser){
            this.parser = parser;
        }
        /**
         * convert a string to an int . replaces the Integer.parseInt method because its super slow
         * @param number string
         * @return the number it represents
         */
        public int stringToInt(String number){
            int i = 0 , ans = 0 , len = number.length();
            boolean isNeg = false;

            //check if negative
            if (number.charAt(0) == '-') {
                isNeg = true;
                i = 1;
            }
            while( i < len) {
                ans *= 10;
                ans += number.charAt(i++) - '0'; //Minus the ASCII code of '0' to get the value of the charAt(i++).
            }

            if (isNeg)
                ans = -ans;

            return ans;
        }

        public void setCorpusPath(String path){
            this.corpusPath = path;
        }

    }

