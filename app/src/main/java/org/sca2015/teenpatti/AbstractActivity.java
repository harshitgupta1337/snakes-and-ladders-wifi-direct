package org.sca2015.teenpatti;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by hp on 10-11-2015.
 */
abstract public class AbstractActivity extends AppCompatActivity {
    abstract void processMessage(String msg, String sender);
}
