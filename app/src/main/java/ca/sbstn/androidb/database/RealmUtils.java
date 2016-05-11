package ca.sbstn.androidb.database;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by tills13 on 2016-05-10.
 */
public class RealmUtils {
    public static Realm realm;
    //public static Context context;

    //public static void initialize(Context context) {
    //    RealmUtils.context = context;
    //}

    public static Realm getRealm(Context context) {
        RealmConfiguration config = new RealmConfiguration.Builder(context).build();
        return Realm.getInstance(config);
    }
}
