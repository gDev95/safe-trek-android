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
     * @params none (for now)
     * @return EmergencyServices
     */

    public EmergencyServices(){
        this.fire = false;
        this.medical = false;
    }
}