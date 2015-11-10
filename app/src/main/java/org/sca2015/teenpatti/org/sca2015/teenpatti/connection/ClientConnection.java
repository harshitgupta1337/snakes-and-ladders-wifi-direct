package org.sca2015.teenpatti.org.sca2015.teenpatti.connection;

import android.os.Handler;

import org.sca2015.teenpatti.MainActivity;

/**
 * Created by hp on 29-10-2015.
 */
public class ClientConnection extends GameConnection {
    public ClientConnection(Handler handler, MainActivity activity) {
        super(handler, activity);
    }
}
