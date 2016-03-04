package ca.sbstn.dbtest.sql;

public class OrderByQueryPart extends QueryPart {
	public static class Direction {
		private static final int DIRECTION_NONE = -1;
		private static final int DIRECTION_ASC = 0;
		private static final int DIRECTION_DESC = 1;

		public static String directionString(int direction) {
			if (direction == DIRECTION_NONE) return "";
			else if (direction == DIRECTION_DESC) return " DESC";
			else if (direction == DIRECTION_ASC) return " ASC";
			else return "";
		}
	}

	private String column;
	private int direction;

	public OrderByQueryPart(String column, int direction) {
		this.column = column;
		this.direction = direction;
	}

	@Override
	public String toString() {
		return this.column + Direction.directionString(this.direction);
	}
}