package kevkevin.wsdt.tagueberstehen;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.takusemba.spotlight.OnSpotlightEndedListener;
import com.takusemba.spotlight.OnSpotlightStartedListener;
import com.takusemba.spotlight.OnTargetStateChangedListener;
import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import kevkevin.wsdt.tagueberstehen.classes.ColorPicker;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.UserLibrary;
import kevkevin.wsdt.tagueberstehen.classes.customviews.CustomEdittext;
import kevkevin.wsdt.tagueberstehen.classes.customviews.DateTimePicker.DateTimePicker;
import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.DialogMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppNotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppPurchaseMgr;
import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_InAppPurchaseMgr.*;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.GlobalAppSettingsMgr;
import kevkevin.wsdt.tagueberstehen.interfaces.IConstants_Global;

import static kevkevin.wsdt.tagueberstehen.classes.manager.interfaces.IConstants_NotificationMgr.IDENTIFIER_COUNTDOWN_ID;
import static kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces.IConstants_DatabaseMgr.*;

public class ModifyCountdownActivity extends AppCompatActivity {
    private Countdown newEditedCountdown;
    private static final String TAG = "ModifyCountdownActivity";
    private int existingCountdownId = (-1); //if edit then this value will be updated and used to overwrite existing countdown
    private InAppPurchaseMgr inAppPurchaseMgr;
    private DialogMgr dialogMgr; //important that kevkevin dialogMgr gets imported and not the one of android!
    private InAppNotificationMgr inAppNotificationMgr = new InAppNotificationMgr(); //must be a member! (to prevent influencing iapnotifications of other activities)
    private GlobalAppSettingsMgr globalAppSettingsMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_countdown);

        //InAppPurchaseMgr if validation is too low countdown can be saved, but it will not be displayed
        this.setInAppPurchaseMgr(new InAppPurchaseMgr(this));
        this.setGlobalAppSettingsMgr(new GlobalAppSettingsMgr(this));


        //ADS - START
        AdMgr adMgr = new AdMgr(this);
        adMgr.loadBannerAd((RelativeLayout) findViewById(R.id.wrappingRLForAdsModifyCountdowns));
        //ADS - END

        //Set custom onclick listener so time and datepicker show up
        setCustomOnClickListener((TextView) findViewById(R.id.startDateTimeValue), (RelativeLayout) findViewById(R.id.startDateTimeValueParent));
        setCustomOnClickListener((TextView) findViewById(R.id.untilDateTimeValue), (RelativeLayout) findViewById(R.id.untilDateTimeValueParent));

        //Create dialogMgr instance for showing purchaseDialogs when product not bought already
        this.setDialogMgr(new DialogMgr(this));

        //Set List for intervalsetter (spinner)
        HelperClass.setIntervalSpinnerConfigurations((Spinner) findViewById(R.id.notificationIntervalSpinner), R.array.countdownIntervalSpinner_LABELS, 8);
        //setIntervalSpinnerConfigurations();

        try {
            this.existingCountdownId = getIntent().getIntExtra(IDENTIFIER_COUNTDOWN_ID, -1);
            //say that we want to edit an existing countdown and not create a new one
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Could not load existing countdown.");
        }

        loadLanguagePacksCheckboxes((GridLayout) findViewById(R.id.modifyCountdownActivity_motivation_languagePacks)); //needs to be done before setFormValues()!

        if (this.existingCountdownId >= 0) {
            Log.d(TAG, "onCreate: Being in EditMode, because countdown already exists.");
            setFormValues(DatabaseMgr.getSingletonInstance(this).getAllCountdowns(this,false).get(this.existingCountdownId));
        }

        onMotivateMeToggleClick(findViewById(R.id.isActive)); //simulate click so it is always at its correct state (enabled/disabled)

        //Remove min constraint of description field (so user can let it empty)
        ((CustomEdittext) findViewById(R.id.countdownDescriptionValue)).setMinLength(0);

        //show spotlight help on start
        if (!this.getGlobalAppSettingsMgr().isModifyCountdownSpotlightHelpAlreadyShown()) {
            this.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    showSpotLightHelp();
                    //REMOVE GlobalLayoutListener
                    if (getWindow().getDecorView().getViewTreeObserver().isAlive()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            //deprecated since api 16 but our min is 15 so this if clause
                            getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                    Log.d(TAG, "showSpotLightHelp: Tried to show spotlight help.");
                }
            });
        } else {
            Log.d(TAG, "showSpotLightHelp: Spotlight has been shown before. Won't show it again.");
        }
    }

    private void showSpotLightHelp() {
        //Craft targets (setPoint[view] must have the same reihenfolge wie in arraylist zugewiesen
        SimpleTarget[] allSimpleTargets = {
                new SimpleTarget.Builder(ModifyCountdownActivity.this)
                        .setPoint(findViewById(R.id.startDateTimeIcon)) //position of target also point obj possible for getting concrete position of an object
                        .setRadius(70f)
                        .setTitle(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_startDateTimeIcon_title))
                        .setDescription(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_startDateTimeIcon_description))
                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                            @Override
                            public void onStarted(SimpleTarget target) {
                                //target started
                            }

                            @Override
                            public void onEnded(SimpleTarget target) {
                                //target ended
                            }
                        }).build(),
                new SimpleTarget.Builder(ModifyCountdownActivity.this)
                        .setPoint(findViewById(R.id.untilDateTimeIcon)) //position of target
                        .setRadius(70f)
                        .setTitle(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_untilDateTimeIcon_title))
                        .setDescription(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_untilDateTimeIcon_description))
                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                            @Override
                            public void onStarted(SimpleTarget target) {
                                //target started
                            }

                            @Override
                            public void onEnded(SimpleTarget target) {
                                //target ended
                            }
                        }).build(),
                new SimpleTarget.Builder(ModifyCountdownActivity.this)
                        .setPoint(findViewById(R.id.exampleHelpBtn4Spotlight)) //position of target
                        .setRadius(70f)
                        .setTitle(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_exampleHelpBtn_title))
                        .setDescription(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_exampleHelpBtn_description))
                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                            @Override
                            public void onStarted(SimpleTarget target) {
                                //target started
                            }

                            @Override
                            public void onEnded(SimpleTarget target) {
                                //target ended
                            }
                        }).build(),
                new SimpleTarget.Builder(ModifyCountdownActivity.this)
                        .setPoint(findViewById(R.id.categoryValue)) //position of target
                        .setRadius(90f)
                        .setTitle(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_categoryNotificationColor_title))
                        .setDescription(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_categoryNotificationColor_description))
                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                            @Override
                            public void onStarted(SimpleTarget target) {
                                //target started
                            }

                            @Override
                            public void onEnded(SimpleTarget target) {
                                //target ended
                            }
                        }).build(),
                new SimpleTarget.Builder(ModifyCountdownActivity.this)
                        .setPoint(findViewById(R.id.isActive)) //position of target
                        .setRadius(170f)
                        .setTitle(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_isActive_title))
                        .setDescription(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_isActive_description))
                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                            @Override
                            public void onStarted(SimpleTarget target) {
                                //target started
                            }

                            @Override
                            public void onEnded(SimpleTarget target) {
                                //target ended
                            }
                        }).build(),
                new SimpleTarget.Builder(ModifyCountdownActivity.this)
                        .setPoint(findViewById(R.id.showLiveCountdown)) //position of target
                        .setRadius(170f)
                        .setTitle(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_showLiveCountdown_title))
                        .setDescription(ModifyCountdownActivity.this.getResources().getString(R.string.spotlight_modifyCountdownActivity_target_showLiveCountdown_description))
                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                            @Override
                            public void onStarted(SimpleTarget target) {
                                //target started
                            }

                            @Override
                            public void onEnded(SimpleTarget target) {
                                //target ended
                            }
                        }).build()};

        //Show/Craft spotlight itself
        Spotlight.with(ModifyCountdownActivity.this)
                .setOverlayColor(ContextCompat.getColor(ModifyCountdownActivity.this, R.color.background))
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                .setTargets(allSimpleTargets)
                .setClosedOnTouchedOutside(true) //close on touch outside (otherwise we would have to implement a next-routine)
                .setOnSpotlightStartedListener(new OnSpotlightStartedListener() {
                    @Override
                    public void onStarted() {
                        //maybe show initial text (you can skip this or always redo)
                    }
                })
                .setOnSpotlightEndedListener(new OnSpotlightEndedListener() {
                    @Override
                    public void onEnded() {
                        //Save that spotlight was fully shown (so it won't show up again)
                        getGlobalAppSettingsMgr().setModifyCountdownSpotlightHelpAlreadyShown(true);
                    }
                }).start();
    }

    public void onSaveClick(View view) {
        //Is use-more-nodes package bought? (here in onclick, so it gets refreshed!) --------------------------------------------------------------
        this.getInAppPurchaseMgr().executeIfProductIsBought(INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                Log.d(TAG, "onCreate:executeIfProductIsBought: UseMoreCountdownNodes is bought. Not blocking anything.");
                //Get values from form
                saveCountdownOfForm();
            }

            @Override
            public void failure_is_false() {
                Log.d(TAG, "onCreate:executeIfProductIsBought: UseMoreCountdownNodes is NOT bought. Blocking save-Button IF already one node saved AND NOT in editing mode.");
                if (DatabaseMgr.getSingletonInstance(ModifyCountdownActivity.this).getAllCountdowns(ModifyCountdownActivity.this, false).size() > 0 && (existingCountdownId < 0)) {
                    Log.d(TAG, "onCreate:executeIfProductIsBought:OnClick: Did not save countdown, because inapp product not bought and more than one node already saved. EditMode disabled, Countdown-Id: " + existingCountdownId);
                    getDialogMgr().showDialog_InAppProductPromotion(INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString());
                    //also show toast for additional clarification
                    Toast.makeText(ModifyCountdownActivity.this, R.string.inAppProduct_notBought_useMoreCountdownNodes, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onCreate:executeIfProductIsBought: No node saved OR being in edit-mode [because countdown submitted in intent (existingCountdownId >= 0)]. So we allow saving the first one.");
                    saveCountdownOfForm();
                }
            }
        });
    }

    private void saveCountdownOfForm() {
        //Helper method because needed twice (in onSaveClick())
        loadFormValues();
        if (areFormValuesValid()) {
            DatabaseMgr.getSingletonInstance(this).saveCountdown(this, this.getNewEditedCountdown());
            Log.d(TAG, "onSaveClick: Tried to save new countdown.");
            ModifyCountdownActivity.this.finish(); //go back to main
        } else {
            //Toast.makeText(this, "Value(s) not allowed!", Toast.LENGTH_SHORT).show(); --> toasts in areFormValuesValid() more specific!
            Log.w(TAG, "onSaveClick: areFormValuesValid() delivered not true!");
        }
    }

    public void onAbortClick(View view) {
        //Finish current activity and go back to main page
        finish();
    }

    private boolean areFormValuesValid() {
        //Validate dates ------------------------------------------------------------------
        if (this.getNewEditedCountdown().getStartDateTime().matches(IConstants_Global.GLOBAL.DATETIME_FORMAT_REGEX) && this.getNewEditedCountdown().getUntilDateTime().matches(IConstants_Global.GLOBAL.DATETIME_FORMAT_REGEX)) {
            // Is UntilDateTime AFTER StartDateTime? -------------------
            //getDateTime(getStartDateTime()).compareTo(getCurrentDateTime()) > 0
            if (getNewEditedCountdown().getDateTime(getNewEditedCountdown().getStartDateTime())
                    .compareTo(getNewEditedCountdown().getDateTime(getNewEditedCountdown().getUntilDateTime())) >= 0) {
                //startdatetime is in "future" is bigger than untildatetima (bad)
                Toast.makeText(this, R.string.modifyCountdownActivity_countdown_validation_UntilAfterStartDateTime, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "areFormValuesValid: UntilDateTime needs to be AFTER StartDateTime.");
                return false;
            }
            // Is UntilDateTime AFTER StartDateTime? - END -------------
        } else {
            Log.e(TAG, "validateFormValue: Dates not valid: " + this.getNewEditedCountdown().getStartDateTime() + " /// " + this.getNewEditedCountdown().getUntilDateTime());
            Toast.makeText(this, R.string.modifyCountdownActivity_countdown_validation_DateTimeNotValid, Toast.LENGTH_LONG).show();
            return false;
        }

        //Validate lengths of texts ------------------------------------------------------------------
        Resources res = getResources();

        CustomEdittext countdownTitleValue = ((CustomEdittext) findViewById(R.id.countdownTitleValue));
        if (countdownTitleValue.getText().toString().length() < countdownTitleValue.getMinLength()) {
            Log.w(TAG, "areFormValuesValid: CountdownTitleValue is not valid!"); //do not use getHint because it might get null if used as label!
            Toast.makeText(this, String.format(res.getString(R.string.modifyCountdownActivity_countdown_validationerror_constraintMinLengthFailed), res.getString(R.string.modifyCountdownActivity_countdown_title_label), countdownTitleValue.getMinLength()), Toast.LENGTH_SHORT).show();
            return false;
        }
        CustomEdittext countdownDescriptionValue = ((CustomEdittext) findViewById(R.id.countdownDescriptionValue));
        if (countdownDescriptionValue.getText().toString().length() < countdownDescriptionValue.getMinLength()) {
            Log.w(TAG, "areFormValuesValid: CountdownDescriptionValue is not valid!");
            Toast.makeText(this, String.format(res.getString(R.string.modifyCountdownActivity_countdown_validationerror_constraintMinLengthFailed), res.getString(R.string.modifyCountdownActivity_countdown_description_label), countdownDescriptionValue.getMinLength()), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (this.userLibraryCheckboxes.size() <= 0) {
            //Toast is done somewhere else so just block user from saving
            Log.w(TAG, "areFormValuesValid: User has no userLibraries installed.");
            return false;
        }

        //TODO: ADD HERE FURTHER VALIDATIONS (also make them seeable in CustomEdittext (if one used))
        return true;
    }

    private void setFormValues(Countdown countdown) {
        ((CustomEdittext) findViewById(R.id.countdownTitleValue)).setText(countdown.getCountdownTitle());
        ((CustomEdittext) findViewById(R.id.countdownDescriptionValue)).setText(countdown.getCountdownDescription());
        ((TextView) findViewById(R.id.startDateTimeValue)).setText(countdown.getStartDateTime());
        ((TextView) findViewById(R.id.untilDateTimeValue)).setText(countdown.getUntilDateTime());
        (findViewById(R.id.categoryValue)).setBackgroundColor(Color.parseColor(countdown.getCategory()));
        ((ToggleButton) findViewById(R.id.isActive)).setChecked(countdown.isActive());
        //set associated entry of interval seconds to spinner
        ((Spinner) findViewById(R.id.notificationIntervalSpinner)).setSelection(Arrays.asList(getResources().getStringArray(R.array.countdownIntervalSpinner_VALUES)).indexOf("" + countdown.getNotificationInterval())); //reduce about 5 otherwise we would add 5 every time we edited it!
        ((ToggleButton) findViewById(R.id.showLiveCountdown)).setChecked(countdown.isShowLiveCountdown());

        GridLayout languagePackList = findViewById(R.id.modifyCountdownActivity_motivation_languagePacks);
        for (UserLibrary languagePack : countdown.getUserSelectedUserLibraries().values()) {
            for (int i = 0; i < languagePackList.getChildCount(); i++) {
                if (languagePackList.getChildAt(i).getTag() != null) {
                    CheckBox tmpCheckbox = ((CheckBox) languagePackList.getChildAt(i));
                    if (languagePackList.getChildAt(i).getTag().toString().equals(languagePack.getLibId()+"")) {
                        tmpCheckbox.setChecked(true);
                    }
                }
            }
        }
    }

    private void loadFormValues() {
        this.setNewEditedCountdown(new Countdown(this,
                ((CustomEdittext) findViewById(R.id.countdownTitleValue)).getText().toString(),
                ((CustomEdittext) findViewById(R.id.countdownDescriptionValue)).getText().toString(),
                ((TextView) findViewById(R.id.startDateTimeValue)).getText().toString(),
                ((TextView) findViewById(R.id.untilDateTimeValue)).getText().toString(),
                ColorPicker.getBackgroundColorHexString(findViewById(R.id.categoryValue)),
                ((ToggleButton) findViewById(R.id.isActive)).isChecked(),
                Integer.parseInt(getResources().getStringArray(R.array.countdownIntervalSpinner_VALUES)[((Spinner) findViewById(R.id.notificationIntervalSpinner)).getSelectedItemPosition()]),
                ((ToggleButton) findViewById(R.id.showLiveCountdown)).isChecked(),
                loadSelectedUserLibrariesFromCheckboxes()/*--> TMP --> TODO: Checkboxen für languagePacks*/));
        // .getProgress()+5 for old seekbar slider +5 seconds by default (because if 0) app crashes
        //line above: gets selected spinner items position and uses this to get the associated array entry with the correct value in seconds.

        //Overwrite countdown id if countdown exists already
        if (this.existingCountdownId >= 0) {
            this.getNewEditedCountdown().setCountdownId(this.existingCountdownId);
            Log.d(TAG, "loadFormValues: Overwrote/Edited countdown.");
        } else {
            Log.d(TAG, "loadFormValues: New countdown created.");
        }
    }

    //Color picker for category value
    public void onClickOpenColorPicker(final View view) {
        this.getInAppPurchaseMgr().executeIfProductIsBought(INAPP_PRODUCTS.CHANGE_NOTIFICATION_COLOR.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                Log.d(TAG, "onClickOpenColorPicker:executeIfProductIsBought: ChangeNotification is bought. Tried to open color picker");
                ColorPicker.openColorPickerDialog(ModifyCountdownActivity.this, view, Color.parseColor(ColorPicker.getBackgroundColorHexString(findViewById(R.id.categoryValue))), false);
            }

            @Override
            public void failure_is_false() {
                Log.d(TAG, "onClickOpenColorPicker:executeIfProductIsBought: ChangeNotification is NOT bought. Blocking color-Button.");
                getDialogMgr().showDialog_InAppProductPromotion(INAPP_PRODUCTS.CHANGE_NOTIFICATION_COLOR.toString());
                Toast.makeText(ModifyCountdownActivity.this, R.string.inAppProduct_notBought_changeNotificationColor, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Show service specific field if toggle button is ON (isActive)
    public void onMotivateMeToggleClick(View view) {
        //Add/Remove field(s) if countdown should run in background
        boolean tbIsChecked = ((ToggleButton) view).isChecked();

        //disable/enable fields (if toggle button is checked = active then buttons should be enabled. otherwise it is false
        TextView notificationIntervalTextView = findViewById(R.id.notificationIntervalTextView);
        RelativeLayout notificationIntervalDescriptionAndSpinner = findViewById(R.id.notificationIntervalInputAndHelp);
        TextView languagePackListTextView = findViewById(R.id.customNotification_random_generic_texts_allArrays_headingLbl);
        RelativeLayout languagePackList = findViewById(R.id.modifyCountdownActivity_motivation_languagePacks_parent);

        if (!tbIsChecked) {
            notificationIntervalTextView.setVisibility(View.GONE);
            notificationIntervalDescriptionAndSpinner.setVisibility(View.GONE);
            languagePackList.setVisibility(View.GONE);
            languagePackListTextView.setVisibility(View.GONE);
        } else {
            notificationIntervalTextView.setVisibility(View.VISIBLE);
            notificationIntervalDescriptionAndSpinner.setVisibility(View.VISIBLE);
            languagePackList.setVisibility(View.VISIBLE);
            languagePackListTextView.setVisibility(View.VISIBLE);
        }
    }

    private HashMap<String, UserLibrary> loadSelectedUserLibrariesFromCheckboxes() {
        HashMap<String, UserLibrary> selectedUserLibraries = new HashMap<>();

        for (CheckBox userLibraryCheckbox : this.userLibraryCheckboxes) {
            if (userLibraryCheckbox.isChecked()) {
                String libId = userLibraryCheckbox.getTag().toString();
                selectedUserLibraries.put(libId, DatabaseMgr.getSingletonInstance(this).getAllUserLibraries(this,false).get(libId));
            }
        }

        if (selectedUserLibraries.size() <= 0) {
            Log.d(TAG, "loadSelectedUserLibrariesFromCheckboxes: User did not select any user libs. Selecting default one.");
            try {
                String libId = this.userLibraryCheckboxes.get(0).getTag().toString(); //assumes that at least one userlib is installed!
                selectedUserLibraries.put(libId, DatabaseMgr.getSingletonInstance(this).getAllUserLibraries(this,false).get(libId));
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this, R.string.modifyCountdownActivity_countdown_userLibrary_noInstalled,Toast.LENGTH_SHORT).show();
                Log.e(TAG, "loadSelectedUserLibrariesFromCheckboxes: No user libs installed.");
                e.printStackTrace();
            }
        }

        return selectedUserLibraries;
    }

    private ArrayList<CheckBox> userLibraryCheckboxes = new ArrayList<>();

    private void loadLanguagePacksCheckboxes(@NonNull GridLayout superiorLayoutView) {
        for (UserLibrary languagepack : DatabaseMgr.getSingletonInstance(this).getAllUserLibraries(this,false).values()) { //pre imkrement!
            //Print Checkboxes etc.
            CheckBox languagePackCheckbox = new CheckBox(this);
            languagePackCheckbox.setTag(languagepack.getLibId()); //en, de etc.
            this.userLibraryCheckboxes.add(languagePackCheckbox);
            superiorLayoutView.addView(languagePackCheckbox); //before text of checkbox
            TextView languagePackLbl = new TextView(this);
            languagePackLbl.setText(String.format(getString(R.string.modifyCountdownActivity_countdown_userLibrary_lblCheckbox),languagepack.getLibName(),languagepack.getLines().size()));
            superiorLayoutView.addView(languagePackLbl);
        }
        Log.d(TAG, "loadLanguagePacksCheckboxes: Tried to load all language packs.");
    }

    //GETTER/SETTER -----------------------------------------------------
    public Countdown getNewEditedCountdown() {
        return newEditedCountdown;
    }

    public void setNewEditedCountdown(Countdown newEditedCountdown) {
        this.newEditedCountdown = newEditedCountdown;
    }

    public InAppPurchaseMgr getInAppPurchaseMgr() {
        return this.inAppPurchaseMgr;
    }

    public void setInAppPurchaseMgr(InAppPurchaseMgr inAppPurchaseMgr) {
        this.inAppPurchaseMgr = inAppPurchaseMgr;
    }

    public DialogMgr getDialogMgr() {
        return dialogMgr;
    }

    public void setDialogMgr(DialogMgr dialogMgr) {
        this.dialogMgr = dialogMgr;
    }


    // ################################################################################################################
    // TIMER/DATE PICKER ##############################################################################################
    // ################################################################################################################

    public void setCustomOnClickListener(@NonNull final TextView targetTextView, @NonNull RelativeLayout parentView) {
        /*parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //do not use this v, because we also can click on other childs of parentView.
                //create onClick new instance to also keep now-member uptodate (only when instantiating refreshed)
                DateTimePickerMgr dateTimePickerMgr = new DateTimePickerMgr(getFragmentManager(), targetTextView);

                String viewTag = (String) targetTextView.getTag();
                if (viewTag != null) {
                    if (viewTag.equals("modifyCountdownActivity_countdown_untilDateTime_label")) {
                        //just add 2 hours for untilDateTime per default (TODO: Does it also work at 23:50 e.g.? --> 01:50
                        dateTimePickerMgr.setSelectedHour(dateTimePickerMgr.getSelectedHour()+2);
                    }
                }
                dateTimePickerMgr.showDialog();
            }
        });*/


        //GregorianCalendar now = new GregorianCalendar(); //now
        GregorianCalendar now = new GregorianCalendar(); //so current time gets automatically set
        final DateTimePicker DATETIMEPICKER = new DateTimePicker(getSupportFragmentManager(), targetTextView);

        String viewTag = (String) targetTextView.getTag();
        int addToHourUntilDateTime = 0; //add two hours to be greater than startdatetime
        if (viewTag != null) {
            if (viewTag.equals("modifyCountdownActivity_countdown_untilDateTime_label")) {
                addToHourUntilDateTime = 2;
            }
        }

        String currentDateTime = String.format(getString(R.string.dateTimePicker_format_DateTime),
                String.format(getString(R.string.dateTimePicker_format_date), now.get(Calendar.DAY_OF_MONTH), (now.get(Calendar.MONTH) + 1), now.get(Calendar.YEAR))
                , String.format(IConstants_Global.GLOBAL.LOCALE, "%02d",
                        now.get(Calendar.HOUR_OF_DAY) + addToHourUntilDateTime),
                String.format(IConstants_Global.GLOBAL.LOCALE, "%02d", now.get(Calendar.MINUTE)), String.format(IConstants_Global.GLOBAL.LOCALE, "%02d", now.get(Calendar.SECOND)));
        Log.d(TAG, "setCustomOnClickListener: Current-Datetime->" + currentDateTime);
        targetTextView.setText(currentDateTime);

        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DATETIMEPICKER.showDateTimePicker();
            }
        });
    }

    // ACTION BAR ------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_modify_countdown, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_showSpotlightHelp:
                showSpotLightHelp(); //show it explicetely again
                Log.d(TAG, "onOptionsItemSelected: Explicetely showing spotlight help again. User requested it.");
                break;
            default:
                Log.e(TAG, "onOptionsItemSelected: Button does not exist: " + item.getItemId());
        }
        return true;
    }

    // ######################### HELP CLICK METHOD ########################################

    /**
     * In order that the method knows which help text to show, the provided view has to contain a valid TAG!
     * --> TAG has to be the exact string resource name! (e.g. modifyCountdownActivity_countdown_category_textview_fieldDescription)
     */
    public void onHelpClick(View view) {
        this.getInAppNotificationMgr().showQuestionMarkHelpText(this, view, (ViewGroup) findViewById(R.id.wrappingRLForAdsModifyCountdowns));
    }

    public InAppNotificationMgr getInAppNotificationMgr() {
        return inAppNotificationMgr;
    }

    public GlobalAppSettingsMgr getGlobalAppSettingsMgr() {
        return globalAppSettingsMgr;
    }

    public void setGlobalAppSettingsMgr(GlobalAppSettingsMgr globalAppSettingsMgr) {
        this.globalAppSettingsMgr = globalAppSettingsMgr;
    }
}
