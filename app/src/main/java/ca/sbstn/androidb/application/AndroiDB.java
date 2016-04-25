package ca.sbstn.androidb.application;

import android.app.Application;

/**
 * Created by tyler on 21/04/16.
 */
public class AndroiDB extends Application {
    public static final String SHARED_PREFS_KEY = "AndroiDB_Prefs";
    public static final String PREFERENCES_KEY_SERVERS = "AndroiDB_Servers";

    public AndroiDB() {
        super();

        this.attemptInstantiateJDBCDrivers();
    }

    private boolean attemptInstantiateJDBCDrivers() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
        } catch (ClassNotFoundException|InstantiationException|IllegalAccessException e) {
            return false;
        }

        return true;
    }
}
