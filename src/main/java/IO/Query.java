package IO;

public class Query {
    private String queryNum;
    private String queryText;

    public Query(String queryNum, String queryText) {
        this.queryNum = queryNum;
        this.queryText = queryText;
    }

    public String getQueryNum() {
        return queryNum;
    }

    public String getQueryText() {
        return queryText;
    }
}
