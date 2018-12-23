package Indexer;

import IO.PostingWriter;
import Structures.Pair;
import Structures.PostingDataStructure;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class PostingBufferMerger {

    private int currentTermID , blockNum ;
    private String targetPath ;
    private final String TERM_INDEX_PATH = "terms_out" , CITY_INDEX_PATH = "citites_out" , DOC_INDEX_PATH = "docs_out";
    private final int BLOCK_SIZE = 4096, ID_BAR = 20000;
    private VariableByteCode vb;
    private ArrayList<PostingBuffer> buffers;
    private byte[] mainBuffer;
    private int bufferIndex, pathIndex;
    private HashMap<Integer,Pair> docPositions;
    private HashMap<String,PostingDataStructure> termIdMap;
    private ArrayList<String> outPaths;
    private PostingWriter writer;
    private SpimiInverter spimi;

    public PostingBufferMerger(HashMap<String, PostingDataStructure> termIdMap,HashMap docPositions , VariableByteCode vb, SpimiInverter spimi, ArrayList<String> paths, ArrayList<String> docPaths, ArrayList<String> cityPaths, String targetPath) {
        this.spimi = spimi;
        this.targetPath = targetPath;
        this.docPositions = docPositions;
        this.termIdMap = termIdMap;
        this.vb = vb;
        this.buffers = new ArrayList<>();
        this.bufferIndex = 0;
        this.pathIndex = 0;
        this.currentTermID = 1 ;
        this.blockNum = 0;
        this.outPaths = new ArrayList<String>() {{
            add("out0");
        }};
        this.writer = new PostingWriter();
       // initializeBuffers(paths);
    }

    /**
     * initialize all buffers with info of size BLOCK_SIZE
     *
     * @param paths
     */
    private void initializeBuffers(ArrayList<String> paths) {

        try {
            this.buffers = new ArrayList<>();

            for (String path :
                    paths) {
                PostingBuffer buffer = new PostingBuffer(path, BLOCK_SIZE);
                this.buffers.add(buffer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * decode a list of bytes using the VB decoder
     *
     * @param list list of bytes encoded with VB
     * @return int number
     */
    private int decode(LinkedList<Byte> list) {

        byte[] temp = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) {
            temp[i] = list.get(i);
        }

        LinkedList<Integer> ans = vb.decode(temp);

        return ans.get(0);
    }

    /**
     * write the main buffer to disk and move the pointer to the start
     */
    private void writeMainBuffer() {

        String path = this.outPaths.get(pathIndex);
        try {
            this.writer.write(mainBuffer);
            this.mainBuffer = new byte[4096];
            this.bufferIndex = 0;
            this.blockNum++;
            this.writer.flush();
           // this.termToPostingMap.get(currentTermID).setSecondValue(blockNum);
        } catch (Exception e) {
            System.out.println("error in writing main buffer");
            e.printStackTrace();
        }
    }


    /**
     * add byte to the main buffer
     *
     * @param b
     */
    private void addByte(byte b) {
        mainBuffer[bufferIndex++] = b;

        if (bufferIndex == mainBuffer.length)
            writeMainBuffer();
    }

    /**
     * move array of bytes to the main buffer
     *
     * @param arr
     */
    private void moveToMainBuffer(byte[] arr) {
        for (int i = 0; i < arr.length; i++)
            addByte(arr[i]);
    }

    /**
     * move list of bytes to the main buffer
     *
     * @param list
     */
    private void moveToMainBuffer(LinkedList<Byte> list) {

        for (byte b :
                list) {
            addByte(b);
        }
    }

    /**
     * opens the writer on a given path
     * @param type the type to be used to decide what the path is
     * @throws IOException
     */
    private void openWriterOnPath(String type) throws IOException {
        if(type.equals("TERMS"))
            this.writer.setPath(this.targetPath+"\\"+TERM_INDEX_PATH);
        else if(type.equals("CITY"))
            this.writer.setPath(this.targetPath+"\\"+CITY_INDEX_PATH);
        else if(type.equals("DOC"))
            this.writer.setPath(this.targetPath+"\\"+DOC_INDEX_PATH);
    }

    /**
     * close the writer
     */
    private void closeWriterOnPath() {
        this.writer.close();
    }

    private byte[] encode(int number){
        LinkedList<Integer> num = new LinkedList<Integer>(){{
            add(number);
        }};

        return this.vb.encode(num);
    }
    /**
     * move all the needed data on a current term to the main buffer
     * @param termID
     * @param termTF
     * @param allData
     */
    private void moveDataToMainBuffer(byte[] termID, LinkedList<Integer> termTF, LinkedList<Byte> allData){
        moveToMainBuffer(termID);
        moveToMainBuffer(vb.encode(termTF));
        moveToMainBuffer(allData);
    }

    private void moveDataToMainBuffer( LinkedList<Byte> alldata){
      //  moveToMainBuffer(vb.encode(tf));
        moveToMainBuffer(alldata);
    }

    private void moveDataToMainBuffer(byte[] termID , LinkedList<Byte> allData){
        moveToMainBuffer(termID);
        moveToMainBuffer(allData);
    }

    private void moveDataToMainBuffer(LinkedList<Byte> termID, LinkedList<Integer> termTF, LinkedList<Byte> docid,
                                      LinkedList<Byte> docTF, LinkedList<Byte> onTitle, LinkedList<Byte> positions) {

        byte[] delimiter = {0};

        moveToMainBuffer(termID);
        //put the tf
        moveToMainBuffer(vb.encode(termTF));
        moveToMainBuffer(docid);
        moveToMainBuffer(docTF);
        moveToMainBuffer(onTitle);
        moveToMainBuffer(positions);
        moveToMainBuffer(delimiter);
    }

    /**
     * determine the out indicator using the type . the out indicator will be used by the engine
     * to determine the right index to be used
     * @param type
     * @return 0 for terms , 1 for cities and 2 for documents
     */
    private int determineOutIndicator(String type){
        if(type.equals("TERMS"))
            return 0;
        else if(type.equals("CITY"))
            return 1;

        return 2;//for docs
    }

    /**
     * update where the given doc starts in the posting docs file
     * @param id
     */
    private void updatePositionOnDocPositionMap(int id){

        System.out.println(id);
        this.docPositions.put(id,new Pair(blockNum,bufferIndex));
    }

    /**
     * update the starting position of the term using the blockNum and bufferIndex .
     * blockNum determines the number of the block and bufferIndex is the offset inside the block.
     * @param termID
     * @param outIndicator output path indicator
     */
    private void updatePositionOntermMap(int termID , int outIndicator) {
        try {
            String term = this.spimi.getTermByID(termID);
            byte[] encodedData = this.termIdMap.get(term).getEncodedData();
            LinkedList<Integer> tlist = (LinkedList<Integer>) vb.decode(encodedData);
            LinkedList<Integer> flist = new LinkedList<Integer>(){{
               add(tlist.get(0));//tf
               add(termID);
               add(blockNum) ;
               add(bufferIndex);
               add(outIndicator+1);
            }};

            byte[] encoded = vb.encode(flist);
            this.termIdMap.get(term).setEncodedData(vb.encode(flist));
        }catch (Exception e){
               // e.printStackTrace();
        }
    }


    public void merge(ArrayList<String> paths , int maxBlockSize , String type){
        initializeBuffers(paths);

        int outIndicator = determineOutIndicator(type);

        try {
            ArrayList<PostingBuffer> toRemove = new ArrayList<>();
            mainBuffer = new byte[maxBlockSize];
            blockNum = 0;
            bufferIndex = 0;
            int bufferStatus = 0, currentIDOnMerge = 1 ;
            byte[] termDelimiter = {0,0};
            openWriterOnPath(type);
            boolean firstBufferWithID = true;

            while (this.buffers.size() != 0) {

                if (currentIDOnMerge % ID_BAR == 0) {//CHANGE TO SIZE
                    writeMainBuffer();
                    //blockNum++ ;
                }

                firstBufferWithID = true;//so it will update the position in the first match

                for(int i = 0 ; i < buffers.size() ; i++){
                    PostingBuffer buffer = buffers.get(i);
                    try {

                        int id = buffer.readTermID(vb,currentIDOnMerge);

                        if (id != -1) {//move all data to the main buffer
                            //  while (!buffer.checkEndOfTermID()) {//so more info on this term

                            if(firstBufferWithID) {
                                if (type.equals("DOC"))
                                    updatePositionOnDocPositionMap(id);
                            }

                            firstBufferWithID = false;

                            LinkedList<Integer> termID = new LinkedList<Integer>() {{
                                add(id);
                            }};


                            LinkedList<Byte> allData = buffer.readToEndOfTerm();
                            moveDataToMainBuffer(vb.encode(termID),allData);

                            //end of 1 buffer
                        }
                    } catch (Exception e) {
                        if(e instanceof EOFException){
                            toRemove.add(buffer);
                            Files.deleteIfExists(Paths.get(buffer.getTempPostingPath()));
                        }
                    }

                }
                //add 00
                moveToMainBuffer(termDelimiter);
                buffers.removeAll(toRemove);
                toRemove=new ArrayList<>();
                currentIDOnMerge++;//

                if(currentIDOnMerge > termIdMap.size()) {//so we are done
                    for(int j = 0 ; j < buffers.size() ; j++){
                        toRemove.add(buffers.get(j));
                        Files.deleteIfExists(Paths.get(buffers.get(j).getTempPostingPath()));
                    }
                    buffers.removeAll(toRemove);
                }

            }//the merge loop
        }catch(IOException e){
            e.printStackTrace();
        }
        this.writer.flush();
        this.writer.close();

    }

    /**
     * the merging method on term id . can implement more methods
     *
     * @param maxBlockSize max size of the buffers
     */
    public void mergeOnTermID(ArrayList<String> paths , int maxBlockSize,String type) {

        initializeBuffers(paths);

        int outIndicator = determineOutIndicator(type);

        try {
            ArrayList<PostingBuffer> toRemove = new ArrayList<>();
            mainBuffer = new byte[maxBlockSize];
            blockNum = 0;
            bufferIndex = 0;
            int bufferStatus = 0, currentIDOnMerge = 1 ;
            byte[] termDelimiter = {0,0};
            openWriterOnPath(type);
            boolean firstBufferWithID = true;

            while (this.buffers.size() != 0) {

                if (currentIDOnMerge % ID_BAR == 0) {//CHANGE TO SIZE
                    writeMainBuffer();
                    //blockNum++ ;
                }

                firstBufferWithID = true;//so it will update the position in the first match
                if(currentIDOnMerge == 8572)
                    System.out.println("");
                for(int i = 0 ; i < buffers.size() ; i++){
                    PostingBuffer buffer = buffers.get(i);
                    try {

                        int id = buffer.readTermID(vb,currentIDOnMerge);

                        if (id != -1) {//move all data to the main buffer
                          //  while (!buffer.checkEndOfTermID()) {//so more info on this term



                            if(firstBufferWithID)
                                if(type.equals("TERMS"))
                                    updatePositionOntermMap(id,outIndicator);
                                else if(type.equals("DOC"))
                                    updatePositionOnDocPositionMap(id);

                            LinkedList<Integer> termID = new LinkedList<Integer>() {{
                                add(id);
                            }};


                                LinkedList<Integer> termTF = new LinkedList<Integer>() {{
                                    add(spimi.getTermTF(id));
                                }};

                                LinkedList<Byte> allData = buffer.readToEndOfTerm();
                                if(termTF.getFirst().equals(0))
                                    continue;

                                if(firstBufferWithID) {
                                    moveDataToMainBuffer(vb.encode(termID), termTF, allData);
                                    firstBufferWithID = false;
                                }else
                                    moveDataToMainBuffer(allData);
                                //    moveDataToMainBuffer(termTF,allData);
                        }

                        if(buffer.isDone()) {//got to the end of the temp file , delete it
                            toRemove.add(buffer);
                            Files.deleteIfExists(Paths.get(buffer.getTempPostingPath()));
                        }

                    } catch (Exception e) {
                        if(e instanceof EOFException){
                            toRemove.add(buffer);
                            Files.deleteIfExists(Paths.get(buffer.getTempPostingPath()));
                        }
                    }

                 }
                 //add 00
                  moveToMainBuffer(termDelimiter);
                  buffers.removeAll(toRemove);
                  toRemove=new ArrayList<>();
                  currentIDOnMerge++;//
                  if(currentIDOnMerge > termIdMap.size()) {//so we are done
                      for(int j = 0 ; j < buffers.size() ; j++){
                          toRemove.add(buffers.get(j));
                          Files.deleteIfExists(Paths.get(buffers.get(j).getTempPostingPath()));
                      }
                      buffers.removeAll(toRemove);
                  }

                }//the merge loop
            }catch(IOException e){
                e.printStackTrace();
            }
        this.writer.flush();
        this.writer.close();

        }
    }