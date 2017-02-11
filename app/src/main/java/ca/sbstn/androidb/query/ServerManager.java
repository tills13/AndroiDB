package ca.sbstn.androidb.query;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.TableListFragment;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import io.realm.Realm;

public class ServerManager {
    private static QueryExecutor executor;
    private static Server server;
    private static Database database;
    private static Table table;
    private static List<OnChangeListener> subscribers;

    static {
        subscribers = new ArrayList<>();
    }

    public static void subscribe(OnChangeListener listener) {
        subscribers.add(listener);
    }

    public static void unsubscribe(OnChangeListener listener) {
        subscribers.remove(listener);
    }

    private static void notifyDatabaseChanged(Database database) {
        for (OnChangeListener subscriber : subscribers) {
            if (subscriber instanceof OnDatabaseChangedListener) {
                ((OnDatabaseChangedListener) subscriber).onDatabaseChanged(database);
            }
        }
    }

    private static void notifyServerChanged(Server server) {
        if (Build.VERSION.SDK_INT > 23) {
            /*subscribers
                .stream()
                .filter((subscriber) -> subscriber instanceof OnServerChangedListener)
                .*/
        }

        for (OnChangeListener subscriber : subscribers) {
            if (subscriber instanceof OnServerChangedListener) {
                ((OnServerChangedListener) subscriber).onServerChanged(server);
            }
        }
    }

    private static void notifyTableChanged(Table table) {
        for (OnChangeListener subscriber : subscribers) {
            if (subscriber instanceof OnTableChangedListener) {
                ((OnTableChangedListener) subscriber).onTableChanged(table);
            }
        }
    }

    public static void setDatabase(Database database) {
        if (ServerManager.database == database) return;

        ServerManager.database = database;
        notifyDatabaseChanged(database);

        executor = QueryExecutor.forServer(ServerManager.server, ServerManager.database);
    }

    public static Database getDatabase() {
        return ServerManager.database;
    }

    public static void reloadDatabase(Context context) {
        reloadDatabase(context, null);
    }

    public static void reloadDatabase(Context context, OnDatabaseReloadedCallback callback) {
        String query = context.getResources().getString(
            R.string.db_query_fetch_database,
            database.getName()
        );

        executor.execute(query, new QueryExecutor.Callback<Database>() {
            @Override
            public boolean onError(Throwable thrown) {
                this.showError((Exception) thrown);
                return false;
            }

            @Override
            public Database onResultAsync(ResultSet results) {
                try {
                    results.beforeFirst();
                    results.next();

                    return new Database(
                        results.getString("name"),
                        results.getString("owner"),
                        results.getString("comment"),
                        results.getString("tablespace_name"),
                        results.getBoolean("is_template")
                    );
                } catch (SQLException exception) {
                    Log.e(TableListFragment.TAG, exception.getMessage());
                    this.showError(exception);
                }

                return null;
            }

            @Override
            public void onResultSync(Database database) {
                ServerManager.setDatabase(database);

                if (callback != null) {
                    callback.onDatabaseReloaded(database);
                }
            }

            private void showError(Exception exception) {
                new android.app.AlertDialog.Builder(context)
                    .setTitle("Whoops, something went wrong")
                    .setMessage(exception.getMessage())
                    .create()
                    .show();
            }
        });
    }

    public static void setServer(Server server) {
        if (ServerManager.server == server) return;

        ServerManager.server = server;
        notifyServerChanged(server);

        executor = QueryExecutor.forServer(ServerManager.server);
    }

    public static void setServer(Server server, Database database) {
        if (ServerManager.server == server && ServerManager.database == database) return;

        ServerManager.server = server;
        ServerManager.database = database;

        executor = QueryExecutor.forServer(ServerManager.server, ServerManager.database);
    }

    public static Server getServer() {
        return ServerManager.server;
    }

    public static void reloadServer() {
        Realm realm = Realm.getDefaultInstance();
        Server server = realm.where(Server.class).equalTo("name", ServerManager.getServer().getName()).findFirst();
        setServer(server);
    }

    public static void setTable(Table table) {
        if (ServerManager.table == table) return;

        ServerManager.table = table;
        notifyTableChanged(table);
    }

    public static Table getTable() {
        return table;
    }

    public static QueryExecutor getExecutor() {
        if (executor == null) throw new IllegalStateException("getExecutor called before setServer");
        return executor;
    }

    public interface OnDatabaseReloadedCallback {
        void onDatabaseReloaded(Database database);
    }

    public interface OnDatabaseChangedListener extends OnChangeListener {
        void onDatabaseChanged(Database database);
    }

    public interface OnServerChangedListener extends OnChangeListener {
        void onServerChanged(Server server);
    }

    public interface OnTableChangedListener extends OnChangeListener {
        void onTableChanged(Table table);
    }

    private interface OnChangeListener {}
}
