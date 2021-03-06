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
import android.support.v7.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kevkevin.wsdt.tagueberstehen.annotations.Enhance;
import kevkevin.wsdt.tagueberstehen.classes.manager.AdMgr;

@Enhance (message = "XML looks terrible, create a nicer UI for this activity. You can also add yourself as developer here if you want.")
public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        final Resources res = getResources();
        FloatingActionButton fab = findViewById(R.id.contactMe);
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

        //Bad style, but hacking now
        String credits = getResources().getString(R.string.creditsActivity_creditSourcesAndAuthors);
        TextView newTextView = ((TextView) findViewById(R.id.quotesField2));
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newTextView.setText(Html.fromHtml(credits, Html.FROM_HTML_MODE_LEGACY)); //deprecated for newer versions
        } else {
            newTextView.setText(Html.fromHtml(credits));
        }

        newTextView.setMovementMethod(LinkMovementMethod.getInstance());
        newTextView.setTextColor(getResources().getColor(R.color.colorDark_111));
        newTextView.setLinkTextColor(getResources().getColor(R.color.color_for_links));

        //add all icons to view
        addAllIconCreditsToView();
    }

    private void addAllIconCreditsToView() {
        GridLayout content = findViewById(R.id.creditList);
        Resources res = getResources();
        String drawableCreditTemplate = getResources().getString(R.string.creditsActivity_drawableCreditTemplate);
        addIconCreditToView(content, String.format(drawableCreditTemplate, res.getString(R.string.creditsActivity_drawableCreditTemplate_app), "http://www.freepik.com", "Freepik", "https://www.flaticon.com/", "Flaticon"), R.drawable.app_icon);
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
        newTextView.setTextColor(getResources().getColor(R.color.colorDark_111));
        newTextView.setLinkTextColor(getResources().getColor(R.color.color_for_links));

        creditList.addView(newImageButton);
        creditList.addView(newTextView);
    }
}
