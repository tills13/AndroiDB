package ca.sbstn.androidb.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Query {
	public static final int TYPE_SELECT = 0;
	public static final int TYPE_DELETE = 1;
	public static final int TYPE_UPDATE = 2;
	public static final int TYPE_INSERT = 3;

	private int type;
	private List<QueryPart> columns;
	private Object [] values;
	private List<QueryPart> tables;
	private List<QueryPart> joins;
	private Expression where;
	private List<QueryPart> orderBy;
	private List<QueryPart> groupBy;

	//private Map<String, Object> parameters;
	private List<Object> parameters;

	private int limit;
	private int offset;

	private IntoQueryPart into;

	protected Query() {
		this.columns = new ArrayList<>();
		this.tables = new ArrayList<>();
		this.joins = new ArrayList<>();
		this.orderBy = new ArrayList<>();
		this.groupBy = new ArrayList<>();

		//this.parameters = new HashMap<>();
		this.parameters = new ArrayList<>();
		this.limit = -1;
		this.offset = 0;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void addColumn(String column, String alias) {
		this.columns.add(new ColumnQueryPart(column, alias));
	}

	public void setValues(Object ... values) {
		this.values = values;
	}

	public void addTable(String table) {
		this.addTable(table, null);
	}

	public void addTable(String table, String alias) {
		this.tables.add(new TableQueryPart(table, alias));
	}

	public void addTable(TableQueryPart table) {
		this.tables.add(table);
	}

	public void addTables(List<TableQueryPart> tables) {
		this.tables.addAll(tables);
	}

	public void setTables(List<TableQueryPart> tables) {
		this.tables.clear();
		this.addTables(tables);
	}

	public void addJoin(JoinQueryPart join) {
		this.joins.add(join);
	}

	public void addJoins(List<JoinQueryPart> joins) {
		this.joins.addAll(joins);
	}

	public void setWhere(Expression where) {
		this.where = where;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void addGroupBy(GroupByQueryPart groupBy) {
		this.groupBy.add(groupBy);
	}

	public void setGroupBy(List<GroupByQueryPart> groupBy) {
		this.groupBy.clear();
		this.groupBy.addAll(groupBy);
	}

	public void addOrderBy(OrderByQueryPart orderBy) {
		this.orderBy.add(orderBy);
	}

	public void setOrderBy(List<OrderByQueryPart> orderBy) {
		this.orderBy.clear();
		this.orderBy.addAll(orderBy);
	}

	public void setInto(String into, String external) {
		this.into = new IntoQueryPart(into, external);
	}

	public void setInto(IntoQueryPart into) {
		this.into = into;
	}

	public boolean is(int type) {
		return this.type == type;
	}

	public void parameters(Object ... parameters) {
		this.parameters = Arrays.asList(parameters);
	}

	public void bind(int key, Object value) {}

	public void bind(String key, Object value) {
		//this.parameters.put(key, value);
	}

	public void bind(Map<String, Object> parameters) {
		//this.parameters = parameters;
	}

	@Override
	public String toString() {
		String query = "";
		switch (this.type) {
			case TYPE_SELECT: {
				query = "SELECT ";

				query = query + this.columnsToString();
				query = query + "FROM " + this.tablesToString();

				for (int i = 0; i < this.joins.size(); i++) query = query + this.joins.get(i);

				if (this.where != null) query = query + "WHERE " + this.where.toString() + "\n";

				if (this.groupBy.size() > 0) query = query + "GROUP BY " + this.groupBysToString();
                if (this.orderBy.size() > 0) query = query + "ORDER BY " + this.orderBysToString();

				if (limit > -1) query = query + "LIMIT " + this.limit + "\n";
				if (offset > 0) query = query + "OFFSET " + this.offset + "\n";

				break;
			}

			case TYPE_INSERT: {
				query = "INSERT INTO " + this.tables.get(0) + "\n";

				if (this.columns != null && this.columns.size() > 0) {
					query = query + " (" + this.columnsToString() + ")\nVALUES\n";
				}

				query = query + "(";
				for (int i = 0; i < this.values.length; i++) {
					query = query + this.values[i].toString();

					if (i != (this.values.length - 1)) query += ", ";
				}

				query = query + ")";

				break;
			}
		}

		if (this.parameters.size() > 0) {
			query = query + "\n\nparameters: [";
			for (int i = 0; i < this.parameters.size(); i++) {
				query = query + SQLUtils.convertArgument(this.parameters.get(i));

				if (i != (this.parameters.size() - 1)) query += ", ";
				else query += "]";
			}
		}

		return query;
	}

	private String columnsToString() {
        String query = "";

		for (int i = 0; i < this.tables.size(); i++) {
			query = query + this.tables.get(i);

			if (i != (this.tables.size() - 1)) query += ", ";
			else query += "\n";
		}

        return query;
	}

    private String tablesToString() {
        String query = "";

        for (int i = 0; i < this.tables.size(); i++) {
            query = query + this.tables.get(i);

            if (i != (this.tables.size() - 1)) query += ", ";
            else query += "\n";
        }

        return query;
    }

    private String groupBysToString() {
        String query = "";

        for (int i = 0; i < this.groupBy.size(); i++) {
            query = query + this.groupBy.get(i);

            if (i != (this.groupBy.size() - 1)) query += ", ";
            else query += "\n";
        }

        return query;
    }

    private String orderBysToString() {
        String query = "";

        for (int i = 0; i < this.orderBy.size(); i++) {
            query = query + this.orderBy.get(i);

            if (i != (this.orderBy.size() - 1)) query += ", ";
            else query += "\n";
        }

        return query;
    }

	public static class Builder {
		private Query query;

		public Builder() {
			this.query = new Query();
		}

		public Builder select(String ... columns) {
			this.query.setType(Query.TYPE_SELECT);
			for (String column : columns) this.select(column, null);

			return this;
		}

		public Builder select(String column, String alias) {
			this.query.setType(Query.TYPE_SELECT);
			this.query.addColumn(column, alias);

			return this;
		}

		public Builder insert() {
			this.query.setType(Query.TYPE_INSERT);

			return this;
		}

		public Builder columns(String ... columns) {
			for (String column : columns) this.query.addColumn(column, null);

			return this;
		}

		public Builder values(Object ... values) {
			this.query.setValues(values);

			return this;
		}

		public Builder from(List<TableQueryPart> tables) {
			this.query.setTables(tables);

			return this;
		}

		public Builder from(String table, String alias) {
			this.query.addTable(table, alias);

			return this;
		}

		private void join(int type, String table, String alias, String conditionType, Expression condition) {
			this.query.addJoin(new JoinQueryPart(type, table, alias, conditionType, condition));
		}

		public Builder join(String table, String alias, String conditionType, Expression condition) {
			this.join(JoinQueryPart.JoinType.TYPE_INNER, table, alias, conditionType, condition);

			return this;
		}

		public Builder join(String table, String alias, String conditionType, String condition) {
			Expression mCondition = new Expression.Builder().with(condition).build();
			this.join(JoinQueryPart.JoinType.TYPE_INNER, table, alias, conditionType, mCondition);

			return this;
		}

		public Builder leftJoin(String table, String alias, String conditionType, Expression condition) {
			this.join(JoinQueryPart.JoinType.TYPE_LEFT, table, alias, conditionType, condition);

			return this;
		}

		public Builder leftJoin(String table, String alias, String conditionType, String condition) {
			Expression mCondition = new Expression.Builder().with(condition).build();
			this.join(JoinQueryPart.JoinType.TYPE_LEFT, table, alias, conditionType, mCondition);

			return this;
		}

		public Builder rightJoin(String table, String alias, String conditionType, Expression condition) {
			this.join(JoinQueryPart.JoinType.TYPE_RIGHT, table, alias, conditionType, condition);

			return this;
		}

		public Builder rightJoin(String table, String alias, String conditionType, String condition) {
			Expression mCondition = new Expression.Builder().with(condition).build();
			this.join(JoinQueryPart.JoinType.TYPE_RIGHT, table, alias, conditionType, mCondition);

			return this;
		}

		public Builder fullJoin(String table, String alias, String conditionType, Expression condition) {
			this.join(JoinQueryPart.JoinType.TYPE_FULL, table, alias, conditionType, condition);

			return this;
		}

		public Builder fullJoin(String table, String alias, String conditionType, String condition) {
			Expression mCondition = new Expression.Builder().with(condition).build();
			this.join(JoinQueryPart.JoinType.TYPE_FULL, table, alias, conditionType, mCondition);

			return this;
		}

		/**
		 * select into
		 * @param  into     [description]
		 * @param  external [description]
		 * @return          [description]
		 */
		public Builder into(String into, String external) {
			if (this.query.is(TYPE_SELECT)) this.query.setInto(into, external);
			//else if (this.query.is(TYPE_INSERT))
			return this;
		}

		public Builder into(String table) {
			if (this.query.is(TYPE_INSERT)) this.query.addTable(table);

			return this;
		}

		public Builder where(Expression where) {
			this.query.setWhere(where);

			return this;
		}

		public Builder limit(int limit) {
			this.query.setLimit(limit);

			return this;
		}

		public Builder offset(int offset) {
			this.query.setOffset(offset);

			return this;
		}

		public Builder groupBy(GroupByQueryPart groupBy) {
			this.query.addGroupBy(groupBy);

			return this;
		}

		public Builder groupBy(String column) {
			this.query.addGroupBy(new GroupByQueryPart(column));

			return this;
		}

		public Builder groupBy(String ... columns) {
			for (String column : columns) this.groupBy(column);

			return this;
		}

		public Builder orderBy(OrderByQueryPart groupBy) {
			this.query.addOrderBy(groupBy);

			return this;
		}

		public Builder orderBy(String column, int direction) {
			this.query.addOrderBy(new OrderByQueryPart(column, direction));

			return this;
		}

		public Builder bind(Map<String, Object> parameters) {
			this.query.bind(parameters);

			return this;
		}

		public Builder bind(String key, String value) {
			this.query.bind(key, value);

			return this;
		}

		public Query build() {
			return this.query;
		}
	}
}










