package com.example.wa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText locationEditText;
    private Button searchButton;
    private TextView resultTextView;
    private TextView temperatureTextView;
    private ImageView backgroundImageView;  // Corrected this line

    private static final String API_KEY = "c56c0f76ba663466e8878ecfdc52b4f2";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationEditText = findViewById(R.id.locationEditText);
        searchButton = findViewById(R.id.searchButton);
        resultTextView = findViewById(R.id.resultTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        backgroundImageView = findViewById(R.id.backgroundImageView);  // Corrected this line

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = locationEditText.getText().toString();
                if (!location.isEmpty()) {
                    String apiUrl = String.format(API_URL, location, API_KEY);
                    new FetchWeatherTask().execute(apiUrl);
                }
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                // Extract weather information
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject firstWeatherObject = weatherArray.getJSONObject(0);
                String description = firstWeatherObject.getString("description");

                // Extract temperature, humidity, and other details
                JSONObject mainObject = jsonObject.getJSONObject("main");
                double temperatureKelvin = mainObject.getDouble("temp");
                double temperatureCelsius = temperatureKelvin - 273.15;
                int humidity = mainObject.getInt("humidity");
                double tempMin = mainObject.getDouble("temp_min");
                double tempMax = mainObject.getDouble("temp_max");

                // Extract wind information
                JSONObject windObject = jsonObject.getJSONObject("wind");
                double windSpeed = windObject.getDouble("speed");

                // Extract precipitation information
                JSONObject rainObject = jsonObject.optJSONObject("rain");
                double precipitation = (rainObject != null) ? rainObject.getDouble("1h") : 0.0;

                // Update background color based on weather description
                updateBackgroundColor(description);

                // Display information in TextView
                String weatherInfo = "Weather: " + description + "\nTemperature: " + temperatureCelsius + "°C\nHumidity: " + humidity + "%"
                        + "\nWind Speed: " + windSpeed + " m/s\nPrecipitation: " + precipitation + " mm"
                        + "\nHighest Temperature: " + tempMax + "°C\nLowest Temperature: " + tempMin + "°C";
                resultTextView.setText(weatherInfo);

                // Display temperatureTextView with rounded value
                int roundedTemperature = (int) Math.round(temperatureCelsius);
                temperatureTextView.setText(String.valueOf(roundedTemperature) + "°C");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBackgroundColor(String weatherDescription) {
        int backgroundImage;

        if (weatherDescription.contains("rain")) {
            backgroundImage = R.drawable.rainy_background;
        } else if (weatherDescription.contains("cloud")) {
            backgroundImage = R.drawable.cloudy_background;
        } else if (weatherDescription.contains("sun")) {
            backgroundImage = R.drawable.sunny_background;
        } else if (weatherDescription.contains("clear")) {
            backgroundImage = R.drawable.clear_background;
        } else {
            backgroundImage = R.drawable.abc;
        }
        backgroundImageView.setImageResource(backgroundImage);
    }
}
