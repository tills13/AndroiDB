package ca.sbstn.dbtest.sql;

import java.sql.Connection;

public class DBConnection {
    private static DBConnection instance;

    private Connection connection;
    private Callback callback;

    private DBConnection() {}

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }

        return instance;
    }

    public DBConnection connect(String url, String username, String password) {
        //AsyncTask<>

        return this;
    }

    public DBConnection then(Callback callback) {
        return this;
    }

    //public void






    public interface OnConnectionStateChangedListener {
        void onConectionStateChanged(int from, int to);
    }

    public interface Callback {
        void onCallback(DBConnection connection);
    }
}
