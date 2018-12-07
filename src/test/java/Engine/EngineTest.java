package Engine;

import Indexer.VariableByteCode;
import org.junit.jupiter.api.Test;

class EngineTest {

    @Test
    public void testEngine() throws Exception{

        VariableByteCode vb = new VariableByteCode();
        byte[] num = new byte[]{-128};
        System.out.println(vb.decodeNumber(num));
    }


}