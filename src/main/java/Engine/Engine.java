package Engine;

import IO.*;
import Indexer.IIndexer;
import Indexer.SpimiInverter;
import Parser.IParser;
import Parser.Parser;
import Ranking.ISearcher;
import Ranking.Ranker;
import Ranking.Searcher;
import Structures.CorpusDocument;
import Structures.Doc;
import Structures.Pair;
import Structures.PostingDataStructure;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.util.*;

/**
     * Class for the engine itself . holds all the parts needed for a search engine to work
     */

    public class Engine {

        private double avgDocLength ;
        private final String TERMS_OUT_CHAMPS = "terms_out_c" ,TERM_ID_MAP_PATH = "term_id.data" , ID_TERM_MAP_PATH = "id_term.data" , DOC_POSITIONS_OUT = "docs_positions.data" , TERMS_OUT = "terms_out" , DOCS_OUT = "docs_out", LANGS_OUT="langs_out.data";
        private final int MAX_SIZE_FOR_BUFFERS = 20480000;//20mb;
        private String corpusPath , targetPath ;
        private boolean stemmerOn ;
        private HashMap<Integer,String> idTermMap; // ID - TERM map
        private HashMap<Integer, Pair> docsPositions ;
        private HashMap<Integer,Integer> docLengths;
        private TreeMap<String, PostingDataStructure> termIdTreeMap;
        private HashMap<String, PostingDataStructure> termIdMap;// 0 - TF , 1 - ID , 2 - blockNumber , 3-index in block , 4 - out path id to be used with the out paths dictionary
        private IParser parser ;
        private IIndexer spimi;
        private IReader reader;
        private ISearcher searcher;
        private IBufferController controller;
      //  private Thread readThread , indexThread , parseThread;
        private int docCount;

        public Engine(boolean stemmerOn , String corpusPath) {

            this.termIdMap = new HashMap<>();
            this.idTermMap = new HashMap<>();
            this.controller = new DocController();
            this.reader = new ReadFile(controller);
            this.docLengths = new HashMap<>();
            this.docsPositions = new HashMap<>();
        }

        public Engine(IIndexer spimi , IParser parser , IReader reader , IBufferController controller){
            this.termIdMap = new HashMap<>();
            this.idTermMap = new HashMap<>();
            this.reader = reader;
            this.parser = parser;
            this.spimi = spimi;
            this.controller = controller;
            this.reader = new ReadFile(this.controller);
            this.docCount = 0;
            this.docLengths = new HashMap<>();
            this.docsPositions = new HashMap<>();
            spimi.setParser(parser);

        }



        public Engine(){

            this.docsPositions = new HashMap<>();
            this.docLengths = new HashMap<>();
            this.parser = new Parser(this.docLengths);
            this.termIdMap = new HashMap<>();
            this.idTermMap = new HashMap<>();
            this.controller = new DocController();
            this.reader = new ReadFile(controller);
            this.spimi = new SpimiInverter(docLengths,termIdMap, idTermMap , docsPositions, parser);
            this.searcher = new Searcher(parser,TERMS_OUT,DOCS_OUT,4096);
        }


        public String loadDictionaryToMemory() throws Exception{


            FileInputStream in = new FileInputStream(targetPath+"\\"+TERM_ID_MAP_PATH);
            ObjectInputStream stream = new ObjectInputStream(in);
            TreeMap dict = (TreeMap)stream.readObject();
            this.termIdTreeMap = dict;
            stream.close();
            in.close();

            in = new FileInputStream(targetPath+"\\"+DOC_POSITIONS_OUT);
            stream = new ObjectInputStream(in);
            HashMap docpos = (HashMap)stream.readObject();
            this.docsPositions = docpos;
            stream.close();
            in.close();

            in = new FileInputStream(targetPath+"\\"+LANGS_OUT);
            stream = new ObjectInputStream(in);
            TreeSet<String> langs = (TreeSet<String>) stream.readObject();
            ((Parser)parser).setDocLangs(langs);
            in.close();
            SemanticHandler.corpusPath = corpusPath;
            stream.close();
            in.close();

            preProcessVectorSpace();
            setDictionariesToSearcher();
            return "Dictionary loaded to memory.";
        }


        private void setDictionariesToSearcher(){
            this.searcher.setDictionaries(this.termIdTreeMap , this.docsPositions);
        }

        public TreeMap sampleRun(String text , boolean stemmerOn){

            int maxsize = 40*1000;
            this.targetPath = "D:\\sample";
            this.parser.initializeStopWordsTreeAndStrategies("C:\\Users\\adam\\Corpus2");
            this.spimi.setStemOn(stemmerOn);
            this.spimi.setTargetPath(this.targetPath);
            //this.spimi.setParser(this.parser);

            String status = "OFF";
            if (stemmerOn)
                status = "ON";

            try {

                Thread indexThread = new Thread(() -> this.spimi.index(maxsize));
                Thread parseThread = new Thread(() -> parser.parse(text));
                System.out.println("Starting Sample");
                long t1 = System.nanoTime();

                indexThread.start();
                parseThread.start();
                parseThread.join();
                Thread.sleep(1000);
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

        public String run(boolean stemmerStatus) {

            this.spimi.setParser(this.parser);
            this.spimi.setStemOn(stemmerStatus);
            this.searcher.setStemmerStatus(stemmerStatus);
            this.spimi.setTargetPath(this.targetPath);
            this.parser.initializeStopWordsTreeAndStrategies(corpusPath);
            SemanticHandler.corpusPath = corpusPath;
            //initialize target path for spimi

            String status = "OFF";
            if (stemmerStatus)
                status = "ON";

            try {

                Thread readThread = new Thread(() -> this.reader.read(corpusPath));
                Thread indexThread = new Thread(() -> this.spimi.index(MAX_SIZE_FOR_BUFFERS));
                Thread parseThread = new Thread(() -> parse());
                System.out.println("Starting");

                long t1 = System.nanoTime();
                readThread.start();
                indexThread.start();
                parseThread.start();

                readThread.join();
                parseThread.join();
                indexThread.join();

                /**
                 * convert the hashmap to a sorted map , store on disk and set the searcher
                 * dictionaries
                 */
                convertTermIdToTreeMap();
                storeDictionariesOnDisk();
                preProcessVectorSpace();
                setDictionariesToSearcher();

                String out = "";
                out += "Total time in seconds : " + ((System.nanoTime() - t1) / (1000 * 1000 * 1000))+"\n";
                out += "Total number of documents parsed and indexed : " + this.docCount+"\n";
                out += "Number of unique terms found : " + this.termIdMap.size()+"\n";
                out += "Stemmer status : " + status;
                return out;
            }catch (Exception e){
                e.printStackTrace();
                return "Error running engine";
            }
        }

        public TreeMap getTermIDDictionary(){

            return this.termIdTreeMap;
        }


    /**
     * store searcher's dictionaires
     */
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

                out = new FileOutputStream(targetPath+"\\"+DOC_POSITIONS_OUT);
                stream = new ObjectOutputStream(out);
                stream.writeObject(this.docsPositions);
                out.close();
                stream.close();

                out = new FileOutputStream(targetPath+"\\"+LANGS_OUT);
                stream = new ObjectOutputStream(out);
                stream.writeObject(((Parser)parser).getDocLangs());
                out.close();
                stream.close();

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        private void parse(){
            Doc temp = null;
            int id = 1 , len;

            try {
                while (!this.reader.getDone()) {
                    temp =(Doc) controller.getBuffer();
                    len = temp.getDocText().length();
                    temp.setEngineID(id++);
                    this.parser.parse(temp);
                    this.docCount++;
                }

                this.parser.setDone(true);
                calculateAVGlength();

            }catch (Exception e){
                System.out.println(temp);
            }
        }

        public void setParser(Parser parser){
            this.parser = parser;
        }

     private void calculateAVGlength(){

         Iterator iterator = this.docLengths.entrySet().iterator();
         while(iterator.hasNext()){
             Map.Entry entry =(Map.Entry) iterator.next();
             this.avgDocLength += (int)entry.getValue();
         }

         this.avgDocLength = this.avgDocLength / this.docLengths.size();
         this.docsPositions.put(-1,new Pair(0,avgDocLength));
     }

    /**
     * set path for reading from
     * @param path
     */
    public void setCorpusPath(String path){
            this.corpusPath = path;
        }

    /**
     * preprocess the vector sapce model for the corpus .
     * also choose the leaders and followers to the cluster pruning algorithm
      */
    private void preProcessVectorSpace(){

    }

    /**
     * set path for writing files to
     * @param path
     */
    public void setTargetPath(String path){
            this.targetPath = path;
        }


    /**
     * convert the hashmap to treemap and delete the hashmap from memory
     */
    private void convertTermIdToTreeMap(){
            this.termIdTreeMap = new TreeMap<>();
            Iterator iterator = this.termIdMap.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry)iterator.next();
                PostingDataStructure data =(PostingDataStructure) entry.getValue();
                this.termIdTreeMap.put((String)entry.getKey(),new PostingDataStructure((PostingDataStructure)entry.getValue()));
              //  this.termIdMap.remove(entry.getKey());//so no overflow in memory
            }
        }

    /**
     * initialize new maps and delete all the recent files in the
     * @param file
     */
    public String deleteAllFiles(File file){

            if(file == null || !file.exists())
                return "please choose a directory first.";

            this.controller = new DocController();
            this.idTermMap = new HashMap<>();
            this.termIdTreeMap = new TreeMap<>();
            this.termIdMap = new HashMap<>();
            this.reader.reset();
            this.parser.reset();
            this.spimi.reset();
            this.docCount = 0;

            try{
                FileUtils.cleanDirectory(file);
                Thread.sleep(2000);
            }catch (Exception e){
                e.printStackTrace();
            }
        return "All resources deleted and reseted ";
        }


        public int getDocLength(int docID){
            int len = this.docLengths.get(docID);
            this.docLengths.remove(docID);

            return len;
        }

        public ArrayList<CorpusDocument> runQuery(Query query , boolean stemmerStatus,HashSet<String> cities){
            this.parser.initializeStopWordsTreeAndStrategies(corpusPath);
            this.searcher.setStemmerStatus(stemmerStatus);
            this.searcher.setAttributes(targetPath+"\\"+TERMS_OUT,targetPath+"\\"+DOCS_OUT,(double)this.docsPositions.get(-1).getSecondValue());
            ArrayList<String>relatedWords = null;

            if(SemanticHandler.includeSemantics){
                if(corpusPath == null)
                    SemanticHandler.corpusPath = corpusPath;
                if(SemanticHandler.wordsVectors==null || SemanticHandler.wordsVectors.size() ==0)
                    SemanticHandler.readGloveFile();
                String [] origQwords = query.getQueryText().replace(",|.|'|\"|?|!|","").split(" ");
                ArrayList<String> queryInArrayList = new ArrayList<>();
                String queryRelatedWordsInString ="";
                //create the semantic handler output
                for(int i = 0 ; i < origQwords.length ; i++)
                    queryInArrayList.add(origQwords[i]);
                relatedWords = SemanticHandler.getRelatedWords(queryInArrayList);
                //chaining the related words to  a string
                for(int i = 0 ; i < relatedWords.size() ; i ++)
                    queryRelatedWordsInString+=relatedWords.get(i)+" ";

                //append the related words to the query text
                if(queryRelatedWordsInString.length() > 0)
                    query.setQueryText(query.getQueryText()+" "+queryRelatedWordsInString.substring(0,queryRelatedWordsInString.length()-1));
            }
            return this.searcher.analyzeAndRank(query.getQueryText()+" "+query.getQueryDesc(),cities);
        }


        public void createResultFileForQueries(String pathToQueriesFileDir,String pathToWriteToResultsFile,boolean stemmerStatus,HashSet<String> cities){
            ArrayList<Query> queries = ReadQueryFile.readQueries(pathToQueriesFileDir);
            //result tuple will be query_id, iter, docno, rank, sim, run_id
            ArrayList<String>results = new ArrayList<>();
            for(Query q:queries){
                ArrayList <CorpusDocument> currQueryBestDocMatches = runQuery(q,stemmerStatus,cities);
                for(CorpusDocument doc: currQueryBestDocMatches){
                    double docRank = doc.getRank();
                    results.add(""+q.getQueryNum()+" "+"0"+ " "+ doc.getName()+" "+docRank+" "+docRank+" "+"run_name");
                }
            }

            try {
                int index = 0 ;
                FileWriter fw = new FileWriter(pathToWriteToResultsFile+"\\results.txt");
                BufferedWriter bw = new BufferedWriter(fw);
                bw.flush();

                while(index < results.size()){
                    bw.write(results.get(index++));
                    bw.newLine();
                    bw.flush();
                }
                //for(String res:results){
                  //  bw.write(res+"\n");
                    //bw.newLine();
                //}

                bw.close();
                fw.close();
                //bw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        public void setRankingParameters(double k, double b, double weightK, double weightB, double weightBM, double weightPos, double weightTitle, double idfLower, double idfDelta){
            this.searcher.setRankingParameters(k,b,weightK,weightB,weightBM,weightPos,weightTitle,idfLower,idfDelta);
        }

        public void addDoclength(int docID, int len){
            this.docLengths.put(docID,len);
        }

        public HashMap getDocLengths(){
            return this.docLengths;
        }

        public int getDocCount(){return docCount;}

        public ObservableList<String> getEntitiesNames(LinkedList<Integer> entitiesIDs){

            ObservableList<String> ans = FXCollections.observableArrayList();
            int index = 0 ;

            for (Integer id:
                 entitiesIDs) {
                ans.add(this.idTermMap.get(id) + "   rank : " + index++);
            }

            return ans;
        }

        public TreeSet<String> getDocsLang(){
            return ((Parser)this.parser).getDocLangs();
        }
    }

