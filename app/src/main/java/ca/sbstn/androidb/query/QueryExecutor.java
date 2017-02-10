package ca.sbstn.androidb.query;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;

public class QueryExecutor {
    private static final String TAG = "QueryExecutor";
    private static Handler handler;
    private static ExecutorService executor;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    static {
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newFixedThreadPool(2);
    }

    private QueryExecutor(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public static QueryExecutor forServer(Server server) {
        return QueryExecutor.forServer(
            server.getHost(),
            server.getPort(),
            server.getDefaultDatabase(),
            server.getUsername(),
            server.getPassword()
        );
    }

    public static QueryExecutor forServer(Server server, Database database) {
        return QueryExecutor.forServer(
            server.getHost(),
            server.getPort(),
            database.getName(),
            server.getUsername(),
            server.getPassword()
        );
    }

    public static  QueryExecutor forServer(String host, int port, String database, String username, String password) {
        if (database == null || database.equals("")) {
            database = "postgres";
        }

        return new QueryExecutor(host, port, database, username, password);
    }

    public void execute(String query) {
        this.execute(query, null);
    }

    public void execute(GetResultsCallback resultsCallback) {
        this.execute(resultsCallback, null);
    }

    public <T> void execute(String query, Callback<T> callback) {
        QueryRunner<T> runner = new QueryRunner<>(this, query, callback);
        executor.execute(runner);
    }

    public <T> void execute(GetResultsCallback resultsCallback, Callback<T> callback) {
        QueryRunner<T> runner = new QueryRunner<>(this, resultsCallback, callback);
        executor.execute(runner);
    }

    private static class QueryRunner<T> implements Runnable {
        private static final String TAG = "QueryRunner";

        private GetResultsCallback query;
        private Callback<T> callback;
        private QueryExecutor executor;

        QueryRunner(QueryExecutor executor, final String query, Callback<T> callback) {
            this(executor, (connection) -> {
                try {
                    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    return statement.executeQuery(query);
                } catch (SQLException e) {
                    Log.d(QueryRunner.TAG, e.getMessage());

                    return null;
                }
            }, callback);
        }

        QueryRunner(QueryExecutor executor, GetResultsCallback query, Callback<T> callback) {
            this.executor = executor;
            this.query = query;
            this.callback = callback;
        }

        @Override
        public void run() {
            String url = String.format(
                Locale.getDefault(),
                "jdbc:postgresql://%s:%d/%s",
                this.executor.host,
                this.executor.port,
                this.executor.database
            );

            try {
                Connection connection = DriverManager.getConnection(url, this.executor.username, this.executor.password);
                ResultSet results = this.query.getResultSet(connection);

                if (this.callback != null) {
                    final T mResult = this.callback.onResultAsync(results);
                    handler.post(() -> callback.onResultSync(mResult));
                }

                connection.close();
            } catch (Exception exception) {
                Log.e(QueryExecutor.TAG, exception.getMessage());

                if (this.callback != null) {
                    handler.post(() -> {
                        if (this.callback.onError(exception)) {
                            callback.onResultSync(null);
                        }
                    });
                }
            }
        }
    }

    public interface GetResultsCallback {
        ResultSet getResultSet(Connection connection) throws SQLException;
    }

    static abstract class AbstractCallback<T> implements Callback<T> {
        private static final String TAG = "QECallback";
        public boolean onError(Throwable thrown) {
            Log.e(AbstractCallback.TAG, thrown.getMessage());
            return true;
        }
    }

    public static class BaseCallback<T> extends AbstractCallback<T> {
        Throwable thrown;

        @Override
        public boolean onError(Throwable thrown) {
            this.thrown = thrown;
            return super.onError(thrown);
        }

        @Override
        public T onResultAsync(ResultSet result) {
            return null;
        }

        @Override
        public void onResultSync(T result) {

        }

        public Throwable getThrownException() {
            return this.thrown;
        }
    }

    public interface Callback<T> {
        boolean onError(Throwable thrown);
        T onResultAsync(ResultSet result);
        void onResultSync(T result);
    }
}
