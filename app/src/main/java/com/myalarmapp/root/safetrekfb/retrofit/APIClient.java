package com.myalarmapp.root.safetrekfb.retrofit;

import com.myalarmapp.root.safetrekfb.models.Alarm;
import com.myalarmapp.root.safetrekfb.models.OAuthToken;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIClient {

    // Send HTTP POST Request to Safe Trek API to create an alarm
    @POST("alarms")
    Call<Alarm> createAlarm(@Body Alarm alarm);

}
