package ca.sbstn.dbtest.sql;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tills13 on 2015-07-10.
 */
public class Server implements Serializable {
    public static String[] colors = new String[] {
            "#E57373", // red_303
            "#64B5F6", // blue_300
            "#F06292",
            "#BA68C8",
            "#9575CD",
            "#7986CB",
            "#4FC3F7",
            "#4DD0E1",
            "#4DB6AC",
            "#81C784",
            "#AED581",
            "#FFB300",
            "#FF8A65"
    };

    private String id;
    private String name;

    private String host;
    private int port;

    private String username;
    private String password;

    private String color;

    private String defaultDatabase;
    private List<Database> databases; // list of all databases

    public Server(String id, String name, String host, int port, String username, String password, String defaultDatabase, String color) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.color = color;

        this.defaultDatabase = defaultDatabase;
    }

    public Server(String id, String name, String host, int port, String username, String password) {
        this(id, name, host, port, username, password, "postgres", "#E57373");
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getDefaultDatabase() {
        return this.defaultDatabase;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password; // no
    }

    public String getColor() {
        return this.color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDefaultDatabase(String database) {
        this.defaultDatabase = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %d %s %s %s",
            this.getId(),
            this.getName(),
            this.getHost(),
            this.getPort(),
            this.getUsername(),
            this.getPassword(),
            this.getColor()
        );
    }
}
