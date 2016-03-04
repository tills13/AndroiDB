package ca.sbstn.dbtest.sql;

import java.util.ArrayList;
import java.util.List;

public class Expression {
	public class Separator {
		public static final String SEPARATOR_AND = " AND ";
		public static final String SEPARATOR_OR = " OR ";
		public static final String SEPARATOR_COMMA = ",";
	}

	public String prefix = "(";
	public String postfix = ")";
	public String separator = Separator.SEPARATOR_COMMA;

	public Expression left;
	public Expression right;

	public Expression() {
		this(null, null, null);
	}

	public Expression(String separator, Expression left, Expression right) {
		this.separator = ((separator == null) ? Separator.SEPARATOR_COMMA : separator);
		this.left = left;
		this.right = right;
	}

	public void setLeft(Expression left) {
		this.left = left;
	}

	public void setRight(Expression right) {
		this.right = right;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public Expression getLeft() {
		return this.left;
	}

	public Expression getRight() {
		return this.right;
	}

	public String getSeparator() {
		return this.separator;
	}

	@Override
	public String toString() {
		if (this.left == null && this.right == null) return "";
		return String.format("%s%s%s%s%s", 
			this.prefix,
			(this.left == null ? "" : this.left.toString()), // shouldn't ... 
			((this.left == null || this.right == null) ? "" : this.separator), // again, shouldn't ...
			(this.right == null ? "" : this.right.toString()),
			this.postfix
		);
	}

	public static class Builder {
		public Expression expression;

		public Builder() {
			this.expression = new Expression();
		}

		public Builder with(String value) {
			if (this.expression.getLeft() != null) throw new IllegalStateException("with() can only be called on blank expressions");
			this.expression.setLeft(new Value(value));

			return this;
		}

		public Builder and(String value) {
			this.expression.setSeparator(Expression.Separator.SEPARATOR_AND);
			this.expression.setRight(new Value(value));

			return this;
		}

		public Builder and(Expression expression) {
			this.expression.setRight(expression);

			return this;
		}

		/*public Builder and(String expression) {
			this.expression.and(new Value(expression));

			return this;
		}

		public Builder or(Expression expression) {
			this.expression.or(expression);

			return this;
		}

		public Builder or(String expression) {
			this.expression.or(new Value(expression));

			return this;
		}*/

		public Expression build() {
			return this.expression;
		}
	}
}