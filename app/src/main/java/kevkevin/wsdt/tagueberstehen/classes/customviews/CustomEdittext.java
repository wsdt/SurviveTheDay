package kevkevin.wsdt.tagueberstehen.classes.customviews;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;


/** Custom Edittext view (special constraints [no enter e.g.], and specific design (see XML)) */
public class CustomEdittext extends android.support.v7.widget.AppCompatEditText {
    public CustomEdittext(Context context) {
        super(context);
    }

    public CustomEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

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
}
