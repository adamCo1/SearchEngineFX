package Indexer;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VariableByteCode {


    public byte[] encodeNumber(int n) {
        if (n == 0) {
            return new byte[]{0};
        }
        int i = (int) (Math.log(n) / Math.log(128)) + 1;
        byte[] rv = new byte[i];
        int j = i - 1;
        do {
            rv[j--] = (byte) (n % 128);
            n /= 128;
        } while (j >= 0);
        rv[i - 1] += 128;
        return rv;
    }


    public byte[] encode(List<Integer> numbers) {
        byte[] rv;
        try {
            ByteBuffer buf = ByteBuffer.allocate(numbers.size() * (Integer.SIZE / Byte.SIZE));
            for (Integer number : numbers) {
                buf.put(encodeNumber(number));
            }
            buf.flip();
             rv = new byte[buf.limit()];
            buf.get(rv);
        }catch (BufferOverflowException e){
            System.out.println("");
            return null;
        }
        return rv;
    }

    public LinkedList<Integer> decode(byte[] byteStream) {
        LinkedList<Integer> numbers = new LinkedList<Integer>();
        int n = 0;
        for (byte b : byteStream) {
            if ((b & 0xff) < 128) {
                n = 128 * n + b;
            } else {
                int num = (128 * n + ((b - 128) & 0xff));
                numbers.addLast(num);
                n = 0;
            }
        }
        return numbers;
    }

    public int decodeNumber(byte[] byteStream){

        int n = 0;
        int ans = 0;
        for (byte b : byteStream) {
            if ((b & 0xff) < 128) {
                n = 128 * n + b;
            } else {
                int num = (128 * n + ((b - 128) & 0xff));
                ans = num;
                n = 0;
            }
        }
        return ans;
    }

}
