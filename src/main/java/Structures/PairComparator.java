package Structures;

import java.util.Comparator;

/**
 * comparator for pairs
 */

public class PairComparator implements Comparator<Pair> {
    @Override
    public int compare(Pair o1, Pair o2) {

        double o1val = (Double)o1.getSecondValue();
        double o2val = (Double)o2.getSecondValue();

        if(o1val > o2val)
            return -1 ;
        else if(o1val == o2val)
            return 0;

        return 1;
    }
}
