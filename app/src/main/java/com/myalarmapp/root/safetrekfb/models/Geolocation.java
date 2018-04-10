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

    //*********** MEMBER FUNCTIONS **********
    // SETTER
    /* set the coordinates (using latitude and longitude)
     * @params double latitude, double longitude
     * @return nothing
     */
    public void setGeolocation(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    // GETTER
    /* get latitude
     * @params none
     * @return double latitude
     */
    public double getLatitude(){
        return this.latitude;
    }
    // GETTER
    /* get longitude
     * @params none
     * @return double longitude
     */
    public double getLongitude(){
        return this.longitude;
    }
}
