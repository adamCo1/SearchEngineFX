package Indexer;

import Engine.Data;
import Engine.Engine;
import Engine.Parser;
import Engine.Stemmer;
import IO.PostingReader;
import IO.PostingWriter;
import Structures.Pair;

import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * implement a variation of the SPIMI algorithm for building posting lists
 */

public class SpimiInverter {

    private String targetPath ;
    private boolean wroteBuffer , stemOn;
    private HashSet<String> titleSet;
    private int onTitle;
    private Stemmer porterStemmer;
    private int key , maxTF;
    private Engine engine ;
    private Parser parser;
    private VariableByteCode vb;
    private HashMap<Integer, Pair<Integer,Byte>> termToPostingMap;
    private HashMap<Integer,HashMap<Integer, Data>> termInfoMap ;
    private HashMap<Integer,String> idTermMap; // ID - TERM map
    //private HashMap<String,Integer> termIdMap;
    private HashMap<String,Pair<Integer,Integer>> termIdMap; //corupusTF at first value , id at second value
    private ArrayList<String> postingPaths;
    private PostingWriter writer ;
    private byte[] mainBuffer = new byte[4096];

    public SpimiInverter(HashMap termIdMap , HashMap idTermMap) {

        titleSet = new HashSet<>();
        porterStemmer = new Stemmer();
        this.termIdMap = termIdMap;
        this.idTermMap = idTermMap;
        this.termInfoMap = new HashMap<>();
        this.vb = new VariableByteCode();
        this.postingPaths = new ArrayList<>();
        this.termToPostingMap = new HashMap<>();
        this.writer = new PostingWriter();
        this.key = 1;
        this.onTitle = 2;//2 means on the title , 1 means not on the title
    }

    public SpimiInverter(Parser parser, VariableByteCode vb,Engine engine) {
        this.parser = parser;
        this.vb = vb;
        this.engine = engine;
        this.key = 1;

    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public void index(int maxSize) {
        //get relevant maps from engine
        int docID = 1 , maxTF = 0 ;
        int docsin = 0;
        try {
            int currentSize = 0, pathIndicator = 0;
            String path = getCanonicalPath()+"p" + pathIndicator , docpath = getCanonicalPath()+"d" + pathIndicator++;
            postingPaths.add(path);
            ArrayList<String> tempBuffer;
            HashMap<Integer, BufferData> buffer = new HashMap<>();
            HashMap<Integer, BufferDataDoc> docBuffer = new HashMap<>();
            int times = 0;
            while (!this.parser.isDone()) {//while parser still parsing , keep taking buffers and index them
                tempBuffer = parser.getBuffer();
                if(tempBuffer == null)
                    break;
                if (currentSize++ > 20000) {
                    writePostingList(buffer,path);
                    writeDocumentsPostingList(docBuffer,docpath);
                    buffer = new HashMap<>();
                    docBuffer = new HashMap<>();
                    path = getCanonicalPath()+"p" + pathIndicator++;
                    this.postingPaths.add(path);
                    currentSize = 0;

                } else {//its not too big yet no need to write it . add the temp buffer to the current held buffer
                    if(this.onTitle == 1) {
                        buildIndexOnTerms(tempBuffer,docID);
                        addToBuffer(buffer, this.termInfoMap);
                        //build the docs index
                        addToBuffer(docBuffer,docID,this.maxTF,getUniquieNumberOfTerms(tempBuffer));
                        maxTF = 0 ;
                    }else//initialize the title list to check with the terms
                        initTitleList(tempBuffer);
                        this.termInfoMap = new HashMap<>();
                }
                if(onTitle == 2)
                    onTitle = 1 ;
                else if(onTitle == 1){
                    onTitle = 2;
                    docID++;
               }

            }
            if(buffer.size() != 0)
                writePostingList(buffer,path);

          //  System.out.println(termIdMap);
            System.out.println("number of terms : " + this.idTermMap.size());
            System.out.println("merging the temp postings");
            writeMergedSortedPostings();

        } catch (Exception e) {
            System.out.println("at index");
            e.printStackTrace();
        }
    }

    private String getCanonicalPath(){
        return this.targetPath+"\\";
    }

    /**
     * add a buffer of documents data to the doc buffer
     * @param buffer
     * @param docID
     * @param maxTF
     * @param uniqueNum
     */
    private void addToBuffer(HashMap<Integer, BufferDataDoc> buffer , int docID , int maxTF , int uniqueNum){

        buffer.put(docID,new BufferDataDoc(encodeNumber(maxTF) , encodeNumber(uniqueNum)));

    }

    /**
     * get the unique number of terms in a document
     */
    private int getUniquieNumberOfTerms(ArrayList<String> list) {

        HashSet<String> temp = new HashSet<>();
        temp.addAll(list);
        return temp.size();
    }

    /**
     * merge the temporary postings
     */
    private void writeMergedSortedPostings(){
        PostingBufferMerger merger = new PostingBufferMerger(this.vb,this,this.postingPaths,this.targetPath);
        merger.mergeOnTermID(4096);
    }

    private void initTitleList(ArrayList<String> titleList){

        this.titleSet = new HashSet<>();

        for (String term :
                titleList) {
            try {
                if (isOneWord(term))
                    term = porterStemmer.stripAffixes(term);
            }catch (Exception e){
                System.out.println("error with : " + term);
            }
            this.titleSet.add(term);
        }
    }

    private boolean isOneWord(String word){

        if(word.equals(""))
            return false;

        if(word.indexOf(' ') != -1)
            return false;

        return true;
    }

    private void buildIndexOnTerms(ArrayList<String> termList , int docID) {


        int position = 1;

        for (String term :
                termList) {
            try {
                //this term is no special , check wether it starts with upper or lower

                if (isOneWord(term)) {
                    if (term.charAt(0) >= 64 && term.charAt(0) < 90) {
                        //so its capital , check if we saw it with small letters
                        if (checkExistInDicWithSmallLetters(stem(term))) {
                            //so store it with letter case
                            addTermToDicts(stem(term), position++, docID);
                        } else {
                            //not seen yet so store with capitals
                            addTermToDicts(term.toUpperCase(), position++, docID);
                        }
                    } else {
                        //its lower check if it stored with capitals
                        if (checkExistInDicWithCapitalLetters(term)) {
                            //then fix and replace with small letters
                            replaceUpperWithLower(term);
                            //and then add to the dict
                            addTermToDicts(stem(term), position++, docID);
                            continue;
                        } else {
                            //not seen yet store with lower case

                            addTermToDicts(stem(term), position++, docID);
                            continue;
                        }
                    }

                } else
                    addTermToDicts(term, position++, docID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean onlyLetters(String word){

        char c;
        int i = 0;

        while(i < word.length()){
            c = word.charAt(i++);
            if(c < 57 && c >= 47)
                return false;
        }

        return true;
    }

    /**
     * add encoded data to the main buffer .
     * we leave the Integer key not coded so we can add more encoded data to it easily
     *
     * @param buffer
     * @param tempBuffer
     */
    public void addToBuffer(HashMap<Integer, BufferData> buffer, HashMap<Integer, HashMap<Integer, Data>> tempBuffer) {

        byte[] encoded;
        Iterator iterator = tempBuffer.entrySet().iterator(), dataIterator;
        while (iterator.hasNext()) {
            Map.Entry tempBufferEntry = (Map.Entry) iterator.next();
            Integer currentTermID = (Integer) tempBufferEntry.getKey();
            dataIterator = ((HashMap) tempBufferEntry.getValue()).entrySet().iterator();

            while (dataIterator.hasNext()) {//add the data to the main buffer
                Map.Entry entry = (Map.Entry) dataIterator.next();
                Integer docID = (Integer) entry.getKey();
                LinkedList<Integer> info = ((Data) entry.getValue()).getPositionList();
                updateMaxTfForDocument(info.size());
                LinkedList<Integer> ontitle = ((Data)entry.getValue()).getOnTitle();
                LinkedList<Integer> docid = new LinkedList<>();
                docid.add(docID);
                byte[] encodedInfo = vb.encode(info);//encode only the info for now so we can add more info on other docs to this term in the buffer
                byte[] encodedDocID = vb.encode(docid);
                byte[] encodedOnTitle = vb.encode(ontitle);

                try {
                    buffer.get(currentTermID).addInfo(encodedDocID,encodedOnTitle, encodedInfo);
                } catch (Exception e) {
                    buffer.put(currentTermID, new BufferData(currentTermID, encodedDocID,encodedOnTitle , encodedInfo));
                }
            }
        }
    }



    /**
     * update the max tf on a document if neede
     * @param size the size of the position list of a term in the document
     */
    private void updateMaxTfForDocument(int size){

        if(this.maxTF < size)
            this.maxTF = size;
    }

    private String stem(String word){
        if(this.stemOn)
            return this.porterStemmer.stripAffixes(word);
        else
            return word;
    }

    private void addTermToDicts(String term , int position , int docID) {

        try {
            int id = (Integer)this.termIdMap.get(term).getSecondValue();
            try {//add another position to the term and increment the total tf by 1
                Data temp = this.termInfoMap.get(id).get(docID);
                Integer last = temp.getLastPosition();
                temp.addPosition(position-last);
                Integer tf = (Integer)this.termIdMap.get(id).getFirstValue();
                this.termIdMap.get(id).setFirstValue(tf+1);

            } catch (Exception e) {//
                  this.termInfoMap.put(new Integer(id), new HashMap<Integer, Data>() {{
                    if(!titleSet.contains(term))//so not on title
                        put(new Integer(docID), new Data(position));
                    else
                        put(new Integer(docID) , new Data(position,(byte)2));
                }});
            }
            Pair pair = this.termToPostingMap.get(id);
            pair.setFirstValue((Integer)pair.getFirstValue()+1);//increment tf by 1
            Pair<Integer,Integer> p = this.termIdMap.get(term);
            p.setFirstValue(p.getFirstValue()+1);

        } catch (Exception e) {
          //  this.termIdMap.put(term,this.key);
            this.termIdMap.put(term,new Pair<Integer,Integer>(1,this.key));
            this.idTermMap.put(this.key, term);
            if(!titleSet.contains(term))//so not on title
            this.termInfoMap.put(new Integer(this.key), new HashMap<Integer, Data>() {{
                put(new Integer(docID), new Data(position));
            }});
            else
                this.termInfoMap.put(new Integer(this.key),new HashMap<Integer, Data>(){{
                    put(new Integer(docID),new Data(position,(byte)2));
                }});

            this.termToPostingMap.put(key,new Pair<Integer, Byte>(1,(byte)-1));//add to the postings map
            this.key += 1;//advance the next term id
        }
    }

    /**
     * for each row , read the term id , then the length left for its data.
     *
     * @param path
     */
    public void readPostingListl(String path) {

        try {
            PostingReader reader = new PostingReader(new FileInputStream(path), vb);
            LinkedList<Integer> line = reader.readLine();
            while (!reader.isDone()) {
                System.out.println(line);
                line = reader.readLine();
            }


        } catch (Exception e) {
            System.out.println("at readPosting");
            e.printStackTrace();
        }

    }

    private byte[] encodeNumber(int number) {

        return vb.encode(new LinkedList<Integer>() {{
            add(number);
        }});
    }

    private int moveDataToMainBuffer( byte[] data , int currentInMain) throws Exception{
        int idx = 0 ;
        int temp = currentInMain ;
        while(idx < data.length){//move to the main buffer
            if(temp >= mainBuffer.length) {//so its full. write it to disk
                this.writer.write(mainBuffer);
                temp = 0;
                mainBuffer = new byte[4096];
                wroteBuffer = true;
            }

            mainBuffer[temp++] = data[idx++];
        }
       // System.out.println("moved steps : " + (currentInMain-temp));
        return temp;
    }


    private void writeDocumentsPostingList(HashMap<Integer, BufferDataDoc> buffer , String path){

        try{

            this.writer.setPath(path);
            byte[] mainBuffer = new byte[4096] , zero = new byte[]{0};//4KB
            int currentPosition = 0 , index = 0  ;
            Set<Integer> set = buffer.keySet();
            Integer[] sorted =  set.stream().toArray(Integer[]::new);
            Arrays.sort(sorted);

            while(index < sorted.length) {
                BufferDataDoc temp = buffer.get(sorted[index]);
                currentPosition = moveDataToMainBuffer(temp.getMaxTF(),currentPosition);
                currentPosition = moveDataToMainBuffer(temp.getUniqueNumber(),currentPosition);
                currentPosition = moveDataToMainBuffer(zero,currentPosition);

                index ++;
            }
            this.writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * ************WRITING FORMAT **********************
     * we write in the following format :
     * -termID1- -tf- -DOC-ID1- -tf1- -on-title- -info1- 0 ....... -DOC-IDN- -tfn- -ontitle- -INFON-
     * 00
     *
     * ******** the term TF in the entire corpus will be written in the merge process , because only then we
     * can know its value *********************
     *
     * termID : encoded in VB . he first negative byte represents the end of the id
     *
     * 0 : will be used as a separator from one doc-id and its info to another
     *
     * 00 : will be used as a delimiter between terms
     *
     * DOC : same as termID.
     *
     * ON-TITLE : whether a term is on this doc id title
     *
     * INFO : the information on the term .
     *
     * *************************************************
     *
     * write each termID to a line with all its needed information
     * write a buffer to the disk
     *
     *
     *               Value - BufferData object , which holds for each docID the term was found at , its information list on the term
     */
    public void writePostingList(HashMap<Integer, BufferData> buffer, String path) {


        int index = 0, blocknum = 0, idxtemp;
       // byte[] mainBuffer = new byte[4096]; //4KB - ALU size in windows
        byte[] zero = new byte[]{0} , doublezero = new byte[]{0,0};
        mainBuffer = new byte[4096];
        Set<Integer> keyset = buffer.keySet();
        Integer[] sorted =  keyset.stream().toArray(Integer[]::new);
        Arrays.sort(sorted);
     //   System.out.println(Arrays.toString(sorted));
        int i = 0;
       /// Iterator entriesIterator = buffer.entrySet().iterator();
        try {
            this.writer.setPath(path);

            while(i < sorted.length){
                if (index == mainBuffer.length) {//write and reset index
                    //this.writer.write(mainBuffer);
                    writer.write(mainBuffer);
                    this.mainBuffer = new byte[4096];
                    index = 0;
                }
                wroteBuffer = false;
                //still need to encode the keys . encode them and move them to the main buffer

                Integer currentTermID = sorted[i];
                index = moveDataToMainBuffer(encodeNumber(currentTermID),index);//moved the termID
                BufferData info = buffer.get(currentTermID);
                //so we can always know where the next termID starts
         //       index = moveDataToMainBuffer(mainBuffer,encodeNumber(info.getDataSize()),index);

                while(info.hasMore()) {

                    byte[] docid = info.getInfo();
                    byte[] onTitle = info.getInfo();
                    byte[] infoOnDocID = info.getInfo();//position list . the length of this list is
                    //the tf of this term on the current document.
                    List<Integer> tf = vb.decode(infoOnDocID);
                    byte[] docTF = encodeNumber(tf.size());

                    index = moveDataToMainBuffer( docid, index);
                    index = moveDataToMainBuffer(docTF,index);
                    index = moveDataToMainBuffer( onTitle, index);
                    index = moveDataToMainBuffer( infoOnDocID, index);
                    index = moveDataToMainBuffer( zero,index);//a new docID will start after this 0
                }

                index = moveDataToMainBuffer(doublezero,index);
                i++;
            }//end while

            if(!wroteBuffer) {
                //this.writer.write(mainBuffer);
                writer.write(mainBuffer);
                this.mainBuffer = new byte[4096];
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("at writing posting");
            e.printStackTrace();
        }

    }


    private boolean checkExistInDicWithSmallLetters(String term){

        try{
            String dictTerm = term.toLowerCase();
            Pair pair = this.termIdMap.get(dictTerm);
            if(pair != null) {
//                System.out.println("should replace : " + this.termIdMap.get(dictTerm));
                return true;
            }
            return false;


        }catch (Exception e){
            return false;
        }

    }


    private boolean checkExistInDicWithCapitalLetters(String term){

        try{
            String dictTerm = term.toUpperCase();
            Pair pair = this.termIdMap.get(dictTerm);
            if(pair == null) {
                return false;
            }
            return true;


        }catch (Exception e){
            return false;
        }

    }

    private void replaceUpperWithLower(String term){
        try {
            String upperTerm = term.toUpperCase();
            Pair pair = this.termIdMap.get(upperTerm);
            if (pair != null) {
                Integer termid = (Integer)pair.getSecondValue();
                // Integer termid = (Integer) this.termIdMap.get(term).getSecondValue();
                Integer tf = (Integer) pair.getFirstValue();
                this.idTermMap.replace(termid, term.toLowerCase());
                this.termIdMap.remove(upperTerm);
                //this.termIdMap.put(upperTerm,termid);
                this.termIdMap.put(term, new Pair<Integer, Integer>(tf, termid));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getTermTF(int id) {

        try {
            String term = this.idTermMap.get(id);
            return this.termIdMap.get(term).getFirstValue();
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }


    public void setTargetPath(String path){
        this.targetPath = path;
    }

    public void setStemOn(boolean stemStatus){
        this.stemOn = stemStatus;
    }

}
