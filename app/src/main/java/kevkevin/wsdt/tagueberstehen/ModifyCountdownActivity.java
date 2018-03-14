package kevkevin.wsdt.tagueberstehen;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;
import kevkevin.wsdt.tagueberstehen.classes.ColorPicker;
import kevkevin.wsdt.tagueberstehen.classes.Constants;
import kevkevin.wsdt.tagueberstehen.classes.Countdown;
import kevkevin.wsdt.tagueberstehen.classes.manager.DialogMgr;
import kevkevin.wsdt.tagueberstehen.classes.HelperClass;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppNotificationMgr;
import kevkevin.wsdt.tagueberstehen.classes.manager.InAppPurchaseMgr;
import kevkevin.wsdt.tagueberstehen.classes.Languagepack;
import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;
import kevkevin.wsdt.tagueberstehen.classes.customviews.CustomEdittext;
import kevkevin.wsdt.tagueberstehen.classes.customviews.DateTimePicker.DateTimePicker;

public class ModifyCountdownActivity extends AppCompatActivity {
    private Countdown newEditedCountdown;
    private static final String TAG = "ModifyCountdownActivity";
    private int existingCountdownId = (-1); //if edit then this value will be updated and used to overwrite existing countdown
    private InAppPurchaseMgr inAppPurchaseMgr;
    private DialogMgr dialogMgr; //important that kevkevin dialogMgr gets imported and not the one of android!
    private InAppNotificationMgr inAppNotificationMgr = new InAppNotificationMgr(); //must be a member! (to prevent influencing iapnotifications of other activities)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_countdown);

        //InAppPurchaseMgr if validation is too low countdown can be saved, but it will not be displayed
        this.setInAppPurchaseMgr(new InAppPurchaseMgr(this));


        //ADS - START
        AdMgr adMgr = new AdMgr(this);
        adMgr.initializeAdmob();
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
            this.existingCountdownId = getIntent().getIntExtra(Constants.CUSTOMNOTIFICATION.IDENTIFIER_COUNTDOWN_ID, -1);
            //say that we want to edit an existing countdown and not create a new one
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Could not load existing countdown.");
        }

        loadLanguagePacksCheckboxes((GridLayout) findViewById(R.id.modifyCountdownActivity_motivation_languagePacks)); //needs to be done before setFormValues()!

        if (this.existingCountdownId >= 0) {
            Log.d(TAG, "onCreate: Being in EditMode, because countdown already exists.");
            setFormValues((DatabaseMgr.getSingletonInstance(this).getCountdown(this, false, this.existingCountdownId)));
        }

        onMotivateMeToggleClick(findViewById(R.id.isActive)); //simulate click so it is always at its correct state (enabled/disabled)

        //Remove min constraint of description field (so user can let it empty)
        ((CustomEdittext) findViewById(R.id.countdownDescriptionValue)).setMinLength(0);
    }

    public void onSaveClick(View view) {
        //Is use-more-nodes package bought? (here in onclick, so it gets refreshed!) --------------------------------------------------------------
        this.getInAppPurchaseMgr().executeIfProductIsBought(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
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
                    getDialogMgr().showDialog_InAppProductPromotion(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.USE_MORE_COUNTDOWN_NODES.toString());
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
            DatabaseMgr.getSingletonInstance(this).setSaveCountdown(this, this.getNewEditedCountdown());
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
        if (this.getNewEditedCountdown().getStartDateTime().matches(Constants.GLOBAL.DATETIME_FORMAT_REGEX) && this.getNewEditedCountdown().getUntilDateTime().matches(Constants.GLOBAL.DATETIME_FORMAT_REGEX)) {
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
        for (Languagepack languagePack : countdown.getQuotesLanguagePacksObj().values()) {
            for (int i = 0; i < languagePackList.getChildCount(); i++) {
                if (languagePackList.getChildAt(i).getTag() != null) {
                    CheckBox tmpCheckbox = ((CheckBox) languagePackList.getChildAt(i));
                    if (languagePackList.getChildAt(i).getTag().toString().equals(languagePack.getLangPackId())) {
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
                loadSelectedLanguagePacksFromCheckboxes()/*--> TMP --> TODO: Checkboxen für languagePacks*/));
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
        this.getInAppPurchaseMgr().executeIfProductIsBought(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.CHANGE_NOTIFICATION_COLOR.toString(), new HelperClass.ExecuteIfTrueSuccess_OR_IfFalseFailure_AfterCompletation() {
            @Override
            public void success_is_true() {
                Log.d(TAG, "onClickOpenColorPicker:executeIfProductIsBought: ChangeNotification is bought. Tried to open color picker");
                ColorPicker.openColorPickerDialog(ModifyCountdownActivity.this, view, Color.parseColor(ColorPicker.getBackgroundColorHexString(findViewById(R.id.categoryValue))), false);
            }

            @Override
            public void failure_is_false() {
                Log.d(TAG, "onClickOpenColorPicker:executeIfProductIsBought: ChangeNotification is NOT bought. Blocking color-Button.");
                getDialogMgr().showDialog_InAppProductPromotion(Constants.INAPP_PURCHASES.INAPP_PRODUCTS.CHANGE_NOTIFICATION_COLOR.toString());
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

    private String[] loadSelectedLanguagePacksFromCheckboxes() {
        StringBuilder selectedLanguagePacks = new StringBuilder();
        int countLanguagePacks = 0;
        for (CheckBox languagePackCheckbox : this.languagePackCheckboxes) {
            if (languagePackCheckbox.isChecked()) {
                if ((countLanguagePacks++) > 0) {
                    selectedLanguagePacks.append(Constants.STORAGE_MANAGERS.DATABASE_STR_MGR.TABLES.ZWISCHENTABELLE_COU_QLP.ATTRIBUTE_ADDITIONALS.LANGUAGE_ID_LIST_SEPARATOR);
                } //before languagepack and only if already one added
                selectedLanguagePacks.append(languagePackCheckbox.getTag().toString());
            }
        }
        if (countLanguagePacks <= 0) { //keep this validation, because user might uncheck all boxes
            //no pack selected, choosing default one (english)
            Log.d(TAG, "loadSelectedLanguagePacksFromCheckboxes: User did not select language pack. Used default one.");
            selectedLanguagePacks.append("en");
        }
        return selectedLanguagePacks.toString().split(Constants.STORAGE_MANAGERS.DATABASE_STR_MGR.TABLES.ZWISCHENTABELLE_COU_QLP.ATTRIBUTE_ADDITIONALS.LANGUAGE_ID_LIST_SEPARATOR); //string to array
    }

    private ArrayList<CheckBox> languagePackCheckboxes = new ArrayList<>();

    private void loadLanguagePacksCheckboxes(@NonNull GridLayout superiorLayoutView) {
        for (Languagepack languagepack : Languagepack.getAllLanguagePacks(this).values()) { //pre imkrement!
            //Print Checkboxes etc.
            CheckBox languagePackCheckbox = new CheckBox(this);
            languagePackCheckbox.setTag(languagepack.getLangPackId()); //en, de etc.
            this.languagePackCheckboxes.add(languagePackCheckbox);
            superiorLayoutView.addView(languagePackCheckbox); //before text of checkbox
            TextView languagePackLbl = new TextView(this);
            languagePackLbl.setText(languagepack.getLabelString(this));
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

    public void setCustomOnClickListener(@NonNull TextView v, @NonNull RelativeLayout parentView) {
        //GregorianCalendar now = new GregorianCalendar(); //now
        GregorianCalendar now = new GregorianCalendar(); //so current time gets automatically set
        final DateTimePicker DATETIMEPICKER = new DateTimePicker(getSupportFragmentManager(), v);

        String viewTag = (String) v.getTag();
        int addToHourUntilDateTime = 0; //add two hours to be greater than startdatetime
        if (viewTag != null) {
            if (viewTag.equals("modifyCountdownActivity_countdown_untilDateTime_label")) {
                addToHourUntilDateTime = 2;
            }
        }

        String currentDateTime = String.format(getString(R.string.dateTimePicker_format_DateTime),
                String.format(getString(R.string.dateTimePicker_format_date),now.get(Calendar.DAY_OF_MONTH),(now.get(Calendar.MONTH) + 1),now.get(Calendar.YEAR))
                ,String.format(Constants.GLOBAL.LOCALE,"%02d",
                        now.get(Calendar.HOUR_OF_DAY)+addToHourUntilDateTime),
                String.format(Constants.GLOBAL.LOCALE,"%02d", now.get(Calendar.MINUTE)),String.format(Constants.GLOBAL.LOCALE,"%02d", now.get(Calendar.SECOND)));
        Log.d(TAG, "setCustomOnClickListener: Current-Datetime->"+currentDateTime);
        v.setText(currentDateTime);

        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DATETIMEPICKER.showDateTimePicker();
            }
        });
    }

    // ######################### HELP CLICK METHOD ########################################

    /**
     * In order that the method knows which help text to show, the provided view has to contain a valid TAG!
     * --> TAG has to be the exact string resource name! (e.g. modifyCountdownActivity_countdown_category_textview_fieldDescription)
     */
    public void onHelpClick(View view) {
        this.getInAppNotificationMgr().showQuestionMarkHelpText(this,view,(ViewGroup) findViewById(R.id.wrappingRLForAdsModifyCountdowns));
    }

    public InAppNotificationMgr getInAppNotificationMgr() {
        return inAppNotificationMgr;
    }
}
