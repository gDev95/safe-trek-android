package com.myalarmapp.root.safetrekfb.models;

import com.squareup.moshi.Json;

public class Alarm {
    // ********* ATTTRIBUTES *********

    @Json(name = "services")
    private EmergencyServices emServices;

    @Json(name = "location.coordinates")
    private Geolocation geolocation;

    // ************** CONSTRUCTOR ***********
    /*
     * @params EmergencyServices, Geolocation
     * @return Alarm
     */
    public Alarm (EmergencyServices emServices, Geolocation geolocation){
        this.geolocation = geolocation;
        this.emServices = emServices;
    }

}
