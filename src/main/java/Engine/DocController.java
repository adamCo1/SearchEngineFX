package Engine;

import Structures.Doc;
import sun.awt.Mutex;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;


/**
 * class that uses 2 semaphores to control the flow of documents to the parser
 */

public class DocController implements IBufferController {

    private final int DOC_NUM = 5;
    private Semaphore putDocSem ;
    private Semaphore takeDocSem ;
    private Mutex mutex = new Mutex();
    private LinkedList<Doc> docBuffer ;

    public DocController(){
        this.docBuffer = new LinkedList<>();
        this.putDocSem = new Semaphore(DOC_NUM);
        this.takeDocSem = new Semaphore(0);
    }

    /**
     * put a doc only if there is a permit in the putDocSem
     * @param doc
     */
    public void addBuffer(Object doc) {

        try {
            //   System.out.println("Number of docs : " + docBuffer.size());
            // mutex.lock();
            this.putDocSem.acquire();
            mutex.lock();
            this.docBuffer.add((Doc)doc);
            takeDocSem.release();
            mutex.unlock();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * take doc only if theres a permit at the takeDocSem semaphore
     * @return
     */
    public Object getBuffer(){
        try {
            //mutex.lock();
            this.takeDocSem.acquire();
            mutex.lock();
            Doc ans = this.docBuffer.poll();
            this.putDocSem.release();
            mutex.unlock();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}