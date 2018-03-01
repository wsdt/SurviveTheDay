package kevkevin.wsdt.tagueberstehen.classes.customviews;


import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewParent;

import kevkevin.wsdt.tagueberstehen.R;
import kevkevin.wsdt.tagueberstehen.classes.Constants;


/**
 * Custom Edittext view (special constraints [no enter e.g.], etc. and specific design (see XML))
 */
public class CustomEdittext extends android.support.v7.widget.AppCompatEditText {
    private static final String TAG = "CustomEdittext";
    private TextInputLayout textInputLayout; //SHOULD BE THE PARENT, setError etc.

    /**
     * Constraints
     */
    private int minLength = 1; //at least one char by default (changeable by setter)

    public CustomEdittext(Context context) {
        super(context);
    }

    public CustomEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * In constructor view has no parent, after constructor call this one follows where parent view called addView (see activity lifecycle)
     * TextInputLayout is nested so we have to call getParent() two times to get the real parent.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.setTextInputLayout(this.getParent().getParent()); //parent should be a textInputLayout (with double parent we get it in this case)
    }

    /**
     * Used to determine, whether provided text or value is according our constraints.
     * Params: We are just using all params from onTextChanged for future use (instead of calculating or getting themselves from view)
     */
    public boolean isInputValid(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        //IMPORTANT: Use text var instead of lengthbefore/after for validation (especially for length purposes!) --> when you write more than maxlength then it might be false
        return (text.length() >= this.getMinLength()); //add more constraints with ||
    }

    /**
     * @param isInputValid: If input is valid, then normal formatting, if not red drawable on the right, text red, etc.
     */
    private void formatValidInvalidInput(boolean isInputValid) throws NullPointerException {
        Resources res = getResources();

        if (this.getTextInputLayout() != null && this.isEnabled()) { //only do this if enabled (e.g. for textView simulation)
            if (isInputValid) {
                this.getTextInputLayout().setErrorEnabled(false); //remove errors
            } else {
                this.getTextInputLayout().setError(String.format(res.getString(R.string.modifyCountdownActivity_countdown_validationerror_constraintMinLengthFailed_generic), Constants.COUNTDOWN.COUNTDOWN_TITLE_LENGTH_MIN));
            }
        }
    }


    /**
     * For formatting etc. when input is invalid
     * also only show warning if constraints failed AND text got changed at least one time (usability)
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        formatValidInvalidInput(isInputValid(text, start, lengthBefore, lengthAfter)); //format acc. to constraints (every text change, not on keypress or similar)
    }

    /**
     * This method is used to escape/remove ENTERs from the value
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //necessary when someone copies enter into field (altough keyboard does not allow it)
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            //Ignore enter key
            return true;
        }

        //All other keys default way
        return super.onKeyDown(keyCode, event);
    }


    //GETTER/SETTER ++++++++++++++++++++++++++++++++++++++++++
    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public TextInputLayout getTextInputLayout() {
        return textInputLayout;
    }

    public void setTextInputLayout(ViewParent textInputLayout) {
        if (textInputLayout instanceof TextInputLayout) {
            this.textInputLayout = ((TextInputLayout) textInputLayout);
        } else if (textInputLayout != null) {
            Log.e(TAG, "setTextInputLayout: CustomEdittext-Parent is NOT a textInputLayout. Won't show error msgs to user. Setting view to null. Parent is->" + textInputLayout.getClass());
            this.textInputLayout = null;
        } else {
            Log.e(TAG, "setTextInputLayout: Parent is null! Presumably it is not set.");
        }
    }
}
