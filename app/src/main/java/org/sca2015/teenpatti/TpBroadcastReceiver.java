package org.sca2015.teenpatti;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by hp on 29-10-2015.
 */
public class TpBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "TpBroadcastReceiver";
    MainActivity activity;
    WifiP2pManager mManager;

    public WifiP2pManager getmManager() {
        return mManager;
    }

    public void setmManager(WifiP2pManager mManager) {
        this.mManager = mManager;
    }

    public MainActivity getActivity() {
        return activity;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public WifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    public void setmChannel(WifiP2pManager.Channel mChannel) {
        this.mChannel = mChannel;
    }

    WifiP2pManager.Channel mChannel;

    public TpBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, final Activity activity){
        super();
        this.activity= (MainActivity)activity;
        this.mManager = mManager;
        this.mChannel = mChannel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive() called");
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                //getActivity().showToast("WIFI_P2P_STATE_ENABLED");
            } else {
                // Wi-Fi P2P is not enabled
                //getActivity().showToast("WIFI_P2P_STATE NOT ENABLED");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            //getActivity().showToast("WIFI_P2P_PEERS_CHANGED_ACTION");
            mManager.requestPeers(mChannel, getActivity());
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            //getActivity().showToast("WIFI_P2P_CONNECTION_CHANGED_ACTION");

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                activity.showToast("CONNECT");
                mManager.requestConnectionInfo(mChannel, getActivity().getmConnectionInfoListener());

            } else {
                // It's a disconnect
                activity.showToast("It's a disconnect");
            }



        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            //getActivity().showToast("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        }
    }
}
