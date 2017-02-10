package ca.sbstn.androidb.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AndroiDB extends Application {
    public static final String SHARED_PREFS_KEY = "AndroiDB_Prefs";
    public static final String PREFERENCES_KEY_SERVERS = "AndroiDB_Servers";

    @Override
    public void onCreate() {
        super.onCreate();

        this.attemptInstantiateJDBCDrivers();

        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded()
                .schemaVersion(1).build();

        Realm.setDefaultConfiguration(configuration);
    }

    private boolean attemptInstantiateJDBCDrivers() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            return false;
        }

        return true;
    }
}
