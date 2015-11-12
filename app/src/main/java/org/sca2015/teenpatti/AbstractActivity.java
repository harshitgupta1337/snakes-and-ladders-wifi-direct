package org.sca2015.teenpatti;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by hp on 10-11-2015.
 */
abstract public class AbstractActivity extends AppCompatActivity {
    abstract void processMessage(String msg, String sender);

    public void showToast(String text){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
