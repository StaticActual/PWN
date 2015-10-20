package com.defiance.chandlerfreeman.pwn;

/**
 * Created by chandlerfreeman on 10/17/15.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.SharedPreferences;

public class WifiSettingsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifisettings);

        final TextView txtName = (TextView) findViewById(R.id.EditTextName);
        final TextView txtPass = (TextView) findViewById(R.id.EditTextEmail);
        Button btnClose = (Button) findViewById(R.id.ButtonSendFeedback);

        Intent i = getIntent();

        final SharedPreferences prefs = this.getSharedPreferences(
                "com.defiance.chandlerfreeman.pwn", getApplicationContext().MODE_PRIVATE);

        // use a default value using new Date()
        String ssid = prefs.getString("ssid", "");
        String pass = prefs.getString("key", "");

        txtName.setText(ssid);
        txtPass.setText(pass);

        // Binding Click event to Button
        btnClose.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Log.v("WifiSettingsDebug", txtName.getText().toString());
                Log.v("WifiSettingsDebug", txtPass.getText().toString());

                prefs.edit().putString("ssid", txtName.getText().toString()).apply();
                prefs.edit().putString("key", txtPass.getText().toString()).apply();
                //Closing SecondScreen Activity
                finish();
            }
        });
    }
}