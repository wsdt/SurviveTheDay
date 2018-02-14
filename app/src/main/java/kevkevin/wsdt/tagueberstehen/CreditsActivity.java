package kevkevin.wsdt.tagueberstehen;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kevkevin.wsdt.tagueberstehen.classes.AdManager;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        //Ads - START
        AdManager adManager = new AdManager(this);
        adManager.initializeAdmob();
        adManager.loadBannerAd((RelativeLayout) findViewById(R.id.wrappingRLForAds));
        //Ads - END

        final Resources res = getResources();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.contactMe);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailDeveloper = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", res.getString(R.string.app_owner_mail), null));
                emailDeveloper.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.creditsActivity_floatingButton_contactAppOwnerMailIntent_mail_subject));
                emailDeveloper.putExtra(Intent.EXTRA_TEXT, res.getString(R.string.creditsActivity_floatingButton_contactAppOwnerMailIntent_mail_body));
                emailDeveloper.putExtra(Intent.EXTRA_EMAIL, new String[]{res.getString(R.string.app_owner_mail)});
                startActivity(Intent.createChooser(emailDeveloper, res.getString(R.string.creditsActivity_floatingButton_contactAppOwnerMailIntent_mail_intentTitle)));
            }
        });

        //So the html gets interpretet
        TextView introduction = ((TextView) findViewById(R.id.creditsText));
        //Makes links etc. clickable
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            introduction.setText(Html.fromHtml(getString(R.string.creditsActivity_creditsIntroductionText), Html.FROM_HTML_MODE_LEGACY)); //deprecated for newer versions
        } else {
            introduction.setText(Html.fromHtml(getString(R.string.creditsActivity_creditsIntroductionText)));
        }
        introduction.setMovementMethod(LinkMovementMethod.getInstance());

        //add all icons to view
        addAllIconCreditsToView();
    }

    private void addAllIconCreditsToView() {
        GridLayout content = (GridLayout) findViewById(R.id.creditList);
        Resources res = getResources();
        String drawableCreditTemplate = getResources().getString(R.string.creditsActivity_drawableCreditTemplate);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_app), "http://www.freepik.com", "Freepik", "https://www.flaticon.com/", "Flaticon"), R.drawable.app_icon);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_plus), "https://www.flaticon.com/authors/smashicons", "Smashicons", "https://www.flaticon.com/", "Flaticon"), R.drawable.plus);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_trash), "http://www.freepik.com", "Freepik", "https://www.flaticon.com/", "Flaticon"), R.drawable.delete);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_edit), "https://www.flaticon.com/authors/smashicons", "Smashicons", "https://www.flaticon.com/", "Flaticon"), R.drawable.edit_16px);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_delete), "http://www.freepik.com", "Freepik", "https://www.flaticon.com/", "Flaticon"), R.drawable.delete_16px);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_motivateMe), "http://www.freepik.com", "Freepik", "https://www.flaticon.com/", "Flaticon"), R.drawable.switchtoggle_16px);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_conversationMark), "http://www.freepik.com", "Freepik", "https://www.flaticon.com/", "Flaticon"), R.drawable.notification_generic_blue);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_stopTimer), "https://www.flaticon.com/authors/smashicons", "Smashicons", "https://www.flaticon.com/", "Flaticon"), R.drawable.notification_timebased_color);
    }

    private void addIconCreditToView(GridLayout creditList, String credits, int drawable) {
        ImageButton newImageButton = new ImageButton(this);
        TextView newTextView = new TextView(this);

        newImageButton.setImageResource(drawable);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 150;
        layoutParams.height = 150; //to change imagebuttons to similar size
        layoutParams.setMargins(0, 0, 5, 0);
        newImageButton.setLayoutParams(layoutParams); //changes the size of button
        newImageButton.setScaleType(ImageView.ScaleType.FIT_XY);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newTextView.setText(Html.fromHtml(credits, Html.FROM_HTML_MODE_LEGACY)); //deprecated for newer versions
        } else {
            newTextView.setText(Html.fromHtml(credits));
        }

        newTextView.setMovementMethod(LinkMovementMethod.getInstance());
        newTextView.setTextColor(getResources().getColor(R.color.dark));

        creditList.addView(newImageButton);
        creditList.addView(newTextView);
    }
}
