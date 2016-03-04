package ca.sbstn.dbtest.sql;

public class GroupByQueryPart extends QueryPart {
	private String column;

	public GroupByQueryPart(String column) {
		this.column = column;
	}

	@Override
	public String toString() {
		return this.column;
	}
}