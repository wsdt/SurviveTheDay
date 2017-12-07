package kevkevin.wsdt.tagueberstehen;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;


/**
 * Created by kevin on 07.12.2017.
 */

public class CountdownService extends IntentService {
    private static String serviceName = "CountdownService";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *  Used to name the worker thread, important only for debugging.
     */
    public CountdownService() {
        super(serviceName);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        new Countdown.CountdownCounter().execute(intent.getDoubleExtra("TOTAL_SECONDS",0D));
    }


}
