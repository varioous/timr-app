package timr.varioous.com.timrapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timr.varioous.com.timrapp.model.FinishedTime;
import timr.varioous.com.timrapp.model.Time;

/**
 * Created by holzm on 14-Feb-17.
 */

/**
 * DataSource to access database
 */
public class TimeDataSource {

    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {"id", "user_id", "status", "date", "completed"};
    private String[] allColumnsFinished = {"id", "timrUserId", "startTime", "endTime", "completed", "startDate"};

    public TimeDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Insert time
     * @param time
     */
    public void insertTime(Time time) {
        ContentValues values = new ContentValues();
        values.put("user_id", time.getUser_id());
        values.put("status", time.getStatus());
        values.put("date", time.getDate());
        values.put("completed", time.getCompleted());
        database.insert("times", null,
                values);
    }

    /**
     * Insert finished time (complete work day)
     * @param timrUser
     * @param startTime
     * @param endTime

     * @param startDate
     */
    public void insertTimeFinished(String timrUser, String startTime, String endTime, long startDate) {
        ContentValues values = new ContentValues();
        values.put("timrUserId", timrUser);
        values.put("startTime", startTime);
        values.put("endTime", endTime);
        values.put("startDate", startDate);
        values.put("completed", 0);
        database.insert("finished_work_items", null,
                values);
    }

    /**
     * Finish time
     * @param userId
     */
    public void finishTime(long userId) {
        database.execSQL("UPDATE times SET completed=1 WHERE user_id= " + userId);
    }

    /**
     * Set that a work item is successfully tranfered to the server
     * @param id
     */
    public void setSuccessfullySyncedItem(long id) {
        database.execSQL("UPDATE finished_work_items SET completed=1 WHERE id= " + id);
    }

    /**
     * Get the last time entry of a user
     * @param userId
     * @return
     */
    public Time getLastTimeOfUser(long userId) {
        Cursor cursor = database.query("times", allColumns, "user_id = " + userId, null, null, null, "id desc");
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Time time = new Time();
            time.setStatus(cursor.getLong(cursor.getColumnIndexOrThrow("status")));
            time.setUser_id(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
            time.setDate(cursor.getLong(cursor.getColumnIndexOrThrow("date")));
            time.setCompleted(cursor.getLong(cursor.getColumnIndexOrThrow("completed")));
            cursor.close();
            return time;
        } else {
            return null;
        }
    }

    /**
     * Get all times of a user that are opened (not finished)
     * @param userId
     * @return
     */
    public List<Time> getOpenTimesOfUser(long userId) {
        List<Time> times = new ArrayList<Time>();
        Cursor cursor = database.query("times", allColumns, "user_id = " + userId + " and completed = 0", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Time time = new Time();
            time.setStatus(cursor.getLong(cursor.getColumnIndexOrThrow("status")));
            time.setUser_id(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
            time.setDate(cursor.getLong(cursor.getColumnIndexOrThrow("date")));
            time.setCompleted(cursor.getLong(cursor.getColumnIndexOrThrow("completed")));
            times.add(time);
            cursor.moveToNext();
        }
        cursor.close();
        return times;
    }

    /**
     * Get all items, that are stored and not yet synched to the server
     * @return
     */
    public List<FinishedTime> getItemsToSync() {
        List<FinishedTime> finishedTimes = new ArrayList<FinishedTime>();
        Cursor cursor = database.query("finished_work_items", allColumnsFinished, "completed = 0", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FinishedTime finishedTime = new FinishedTime();
            finishedTime.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            finishedTime.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("endTime")));
            finishedTime.setCompleted(cursor.getLong(cursor.getColumnIndexOrThrow("completed")));
            finishedTime.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("startTime")));
            finishedTime.setTimrUserId(cursor.getString(cursor.getColumnIndexOrThrow("timrUserId")));

            finishedTimes.add(finishedTime);
            cursor.moveToNext();
        }
        cursor.close();
        return finishedTimes;
    }

    /**
     * Get all finished Work items of a user
     * @param timrUserId
     * @return
     */
    public List<FinishedTime> getFinishedTimesOfUser(String timrUserId) {
        List<FinishedTime> finishedTimes = new ArrayList<FinishedTime>();
        Cursor cursor = database.query("finished_work_items", allColumnsFinished, "timrUserId = ?", new String[]{timrUserId}, null, null, "id desc","25");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FinishedTime finishedTime = new FinishedTime();
            finishedTime.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("endTime")));
            finishedTime.setCompleted(cursor.getLong(cursor.getColumnIndexOrThrow("completed")));
            finishedTime.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("startTime")));
            finishedTime.setTimrUserId(cursor.getString(cursor.getColumnIndexOrThrow("timrUserId")));
            finishedTime.setStartDate(cursor.getLong(cursor.getColumnIndexOrThrow("startDate")));
            finishedTimes.add(finishedTime);
            cursor.moveToNext();
        }
        cursor.close();
        return finishedTimes;
    }
}
