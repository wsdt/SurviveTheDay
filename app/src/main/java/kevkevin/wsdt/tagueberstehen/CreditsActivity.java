package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Ads - START
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadBannerAd((RelativeLayout) findViewById(R.id.wrappingRLForAds));
        //Ads - END

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.contactMe);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailDeveloper = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto","kevin.riedl.privat@gmail.com", null));
                emailDeveloper.putExtra(Intent.EXTRA_SUBJECT, "SurviveTheDay: Review");
                emailDeveloper.putExtra(Intent.EXTRA_TEXT, "Please only German or English messages. :)");
                emailDeveloper.putExtra(Intent.EXTRA_EMAIL, new String[] {"kevin.riedl.privat@gmail.com"});
                startActivity(Intent.createChooser(emailDeveloper, "Send e-mail.."));
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        //So the html gets interpretet
        TextView introduction = ((TextView) findViewById(R.id.creditsText));
        //Makes links etc. clickable
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            introduction.setText(Html.fromHtml(getString(R.string.creditsText), Html.FROM_HTML_MODE_LEGACY)); //deprecated for newer versions
        } else {
            introduction.setText(Html.fromHtml(getString(R.string.creditsText)));
        }
        introduction.setMovementMethod(LinkMovementMethod.getInstance());

        //add all icons to view
        addAllIconCreditsToView();

        //APP ICON: <div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
        //CREDITS: CAMPFIRE ICON: <div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
        // Plus Icon: <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
        // Trash Icon: <div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
        //Warning icon: <div>Icons made by <a href="https://www.flaticon.com/authors/twitter" title="Twitter">Twitter</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
        // Edit Icon: <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
        // Delete small icon: <div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
        // MotivateMeToggle: <div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    }

    private void addAllIconCreditsToView() {
        GridLayout content = (GridLayout) findViewById(R.id.creditList);
        addIconCreditToView(content,"<div><b>App Icon</b><br />Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>",R.drawable.app_icon);
        addIconCreditToView(content,"<div><b>Plus Icon</b><br />Icons made by <a href=\"https://www.flaticon.com/authors/smashicons\" title=\"Smashicons\">Smashicons</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>",R.drawable.plus);
        addIconCreditToView(content,"<div><b>Trash Icon</b><br />Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>",R.drawable.delete);
        addIconCreditToView(content,"<div><b>Edit Icon</b><br />Icons made by <a href=\"https://www.flaticon.com/authors/smashicons\" title=\"Smashicons\">Smashicons</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>",R.drawable.edit_16px);
        addIconCreditToView(content,"<div><b>Delete Icon</b><br />Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>",R.drawable.delete_16px);
        addIconCreditToView(content,"<div><b>Motivate me toggle</b><br />Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>",R.drawable.switchtoggle_16px);
        addIconCreditToView(content,"<div><b>Conversation mark</b><br />Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>", R.drawable.notification_generic_blue);
        addIconCreditToView(content,"<div><b>Stoptimer</b><br />Icons made by <a href=\"https://www.flaticon.com/authors/smashicons\" title=\"Smashicons\">Smashicons</a> from <br /><a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <br /><a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>",R.drawable.notification_timebased_color);
    }

    private void addIconCreditToView(GridLayout creditList, String credits, int drawable) {
        ImageButton newImageButton = new ImageButton(this);
        TextView newTextView = new TextView(this);

        newImageButton.setImageResource(drawable);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 150;
        layoutParams.height = 150; //to change imagebuttons to similar size
        newImageButton.setLayoutParams(layoutParams); //changes the size of button
        newImageButton.setScaleType(ImageView.ScaleType.FIT_XY);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newTextView.setText(Html.fromHtml(credits, Html.FROM_HTML_MODE_LEGACY)); //deprecated for newer versions
        } else {
            newTextView.setText(Html.fromHtml(credits));
        }

        newTextView.setMovementMethod(LinkMovementMethod.getInstance());

        creditList.addView(newImageButton);
        creditList.addView(newTextView);
    }
}
