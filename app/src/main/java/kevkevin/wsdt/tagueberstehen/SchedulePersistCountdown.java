package kevkevin.wsdt.tagueberstehen;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class SchedulePersistCountdown {
    private int countdownId;
    private
    private static SQLiteDatabase db;

    public SchedulePersistCountdown(Context context, int countdownId) {
        if (SchedulePersistCountdown.db == null) {
            SchedulePersistCountdown.db = context.openOrCreateDatabase("COUNTDOWNS",context.MODE_PRIVATE,null);
            Log.d("SchedPersCountdown","Database created.");
        }
        this.setCountdownId(countdownId);
    }

    public void saveCountdown() {
        /*SharedPreferences.Editor editor = this.prefs.edit();
        editor.putInt("ID",this.getCountdownId());
        editor.putString("LAST_EDIT","DD.MM.YYYY hh:mm:ss"); //TODO: current time/date
        editor.putString("FINISH","DD.MM.YYYY hh:mm:ss"); //TODO: timestamp/date with end (totalSeconds are calculated by that)
        editor.putString("CATEGORY","WORK"); //TODO: work, school etc.
        editor.apply();*/
    }


    // PERSIST COUNTDOWNS (SAVE TIMESTAMP WITH DATE, so only one time to save or when user edited it)
    /*public void saveCountdown(date and time);
    public void saveCountdowns(arr);
    public void getTotalSeconds(); //get totalSeconds from Date/Time from current countdown
    public void getCountdown();
    public void getCountdowns();*/

    //public void scheduleCountdownNotifications() { }



    private String getCurrentDateTime() {
        return formatGregorianDate(new GregorianCalendar());
    }

    private String getFinishDateTime(int day,int month,int year,int hour,int minute,int second) {
        return formatGregorianDate(new GregorianCalendar(year,month,day,hour,minute,second));
    }

    private GregorianCalendar getFinishDateTime(String formattedDate) {
        String[] dateArr = formattedDate.split("\\.");
        //return new GregorianCalendar(//TODO: extract here);
    }

    private String formatGregorianDate(GregorianCalendar c) { //IMPORTANT: Separation of dot must be the same for all attributes
        return c.get(Calendar.DAY_OF_MONTH)+"."+c.get(Calendar.MONTH)+"."+c.get(Calendar.YEAR)+"."+c.get(Calendar.HOUR)+"."+c.get(Calendar.MINUTE)+"."+c.get(Calendar.SECOND);
    }

    // GETTER / SETTER ++++++++++++++++++++++++++++++++++++++++++++
    public int getCountdownId() {
        return this.countdownId;
    }

    public void setCountdownId(int countdownId) {
        this.countdownId = countdownId;
    }

}
