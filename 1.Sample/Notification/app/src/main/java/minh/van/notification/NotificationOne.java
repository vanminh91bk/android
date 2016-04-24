package minh.van.notification;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by vanmi on 11/15/2015.
 */
public class NotificationOne extends Activity {
    private  final String TAG = "notification";
    private  final String TAG_ACTIVITY = "NotificationOne";
    private  final boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_one);
        CharSequence s = "inside the activity of Notification one";
        int id = 0;
        Bundle exBundle = getIntent().getExtras();
        if (exBundle == null) {
            s = "error";
        } else {
            id = exBundle.getInt("notificationID");
        }

        TextView t = (TextView) findViewById(R.id.text1);
        s = s + "with id = " + id;
        t.setText(s);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(id);
    }


    private void showLog(String mes) {
        if(DEBUG) {
            Log.d(TAG, TAG_ACTIVITY + ": " + mes);
        }
    }
}
