package Engine;


import java.util.LinkedList;

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

    public void addPosition(int position){
        this.lastPositionNormalizer += position;
        this.positionList.add(new Integer(position));
    }

    public void setAtHeadline(byte at){
        this.atHeadline = at;
    }

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
