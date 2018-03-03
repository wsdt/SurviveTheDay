package kevkevin.wsdt.tagueberstehen.classes;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;

import kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.DatabaseMgr;

public class Languagepack {
    private static final String TAG = "LanguagePack";
    private static HashMap<String,Languagepack> allLanguagePacks = new HashMap<>(); //important that here declared
    private static boolean currentlyLoadingAllLanguagePacks = false; //to avoid multiple cursor allocations
    private SparseArray<Quote> languagePackQuotes;
    private String langPackId;

    public Languagepack(@NonNull Context context, @NonNull String langPackId) {
        this.setLangPackId(langPackId);
        //getAllLanguagePacks().put(this.getLangPackId(),this); //add new instance to hashmap (with own id, by hashmap no double values possible)
        downloadAllLanguagePacks(context); //download/extract all lpacks if not already done
    }

    public static void downloadAllLanguagePacks(@NonNull Context context) { //if no quotes in db, this method might run every time!
        if (Languagepack.allLanguagePacks.size() <= 0 && !currentlyLoadingAllLanguagePacks) {
            Log.d(TAG, "downloadAllLanguagePacks: Trying to extract all languagepacks from db.");
            //Quotes not extracted now, doing it now.
            currentlyLoadingAllLanguagePacks = true; //to block other method calls
            Languagepack.setAllLanguagePacks(DatabaseMgr.getSingletonInstance(context).getAllLanguagePacks(context,false));
            currentlyLoadingAllLanguagePacks = false; //now allowing it again
        }
    }

    public String getLabelString(@NonNull Activity activity) {
        Resources res = activity.getResources();
        String labelStr = "";
        try {
            labelStr = String.format(res.getString(res.getIdentifier("customNotification_random_generic_texts_allArrays_Lbls_"+this.getLangPackId(), "string", activity.getPackageName())), getLanguagePackQuotes(activity).size());
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getLabelString: Could not find langaugepack resource! Returned empty string.");
            e.printStackTrace();
        }
        return labelStr;
    }

    //GETTER/SETTER
    public String getLangPackId() {
        return langPackId;
    }

    public void setLangPackId(String langPackId) {
        this.langPackId = langPackId;
    }


    public static HashMap<String,Languagepack> getAllLanguagePacks(@NonNull Context context) {
        downloadAllLanguagePacks(context); //extract from db if not already done
        return allLanguagePacks;
    }

    public static void setAllLanguagePacks(HashMap<String,Languagepack> allLanguagePacks) {
        Languagepack.allLanguagePacks = allLanguagePacks;
    }

    public void setLanguagePackQuotes(SparseArray<Quote> languagePackQuotes) {
        this.languagePackQuotes = languagePackQuotes;
    }

    public SparseArray<Quote> getLanguagePackQuotes(@NonNull Context context) {
        if (this.languagePackQuotes == null) {
            SparseArray<Quote> quoteSparseArray = Quote.getAllQuotes(context);
            SparseArray<Quote> filteredQuoteList = new SparseArray<>();
            for (int i = 0; i < quoteSparseArray.size(); i++) {
                Quote tmpQuote = quoteSparseArray.get(i);
                if (tmpQuote.getLanguagePack().equals(this.getLangPackId())) {
                    filteredQuoteList.put(tmpQuote.getQuoteId(), tmpQuote); //gaps might occur!!
                }
            }
            this.setLanguagePackQuotes(filteredQuoteList); //set it now
        } //so if already not null, we dont have to do this again
        return this.languagePackQuotes;
    }
}
