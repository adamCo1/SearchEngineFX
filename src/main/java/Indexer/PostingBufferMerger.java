package Indexer;

import IO.PostingWriter;
import Structures.Pair;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class PostingBufferMerger {

    private String targetPath ;
    private final int BLOCK_SIZE = 4096, ID_BAR = 20000;
    private VariableByteCode vb;
    private ArrayList<PostingBuffer> buffers;
    private byte[] mainBuffer;
    private int bufferIndex, pathIndex;
    private ArrayList<String> outPaths;
    private PostingWriter writer;
    private SpimiInverter spimi;

    public PostingBufferMerger(VariableByteCode vb,SpimiInverter spimi, ArrayList<String> paths,String targetPath) {
        this.spimi = spimi;
        this.targetPath = targetPath;
        this.vb = vb;
        this.buffers = new ArrayList<>();
        this.bufferIndex = 0;
        this.pathIndex = 0;
        this.outPaths = new ArrayList<String>() {{
            add("out0");
        }};
        this.writer = new PostingWriter();
        initializeBuffers(paths);
    }

    /**
     * initialize all buffers with info of size BLOCK_SIZE
     *
     * @param paths
     */
    private void initializeBuffers(ArrayList<String> paths) {

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

        } catch (IOException e) {
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
     * delete a file when its buffer is done
     * @param path the path from the buffer
     */
    private void delete(String path){

        try{
            File file = new File(path);
            file.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void openWriterOnPath(String path) throws IOException {
        this.writer.setPath(path);
    }

    private void closeWriterOnPath() {
        this.writer.close();
    }


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
     * the merging method on term id . can implement more methods
     *
     * @param maxBlockSize max size of the buffers
     */
    public void mergeOnTermID(int maxBlockSize) {
        try {
            ArrayList<PostingBuffer> toRemove = new ArrayList<>();
            mainBuffer = new byte[maxBlockSize];
            int currentID = 1, bufferStatus = 0;
            byte[] termDelimiter = {0,0};
            openWriterOnPath(this.targetPath+"\\"+this.outPaths.get(pathIndex));

            while (this.buffers.size() != 0) {

                if (currentID % ID_BAR == 0) {//each multiple of ID_BAR
                    writeMainBuffer();
                    closeWriterOnPath();
                    pathIndex++;
                    this.outPaths.add(pathIndex, "out" + pathIndex);
                    openWriterOnPath(this.targetPath+"\\"+this.outPaths.get(this.pathIndex));
                }

                for(int i = 0 ; i < buffers.size() ; i++){
                    PostingBuffer buffer = buffers.get(i);
                    try {

                        int id = buffer.readTermID(vb,currentID);

                        if (id != -1) {//move all data to the main buffer
                          //  while (!buffer.checkEndOfTermID()) {//so more info on this term
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
                           // delete(this.targetPath);
                       //     e.printStackTrace();
                        }
                    }
                    //add 00
                    moveToMainBuffer(termDelimiter);

                 }
                  buffers.removeAll(toRemove);
                  toRemove=new ArrayList<>();
                  currentID++;//
                }//the merge loop
            }catch(IOException e){
                e.printStackTrace();
            }
        this.writer.close();
        System.out.println("Done merging . out list : " + this.outPaths);
        }
    }