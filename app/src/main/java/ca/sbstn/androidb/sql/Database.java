package ca.sbstn.androidb.sql;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by tills13 on 2015-07-10.
 */
public class Database implements Serializable {
    private Server server;

    private String name;
    private String comment;
    private String owner;

    public Database(Server server) {
        this.server = server;
    }

    public static Database from(ResultSet resultSet, Server server) {
        Database database = new Database(server);

        try {
            //Statement statement = resultSet.getStatement();
            //Connection connection = statement.getConnection();
            //ResultSetMetaData rsmd = resultSet.getMetaData();
            //DatabaseMetaData dbmd = connection.getMetaData();

            database.name = resultSet.getString("name");
            database.comment = resultSet.getString("comment");
            database.owner = resultSet.getString("owner");
        } catch (SQLException e) {
            return null;
        }

        return database;
    }

    public String getConnectionString() {
        return String.format("jdbc:postgresql://%s:%d/%s",
                this.server.getHost(),
                this.server.getPort(),
                this.name
        );
    }

    public Server getServer() {
        return this.server;
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    public String getOwner() {
        return this.owner;
    }
}
