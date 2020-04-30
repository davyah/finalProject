package com.example.finalproject.placesApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressProvider {
    APIInterface apiService;
    public AddressProvider(final String latLngString,AddressListener listener){
        apiService = ApiClient.getClient().create(APIInterface.class);
        Call<PlaceResponse> call = apiService.getCurrentAddress(latLngString, ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {

                PlaceResponse details = (PlaceResponse) response.body();

                if ("OK".equalsIgnoreCase(details.status)) {

                    if (listener!=null)listener.onAddressFetched(details.results.get(0).formatted_adress);

                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                call.cancel();
            }
        });

    }

    public interface AddressListener{
        void onAddressFetched(String message);
    }
}
