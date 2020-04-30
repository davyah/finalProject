package com.example.finalproject.placesApi;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PlaceProvider {
    private APIInterface apiService;
    private PlaceProviderListener listener;
    public PlaceProvider(String placeType, String latLngString, long radius) {
        apiService = ApiClient.getClient().create(APIInterface.class);
        fetchPlaces(placeType, latLngString, radius);

    }
    public void setPlaceProviderListener(PlaceProviderListener providerListener){
        this.listener = providerListener;
    }
    private void fetchPlaces(String placeType, String latLngString, long radius) {
        Call<PlacesResponse.Root> call = apiService.doPlaces(latLngString, radius, placeType, ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesResponse.Root>() {
            @Override
            public void onResponse(Call<PlacesResponse.Root> call, Response<PlacesResponse.Root> response) {
                PlacesResponse.Root root = (PlacesResponse.Root) response.body();

                if (response.isSuccessful()) {

                    switch (root.status) {
                        case "OK":

                            ArrayList<PlacesResponse.CustomA>results = root.customA;


                            String photourl;
                            Log.i(TAG, "fetch stores");


                            for (int i = 0; i < results.size(); i++) {

                                PlacesResponse.CustomA info = (PlacesResponse.CustomA) results.get(i);

                                String place_id = results.get(i).place_id;


                                if (results.get(i).photos != null) {

                                    String photo_reference = results.get(i).photos.get(0).photo_reference;

                                    photourl = ApiClient.base_url + "place/photo?maxwidth=100&photoreference=" + photo_reference +
                                            "&key=" + ApiClient.GOOGLE_PLACE_API_KEY;

                                } else {
                                    photourl = "NA";
                                }

                                fetchDistance(info, place_id, photourl,latLngString);


                                Log.i("Coordinates  ", info.geometry.locationA.lat + " , " + info.geometry.locationA.lng);
                                Log.i("Names  ", info.name);

                            }

                            break;
                        case "ZERO_RESULTS":
                            if(listener!=null)listener.onError("ZERO RESULTS");
                            break;
                        case "OVER_QUERY_LIMIT":
                            if(listener!=null)listener.onError("OVER_QUERY_LIMIT");
                            break;
                        default:
                            if(listener!=null)listener.onError("ERROR");
                            break;
                    }

                } else if (response.code() != 200) {
                    if(listener!=null)listener.onError("Error " + response.code() + " found.");

                }

            }

            @Override
            public void onFailure(Call<PlacesResponse.Root> call, Throwable t) {

            }
        });
    }
    private void fetchDistance(final PlacesResponse.CustomA info, final String place_id, final String photourl,String latLngString) {

        Log.i(TAG,"Distance API call start");

        Call<DistanceResponse> call = apiService.getDistance(latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng,ApiClient.GOOGLE_PLACE_API_KEY);

        call.enqueue(new Callback<DistanceResponse>() {
            @Override
            public void onResponse(Call<DistanceResponse> call, Response<DistanceResponse> response) {

                DistanceResponse resultDistance = (DistanceResponse) response.body();

                if (response.isSuccessful()) {

                    Log.i(TAG, resultDistance.status);

                    if ("OK".equalsIgnoreCase(resultDistance.status)) {
                        DistanceResponse.InfoDistance row1 = resultDistance.rows.get(0);
                        DistanceResponse.InfoDistance.DistanceElement element1 = row1.elements.get(0);

                        if ("OK".equalsIgnoreCase(element1.status)) {

                            DistanceResponse.InfoDistance.ValueItem itemDistance = element1.distance;

                            String total_distance = itemDistance.text;

                            fetchPlace_details(info, place_id, total_distance, info.name, photourl);
                        }


                    }

                }
                else if (response.code() != 200){
                    if(listener!=null)listener.onError("Error " + response.code() + " found.");
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                if(listener!=null)listener.onError("Error in Fetching Details,Please Refresh");
                call.cancel();
            }
        });

    }



    private void fetchPlace_details(final PlacesResponse.CustomA info, final String place_id, final String totaldistance, final String name, final String photourl)
    {

        Call<PlaceResponse> call = apiService.getPlaceDetails(place_id, ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {

                PlaceResponse details = (PlaceResponse) response.body();

                if ("OK".equalsIgnoreCase(details.status)) {

                    String address = details.result.formatted_adress;
                    String phone = details.result.international_phone_number;
                    float rating = details.result.rating;

                    Place place =new Place(address, phone, rating,totaldistance,name,photourl);
                    if(listener!=null)listener.onPlaceFetched(place);


                }

            }

            @Override
            public void onFailure(Call call, Throwable t)
            {
                call.cancel();
            }
        });

    }
    public interface PlaceProviderListener{
        void onError(String msg);
        void onPlaceFetched(Place place);
    }
}

