package timr.varioous.com.timrapp.model;

/**
 * Created by holzm on 28-Feb-17.
 */

/**
 * Model for User
 */
public class User {

    private String nfcTagId;
    private String timeUserName;
    private int userId;
    private String name;

    public User() {

    }

    public String getNfcTagId() {
        return nfcTagId;
    }

    public void setNfcTagId(String nfcTagId) {
        this.nfcTagId = nfcTagId;
    }

    public String getTimeUserName() {
        return timeUserName;
    }

    public void setTimeUserName(String timeUserName) {
        this.timeUserName = timeUserName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
