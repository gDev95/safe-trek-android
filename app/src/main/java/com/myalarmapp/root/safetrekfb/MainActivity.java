package com.myalarmapp.root.safetrekfb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.myalarmapp.root.safetrekfb.models.OAuthToken;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Intent.ACTION_VIEW;

public class MainActivity extends AppCompatActivity {
// does not belong here
//    CallbackManager callbackManager = CallbackManager.Factory.create();

    Button btnDo;
    TextView txvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        txvResult = (TextView) findViewById(R.id.txtView);
        txvResult.setMovementMethod(new ScrollingMovementMethod());
        showOAuthToken();

    }
    public void showOAuthToken(){
        OAuthToken oauthToken = OAuthToken.Factory.create();
        txvResult.setText(oauthToken.getRefreshToken());
    }
}
