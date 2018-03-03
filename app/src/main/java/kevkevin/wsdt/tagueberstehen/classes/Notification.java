package kevkevin.wsdt.tagueberstehen.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import kevkevin.wsdt.tagueberstehen.R;

public class Notification {
    private static final String TAG = "Notification";
    private static NotificationManager notificationManager;
    private NotificationCompat.Builder builtNotification;
    private Context context;
    private int notificationId;
    private int smallIcon;
    private int notificationColor;
    private String title;
    private String text;
    private String ticker;
    private boolean autoCancel;
    private boolean onGoing;
    private boolean addShareAction;
    private Class targetForIntent;
    private PendingIntent pendingIntent;
    private PendingIntent sharePendingIntent;


    public Notification(@NonNull Context context, int notificationId, int smallIcon, int notificationColor, String title, String text, String ticker,
                        boolean autoCancel, boolean onGoing, boolean addShareAction, Class targetForIntent, PendingIntent pendingIntent) {
        this.setContext(context);
        this.setNotificationId(notificationId);
        this.setSmallIcon(smallIcon);
        this.setNotificationColor(notificationColor);
        this.setTitle(title);
        this.setText(text);
        this.setTicker(ticker);
        this.setAutoCancel(autoCancel);
        this.setOnGoing(onGoing);
        this.setAddShareAction(addShareAction);
        this.setTargetForIntent(targetForIntent);
        this.setPendingIntent(pendingIntent);

        if (Notification.getNotificationManager() == null) {
            Notification.setNotificationManager((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        }
    }

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public static void setNotificationManager(NotificationManager notificationManager) {
        Notification.notificationManager = notificationManager;
    }

    public void issue() {
        try {
            Notification.getNotificationManager().notify(this.getNotificationId(), this.getBuiltNotification().build());
        } catch(NullPointerException e) {
            Log.e(TAG, "issueNotification: Notification not issued. CustomNotification-No.: "+this.getNotificationId());
            e.printStackTrace();
        }
    }

    public NotificationCompat.Builder build() {
        return new NotificationCompat.Builder(this.getContext(),Constants.CUSTOMNOTIFICATION.DEFAULT_NOTIFICATION_CHANNEL)
                        .setSmallIcon(this.getSmallIcon())
                        .setContentTitle(this.getTitle())
                        //onMs = how long on / offMs = how long off (repeating, so blinking!)
                        //USE category color
                        .setLights(this.getNotificationColor(), Constants.CUSTOMNOTIFICATION.NOTIFICATION_BLINK_ON_TIME_IN_MS, Constants.CUSTOMNOTIFICATION.NOTIFICATION_BLINK_OFF_TIME_IN_MS)
                        .setTicker(this.getTicker())
                        .setAutoCancel(this.isAutoCancel()) //remove NOT after clicking on it (realizing with button instead [action below])
                        .setOngoing(this.isOnGoing()) //notification IS REMOVABLE
                        .setContentIntent(getPendingIntent())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getText())) //make notification extendable
                        .addAction(R.drawable.colorgrey555_share,this.getContext().getString(R.string.actionBar_countdownActivity_menu_shareCountdown_title), this.getSharePendingIntent())
                        .setContentText(this.getText());
    }

    //GETTER/SETTER ---------------------------
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Class getTargetForIntent() {
        return targetForIntent;
    }

    public void setTargetForIntent(Class targetForIntent) {
        this.targetForIntent = targetForIntent;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(int smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public boolean isAutoCancel() {
        return autoCancel;
    }

    public void setAutoCancel(boolean autoCancel) {
        this.autoCancel = autoCancel;
    }

    public boolean isOnGoing() {
        return onGoing;
    }

    public void setOnGoing(boolean onGoing) {
        this.onGoing = onGoing;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    public boolean isAddShareAction() {
        return addShareAction;
    }

    public void setAddShareAction(boolean addShareAction) {
        this.addShareAction = addShareAction;
    }

    public int getNotificationColor() {
        return notificationColor;
    }

    public void setNotificationColor(int notificationColor) {
        this.notificationColor = notificationColor;
    }

    public PendingIntent getSharePendingIntent() {
        return sharePendingIntent;
    }

    public void setSharePendingIntent(PendingIntent sharePendingIntent) {
        this.sharePendingIntent = sharePendingIntent;
    }

    public NotificationCompat.Builder getBuiltNotification() {
        return builtNotification;
    }

    public void setBuiltNotification(NotificationCompat.Builder builtNotification) {
        this.builtNotification = builtNotification;
    }
}
