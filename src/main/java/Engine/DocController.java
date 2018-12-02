package Engine;

import Structures.Doc;
import sun.awt.Mutex;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * class that uses 2 semaphores to control the flow of documents to the parser
 */

public class DocController {

    private final int DOC_NUM = 5;
    private Semaphore putDocSem = new Semaphore(DOC_NUM);
    private Semaphore takeDocSem = new Semaphore(0);
    private Mutex mutex = new Mutex();
    private LinkedList<Doc> docBuffer = new LinkedList<>();

    public DocController(){
        this.docBuffer = new LinkedList<>();
    }

    /**
     * put a doc only if there is a permit in the putDocSem
     * @param doc
     */
    public void addDoc(Doc doc) {

        try {
            //   System.out.println("Number of docs : " + docBuffer.size());
            // mutex.lock();
            this.putDocSem.acquire();
            mutex.lock();
            this.docBuffer.add(doc);
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
    public Doc takeDoc(){
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