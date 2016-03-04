package ca.sbstn.dbtest.sql;

public class Value extends Expression {
	public String value;

	public Value() {
		super();
	}

	public Value(String value) {
		this(null, null, null);
		this.value = value;
	}

	public Value(String separator, Expression left, Expression right) {
		super(separator, left, right);
	}

	@Override
	public String toString() {
		return this.value;
	}
}