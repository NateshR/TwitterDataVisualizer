package com.nateshr.twitterdatavisualizer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.nateshr.twitterdatavisualizer.JavaClasses.MainJson;

import java.util.logging.Handler;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by natesh on 2/5/15.
 */
public class GetTweetsOnMap extends Activity {
    private String[] current_location;
    private String TAG = "GetTweetsOnMap";
    String url;
    private GoogleMap googleMap;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        //Getting current location from previous Intent
        current_location = getIntent().getStringExtra("current_location").split(":");
        Log.d(TAG, "Latitude: " + current_location[0] + " Longitude: " + current_location[1]);

        try {
            // Loading map
            initializeMap();

        } catch (Exception e) {
            Log.d(TAG, "Map error: " + e);
            e.printStackTrace();
        }
        //Setting map to currrent postion
        LatLng current = new LatLng(Double.parseDouble(current_location[0]), Double.parseDouble(current_location[1]));
        //To use zoom effect when clicked on map
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                current).zoom(13).build();
        //Animate map from the zoom set above at cameraPostion
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //Generating URL
        url = "https://api.twitter.com/1.1/search/tweets.json?q=&geocode=" + current_location[0] + "," + current_location[1] + ",1.6km&lang=en&result_type=recent";
        Log.d(TAG, "URL: " + url);

        //Fetch past tweets
        new ShowTweets().execute();
        //For upcoming tweets using bounding box
        streamIt();

    }

    private class ShowTweets extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            //Make a service call on url
            ServiceHandler serviceHandler = new ServiceHandler();
            String response = serviceHandler.makeServiceCall(url);
            Log.d(TAG, "Response:" + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            int i;
            Gson gson = new Gson();
            //Converting JSON to Java classes
            final MainJson mainJson = gson.fromJson(result, MainJson.class);
            //Check if statuses are equal to 100, remove 10 oldest tweets
            if (mainJson.statuses.length == 100) {
                Log.d(TAG, "Tweets more than 100");
                for (i = 0; i < mainJson.statuses.length - 10; i++) {
                    //Check if geo is not null
                    if (mainJson.statuses[i].geo != null) {
                        //Create a point for map
                        LatLng current = new LatLng(mainJson.statuses[i].geo.coordinates[0], mainJson.statuses[i].geo.coordinates[1]);
                        MarkerOptions curmarker = new MarkerOptions().position(current).title(mainJson.statuses[i].user.name + ": " + mainJson.statuses[i].text);
                        //Add marker
                        googleMap.addMarker(curmarker);

                    } else {
                        //If geo is null, it may be retweeted status
                        LatLng current = new LatLng(mainJson.statuses[i].retweetedStatus.geo.coordinates[0], mainJson.statuses[i].retweetedStatus.geo.coordinates[1]);
                        MarkerOptions curmarker = new MarkerOptions().position(current).title(mainJson.statuses[i].user.name + ": " + mainJson.statuses[i].text);
                        //Add marker
                        googleMap.addMarker(curmarker);
                    }
                }
            } else {
                Log.d(TAG, "Tweets less than 100");
                for (i = 0; i < mainJson.statuses.length; i++) {
                    //Check if geo is not null
                    if (mainJson.statuses[i].geo != null) {
                        //Create a point for map
                        LatLng current = new LatLng(mainJson.statuses[i].geo.coordinates[0], mainJson.statuses[i].geo.coordinates[1]);
                        MarkerOptions curmarker = new MarkerOptions().position(current).title(mainJson.statuses[i].user.name + ": " + mainJson.statuses[i].text);
                        //Add marker
                        googleMap.addMarker(curmarker);

                    } else {
                        //If geo is null, it may be retweeted status
                        LatLng current = new LatLng(mainJson.statuses[i].retweetedStatus.geo.coordinates[0], mainJson.statuses[i].retweetedStatus.geo.coordinates[1]);
                        MarkerOptions curmarker = new MarkerOptions().position(current).title(mainJson.statuses[i].user.name + ": " + mainJson.statuses[i].text);
                        //Add marker
                        googleMap.addMarker(curmarker);
                    }
                }
            }


        }
    }

    private void initializeMap() {
        //Create map
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
                Log.d(TAG, "Unable to create maps");
            }

        }
    }


    public void streamIt() {
        //Set up twitter stream
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(ServiceHandler.consumerKeyStr)
                .setOAuthConsumerSecret(ServiceHandler.consumerSecretStr)
                .setOAuthAccessToken(ServiceHandler.accessTokenStr)
                .setOAuthAccessTokenSecret(ServiceHandler.accessTokenSecretStr);
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        //Inject a query of bounding box, I am using box near "janakpuri", it can be set according to requirement
        FilterQuery fq = getBoundingBoxFilter();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(final Status status) {
                final GeoLocation gl = status.getGeoLocation();

                //Check if geo is not null
                if (gl != null) {
                    //Check if the distance inside bounding box is less than 1 mile from current location
                    if (distance(Double.parseDouble(current_location[0]), Double.parseDouble(current_location[1]), gl.getLatitude(), gl.getLongitude()) <= 1.6) {
                        //Update the UI
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(TAG, "Upcoming tweets: " + status.getUser().getName() + "lat:" + gl.getLatitude() + ", " + gl.getLongitude());
                                //Create a point
                                LatLng current = new LatLng(gl.getLatitude(), gl.getLongitude());
                                MarkerOptions curmarker = new MarkerOptions().position(current).title(status.getUser().getName() + ": " + status.getText());
                                //Add marker
                                googleMap.addMarker(curmarker);
                            }
                        });

                    }
                } else {
                    if (status.getRetweetedStatus() != null) {
                        //If geo is null, it may be retweeted status
                        final GeoLocation gl_retweeted = status.getRetweetedStatus().getGeoLocation();
                        //Check if geo of retweetedStatus is not null

                        if (gl_retweeted != null) {
                            //Check if the distance inside bounding box is less than 1 mile from current location
                            if (distance(Double.parseDouble(current_location[0]), Double.parseDouble(current_location[1]), gl_retweeted.getLatitude(), gl_retweeted.getLongitude()) <= 1.6) {
                                //Update UI thread
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        // Create a point
                                        Log.d(TAG, "Upcoming tweets: " + status.getUser().getName() + "lat:" + gl_retweeted.getLatitude() + ", " + gl_retweeted.getLongitude());
                                        LatLng current = new LatLng(gl_retweeted.getLatitude(), gl_retweeted.getLongitude());
                                        MarkerOptions curmarker = new MarkerOptions().position(current).title(status.getUser().getName() + ": " + status.getText());
                                        //Add marker
                                        googleMap.addMarker(curmarker);

                                    }
                                });
                            }
                        }
                    }

                }

            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onException(Exception ex) {
                if (!(ex instanceof IllegalStateException)) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        //Add statusListener to twitterStream
        twitterStream.addListener(listener);
        if (fq != null) {
            twitterStream.filter(fq);
        }

    }

    //For filtering query
    private FilterQuery getBoundingBoxFilter() {
        // new "janakpuri, new delhi, India"
        //Note- bounding box may be wrong. If its not working, it can be set more.
        double lat = Double.parseDouble(current_location[0]);
        double lon = Double.parseDouble(current_location[1]);
        double lon1 = lon - 1;
        double lon2 = lon + 1;
        double lat1 = lat - 1;
        double lat2 = lat + 1;

        double bbox[][] = {{lon1, lat1}, {lon2, lat2}};
        FilterQuery filtro = new FilterQuery();
        return filtro.locations(bbox);
    }

    //For calculating distance between a pair of lat/long
    private double distance(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1.609344;
        return (dist);

    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);

    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);

    }
}
