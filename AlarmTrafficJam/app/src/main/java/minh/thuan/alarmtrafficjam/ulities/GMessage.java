package minh.thuan.alarmtrafficjam.ulities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;


import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import minh.thuan.alarmtrafficjam.R;

/**
 * Created by vanmi on 10/5/2015.
 */
public class GMessage {
    private static boolean Debug_Toast = true;
    private static boolean Debug_Log = true;

    public static void showMessage(Context context, String TAG, String mes) {
        if (Debug_Toast) {
            Toast.makeText(context, TAG + ": " + mes, Toast.LENGTH_SHORT).show();
        }
        if (Debug_Log) {
            Log.d(TAG, mes);
        }
    }

    public static void showMessage(Context context, String mes)
    {
        Toast.makeText(context, mes , Toast.LENGTH_LONG).show();
    }

    public static void showMessage(String TAG, String mes) {
        if (Debug_Log) {
            Log.d(TAG, mes);
        }
    }

    public static void showSnackbar(Context context, String mes, String action, ActionClickListener actionClickListener) {
//        SnackbarManager.show(
//                Snackbar.with(context) // context
//                        .text(mes) // text to be displayed
//                        .actionLabel(action) // action button label
//                        .actionColor(Color.YELLOW) // action button label color
//                        .actionListener(actionClickListener) // action button's ActionClickListener
//                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
//                , (Activity) context); // activity where it is displayed
    }

}
