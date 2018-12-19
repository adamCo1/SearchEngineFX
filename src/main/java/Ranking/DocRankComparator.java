package Ranking;

import java.util.Comparator;

public class DocRankComparator implements Comparator<DocRank> {

    @Override
    public int compare(DocRank o1, DocRank o2) {

        if(o1.getRank() > o2.getRank())
            return 1;
        else if (o1.getRank() == o2.getRank())
            return 0;

        return -1;
    }
}
