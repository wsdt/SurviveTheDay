package kevkevin.wsdt.tagueberstehen.classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import kevkevin.wsdt.tagueberstehen.classes.StorageMgr.DatabaseMgr;

public class Quote {
    private static final String TAG = "Quote";
    private static SparseArray<Quote> allQuotes = new SparseArray<>();
    private static boolean currentlyLoadingAllQuotes = false; //to avoid multiple cursor allocations
    private int quoteId; //primary key
    private String quoteText;
    private String languagePack; //foreign key

    public Quote(@NonNull Context context, int quoteId, @NonNull String quoteText, @NonNull String languagePack) {
        this.setQuoteId(quoteId);
        this.setQuoteText(quoteText);
        this.setLanguagePack(languagePack);
        //Quote.getAllQuotes().put(quoteId,this); //add to global static quotelist
        Quote.downloadAllQuotes(context); //only downloads/extracts all quotes from db, if they are not already loaded
    }

    public static void downloadAllQuotes(@NonNull Context context) { //if no quotes in db, this method might run every time!
        if (Quote.allQuotes.size() <= 0 && !currentlyLoadingAllQuotes) {
            Log.d(TAG, "downloadAllQuotes: Trying to extract all quotes from db.");
            //Quotes not extracted now, doing it now.
            currentlyLoadingAllQuotes = true; //blocking other method calls
            Quote.setAllQuotes(DatabaseMgr.getSingletonInstance(context).getAllQuotes(context,false));
            currentlyLoadingAllQuotes = false; //now allowing it again
        }
    }

    public static Quote getRandomQuoteFromAll(@NonNull Context context) { //might return null if no quotes!
        return Quote.getAllQuotes(context).get(RandomFactory.getRandNo_int(0,Quote.getAllQuotes(context).size()-1));
    }

    @Override
    public String toString() {
        return "QUOTE->"+this.getQuoteId()+";"+this.getQuoteText()+";"+this.getLanguagePack();
    }

    //GETTER/SETTER ------------------------------------------
    public int getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(int quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getLanguagePack() {
        return languagePack;
    }

    public void setLanguagePack(String languagePack) {
        this.languagePack = languagePack;
    }


    public static SparseArray<Quote> getAllQuotes(@NonNull Context context) {
        downloadAllQuotes(context); //extract from db if not already done
        return allQuotes;
    }

    public static void setAllQuotes(SparseArray<Quote> allQuotes) {
        Quote.allQuotes = allQuotes;
    }
}
