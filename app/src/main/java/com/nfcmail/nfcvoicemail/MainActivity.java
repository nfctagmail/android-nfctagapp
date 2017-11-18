package com.nfcmail.nfcvoicemail;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Puts the name of the app in the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Button refresh = findViewById(R.id.button);
        refresh.setVisibility(View.GONE);

        TextView textview = findViewById(R.id.textView);

        // If the phone the app is running on doesn't have NFC
        /*if(nfcAdapter == null) {
            textview.setText("Your device is not compatible with NFC.");
            // Quit the app, with an error message.
            Toast.makeText(this, "Your device is not compatible with NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }*/

        // If the user deactivated NFC in the settings
        /*if(!nfcAdapter.isEnabled())
        {
            textview.setText("NFC is disabled in the Settings. Activate it to be able to use this app.");
            refresh.setVisibility(View.VISIBLE);
        }*/

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });

        // Start the Configuration Activity when pressing the F.A.B
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConfiguration();
                //Snackbar.make(view, "This is dope.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    public void startConfiguration() {
        Intent intent = new Intent(this, NewMessageActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings WIP.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
