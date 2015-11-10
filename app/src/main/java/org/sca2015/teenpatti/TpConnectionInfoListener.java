package org.sca2015.teenpatti;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by hp on 29-10-2015.
 */
public class TpConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {

    private MainActivity activity;

    public MainActivity getActivity() {
        return activity;
    }

    public TpConnectionInfoListener(MainActivity activity){
        super();
        this.activity = activity;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        getActivity().setIsOwner(info.isGroupOwner);
        if(!info.isGroupOwner)
            getActivity().sendConnectionInitMessage(info.groupOwnerAddress.toString().substring(1));
    }
}
