/*

    This was created by Paul Meyer on April 6th, 2018.
    This code should not be redistributed by any third-party
    if not otherwise stated or declared by the creator of this code itself.
    For any questions mail paulmeyerber@gmail.com
 */

/*
    This MainActivity is the main user interface for the app.
    The User can:
    - Edit his settings (share location, customized post message)
    - Trigger an Alarm (using Safe Trek's API)

 */
package com.myalarmapp.root.safetrekfb;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.location.Geocoder;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import com.facebook.places.PlaceManager;
import com.facebook.places.model.CurrentPlaceRequestParams;
import com.facebook.places.model.PlaceFields;
import com.myalarmapp.root.safetrekfb.models.Alarm;
import com.myalarmapp.root.safetrekfb.models.EmergencyServices;
import com.myalarmapp.root.safetrekfb.models.Geolocation;
import com.myalarmapp.root.safetrekfb.models.OAuthToken;
import com.myalarmapp.root.safetrekfb.retrofit.APIClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class MainActivity extends AppCompatActivity {
    // FOR FACEBOOKS PLACES API
    /*
     * Creates a callback class that is passed into the place request
     */
    private class CurrentPlaceRequestCallback
            implements PlaceManager.OnRequestReadyCallback, GraphRequest.Callback {

        @Override
        public void onRequestReady(GraphRequest graphRequest) {
            // The place search request is ready to be executed.
            // You can customize the request here (if needed).

            // Sets the callback, and executes the request.
            graphRequest.setCallback(this);
            graphRequest.executeAsync();
        }

        // since all facebooks operation are async, everything that happens after the location
        // is retrieved as a response is happening here
        @Override
        public void onCompleted(GraphResponse response) {
            // Event invoked when the place search response is received.
            // Parse the places from the response object.
            Log.e(TAG,"response: " +response.getRawResponse());

            try{
                // cut out the information we need
                // the best match of our location is at index 0 of the list of places
                // that we get back as a response
                JSONObject jsonObj = new JSONObject(response.getRawResponse());
                JSONArray data = jsonObj.getJSONArray("data");
                JSONObject placeObject = data.getJSONObject(0);

                placeID = placeObject.getString("id");

                Bundle params = new Bundle();
                String postMessage = sp.getString("message", null);
                Log.e(TAG, "PlaceID: " + placeID);
                params.putString("message", postMessage );
                Log.e(TAG, "Message : " + placeID);
                params.putString("place", placeID);
                /* send HTTP POST Request to Safe Trek */
                //Log.e(TAG, "Creating an alarm request");

                createAlarmRequest();
                /* make the API call to Facebook*/
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/feed",
                        params,
                        HttpMethod.POST,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                /* log successful */

                                if(response.getError() != null) {
                                    Log.e(TAG, "Error : " + response.getError().getErrorMessage());
                                    Snackbar.make(mLayout, "A problem posting to Facebook occurred", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                                Log.d(TAG, "Successful Facebook Post " + response.toString());
                                Snackbar.make(mLayout, "Alarm Triggered", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                ).executeAsync();
            }
            catch (NullPointerException e) {
                // Is caught when response is null meaning that their was a problem with the internet connection
                Log.e(TAG, "NullPointer Exception: " + e);
                Snackbar.make(mLayout, "No Alarm created, check your internet connection", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            catch(JSONException e){
                Log.e(TAG, "JSON Exception: " + e);
                Snackbar.make(mLayout, "There was a problem with Facebook", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }



        }

        @Override
        public void onLocationError(PlaceManager.LocationError error) {
            // Invoked if the Places Graph SDK failed to retrieve
            // the device location.
            Log.e(TAG, "Location Error: " + error.toString());
            Snackbar.make(mLayout, "There was a problem with Facebook, try again later or disable share Location", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    // ************ CONSTANTS *****************
    private static final String TAG = "Main Activity";

    // ************* UI COMPONENTS ********
    FloatingActionButton fab;

    Button settingsBtn;

    View mLayout;


    // ************ USER APP SETTINGS ************
    private  SharedPreferences sp = MyApplication.instance.getSharedPreferences("Settings", MODE_PRIVATE);

    // ************ AUTHENTICATION ************
    private OAuthToken oauthToken;

    // ************ GEOLOCATION
    private Geolocation geolocation = new Geolocation(0.0,0.0);
    private String placeID;

    //************* PERMISSION REQUESTS
    /* ask for permission to obtain device location
     * @params none
     * @return nothing
     */
    public void requestLocationPermission(){

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

        }
    /* get Result of permission(s) request
     * @params int requestCode, String[] permissions, int[] grantResults
     * @return nothing
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Log.i(TAG, "Location Permission has been granted.");
                    Snackbar.make(mLayout,"Location permission granted.",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Location permission was NOT granted.");
                    // The User should not be able to interact with app if permission was not granted
                    // Create Alert Window and then kill the app
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("This app needs to obtain your address for the emergency service. Please close app and open again.")
                            .setTitle("Location Services");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        }
    }
    //************** HTTP REQUEST ************
    // send a HTTP POST Request to Safe Trek API to create an alarm
    /*
     * @params none
     * @return nothing
     */
    private void createAlarmRequest() {
        String accessToken = oauthToken.getAccessToken();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder().addInterceptor(interceptor);

        builder.addNetworkInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                Log.e(TAG, "Adding headers");
                builder.addHeader("Authorization", "Bearer " + oauthToken.getAccessToken());
                builder.addHeader("Content-Type", "application/json");
                return chain.proceed(builder.build());
            }
        });
        OkHttpClient client = builder.build();
        // create emergency services object for Alarm
        EmergencyServices emServices = new EmergencyServices();

        Alarm alarm = new Alarm (emServices, geolocation);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .baseUrl("https://api-sandbox.safetrek.io/v1/")
                .build();
        APIClient apiClient = retrofit.create(APIClient.class);
        Call<Alarm> createAlarmApiCall = apiClient.createAlarm(alarm);


        createAlarmApiCall.enqueue(new Callback<Alarm>() {
            @Override
            public void onResponse(Call<Alarm> call, retrofit2.Response<Alarm> response) {
                Log.e("Response:", response.code() + "  | body = " + response.body());

            }

            @Override
            public void onFailure(Call<Alarm> call, Throwable t) {
                Log.e(TAG, t.toString());
            }
        });


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        mLayout = findViewById(android.R.id.content);

        // Get FontAwesome Fonts
        Typeface font = Typeface.createFromAsset( getAssets(), "fonts/fontawesome-webfont.ttf" );

        // get settings button from layout
        settingsBtn = (Button)findViewById( R.id.settingsBtn );
        // set its typeface so that the FontIcon appears
        settingsBtn.setTypeface(font);
        // set on click listener that starts settings activity when clicked
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view ) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        oauthToken = OAuthToken.Factory.create();
        // need to ask the user for permission to obtain location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();


        } else {
//            Log.e(TAG, "Now get location and proceed");
            // Create a location manager
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

            // get current GPS locations latitude & longitude
            Location getLastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            double latitude = 0;
            double longitude = 0;
            // attempt to set location
            try{
                latitude = getLastLocation.getLongitude();
                longitude = getLastLocation.getLatitude();
                geolocation.setGeolocation(latitude,longitude);
            }
            catch(Exception e) {
                e.printStackTrace();
                Snackbar.make(mLayout, "A problem occured obtaining your location", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            // update the geolocation of user

            // listen for updates
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {

                @Override
                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                    Log.d(TAG,"status change");
                }

                @Override
                public void onProviderEnabled(String arg0) {
                    Log.d(TAG, "Location services for app have been enabled");

                }

                @Override
                public void onProviderDisabled(String arg0) {
                    Log.e(TAG,"disabled");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

                    builder.setMessage("In order to use this app, you need to enable your location services")
                            .setTitle("Location Services");

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    finish();

                }

                @Override
                public void onLocationChanged(Location arg0) {
                    double latitude = (Double) arg0.getLatitude();
                    double longitude = (Double) arg0.getLongitude();
                    geolocation.setGeolocation(latitude,longitude);

                }
            });
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean shareLocation = sp.getBoolean("shareLocation", false);
                String postMessage = sp.getString("message", null);

                // ********* LOGGING FOR DEBUGGING ***********
                //Log.e(TAG, "sharelocation:" + Boolean.toString(shareLocation));
                //Log.e(TAG, "Customize post message " + postMessage);

                if(shareLocation){

//                    This is in case you want to display the full address of the user location
//                    Utilizes the Geocoder from Android

//                    Geocoder geoc = new Geocoder(getApplicationContext(), Locale.getDefault());
//
//                    double latitude = geolocation.getLatitude();
//                    double longitude = geolocation.getLongitude();
//
//                    List <Address> addresses;
//                    StringBuilder sb = new StringBuilder();
//                    String fullLocation = "";
//
//                    try {
//                       addresses = geoc.getFromLocation(latitude, longitude, 1);
//                        if (addresses.size() > 0) {
//                            Address address = addresses.get(0);
//                            for(int i = 0; i < address.getMaxAddressLineIndex(); i++)
//                            sb.append(address.getAddressLine(i)).append(",");
//                            sb.append(address.getCountryName());
//                            fullLocation = sb.toString();
//                        }
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }

                    // Build request to obtain place on Facebook closest to current location
                    CurrentPlaceRequestParams.Builder builder = new CurrentPlaceRequestParams.Builder();

                    builder.setMinConfidenceLevel(CurrentPlaceRequestParams.ConfidenceLevel.LOW);
                    builder.setLimit(20);
                    builder.addField(PlaceFields.NAME);
                    builder.addField(PlaceFields.CONFIDENCE_LEVEL);
                    builder.addField(PlaceFields.LOCATION);
                    builder.addField(PlaceFields.COVER);

                    CurrentPlaceRequestCallback callback = new CurrentPlaceRequestCallback();
                    // within the callback we will post to facebook and send the alarm
                    PlaceManager.newCurrentPlaceRequest(builder.build(), callback);

                }
                else {
                    // initialize params for graph request
                    Bundle params = new Bundle();
                    // see a list of possible parameters in docs
                    // message, link or place needs to be passed to request
                    params.putString("message", postMessage);

                    /* send HTTP POST Request to Safe Trek */
                    createAlarmRequest();
                    /* make the API call to Facebook to post to user's feed*/
                    new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/me/feed",
                            params,
                            HttpMethod.POST,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    /* log successful */
                                    String error = response.getError().getErrorMessage();
                                    if(error.length() > 0) {
                                        Log.e(TAG, error);
                                        Snackbar.make(mLayout, "A problem posting to Facebook occurred", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                    Log.d(TAG, "Successful Facebook Post " + response.toString());
                                    Snackbar.make(mLayout, "Alarm Triggered", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }
                    ).executeAsync();
                }

            }
        });
    }
}
