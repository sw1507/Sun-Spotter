package edu.uw.sw1507.sun;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import static android.R.attr.data;

/**
 * Forecast Adapter assis transform weather forecast data with both icons and strings to fit ListView format.
 * Created by Su Wang on 2017/10/13.
 */

public class ForecastAdapter extends ArrayAdapter<ForecastAdapter.ForecastData> {
    public static class ForecastData {
        public Drawable picture;
        public String weather;
        public String time;
        public String temp;


        public ForecastData(Drawable picture, String weather, String time, String temp) {
            this.weather = weather;
            this.picture = picture;
            this.time = time;
            this.temp = temp;
        }
    }
        public ForecastAdapter(Context context, ArrayList<ForecastData> data) {
            super(context, 0, data);
        }

        public static class ViewHolder {
            TextView text;
            ImageView picture;
            int position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            // Get the data item for this position
            ForecastData user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
                // Lookup view for data population
                holder = new ViewHolder();

                holder.text= (TextView) convertView.findViewById(R.id.tvText);
                holder.picture = (ImageView) convertView.findViewById(R.id.tvPicture);
                // Populate the data into the template view using the data object
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            String weatherCondition = user.weather + " @ " + user.time +" (" + user.temp + "\u00b0" + ")";
            holder.text.setText(weatherCondition);
            holder.picture.setImageDrawable(user.picture);
            // Return the completed view to render on screen
            return convertView;

        }
    }
