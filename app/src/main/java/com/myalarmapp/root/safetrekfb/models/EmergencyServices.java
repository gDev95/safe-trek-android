package com.myalarmapp.root.safetrekfb.models;

import com.squareup.moshi.Json;

public class EmergencyServices {

    // ******* ATTRIBUTES ******
    @Json(name = "police")
    private boolean police = true;

    @Json(name = "fire")
    private boolean fire;

    @Json(name = "medical")
    private boolean medical;

    // ********* CONSTRUCTOR *********
    /*
     * !Note! that you can scale the constructor as you want
     * For example could the constructor take in booleans to determine
     * which services are needed. For my work, all I wanted/needed was the police service
     * to be true
     */

    /*
     * @params none (for now)
     * @return EmergencyServices
     */

    public EmergencyServices(){
        this.fire = false;
        this.medical = false;
    }
}