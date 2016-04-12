package ca.sbstn.androidb.sql;

public class JoinQueryPart extends QueryPart {
	public static class JoinType {
		public static final int TYPE_INNER = 0;
		public static final int TYPE_OUTER = 1;
		public static final int TYPE_NATURAL = 2;
		public static final int TYPE_FULL = 3;
		public static final int TYPE_RIGHT = 4;
		public static final int TYPE_LEFT = 5;

		public static String typeString(int type) {
			if (type == TYPE_INNER) return "INNER";
			else if (type == TYPE_OUTER) return "OUTER";
			else if (type == TYPE_NATURAL) return "NATURAL";
			else return "";
		}
	}
	
	public enum JoinCondition { JOIN_ON, JOIN_WITH }

	public int type;
	public String table;
	public String alias;
	public String conditionType;
	public Expression condition;

	public JoinQueryPart(int type, String table, String alias, String conditionType, Expression condition) {
		this.type = type;
		this.table = table;
		this.alias = alias;
		this.conditionType = conditionType;
		this.condition = condition;
	}

	public JoinQueryPart(int type, String table, String alias, String conditionType, String condition) {
		this(type, table, alias, conditionType, new Value(condition));
	}



	@Override
	public String toString() {
		return String.format("%sJOIN %s%s %s %s\n", 
			(JoinType.typeString(this.type) + " "),
			this.table,
			((this.alias == null || this.alias == "") ? "" : (" AS " + this.alias)),
			this.conditionType, 
			this.condition.toString()
		);
	}
}