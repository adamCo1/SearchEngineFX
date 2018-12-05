package Indexer;

import IO.PostingWriter;
import Structures.Pair;

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
    private HashMap<String,Integer[]> termIdMap;
    private ArrayList<String> outPaths;
    private PostingWriter writer;
    private SpimiInverter spimi;

    public PostingBufferMerger(HashMap<String,Integer[]> termIdMap , VariableByteCode vb, SpimiInverter spimi, ArrayList<String> paths, ArrayList<String> docPaths, ArrayList<String> cityPaths, String targetPath) {
        this.spimi = spimi;
        this.targetPath = targetPath;
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

        this.buffers = new ArrayList<>();

        for (String path :
                paths) {
            PostingBuffer buffer = new PostingBuffer(path, BLOCK_SIZE);
            this.buffers.add(buffer);
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

        ArrayList<Integer> ans = (ArrayList<Integer>) vb.decode(temp);

        return ans.get(0);
    }

    /**
     * write the main buffer to disk and move the pointer to the start
     */
    private void writeMainBuffer() {

        String path = this.outPaths.get(pathIndex);
        try {
            this.writer.write(mainBuffer);
            this.bufferIndex = 0;
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
     * update the starting position of the term using the blockNum and bufferIndex .
     * blockNum determines the number of the block and bufferIndex is the offset inside the block.
     * @param termID
     * @param outIndicator output path indicator
     */
    private void updatePositionOntermMap(int termID , int outIndicator) {
        try {
            String term = this.spimi.getTermByID(termID);
            this.termIdMap.get(term)[2] = this.blockNum;
            this.termIdMap.get(term)[3] = this.bufferIndex;
            this.termIdMap.get(term)[4] = outIndicator;
        }catch (Exception e){
            e.printStackTrace();
        }
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
            int bufferStatus = 0, currentIDOnMerge = 1 ;
            byte[] termDelimiter = {0,0};
            openWriterOnPath(type);
            boolean firstBufferWithID = true;

            while (this.buffers.size() != 0) {

                if (currentIDOnMerge % ID_BAR == 0) {//CHANGE TO SIZE
                    writeMainBuffer();
                    blockNum++ ;
                }

                firstBufferWithID = true;//so it will update the position in the first match

                for(int i = 0 ; i < buffers.size() ; i++){
                    PostingBuffer buffer = buffers.get(i);
                    try {

                        int id = buffer.readTermID(vb,currentIDOnMerge);

                        if (id != -1) {//move all data to the main buffer
                          //  while (!buffer.checkEndOfTermID()) {//so more info on this term

                            if(firstBufferWithID)
                                updatePositionOntermMap(id,outIndicator);

                            firstBufferWithID = false;

                            LinkedList<Integer> termID = new LinkedList<Integer>() {{
                                add(id);
                            }};

                                LinkedList<Integer> termTF = new LinkedList<Integer>() {{
                                    add(spimi.getTermTF(id));
                                }};
                                LinkedList<Byte> allData = buffer.readToEndOfTerm();

                               // moveDataToMainBuffer(termID, termTF, docid, docTF, onTitle, positions);
                                moveDataToMainBuffer(vb.encode(termID),termTF,allData);
                            }
                    } catch (Exception e) {
                        if(e instanceof EOFException){
                            toRemove.add(buffer);
                            Files.deleteIfExists(Paths.get(buffer.getTempPostingPath()));
                        }
                    }
                    //add 00
                    moveToMainBuffer(termDelimiter);

                 }
                  buffers.removeAll(toRemove);
                  toRemove=new ArrayList<>();
                  currentIDOnMerge++;//
                }//the merge loop
            }catch(IOException e){
                e.printStackTrace();
            }
        this.writer.flush();
        this.writer.close();

        }
    }