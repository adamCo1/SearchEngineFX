package IO;

public class Query {
    private String queryNum;
    private String queryText;
    private String queryDesc;

    public Query(String queryNum, String queryText, String queryDesc) {
        this.queryNum = queryNum;
        this.queryText = queryText;
        this.queryDesc = queryDesc;
    }

    public String getQueryNum() {
        return queryNum;
    }

    public String getQueryText() {
        return queryText;
    }

    public String getQueryDesc() {
        return queryDesc;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
}
