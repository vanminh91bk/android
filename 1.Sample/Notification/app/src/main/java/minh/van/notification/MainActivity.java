package minh.van.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    // TODO Example about basic notification
    // Link tutorial: http://examples.javacodegeeks.com/android/core/ui/notifications/android-notifications-example/
    //
    //

    private final String TAG = "notification";
    private final String TAG_ACTIVITY = "MainActivity";
    private final boolean DEBUG = true;
    private NotificationManager myNotificationManager;
    private int notificationIdOne = 111;
    private int notificationIdTwo = 112;
    private int numMessagesOne = 0;
    private int numMessagesTwo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onclickButton(View view) {
        showLog("onclick");
        switch (view.getId()) {
            case R.id.btnNoti1:
                displayNoti1();
                break;
            case R.id.btnNoti2:
                displayNotificationTwo();
                break;
            default:
                break;
        }
    }

    private void displayNoti1() {
        showLog("displayNoti1");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("New message with explicit intent");
        mBuilder.setContentText("Set content text");
        mBuilder.setTicker("Ticker");
        mBuilder.setSmallIcon(R.drawable.icontest);
        mBuilder.setNumber(++numMessagesOne);

        Intent resultItent = new Intent(this, NotificationOne.class);
        resultItent.putExtra("notificationID", notificationIdOne);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationOne.class);

        stackBuilder.addNextIntent(resultItent);


        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(resultPendingIntent);
        myNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myNotificationManager.notify(notificationIdOne, mBuilder.build());
    }

    protected void displayNotificationTwo() {
        // Invoking the default notification service
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("New Message with implicit intent");
        mBuilder.setContentText("New message from javacodegeeks received...");
        mBuilder.setTicker("Implicit: New Message Received!");
        mBuilder.setSmallIcon(R.drawable.icontest);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] events = new String[3];
        events[0] = new String("1) Message for implicit intent");
        events[1] = new String("2) big view Notification");
        events[2] = new String("3) from javacodegeeks!");
        // Sets a title for the Inbox style big view
        inboxStyle.setBigContentTitle("More Details:");
        // Moves events into the big view
        for (int i = 0; i < events.length; i++) {
            inboxStyle.addLine(events[i]);
        }
        mBuilder.setStyle(inboxStyle);
        // Increase notification number every time a new notification arrives
        mBuilder.setNumber(++numMessagesTwo);
        // When the user presses the notification, it is auto-removed
        mBuilder.setAutoCancel(true);
        // Creates an implicit intent
        Intent resultIntent = new Intent("minh.van.TEL_INTENT",
                Uri.parse("tel:123456789"));
        resultIntent.putExtra("from", "javacodegeeks");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationTwo.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_ONE_SHOT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myNotificationManager.notify(notificationIdTwo, mBuilder.build());
    }


    private void showLog(String mes) {
        if (DEBUG) {
            Log.d(TAG, TAG_ACTIVITY + ": " + mes);
        }
    }

}
