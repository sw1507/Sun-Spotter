package edu.uw.sw1507.sun;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ForecastAdapter adapter;
    private static final String TAG = "MainActivity";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEE, h:mm a");
    private static final String MSG_KEY = "message_key";
    private static String MSG_VALUE;

    /**
     * Setup the layout, checkif there is previous downloads exist, if not download weather data according to user input
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final EditText searchField = (EditText) findViewById(R.id.txt_search);
        final Button searchButton = (Button) findViewById(R.id.btnSearch);
        //get the data
        ArrayList<ForecastAdapter.ForecastData> weatherData = new ArrayList<>();
        //set an adapter to connect data and the ListView
        this.adapter = new ForecastAdapter(this, weatherData);
        AdapterView listView = (AdapterView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        if(savedInstanceState != null){ //Activity has been recreated
            String msg = savedInstanceState.getString(MSG_KEY);
            String apiKey = getString(R.string.API_KEY);
            downloadWeatherData(msg,apiKey);
        }

        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String searchTerm = searchField.getText().toString();
                Log.v(TAG, "Searching for: " + searchTerm);
                String apiKey = getString(R.string.API_KEY);
                downloadWeatherData(searchTerm,apiKey);
                MSG_VALUE = searchTerm;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(MSG_KEY, MSG_VALUE);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Download weather data according to user's input of city name.
     * @param city should be a string
     * @param apiKey should be api key
     */
    private void downloadWeatherData(String city, String apiKey) {
        //construct the url for the Open Weather APi
        Uri.Builder apiBuilder = new Uri.Builder();

        apiBuilder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("units", "imperial")
                .appendQueryParameter("appid", apiKey);

        apiBuilder.appendQueryParameter("q", city);

        String urlString = apiBuilder.build().toString();
        Log.v(TAG, "urlString: " + urlString);

        Request request = new JsonObjectRequest(Request.Method.GET, urlString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject weathers) {
                        ArrayList<String> weathersResults = new ArrayList<String>();

                        Boolean sunny = false;
                        Date sunnyDay = null;
                        Log.v(TAG, "weathers" + weathers.toString());
                        if (weathers != null) {
                            adapter.clear();
                            Log.v(TAG, "start");
                            try {
                                //parse the JSON results
                                JSONArray jsonArr = weathers.getJSONArray("list");
                                for (int i = 0; i < jsonArr.length(); i++) {
                                    Log.v(TAG, "loop start");
                                    JSONObject jsonObj = jsonArr.getJSONObject(i);

                                    String weather = jsonObj.getJSONArray("weather").getJSONObject(0).get("main").toString();
                                    String icon = jsonObj.getJSONArray("weather").getJSONObject(0).get("icon").toString();

                                    //convert UNIX time to simple date format
                                    int dt = jsonObj.getInt("dt");
                                    Date date = new Date(dt * 1000L);
                                    String dateString = SIMPLE_DATE_FORMAT.format(date);

                                    String temp = jsonObj.getJSONObject("main").getString("temp");

                                    //check sunny time
                                    if (weather.equals("Clear") && !sunny) {
                                        sunny = true;
                                        sunnyDay = date;
                                    }
                                    int drawableId = getResources().getIdentifier("icon" + icon, "drawable", getPackageName());
                                    Drawable drawableIcon = ContextCompat.getDrawable(MainActivity.this,drawableId);
                                            //getDrawable(drawableId);
                                    adapter.add(new ForecastAdapter.ForecastData(drawableIcon, weather, dateString, temp));
                                }
                                ImageView img = (ImageView) findViewById(R.id.weatherImg);
                                TextView weatherTitle = (TextView) findViewById(R.id.weatherResult);
                                TextView weatherTxt = (TextView) findViewById(R.id.weatherDetail);
                                if (sunny) {
                                    //sunny day case
                                    img.setImageResource(R.drawable.ic_check_circle_black_24dp);
                                    img.setColorFilter(Color.YELLOW);
                                    weatherTitle.setText("There will be Sun!");
                                    weatherTxt.setText("At " + SIMPLE_DATE_FORMAT.format(sunnyDay));
                                } else {
                                    //not-sunny day case
                                    img.setImageResource(R.drawable.ic_highlight_off_black_24dp);
                                    img.setColorFilter(Color.GRAY);
                                    weatherTitle.setText("There's no SUN!");
                                    weatherTxt.setText("LOL...");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "ERROR");
                Log.e(TAG, error.toString());
            }
        });

        RequestSingleton.getInstance(this).add(request);
    }

    protected static class RequestSingleton {
        //the single instance of this singleton
        private static RequestSingleton instance;

        private RequestQueue requestQueue = null; //the singleton's RequestQueue
        private ImageLoader imageLoader = null;

        //private constructor; cannot instantiate directly
        private RequestSingleton(Context ctx){
            //create the requestQueue
            this.requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());

            //create the imageLoader
            imageLoader = new ImageLoader(requestQueue,
                    new ImageLoader.ImageCache() {  //define an anonymous Cache object
                        //the cache instance variable
                        private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

                        //method for accessing the cache
                        @Override
                        public Bitmap getBitmap(String url) {
                            return cache.get(url);
                        }

                        //method for storing to the cache
                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            cache.put(url, bitmap);
                        }
                    });
        }

        //call this "factory" method to access the Singleton
        public static RequestSingleton getInstance(Context ctx) {
            //only create the singleton if it doesn't exist yet
            if(instance == null){
                instance = new RequestSingleton(ctx);
            }

            return instance; //return the singleton object
        }

        //get queue from singleton for direct action
        public RequestQueue getRequestQueue() {

            return this.requestQueue;
        }

        //convenience wrapper method
        public <T> void add(Request<T> req) {
            requestQueue.add(req);
        }

        public ImageLoader getImageLoader() {
            return this.imageLoader;
        }
    }
}