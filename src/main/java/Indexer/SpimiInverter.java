package Indexer;

import Engine.IParser;
import Engine.Data;
import Engine.Engine;
import Engine.Parser;
import Engine.Stemmer;
import IO.PostingReader;
import IO.PostingWriter;
import ReadFromWeb.City;
import Structures.Pair;
import jdk.nashorn.internal.runtime.ECMAException;
import static ReadFromWeb.ReadFromWeb.allCities;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private IParser parser;
    private VariableByteCode vb;
    private HashMap<Integer,ABufferData> cityBuffer;
    private HashMap<Integer,HashMap<Integer, Data>> termInfoMap ;
    private HashMap<Integer,String> idTermMap; // ID - TERM map
    private HashMap<String,Integer[]> termIdMap; //corupusTF at first value , id at second value
    private ArrayList<String> postingPaths , cityPaths , docPaths;
    private PostingWriter writer ;
    private byte[] mainBuffer = new byte[4096];

    public SpimiInverter(HashMap termIdMap , HashMap idTermMap) {

        titleSet = new HashSet<>();
        porterStemmer = new Stemmer();
        this.cityPaths = new ArrayList<>();
        this.docPaths = new ArrayList<>();
        this.cityBuffer = new HashMap<>();
        this.termIdMap = termIdMap;
        this.idTermMap = idTermMap;
        this.termInfoMap = new HashMap<>();
        this.vb = new VariableByteCode();
        this.postingPaths = new ArrayList<>();
        this.writer = new PostingWriter();
        this.key = 1;
        this.onTitle = 2;//2 means on the title , 1 means not on the title
    }

    public SpimiInverter(IParser parser, VariableByteCode vb, Engine engine) {
        this.parser = parser;
        this.vb = vb;
        this.key = 1;

    }

    public void setParser(IParser parser) {
        this.parser = parser;
    }

    public void index(int maxSize) {
        //get relevant maps from engine
        int docID = 1 , maxTF = 0 ;
        int docsin = 0;
        try {
            int currentSize = 0, pathIndicator = 0 , cityIncicator = 0;
            String path = getCanonicalPath()+"p" + pathIndicator , docpath = getCanonicalPath()+"d" + pathIndicator++;
            String cityPath = getCanonicalPath()+"c"+cityIncicator++;
            postingPaths.add(path);
            cityPaths.add(cityPath);
            //cityPaths.add(cityPath);
            ArrayList<String> tempBuffer;
            HashMap<Integer, ABufferData> buffer = new HashMap<>();
            HashMap<Integer, BufferDataDoc> docBuffer = new HashMap<>();
            int times = 0;
            while (!this.parser.isDone()) {//while parser still parsing , keep taking buffers and index them
                tempBuffer = parser.getBuffer();
                if(tempBuffer == null)
                    break;
                if (currentSize++ > 40000) {
                    writePostingList(buffer,path);
                    writeDocumentsPostingList(docBuffer,docpath);
                    writeCityBufferToPostingList(cityPath);

                    buffer = new HashMap<>();
                    docBuffer = new HashMap<>();
                    path = getCanonicalPath()+"p" + pathIndicator++;
                    cityPath = getCanonicalPath()+"c"+cityIncicator++;
                    this.postingPaths.add(path);
                    this.cityPaths.add(cityPath);
                    this.cityBuffer = new HashMap<>();
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

            //Files.deleteIfExists(Paths.get(postingPaths.get(0)));

            cityPaths.remove(cityPaths.size()-1);
            System.out.println("number of terms : " + this.idTermMap.size());
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
        PostingBufferMerger merger = new PostingBufferMerger(this.termIdMap ,this.vb,this,this.postingPaths,this.docPaths,this.cityPaths,this.targetPath);
        merger.mergeOnTermID(this.postingPaths,4096,"TERMS");
        merger.mergeOnTermID(this.cityPaths,4096,"CITY");

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
                    String proccessedTerm=term;
                    if (term.charAt(0) >= 65 && term.charAt(0) <= 90) {
                        //so its capital , check if we saw it with small letters

                        if(stemOn)
                            proccessedTerm=stem(term);

                        if (checkExistInDicWithSmallLetters(proccessedTerm)) {
                            //so store it with letter case
                            addTermToDicts(proccessedTerm, position++, docID);
                        } else {
                            //not seen yet so store with capitals
                            addTermToDicts(term.toUpperCase(), position++, docID);
                        }
                    }else{
                        if(onlyLetters(term) && checkExistInDicWithCapitalLetters(term.toUpperCase())){
                            if(stemOn)
                                proccessedTerm=stem(term);
                            //then fix and replace with small letters
                            replaceUpperWithLower(term);
                            //and then add to the dict
                            addTermToDicts(stem(term).toLowerCase(), position++, docID);
                            continue;
                        } else {
                            //not seen yet store with lower case

                            addTermToDicts(stem(term), position++, docID);
                            continue;
                        }

                    }
                } else
                    addTermToDicts(term.toLowerCase(), position++, docID);


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
            if(c < 58 && c >= 48)
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
    public void addToBuffer(HashMap<Integer, ABufferData> buffer, HashMap<Integer, HashMap<Integer, Data>> tempBuffer) {

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
                LinkedList<Integer> ontitle = ((Data) entry.getValue()).getOnTitle();
                LinkedList<Integer> docid = new LinkedList<>();
                docid.add(docID);
                byte[] encodedInfo = vb.encode(info);//encode only the info for now so we can add more info on other docs to this term in the buffer
                byte[] encodedDocID = vb.encode(docid);
                byte[] encodedOnTitle = vb.encode(ontitle);

                String term = this.idTermMap.get(currentTermID);

                if (allCities.get(term) != null && allCities.get(term).getName().equals(term) ) {//term info need to be added to the city buffer
                    try {
                        ((BufferDataString) this.cityBuffer.get(currentTermID)).addInfo(encodedDocID, encodedOnTitle, encodedInfo);
                    } catch (Exception e) {
                        City city = allCities.get(term);//get the city object
                        this.cityBuffer.put(currentTermID, new BufferDataString(currentTermID, encodedDocID
                                , encodedOnTitle, encodedInfo, city.getName(), city.getCountry(), city.getCurrency(), city.getPopulation()));
                    }
                }

                try {//regular term add
                    ((BufferDataByte) buffer.get(currentTermID)).addInfo(encodedDocID, encodedOnTitle, encodedInfo);
                } catch (Exception e) {
                    buffer.put(currentTermID, new BufferDataByte(currentTermID, encodedDocID, encodedOnTitle, encodedInfo));
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

    /**
     * stem a given string
     * @param word
     * @return the stemmed form of the given string
     */
    private String stem(String word){
        if(this.stemOn)
            return this.porterStemmer.stripAffixes(word);
        else
            return word;
    }

    /**
     * add a term to the dictionaries
     * @param term term to add
     * @param position the position in the document
     * @param docID the id of the document
     */
    private void addTermToDicts(String term , int position , int docID) {

        try {
            int id = (Integer)this.termIdMap.get(term)[1];
            try {//add another position to the term and increment the total tf by 1
                Data temp = this.termInfoMap.get(id).get(docID);
                Integer last = temp.getLastPosition();
                temp.addPosition(position-last);
                Integer tf = (Integer)this.termIdMap.get(term)[0];
               // this.termIdMap.get(term)[0] = tf+1;

            } catch (Exception e) {//so no data. maybe this happended cuz of same hash value . fix
                //this by looking for contains which overrides equals . this will take more time but
                //there wont be any mistakes and it wont happen much
                    this.termInfoMap.put(new Integer(id), new HashMap<Integer, Data>() {{
                        if (!titleSet.contains(term))//so not on title
                            put(new Integer(docID), new Data(position));
                        else
                            put(new Integer(docID), new Data(position, (byte) 2));
                    }});
            }
            //Pair pair = this.termToPostingMap.get(id);
            //pair.setFirstValue((Integer)pair.getFirstValue()+1);
            // increment tf by 1
            this.termIdMap.get(term)[0] = this.termIdMap.get(term)[0]+1 ;//

        } catch (Exception e) {
          //  this.termIdMap.put(term,this.key);
            this.termIdMap.put(term,new Integer[]{1,this.key,0,0,0});
            this.idTermMap.put(this.key, term);
            if(!titleSet.contains(term))//so not on title
            this.termInfoMap.put(new Integer(this.key), new HashMap<Integer, Data>() {{
                put(new Integer(docID), new Data(position));
            }});
            else
                this.termInfoMap.put(new Integer(this.key),new HashMap<Integer, Data>(){{
                    put(new Integer(docID),new Data(position,(byte)2));
                }});

            //this.termToPostingMap.put(key,new Pair<Integer, Byte>(1,(byte)-1));//add to the postings map
            this.key += 1;//advance the next term id
        }
    }

    /**
     * encode strings representing info on cities
     * @param word the info to encode
     * @return array of bytes representing the encoded sting
     */
    private byte[] encodeCityInfo(String word){

        byte[] ans = new byte[word.length()];

        for(int i = 0 ; i < word.length();i++){
            ans[i] = encodeNumber(word.charAt(i))[0];
        }

        return ans;
    }

    /**
     * *********************WRITING FORMAT*******************
     * -termID- -tf- -docID- -ontitle- -infoondoc- 0 ...... 00 ... -termID- ... AT THE END : 4 strings
     * representing the data required of us on the requirements .
     *
     * -termID- id of the term
     * -docID- id of the doc
     * -ontitle- whether on title or not
     * -info- all the info saved on the term
     * -tf- term freq on doc
     *
     * @param path
     */
    private void writeCityBufferToPostingList(String path){

        if(path.charAt(path.length()-3) == 'c' && path.charAt(path.length()-2) == '2' &&
        path.charAt(path.length()-1) == '3')
            System.out.println("path = [" + path + "]");

        int index = 0, blocknum = 0, idxtemp;
        // byte[] mainBuffer = new byte[4096]; //4KB - ALU size in windows
        byte[] zero = new byte[]{0} , doublezero = new byte[]{0,0};
        mainBuffer = new byte[4096];
        Set<Integer> keyset = cityBuffer.keySet();
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
                BufferDataString info = (BufferDataString)cityBuffer.get(currentTermID);
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
                    index = moveDataToMainBuffer( infoOnDocID, index);
                    index = moveDataToMainBuffer( zero,index);//a new docID will start after this 0
                }

                byte[] encodedName = encodeCityInfo(info.getFullName());
                byte[] encodedCountry = encodeCityInfo(info.getCountryName());
                byte[] encodedPop = encodeCityInfo(info.getPopulation());
                byte[] encodedCurr = encodeCityInfo(info.getCurrency());
                index = moveDataToMainBuffer(encodedName,index);
                index = moveDataToMainBuffer(encodedCountry,index);
                index = moveDataToMainBuffer(encodedCurr,index);
                index = moveDataToMainBuffer(encodedPop,index);
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

    /**
     * encode an integer using VB encoding
     * @param number
     * @return array of encoded bytes
     */
    private byte[] encodeNumber(int number) {

        return vb.encode(new LinkedList<Integer>() {{
            add(number);
        }});
    }

    /**
     * move an array of bytes into the main buffer
     * @param data array of bytes to be writted to disk
     * @param currentInMain current index in the main buffer
     * @return the new udated index in the main buffer
     * @throws Exception
     */
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

    /**
     * *****************WRITING FORMAT ****************************
     * -docID- -maxTF- -uniqueTermsNum- -city- 00
     *
     * -docID- : ID of the doc
     * -maxTF- : the max term frequency in the doc
     * -uniqueTermsNum- the number of unique terms in the doc
     * -city- the city in the tag
     *
     * @param buffer
     * @param path
     */
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
    public void writePostingList(HashMap<Integer, ABufferData> buffer, String path) {


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
                BufferDataByte info = (BufferDataByte)buffer.get(currentTermID);
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

    /**
     * check if a term exists in the main dictionary with small letters
     * @param term term to check
     * @return true if exists
     */
    private boolean checkExistInDicWithSmallLetters(String term){

        try{
            String dictTerm = term.toLowerCase();
            Integer[] data = this.termIdMap.get(dictTerm);
            if(data != null) {
//                System.out.println("should replace : " + this.termIdMap.get(dictTerm));
                return true;
            }
            return false;


        }catch (Exception e){
            return false;
        }

    }

    /**
     * check if a term exists in the main dicionary with capital letters
     * @param term
     * @return true if exists
     */
    private boolean checkExistInDicWithCapitalLetters(String term){

        try {
            String dictTerm = term.toUpperCase();
            //return this.termIdMap.containsKey(dictTerm);
            Integer[] data = this.termIdMap.get(dictTerm);
            if(data == null) {
              return false;
        }
            return true;


        }catch (Exception e){
            return false;
        }

    }

    /**
     * replace an entry of upper case term with lower case term in the main dictionary
     * @param term
     */
    private void replaceUpperWithLower(String term) {
        try {
            String upperTerm = term.toUpperCase();
            Integer[] data = this.termIdMap.get(upperTerm);
            if (data != null) {
                String proccessedTerm = stem(term);
                Integer termid = (Integer) data[1];
                // Integer termid = (Integer) this.termIdMap.get(term).getSecondValue();
                Integer tf = (Integer) data[0];
                //this.termIdMap.put(upperTerm,termid);

                this.idTermMap.remove(termid);

                Integer[] otherSmallLetterInstance = this.termIdMap.get(proccessedTerm);
                if(otherSmallLetterInstance!=null) {
                    //so need to remove the old id and put the capital id on this instance since for sure capital came first
                    idTermMap.remove(otherSmallLetterInstance[1]);
                    idTermMap.put(otherSmallLetterInstance[1],proccessedTerm);
                }

                     this.idTermMap.put(termid, proccessedTerm);


                this.termIdMap.remove(upperTerm);
                Integer[] temp = termIdMap.get(proccessedTerm);
                if(temp != null)
                //this.termIdMap.get(stemmed)[0]++;
                    this.termIdMap.put(proccessedTerm, new Integer[]{data[0]+temp[0],temp[1], data[2], data[3], data[4]});
                else
                    this.termIdMap.put(proccessedTerm,new Integer[]{data[0],data[1],data[2],data[3],data[4]});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get the corpus TF of a term by id
     * @param id the id of the term
     * @return the TF
     */
    public int getTermTF(int id) {

        try {
            String term = this.idTermMap.get(id);
            if(term == null){
                System.out.println("id = [" + id + "]");
            }
            return this.termIdMap.get(term)[0];
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * return the term name by its id in the dict
     * @param id id of the term
     * @return
     */
    public String getTermByID(int id){
        return this.idTermMap.get(id);
    }

    /**
     * set the target path . the target path determines where the engine will write all of its otput files
     * @param path
     */
    public void setTargetPath(String path){
        this.targetPath = path;
    }

    /**
     * set whether to use stem
     * @param stemStatus
     */
    public void setStemOn(boolean stemStatus){
        this.stemOn = stemStatus;
    }

}
