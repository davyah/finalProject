package com.example.finalproject.adapter;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.placesApi.Place;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
/**java file for home page*/
public class HomepageAdapter extends RecyclerView.Adapter<HomepageAdapter.MyViewHolder> {

    private List<Place> storeModels;
    private Context context;
    private String current_address;
    private onClickListener onClickListener;

    public void setOnClickListener(onClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    public HomepageAdapter(Context context ,List<Place> storeModels, String current_address)
    {

        this.context = context;
        this.storeModels = storeModels;
        this.current_address = current_address;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_restaurant, parent, false);

            return new MyViewHolder(itemView,viewType);

    }



    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onClickListener!=null){
                        onClickListener.OnClick(position);
                    }
                }
            });
            holder.res_name.setText(storeModels.get(position).name);

            Picasso.with(context).load(storeModels.get(position) .photourl)
                    .resize(100, 100).into(holder.res_image);


            holder.res_address.setText(storeModels.get(position).address);

//            if(storeModels.get(holder.getAdapterPosition() - 1).phone_no == null)
//            {
//                holder.res_phone.setText("N/A");
//            }
//            else  holder.res_phone.setText(storeModels.get(holder.getAdapterPosition() - 1).phone_no);

            holder.res_rating.setRating(storeModels.get(position).rating);

//            holder.res_distance.setText(storeModels.get(holder.getAdapterPosition() - 1).distance);

            Log.i("details on adapter", storeModels.get(position).name + "  " +
                    storeModels.get(position).address +
                    "  " +  storeModels.get(position).distance);
        }

//        else if (holder.view_type == TYPE_HEAD)
//        {
//            if(current_address == null)
//            {
//                holder.current_location.setText("Unable to Detect Current Location");
//            }
//            else {
//                holder.current_location.setText(current_address);
//            }
//        }


    @Override
    public int getItemCount() {

        return  storeModels.size();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView res_name;
        RatingBar res_rating;
        TextView res_address;
//        TextView res_phone;
//        TextView res_distance;
//        TextView current_location;
        ImageView res_image,location_image;

        int view_type;

        public MyViewHolder(final View itemView, final int viewType) {
            super(itemView);



                view_type=1;
                this.res_name = (TextView) itemView.findViewById(R.id.res_name);
                this.res_rating = (RatingBar) itemView.findViewById(R.id.rating);
                this.res_address = (TextView) itemView.findViewById(R.id.address);
//                this.res_phone = (TextView) itemView.findViewById(R.id.phone);
//                this.res_distance = (TextView) itemView.findViewById(distance);
                this.res_image = (ImageView) itemView.findViewById(R.id.res_image);

//            else  if(viewType == TYPE_HEAD){
//                view_type = 0;
//                this.current_location = (TextView) itemView.findViewById(R.id.location_tv);
//                this.location_image = (ImageView) itemView.findViewById(R.id.current_location);
//                location_image.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Log.i("button","clicked");
//                        Intent intent = new Intent(view.getContext(), MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        view.getContext().startActivity(intent);
//
//                    }
//                });
//
//            }

        }


    }
    public interface onClickListener{
        void OnClick(int position);
    }
}
