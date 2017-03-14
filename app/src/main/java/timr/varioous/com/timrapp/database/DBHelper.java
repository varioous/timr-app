package timr.varioous.com.timrapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by holzm on 14-Feb-17.
 */

/**
 * DBHelper class
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERISON = 1;
    public static final String DATABASE_NAME = "TimrApp.db";

    private static final String DATABASE_CREATE_1 = "create table times (" +
            "id integer primary key autoincrement, " +
            "user_id integer, " +
            "status integer, " +
            "date integer, " +
            "completed integer" + ")";

    private static final String DATABASE_CREATE_2 = "create table finished_work_items  (" +
            "id integer primary key autoincrement, " +
            "timrUserId text, " +
            "startTime text, " +
            "startDate integer, " +
            "endTime text," +
            "completed integer" + ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERISON);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_1);
        db.execSQL(DATABASE_CREATE_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //nothing to do
    }
}
