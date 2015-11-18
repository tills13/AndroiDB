package ca.sbstn.dbtest.sql;

/**
 * Created by tills13 on 2015-07-11.
 */
public class QueryBuilder {
    private String mode;

    public QueryBuilder() {
        this.mode = "select";
    }

    public QueryBuilder select() {
        return this;
    }
}
