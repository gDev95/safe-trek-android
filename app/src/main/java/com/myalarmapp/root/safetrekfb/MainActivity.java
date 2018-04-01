package com.myalarmapp.root.safetrekfb;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CallbackManager callbackManager = CallbackManager.Factory.create();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List permissions = new ArrayList<String>();
        permissions.add("publish_actions");
        LoginManager lm = LoginManager.getInstance();
        lm.logInWithPublishPermissions(MainActivity.this,permissions );
        lm.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Success Login");
            }

            @Override
            public void onCancel() {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Cancelled Login");
            }

            @Override
            public void onError(FacebookException error) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Error Login: " + error.toString() );
            }
        });


    }

}
