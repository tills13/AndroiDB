package ca.sbstn.androidb.sql;

public class TableQueryPart extends QueryPart {
	private String table;
	private String alias;

	public TableQueryPart(String table, String alias) {
		this.table = table;
		this.alias = alias;
	}

	@Override
	public String toString() {
		return (this.table) + ((this.alias == null || this.alias == "") ? "" : (" AS " + this.alias));
	}
}