package timr.varioous.com.timrapp.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import timr.varioous.com.timrapp.R;
import timr.varioous.com.timrapp.database.TimeDataSource;
import timr.varioous.com.timrapp.model.FinishedTime;
import timr.varioous.com.timrapp.model.Time;
import timr.varioous.com.timrapp.model.User;
import timr.varioous.com.timrapp.timr.TimrInterfaceTask;

/**
 * Main Activity - Time Tracking Interface
 */
public class TimrActivity extends Activity {
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;

    private List<User> users;

    public static final long STATUS_KOMMEN = 1;
    public static final long STATUS_GEHEN = 2;

    private int userIdClicked = 0;

    private TextView textViewName;
    private TextView textViewHours;
    private TextView textViewTags;
    private TextView textViewGreeting;

    /**
     * Get Users that are registered
     *
     * @return
     */
    private List<User> getUsers() {
        List<User> users = new ArrayList<User>();

        User user_1 = new User();
        user_1.setNfcTagId("XX XX XX XX XX XX XX");
        user_1.setName("John\nDoe");
        user_1.setTimeUserName("XXX-1");
        user_1.setUserId(1);
        users.add(user_1);

        User user_2 = new User();
        user_2.setNfcTagId("XX XX XX XX XX XX XX");
        user_2.setName("Joe\nDoe");
        user_2.setTimeUserName("XXX-2");
        user_2.setUserId(3);
        users.add(user_2);

        User user_3 = new User();
        user_3.setNfcTagId("XX XX XX XX XX XX XX");
        user_3.setName("Solo\nASK");
        user_3.setTimeUserName("XXX-3");
        user_3.setUserId(2);
        users.add(user_3);

        return users;
    }

    /**
     * On Create Method
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timr);
        resolveIntent(getIntent());
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.hideSystemUI();
        this.textViewName = (TextView) findViewById(R.id.textViewName);
        this.textViewHours = (TextView) findViewById(R.id.textViewHours);
        this.textViewTags = (TextView) findViewById(R.id.textViewTags);
        this.textViewGreeting = (TextView) findViewById(R.id.textViewGreeting);

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true)});

        this.users = getUsers();

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //start task to send to timr
                            TimrInterfaceTask task = new TimrInterfaceTask(getApplicationContext());
                            task.execute();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        //automatic sync every hour
        timer.schedule(doAsynchronousTask, 0, 600000);
    }

    /**
     * Called when a nfc tag is detected
     *
     * @param text
     * @param locale
     * @param encodeInUtf8
     * @return
     */
    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    /**
     * onResume method
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.hideSystemUI();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
        }
    }

    /**
     * onPause method
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }
    }

    /**
     * Resolve Intent method, called when a nfc tag is detected
     *
     * @param intent
     */
    private void resolveIntent(Intent intent) {
        this.hideSystemUI();
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String hexTagId = getHex(id);

            // check if tag is known
            for (User u : this.users) {
                if (hexTagId.equals(u.getNfcTagId())) {
                    this.userIdClicked = u.getUserId();
                    //check if the person is coming or going
                    TimeDataSource timeDataSource = new TimeDataSource(getApplicationContext());
                    timeDataSource.open();
                    Time lastTimeOfUser = timeDataSource.getLastTimeOfUser(this.userIdClicked);
                    timeDataSource.close();

                    boolean coming = false;
                    if (lastTimeOfUser == null || lastTimeOfUser.getCompleted() == 1 || lastTimeOfUser.getStatus() == STATUS_GEHEN) {
                        coming = true;
                    }

                    if (coming) {
                        //user is coming
                        this.textViewTags.setText("#i #am #not #doing #shit #today");
                        this.textViewGreeting.setText("Hello");

                        Time time = new Time();
                        time.setCompleted(0);
                        time.setDate(System.currentTimeMillis());
                        time.setStatus(STATUS_KOMMEN);
                        time.setUser_id(this.userIdClicked);
                        timeDataSource.open();
                        timeDataSource.insertTime(time);
                        timeDataSource.close();
                        this.textViewHours.setText(String.valueOf(this.getHoursThisWeek(u)));
                    } else {
                        //user is going
                        this.textViewTags.setText("#great #job #party #hard #afternoon");
                        this.textViewGreeting.setText("Bye");

                        Time time = new Time();
                        time.setCompleted(0);
                        time.setDate(System.currentTimeMillis());
                        time.setStatus(STATUS_GEHEN);
                        time.setUser_id(this.userIdClicked);
                        timeDataSource.open();
                        timeDataSource.insertTime(time);
                        List<Time> getTimeOfDay = timeDataSource.getOpenTimesOfUser(this.userIdClicked);
                        timeDataSource.close();

                        Time kommen = null;
                        Time gehen = time;

                        for (Time t : getTimeOfDay) {
                            if (t.getStatus() == STATUS_KOMMEN) {
                                kommen = t;
                            }
                        }

                        timeDataSource.open();
                        int hoursDuration = (int) (((gehen.getDate() - kommen.getDate()) / (1000 * 60 * 60)) % 24);

                        timeDataSource.insertTimeFinished(u.getTimeUserName(), this.milisecToTimrDateString(kommen.getDate()), this.milisecToTimrDateString(gehen.getDate()), kommen.getDate());

                        timeDataSource.finishTime(this.userIdClicked);
                        timeDataSource.close();

                        this.textViewHours.setText(this.getHoursThisWeek(u) + " (" + hoursDuration + ")");
                    }

                    this.textViewName.setText(u.getName());
                    this.textViewName.setVisibility(View.VISIBLE);
                    this.textViewHours.setVisibility(View.VISIBLE);
                    this.textViewTags.setVisibility(View.VISIBLE);
                    this.textViewGreeting.setVisibility(View.VISIBLE);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            textViewName.setVisibility(View.INVISIBLE);
                            textViewHours.setVisibility(View.INVISIBLE);
                            textViewTags.setVisibility(View.INVISIBLE);
                            textViewGreeting.setVisibility(View.INVISIBLE);
                        }
                    }, 8000);
                }
            }
        }
    }

    /**
     * Calculates how many times the users already worked this week
     *
     * @param u
     * @return
     */
    private int getHoursThisWeek(User u) {
        TimeDataSource timeDataSource = new TimeDataSource(getApplicationContext());
        timeDataSource.open();
        List<FinishedTime> lastTimeOfUser = timeDataSource.getFinishedTimesOfUser(u.getTimeUserName());
        timeDataSource.close();

        Calendar cal = Calendar.getInstance();
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_WEEK, -1);
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        long timeStartOfWeek = cal.getTimeInMillis();

        int minutes = 0;

        for (FinishedTime f : lastTimeOfUser) {
            if (f.getStartDate() >= timeStartOfWeek) {
                long duration = this.timrDateStringToMilisec(f.getEndTime()) - this.timrDateStringToMilisec(f.getStartTime());
                int durationMinutes = (int) duration / 1000 / 60;
                //consider automatic break time
                if (durationMinutes > 361) {
                    durationMinutes -= 45;
                }
                //sum up minutes
                minutes += durationMinutes;
            }
        }

        return minutes / 60;
    }

    /**
     * Hides the system ui (activity is fullscreen)
     */
    private void hideSystemUI() {
        View mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * Byte to hex
     *
     * @param bytes
     * @return
     */
    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * NewIntent method
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    /**
     * Calculated miliseconds to valid timr date string
     *
     * @param millisec
     * @return
     */
    private String milisecToTimrDateString(long millisec) {
        Date d = new Date(millisec);
        String dateString;
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        dateString = df.format("yyyy-MM-dd", d).toString();
        dateString = dateString + "T";
        dateString = dateString + df.format("HH:mm:ss", d).toString();
        return dateString;
    }

    /**
     * Calculated valid timr date string to miliseconds
     *
     * @param timrDate
     * @return
     */
    private long timrDateStringToMilisec(String timrDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(timrDate);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}
