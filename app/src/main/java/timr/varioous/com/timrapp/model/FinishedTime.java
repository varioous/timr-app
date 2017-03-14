package timr.varioous.com.timrapp.model;

/**
 * Created by holzm on 14-Feb-17.
 */

/**
 * Model for finished time
 */
public class FinishedTime {
    private long id;
    private String timrUserId;
    private String startTime;
    private String endTime;
    private long completed;
    private long startDate;

    public FinishedTime() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimrUserId() {
        return timrUserId;
    }

    public void setTimrUserId(String timrUserId) {
        this.timrUserId = timrUserId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }
}
