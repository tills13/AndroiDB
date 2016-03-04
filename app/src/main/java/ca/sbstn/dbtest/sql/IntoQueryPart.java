package ca.sbstn.dbtest.sql;

public class IntoQueryPart extends QueryPart {
	private String into;
	private String external;

	public IntoQueryPart(String into, String external) {
		this.into = into;
		this.external = external;
	}

	@Override
	public String toString() {
		return this.into + ((this.external == null || this.external.equals("")) ? "" : " IN " + this.external);
	}
}