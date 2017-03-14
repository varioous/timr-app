package timr.varioous.com.timrapp.model;

/**
 * Created by holzm on 14-Feb-17.
 */

/**
 * Model for Time
 */
public class Time {
    private long id;
    private long user_id;
    private long status;
    private long date;
    private long completed;

    public Time() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }
}
