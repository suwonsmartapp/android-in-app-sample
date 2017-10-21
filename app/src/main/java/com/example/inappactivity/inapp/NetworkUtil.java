
package com.example.inappactivity.inapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by donghaechoi on 2016. 6. 9..
 */
public class NetworkUtil {

    /**
     * 연결 됨
     */
    public static int CONNECTED = 1;

    /**
     * 연결 안 됨
     */
    public static int NOT_CONNECTED = 0;

    /**
     * 네트워크 상태 점검
     * 
     * @param context context
     * @return CONNECTED(1) or NOT_CONNECTED(0)
     */
    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return CONNECTED;
        }
        return NOT_CONNECTED;
    }

}
