package kevkevin.wsdt.tagueberstehen;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.ArrayList;

public class Notification { //one instance for every countdown or similar
    private Context activityThisTarget;
    private Intent resultIntent;
    private int mNotificationId = 0; //start with 0 should be first notification (index starts at 0)
    private ArrayList<NotificationCompat.Builder> notifications = new ArrayList<>(); //NOT static, because every instance should have own Arraylist!
    private NotificationManager mNotifyMgr;
    private PendingIntent resultPendingIntent; //open countdown of current notifications

    public Notification (Context activityThisTarget,Class targetActivityClass, NotificationManager mNotifyMgr, int CountdownId) { //(NotifyManager) getSystemService(Notification_Service);
        this.setActivityThisTarget(activityThisTarget);
        this.setmNotifyMgr(mNotifyMgr);

        //With countdown ID we are able to look in our persistent storage for the right countdown
        Intent tmp = new Intent(this.getActivityThisTarget(),targetActivityClass);
        tmp.putExtra("COUNTDOWN_ID",CountdownId);
        this.setResultIntent(tmp);
        this.setResultPendingIntent(
                PendingIntent.getActivity(
                        this.getActivityThisTarget(),
                        0,
                        this.getResultIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        Log.d("Notification","mNotificationId: "+getmNotificationId());
    }

    public void issueNotification(int mNotificationId) {
        try {
            this.getmNotifyMgr().notify(mNotificationId, this.getNotifications().get(mNotificationId).build());
        } catch(IndexOutOfBoundsException e) {
            Log.e("issueNotification","Notification not defined! Notification-No.: "+mNotificationId);
            e.printStackTrace();
        }
    }

    public int createNotification(String title,String text, int icon) {
        this.getNotifications().add( //save with current id
                new NotificationCompat.Builder(this.getActivityThisTarget())
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentIntent(this.getResultPendingIntent())
                .setContentText(text));

        incrementmNotificationId(); //because of new notification (but return old No. because index starts at 0!
        return (mNotificationId-1); //saved under that id-1 because incremented
    }

    //GETTER / SETTER +++++++++++++++++++++++++
    public Context getActivityThisTarget() {
        return activityThisTarget;
    }

    public void setActivityThisTarget(Context activityThisTarget) {
        this.activityThisTarget = activityThisTarget;
    }


    public int getmNotificationId() {
        return this.mNotificationId;
    }

    public void incrementmNotificationId() {
        Log.d("incrementNoficiationId","Old: "+this.getmNotificationId()+"/ Incremented: "+(this.getmNotificationId()+1));
        this.mNotificationId = (this.getmNotificationId()+1);
    }

    public NotificationManager getmNotifyMgr() {
        return mNotifyMgr;
    }

    public void setmNotifyMgr(NotificationManager mNotifyMgr) {
        this.mNotifyMgr = mNotifyMgr;
    }

    public ArrayList<NotificationCompat.Builder> getNotifications() {
        return this.notifications;
    }

    public void setNotifications(ArrayList<NotificationCompat.Builder> notifications) {
        this.notifications = notifications;
    }

    public PendingIntent getResultPendingIntent() {
        return resultPendingIntent;
    }

    public void setResultPendingIntent(PendingIntent resultPendingIntent) {
        this.resultPendingIntent = resultPendingIntent;
    }

    public Intent getResultIntent() {
        return resultIntent;
    }

    public void setResultIntent(Intent resultIntent) {
        this.resultIntent = resultIntent;
    }
}
