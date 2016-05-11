package ca.sbstn.androidb.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by tills13 on 2016-05-10.
 */
public class Server extends RealmObject {
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

    @PrimaryKey private int id;
    @Required private String name;

    @Required private String host;
    private int port;

    private String username;
    private String password;

    private String color;
    private String defaultDatabase;
    //private List<Database> databases;

    public Server() {
        this.defaultDatabase = "postgres";
        this.port = 5432;
    }

    public String getDefaultDatabase() {
        return defaultDatabase;
    }

    public void setDefaultDatabase(String defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }



    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
