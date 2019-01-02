package Engine;

public interface IBufferController {

    /**
     * add a buffer to the controller
     * @param buffer a buffer of any class
     */
    void addBuffer(Object buffer);

    /**
     * get a buffer
     * @return the next buffer in the list
     */
    Object getBuffer();
}

