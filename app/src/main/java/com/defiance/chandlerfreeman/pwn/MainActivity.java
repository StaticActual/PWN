package com.defiance.chandlerfreeman.pwn;

import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.app.AlertDialog;

import android.net.wifi.WifiManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.result.WifiParsedResult;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import se.simbio.encryption.Encryption;

import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends ActionBarActivity {

    // Declaring Your View and Variables
    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Scan","Share"};
    int Numboftabs =2;

    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    public final static int WIDTH = 650;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        if(android.os.Build.VERSION.SDK_INT >= 19) {
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.wifi_settings) {
            Intent i = new Intent(getApplicationContext(), WifiSettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public void scanButtonOnClick(View v) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        Log.v("DebugLog", "requestCode is " + requestCode + " and resultCode is " + resultCode);
        if (resultCode != 0 && scanResult != null) {
            String contents = intent.getStringExtra("SCAN_RESULT");
            Log.v("DebugLog", "Success! Undecrypted Contents: " + contents);
            Encryption decryption = Encryption.getDefault("{{AMBER-LYNN12[#", "m16", new byte[16]);
            String decrypted = decryption.decryptOrNull(contents);
            Log.v("DebugLog", "Success! Decrypted Contents: " + decrypted);
            WifiParsedResult wifiParsedResult = new QRWifiParser().parse(decrypted);
            Log.v("DebugLog", "Success! Contents: " + contents);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Connecting to network")
                    .setMessage("Connecting to " + wifiParsedResult.getSsid())
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            WifiManager wManager = (WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE);
            Object config = new WifiConfig(wManager).connectToWifi(wifiParsedResult);
        }
    }

    public void genQR(View v) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        ImageView imageView = (ImageView) findViewById(R.id.qrCode);

        SharedPreferences prefs = this.getSharedPreferences("com.defiance.chandlerfreeman.pwn", getApplicationContext().MODE_PRIVATE);

        // Use a default value using new Date()
        String ssid = prefs.getString("ssid", "");
        String pass = prefs.getString("key", "");

        String STR = "WIFI:S:" + ssid + ";T:WPA;P:" + pass + ";;";

        Encryption encryption = Encryption.getDefault("{{AMBER-LYNN12[#", "m16", new byte[16]);
        String encrypted = encryption.encryptOrNull(STR);

        Log.v("Debug", "Generated encrypted string: " + encrypted);

        if (!ssid.isEmpty() && !pass.isEmpty()) {
            try {
                Bitmap bitmap = encodeAsBitmap(encrypted);
                imageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Error")
                    .setMessage("Please enter your wifi network information in the 'Your Wifi Settings' menu")
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}