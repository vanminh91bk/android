package minh.thuan.alarmtrafficjam.ulities;

import android.content.Context;

import com.minhthuan.lib.ultil.Global;
import com.minhthuan.lib.user.User;

import minh.thuan.alarmtrafficjam.communication.MyClient;


/**
 * Created by vanmi on 10/18/2015.
 */
public class Ultil {
    public final static String PRE_ID_USER = "id_user";
    public final static String PRE_PASS_USER = "pass_user";
    public final static String PRE_IS_LOGIN = "is_user_login";
    public final static String AC_LOCATION = "my_location";
    public final static MyClient MY_CLIENT = new MyClient(Global.IP, Global.PORT);
    public static User user;// = new User("vanminh91bk","12345");


    public static boolean isNetwork(Context context) {
        // TODO: Check network
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//        if (mWifi.isConnected()) {
//            return true;
//        }
//        return false;
        return true;
    }
}
