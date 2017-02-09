package ca.sbstn.androidb.sql;

import java.io.Serializable;

public class Database implements Serializable {
    private String name;
    private String comment;
    private String owner;
    private String tableSpace;
    private boolean isTemplate;

    public Database() {}

    public Database(String name, String owner) {
        this(name, owner, null);
    }

    public Database(String name, String owner, String comment) {
        this(name, owner, comment, null, false);
    }

    public Database(String name, String owner, String comment, String tableSpace, boolean isTemplate) {
        this.name = name;
        this.owner = owner;
        this.comment = comment;
        this.tableSpace = tableSpace;
        this.isTemplate = isTemplate;
    }

    /*public String getConnectionString() {
        return String.format(Locale.getDefault(), "jdbc:postgresql://%s:%d/%s",
            this.server.getHost(),
            this.server.getPort(),
            this.name
        );
    }*/

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public boolean getIsTemplate() {
        return this.isTemplate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }

    public String getTableSpace() {
        return this.tableSpace;
    }
}
