package ca.sbstn.androidb.sql;

public class QueryBuilder {
	public static final int TYPE_SELECT = 0;
	public static final int TYPE_DELETE = 1;
	public static final int TYPE_UPDATE = 2;
	public static final int TYPE_INSERT = 3;

	private int type = QueryBuilder.TYPE_SELECT;

	private String[] selects;

	//private 



	public QueryBuilder() {

	}

	public QueryBuilder select(String ... selects) {
		this.type = QueryBuilder.TYPE_SELECT;

		this.selects = selects;
		return this;
	}

	public String getQuery() {
		return this.toString();
	}

	@Override
	public String toString() {
		return "";
	}
}