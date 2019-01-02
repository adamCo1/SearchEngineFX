package Ranking;

import Structures.CorpusDocument;
import java.util.Comparator;

/**
 * comparator class for comparing doc ranks
 */

public class DocRankComparator implements Comparator<CorpusDocument> {

    @Override
    public int compare(CorpusDocument o1, CorpusDocument o2) {

        if(o1.getRank() > o2.getRank())
            return -1;
        else if (o1.getRank() == o2.getRank())
            return 0;

        return 1;
    }
}
