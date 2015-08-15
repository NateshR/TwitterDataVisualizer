package com.nateshr.twitterdatavisualizer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Button getTweets;
    private String TAG = "MainActivity";
    private ProgressDialog progressDialog;
    // Google client to interact with Google API
    private GoogleApiClient googleApiClient;
    //Used to check google play services request code
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    //Used to retrieve last location object
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getTweets = (Button) findViewById(R.id.bGetTweets);
        getTweets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDeviceLocationEnable()) {
                    //Check if play services are there
                    if (checkPlayServices()) {
                        //building/initializing google api client used to extract address from lat/long
                        buildGoogleApiClient();
                    }
                    if (googleApiClient != null) {
                        Log.d(TAG, "Connecting google api client");
                        googleApiClient.connect();
                    } else {
                        Log.d(TAG, "Null google api client");
                    }
                } else {
                    //Show setting alert dialog when location is not enabled.
                    showSettingsAlert();
                }
            }
        });
    }

    //Checking if location is enabled on device
    private boolean checkDeviceLocationEnable() {
        boolean isGPSEnabled;
        boolean isNetworkEnabled;

        //Get location service
        LocationManager locationManager = (LocationManager) MainActivity.this
                .getSystemService(LOCATION_SERVICE);
        //Check if gps is enabled
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //Check if mobile network is enabled
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            return false;
        } else {
            return true;
        }
    }

    //Check if play services are installed on device
    private boolean checkPlayServices() {
        //Checking result code if available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            //If code is recoverable then show dialog
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG);
                Log.d(TAG, "Google Play Services not supported");
                finish();
            }
            return false;
        }
        return true;
    }

    //AlertDialog to be shown when location is not enabled.
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Location is settings");

        // Setting Dialog Message
        alertDialog.setMessage("Location is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //Convert lat and lang into proper address from current location
    @Override
    public void onConnected(Bundle bundle) {
        new GetCurrentLocation().execute();
    }


    //Connect again if suspended
    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed google api connection");
    }

    private class GetCurrentLocation extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Fetching Data");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "Extracting location after connecting google api client");
            String location = null;

            //Retrieving last location form google api client
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            //If location is not null, extract latitude and longitude
            if (lastLocation != null) {
                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();
                location = String.valueOf(latitude) + ":" + String.valueOf(longitude);
                return location;
            } else {
                Toast.makeText(MainActivity.this, "Unable to retrieve location !", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Unable to retrieve location");
                return null;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }

            if (result != null) {

                String[] arrayResult = result.split(":");
                Toast.makeText(MainActivity.this, "Latitude: " + arrayResult[0] + " Longitude: " + arrayResult[1], Toast.LENGTH_SHORT).show();
                Intent intentMap = new Intent(MainActivity.this,GetTweetsOnMap.class);
                intentMap.putExtra("current_location",result);
                startActivity(intentMap);
            }

        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
