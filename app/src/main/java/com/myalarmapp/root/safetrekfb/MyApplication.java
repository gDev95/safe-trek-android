package com.myalarmapp.root.safetrekfb;

import android.app.Application;

/*
 * Create an instance of the application when launched
 * so that we can use it to get to the shared Preferences
 * which is the interal storage of information of your app
 *
 *
 */
public class MyApplication extends Application {
    public static MyApplication instance;
    @Override
    public void onCreate(){
        super.onCreate();
        instance=this;
    }
}
