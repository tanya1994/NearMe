package com.example.sangeetha.hciapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by sangeetha on 3/27/16.
 */
public class ChangeLocation extends Activity {

    EditText new_location;
    Button submit;
    String location = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_location);

        ActionBar actionBar;
        actionBar = getActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0044ff"));
        actionBar.setBackgroundDrawable(colorDrawable);

        submit = (Button) findViewById(R.id.button_change_location);
        new_location = (EditText) findViewById(R.id.id_location);

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                location = new_location.getText().toString();
                Toast.makeText(getApplicationContext(),
                "Location changed to " + location,
                Toast.LENGTH_LONG).show();

                Intent backtoIntroPage = new Intent(ChangeLocation.this, welcome.class);
                startActivity(backtoIntroPage);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.id_location:
                MenuOptions m = new MenuOptions(getApplicationContext());
                m.changeLocation();
                return true;
            case R.id.id_back_to_home:
                MenuOptions m1 = new MenuOptions(getApplicationContext());
                m1.backToHome();
                return true;
            case R.id.contact_us:
                MenuOptions m2 = new MenuOptions(getApplicationContext());
                m2.contactUs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
