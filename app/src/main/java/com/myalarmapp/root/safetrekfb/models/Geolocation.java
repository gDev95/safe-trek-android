package com.myalarmapp.root.safetrekfb.models;

import com.squareup.moshi.Json;

public class Geolocation {

    /********* ATTRIBUTES ***********/

    @Json(name = "lat")
    private double latitude;
    @Json(name = "lng")
    private double longitude;
    @Json(name="accuracy")
    private int accurracy = 10;

    //**********CONSTRUCTOR**********
     /*
     *@params latitude and longitude
     *@return Geolocation
     */
    public Geolocation (double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
