package ca.sbstn.dbtest.sql;

public class ColumnQueryPart extends QueryPart {
	private String column;
	private String alias;

	public ColumnQueryPart(String column, String alias) {
		this.column = column;
		this.alias = alias;
	}

	@Override
	public String toString() {
		return (this.column) + ((this.alias == null || this.alias == "") ? "" : (" AS " + this.alias));
	}
}