package Engine;

import java.util.LinkedList;

/**
 * class that encapsulates data of a term in a document after being parsed.
 *
 * lastPositionNormalizer : positions are saved as gaps . this will hold the last real position .
 * atHeadLine : is this term at the head line or not
 */

public class Data {

    private int lastPositionNormalizer;
    private int atHeadline ;
    private LinkedList<Integer> positionList;

    public Data(Data other){
        this.lastPositionNormalizer = other.lastPositionNormalizer;
        this.atHeadline = other.atHeadline;
        this.positionList = new LinkedList<Integer>(){{
            for (Integer integer:
                    other.positionList) {
                add(new Integer(integer));
            }
        }};
    }

    /**
     * initialize a term's data object
     * @param position first seen position
     * @param atHeadline at headline of current document or not
     */
    public Data(int position, byte atHeadline)
    {
        this.positionList = new LinkedList<>();
        this.positionList.add(position);
        this.atHeadline = atHeadline;
        lastPositionNormalizer =position;
    }

    public Data(int position){
        this.positionList = new LinkedList<>();
        this.positionList.add(position);
        this.atHeadline = 1;
        this.lastPositionNormalizer = position;

    }

    /**
     * adds a position to the positions list
     * @param position position to be added
     */
    public void addPosition(int position){
        this.lastPositionNormalizer += position;
        this.positionList.add(new Integer(position));
    }

    /**
     * get the last position of this term
     * @return
     */
    public Integer getLastPosition(){
        int ans = this.lastPositionNormalizer;
        return ans;
    }

    public LinkedList<Integer> getPositionList(){
        return this.positionList;
    }

    public LinkedList<Integer> getOnTitle(){
        return new LinkedList<Integer>(){{
            add(atHeadline);
        }};
    }

    public String toString(){
        String ans = "";
        ans += atHeadline+ " , ";
        ans += positionList.toString();

        return ans ;
    }
}
