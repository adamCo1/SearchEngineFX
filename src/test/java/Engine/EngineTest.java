package Engine;

import org.junit.jupiter.api.Test;

class EngineTest {

    @Test
    public void testEngine() throws Exception{

        String corpusPath = "C:\\Users\\adam\\Corpus2";
        Engine engine = new Engine(true,corpusPath);
        engine.run();
    }


}