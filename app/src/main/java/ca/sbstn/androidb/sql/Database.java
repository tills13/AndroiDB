package ca.sbstn.androidb.sql;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by tills13 on 2015-07-10.
 */
public class Database implements Serializable {
    private Server server;

    private String name;
    private String comment;
    private String owner;

    public Database() {

    }

    public Database(Server server, String name, String owner) {
        this(server, name, owner, null);
    }

    public Database(Server server, String name, String owner, String comment) {
        this.server = server;
        this.name = name;
        this.owner = owner;
        this.comment = comment;
    }

    public String getConnectionString() {
        return String.format(Locale.getDefault(), "jdbc:postgresql://%s:%d/%s",
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
