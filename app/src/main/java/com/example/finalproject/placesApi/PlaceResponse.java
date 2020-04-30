package com.example.finalproject.placesApi;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by samarthkejriwal on 10/08/17.
 */

public class PlaceResponse {

    @SerializedName("results")
    public ArrayList<results> results = new ArrayList<>();

    @SerializedName("status")
    public String status;

    @SerializedName("photos")
    public ArrayList<Object> objects;

    @SerializedName("result")
    public result result;

    public class results implements Serializable {
        @SerializedName("formatted_address")
        public String formatted_adress;

    }

    public class result implements Serializable
    {
        @SerializedName("formatted_address")
        public String formatted_adress;
        @SerializedName("international_phone_number")
        public String international_phone_number;
        @SerializedName("rating")
        public float rating;
        @SerializedName("place_id")
        public String placeId;
    }
}
