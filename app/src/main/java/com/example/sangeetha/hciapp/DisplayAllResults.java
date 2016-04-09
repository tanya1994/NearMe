package com.example.sangeetha.hciapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sangeetha on 4/8/16.
 */
public class DisplayAllResults extends Activity {

    // Google Places
    GooglePlaces googlePlaces;

    // Progress dialog
    ProgressDialog pDialog;

    // Places List
    PlacesList nearPlaces;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // GPS Location
    GPSTracker gps;

    ListView lv;

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name
    public static String KEY_DISTANCE = "0"; // distance between current location and found location
    public static String TERM_TO_SEARCH = "";
    String search_term;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_all_results);
        MultiDex.install(this);

        Intent i = getIntent();

        // Place referece id
        search_term = i.getStringExtra(TERM_TO_SEARCH);
        this.getActionBar().setTitle(search_term);

        lv = (ListView) findViewById(R.id.list);

        // creating GPS Class object
        gps = new GPSTracker(this);

        // check if GPS location can get
        if (gps.canGetLocation()) {
            Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
        } else {
            // Can't get user's current location
            alert.showAlertDialog(DisplayAllResults.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS",
                    false);
            // stop executing code by return
            return;
        }

        placesListItems.clear();
        // Calling a Async Background thread
        new LoadPlaces().execute(search_term);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//                 lv.setAdapter(null);
                // getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);

                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                startActivity(in);
            }
        });
    }

    /**
     * Background Async Task to Load Google places
     */
    class LoadPlaces extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DisplayAllResults.this);
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();

            try {
                // Separeate your place types by PIPE symbol "|"
                // If you want all types places make it as null
                // Check list of types supported by google
                //
                String types = search_term; // Listing places only cafes, restaurants

                // Radius in meters - increase this value if you don't find any places
                double radius = 16093.4; // in meters -- 10 miles

                // get nearest places
                nearPlaces = googlePlaces.search(gps.getLatitude(),
                        gps.getLongitude(),radius, types);
                Log.d("got here", nearPlaces.results.toString());
                Log.d("nearplace is ", nearPlaces.status);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = nearPlaces.status;
                    Log.d("Status is ", status);
                    if (status.equals(null) || (status.equals("null"))) {
                        alert.showAlertDialog(DisplayAllResults.this, "Internet Connection Error",
                                "Poor Internet Connection", false);
                        // stop executing code by return
                        return;
                    }

                    // Check for all possible status
                    if (status.equals("OK")) {
                        // Successfully got places details
                        if (nearPlaces.results != null) {
                            // loop through each place
                            for (Place p : nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                Location currentLocation = new Location("Current");
                                currentLocation.setLatitude(gps.getLatitude());
                                currentLocation.setLatitude(gps.getLongitude());

                                Location resultLocation = new Location("result");
                                resultLocation.setLatitude(p.geometry.location.lat);
                                resultLocation.setLatitude(p.geometry.location.lng);

                                String floatToString = String.format("%.2f", (currentLocation.distanceTo(resultLocation)) / ((float) 1609.344));
                                floatToString += " mi";


                                // Place reference won't display in listview - it will be hidden
                                // Place reference is used to get "place full details"
                                map.put(KEY_REFERENCE, p.reference);

                                // Place name
                                map.put(KEY_NAME, p.name);

                                // place vicinity
                                map.put(KEY_VICINITY, p.vicinity);

                                // place distance
                                map.put(KEY_DISTANCE, floatToString);

                                // adding HashMap to ArrayList
                                placesListItems.add(map);
                            }
                            // list adapter
                            ListAdapter adapter = new SimpleAdapter(DisplayAllResults.this, placesListItems,
                                    R.layout.list_item,
                                    new String[]{KEY_REFERENCE, KEY_NAME, KEY_VICINITY, KEY_DISTANCE}, new int[]{
                                    R.id.reference, R.id.name, R.id.vicinity, R.id.distance});

                            // Adding data into listview
                            lv.setAdapter(adapter);

                        }
                    } else if (status.equals("ZERO_RESULTS")) {
                        // Zero results found
                        alert.showAlertDialog(DisplayAllResults.this, "Near Places",
                                "Sorry no places found. Try to change the types of places",
                                false);
                    } else if (status.equals("UNKNOWN_ERROR")) {
                        alert.showAlertDialog(DisplayAllResults.this, "Places Error",
                                "Sorry unknown error occured.",
                                false);
                    } else if (status.equals("OVER_QUERY_LIMIT")) {
                        alert.showAlertDialog(DisplayAllResults.this, "Places Error",
                                "Sorry query limit to google places is reached",
                                false);
                    } else if (status.equals("REQUEST_DENIED")) {
                        alert.showAlertDialog(DisplayAllResults.this, "Places Error",
                                "Sorry error occured. Request is denied",
                                false);
                    } else if (status.equals("INVALID_REQUEST")) {
                        alert.showAlertDialog(DisplayAllResults.this, "Places Error",
                                "Sorry error occured. Invalid Request",
                                false);
                    } else {
                        alert.showAlertDialog(DisplayAllResults.this, "Places Error",
                                "Sorry error occured.",
                                false);
                    }
                }
            });

        }

    }
}