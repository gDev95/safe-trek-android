package com.myalarmapp.root.safetrekfb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import com.myalarmapp.root.safetrekfb.models.Alarm;
import com.myalarmapp.root.safetrekfb.models.EmergencyServices;
import com.myalarmapp.root.safetrekfb.models.Geolocation;
import com.myalarmapp.root.safetrekfb.models.OAuthToken;
import com.myalarmapp.root.safetrekfb.retrofit.APIClient;

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

    // ************ CONSTANTS *****************
    private static final String TAG = "Main Activity";

    // ************* UI COMPONENTS ********
    FloatingActionButton fab;

    TextView txvResult;

    View mLayout;

    Button settingsBtn;


    // ************ USER APP SETTINGS ************
    private  SharedPreferences sp = MyApplication.instance.getSharedPreferences("Settings", MODE_PRIVATE);

    // ************ AUTHENTICATION ************
    private OAuthToken oauthToken;

    // ************ GEOLOCATION
    private Geolocation geolocation;

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

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i(TAG, "Location Permission has been granted.");

                    Snackbar.make(mLayout,"Location permission granted.",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "CAMERA permission was NOT granted.");
                    Snackbar.make(mLayout, "Location permission not granted.",
                            Snackbar.LENGTH_SHORT).show();


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

        oauthToken = OAuthToken.Factory.create();

        mLayout = findViewById(android.R.id.content);

        // Use instance field for listener

        // It will not be gc'd as long as this instance is kept referenced


        txvResult = (TextView) findViewById(R.id.txtView);


        // Get FontAwesome Fonts
        Typeface font = Typeface.createFromAsset( getAssets(), "fonts/fontawesome-webfont.ttf" );

        settingsBtn = (Button)findViewById( R.id.settingsBtn );

        settingsBtn.setTypeface(font);

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view ) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        // need to ask the user for permission to obtain location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();


        } else {
            Log.e(TAG, "Now get location and proceed");

            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {

                @Override
                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onProviderEnabled(String arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onProviderDisabled(String arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onLocationChanged(Location arg0) {
                    txvResult.setText("Latitude: "+arg0.getLatitude()+" \nLongitude: "+arg0.getLongitude());
                    double latitude = (Double) arg0.getLatitude();
                    double longitude = (Double) arg0.getLongitude();
                    geolocation.setGeolocation(latitude, longitude);
                }
            });
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Alarm Triggered", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Bundle params = new Bundle();
                Boolean shareLocation = sp.getBoolean("shareLocation", false);
                String postMessage = sp.getString("message", null);
                // ********* LOGGING FOR DEBUGGING ***********
                //Log.e(TAG, "sharelocation:" + Boolean.toString(shareLocation));
                //Log.e(TAG, "Customize post message " + postMessage);
                params.putString("message", postMessage);
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
                                Log.d(TAG, "Successful Facebook Post " + response.toString());
                            }
                        }
                ).executeAsync();




            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


}
