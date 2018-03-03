package kevkevin.wsdt.tagueberstehen.classes.manager;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.GlobalAppSettingsMgr;

public class InAppNotificationMgr {
    private static final String TAG = "InAppNotifMgr";
    private boolean isNotificationShowing = false; //used to prevent user opening several notifications at the same time.

    /** This method shows an inAppNotification, but crafts
     * MUST NOT BE STATIC: If static we have no control whether current activity is showing inappnotification or a hidden one
     * FOR THAT CASE HelperClass must be a member of the calling class!! */
    public <VG extends ViewGroup> void showQuestionMarkHelpText(@NonNull Activity activity, @NonNull View view, @NonNull VG wrappingRootViewgroup) {
        try {
            String tag = (String) view.getTag(); //casting works if null :)
            if (tag != null) {
                Resources res = activity.getResources();
                showInAppNotification(activity, res.getString(R.string.generic_help_inappnotification_default_title),
                        res.getString(res.getIdentifier(tag, "string", activity.getPackageName())),
                        R.drawable.coloraccent_help_btn, wrappingRootViewgroup, true); //prevent multiple at the same time, so user click on help btn might not do anything when notifciation is showing
            } else {
                Toast.makeText(activity, R.string.error_contactAdministrator, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onHelpClick: Could not show help message. Tag is null!");
            }
        } catch (Resources.NotFoundException | NullPointerException e) {
            //inform user that button is not broken, but we have no helpttext here
            Toast.makeText(activity,R.string.helptext_error_nohelptextfound,Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onHelpClick: Could not show help message. ERROR happened. See below.");
            e.printStackTrace();
        }
    }

    /**
     * Method is used to show an inAppNotification / Closing method is after this method
     * MUST NOT BE STATIC: To prevent other activities from influencing their inappnotification show behaviour (because animation is running on paused activity)
     * FOR THAT CASE HelperClass must be a member of the calling class!!
     */
    public <VG extends ViewGroup> void showInAppNotification(@NonNull final Activity activity, @NonNull final String title, @NonNull final String text, final int drawableIcon, @NonNull VG rootLayout, boolean preventMultipleSimultaneousNotifications) {
        if (isNotificationShowing && preventMultipleSimultaneousNotifications) {
            //if already other inappnotifications are shown on the same activity then prevent this method call to create another one
            //we prevent this automatically by other activities by helper class instance
            Toast.makeText(activity,R.string.inappnotification_preventmultiple_closeothermsgorwait,Toast.LENGTH_SHORT).show();
            Log.d(TAG, "showInAppNotification: Prevented opening new in app notification, before previous one was closed.");
            return;
        } else if (preventMultipleSimultaneousNotifications) {
            isNotificationShowing = true; //block other method calls
        }

        //Wait until views are drawn (for size etc.)
        Log.d(TAG, "showInappNotification: Trying to show inAppNotification.");
        final RelativeLayout notificationContent = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.inappnotification_template, rootLayout, false); //call after values assigned so correct height
        rootLayout.addView(notificationContent); //necessary because view not a child anymore (layout template inflate)

        //Show notification if activity is loaded, so we can calculate necessary params etc.
        final ViewTreeObserver observer = rootLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if ((drawableIcon != (-1) && drawableIcon != 0) && !title.equals("") && !text.equals("")) {
                    //Set notification contents
                    ((ImageView) notificationContent.findViewById(R.id.notificationImage)).setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), drawableIcon));
                    ((TextView) notificationContent.findViewById(R.id.notificationHeading)).setText(title);
                    ((TextView) notificationContent.findViewById(R.id.notificationFullText)).setText(text);
                    Log.d(TAG, "showInappNotification: Tried to assign values to view. ");

                    //Set negative Y but assign values BEFORE (because height might change because of wrap content)
                    final int hiddenPosition = ((notificationContent.getHeight()) * (-1));
                    notificationContent.setY(hiddenPosition); //assign height*-1 so notification will get exactly behind display
                    Log.d(TAG, "showInappNotification: Tried to positionate inapp-notification outside screen: " + hiddenPosition);

                    //Set OnClickListener for close Button
                    notificationContent.findViewById(R.id.notificationClose).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "showInappNotification:onClick: Tried to close inapp notification, because close btn clicked.");
                            closeActiveInAppNotification(notificationContent, 0); //close instantly when clicking on button
                        }
                    });

                    final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(notificationContent, "y", 0); //get it back to positive animated (0 because we want no small space above shape)
                    objectAnimator.setDuration(Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS); //1,5 seconds
            /*objectAnimator.setRepeatCount(1); //show it and hide it after duration expired [making this with count var and restarting animation in onAnimationEnd()]
            objectAnimator.setRepeatMode(ValueAnimator.REVERSE);*/
                    objectAnimator.addListener(new Animator.AnimatorListener() {
                        int count = 0;

                        @Override
                        public void onAnimationStart(Animator animator) {
                            Log.d(TAG, "onAnimationStart: Inapp notification animation started.");
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            if (count++ < 1) {
                                closeActiveInAppNotification(notificationContent, new GlobalAppSettingsMgr(activity).getInAppNotificationShowDurationInMs() + Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_ANIMATION_DURATION_IN_MS); //how long should be notification displayed (adding animation duration because time period delay is inclusive animation));
                            } else {
                                Log.d(TAG, "onAnimationEnd: Inapp notification animation finished.");
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                            Log.d(TAG, "onAnimationCancel: Cancelled animation of inappnotification_template.");
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                            Log.d(TAG, "onAnimationRepeat: Repeated in app notification animation.");
                            //objectAnimator.setStartDelay(3000); //do not use animator (start delay would be ignored) does not work because not called
                        }
                    });
                    objectAnimator.start();
                } else {
                    notificationContent.setVisibility(View.GONE);
                    Log.w(TAG, "showInAppNotification: Delivered method parameters not allowed (maybe empty strings or drawable 0||-1.");
                }
                notificationContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    //Helper method for closeBtn of inAppNotification and objectAnimator end / MUST NOT BE STATIC
    public void closeActiveInAppNotification(@NonNull RelativeLayout notificationContent, int delay) {
        ObjectAnimator objectAnimatorClose = ObjectAnimator.ofFloat(notificationContent, "alpha", 0);
        objectAnimatorClose.setDuration(Constants.COUNTDOWN_ACTIVITY.INAPP_NOTIFICATION_CLOSE_ANIMATION_DURATION_IN_MS);
        objectAnimatorClose.setStartDelay(delay);
        objectAnimatorClose.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //here we can set it to false, regardless whether preventing multiple instances is on
                isNotificationShowing = false; //allow other method calls opening a new inapp notification
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimatorClose.start(); //no delay if clicked on close btn, but delay if automatic close
        Log.d(TAG, "closeActiveInAppNotification: Tried to close active in app notification.");
    }
}
