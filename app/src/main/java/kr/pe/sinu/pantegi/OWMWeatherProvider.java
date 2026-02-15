package kr.pe.sinu.pantegi;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OWMWeatherProvider implements WeatherProvider {
    private String lat = null;
    private String lon = null;
    private String apiKey = null;

    public OWMWeatherProvider(String lat, String lon, String apiKey) {
        this.lat = lat;
        this.lon = lon;
        this.apiKey = apiKey;
    }

    @Override
    public WeatherData fetchWeather(Context context, OkHttpClient client) {
        File cachedWeatherDataFile = new File(context.getFilesDir(), "last_weather.json");
        if (cachedWeatherDataFile.exists()) {
            StringBuilder sb = new StringBuilder();
            String cachedWeatherDataString = null;
            try (var is = context.openFileInput("last_weather.json"); var isr = new InputStreamReader(is, StandardCharsets.UTF_8); var reader = new BufferedReader(isr)) {
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line).append('\n');
                    line = reader.readLine();
                }
                cachedWeatherDataString = sb.toString();
            } catch (Exception ignored) {}
            if (cachedWeatherDataString != null) {
                var cachedWeatherData = WeatherData.fromJson(cachedWeatherDataString);
                if (cachedWeatherData != null) {
                    var cachedWeatherDataFetchedTimestamp = cachedWeatherData.lastUpdated;
                    var nowTimestamp = System.currentTimeMillis();
                    if (nowTimestamp - cachedWeatherDataFetchedTimestamp <= 1800000L) { // 30 mins
                        Log.d("OWMWeatherProvider", "Cached weather data is still valid, returning cached one");
                        cachedWeatherData.status = "C";
                        return cachedWeatherData;
                    }
                }
            }
        }

        Log.d("OWMWeatherProvider", "Cached weather expired or doesn't exist, fetching again");

        WeatherData data = new WeatherData();
        String failed = "";

        String[] weekdayLabels = new String[8];
        weekdayLabels[1] = context.getString(R.string.weather_5day_sunday);
        weekdayLabels[2] = context.getString(R.string.weather_5day_monday);
        weekdayLabels[3] = context.getString(R.string.weather_5day_tuesday);
        weekdayLabels[4] = context.getString(R.string.weather_5day_wednesday);
        weekdayLabels[5] = context.getString(R.string.weather_5day_thursday);
        weekdayLabels[6] = context.getString(R.string.weather_5day_friday);
        weekdayLabels[7] = context.getString(R.string.weather_5day_saturday);

        WeatherData.AncillaryData[] ancillaryData = new WeatherData.AncillaryData[3];
        ancillaryData[0] = new WeatherData.AncillaryData();
        ancillaryData[0].type = WeatherData.ANCILLARY_DATA_TYPE_WITH_INDICATOR;
        ancillaryData[0].title = WeatherData.ANCILLARY_DATA_TITLE_PM10;
        ancillaryData[1] = new WeatherData.AncillaryData();
        ancillaryData[1].type = WeatherData.ANCILLARY_DATA_TYPE_WITH_INDICATOR;
        ancillaryData[1].title = WeatherData.ANCILLARY_DATA_TITLE_PM25;
        ancillaryData[2] = new WeatherData.AncillaryData();
        ancillaryData[2].type = WeatherData.ANCILLARY_DATA_TYPE_SIMPLE;
        ancillaryData[2].title = WeatherData.ANCILLARY_DATA_TITLE_HUMIDITY;

        try {
            String pmRaw = makeRequest(String.format("http://api.openweathermap.org/data/2.5/air_pollution?lat=%s&lon=%s&appid=%s", lat, lon, apiKey), client);
            if (pmRaw != null) {
                JSONObject root = new JSONObject(pmRaw);
                JSONObject components = root.getJSONArray("list").getJSONObject(0).getJSONObject("components");

                double pm10 = components.getDouble("pm10");
                double pm25 = components.getDouble("pm2_5");

                ancillaryData[0].primaryValue = String.valueOf(Math.round(pm10));
                ancillaryData[0].secondaryValue = getPm10Grade(pm10);

                ancillaryData[1].primaryValue = String.valueOf(Math.round(pm25));
                ancillaryData[1].secondaryValue = getPm25Grade(pm25);
            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            ancillaryData[0].primaryValue = "-"; ancillaryData[0].secondaryValue = ""; ancillaryData[0].type = WeatherData.ANCILLARY_DATA_TYPE_SIMPLE;
            ancillaryData[1].primaryValue = "-"; ancillaryData[1].secondaryValue = ""; ancillaryData[1].type = WeatherData.ANCILLARY_DATA_TYPE_SIMPLE;
            failed = failed + "1";
        }

        WeatherData.FiveDayForecast[] fiveDayForecasts = new WeatherData.FiveDayForecast[5];
        for (int i = 0; i < 5; i++) fiveDayForecasts[i] = new WeatherData.FiveDayForecast();

        try {
            String tempRaw = makeRequest(String.format("https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&exclude=minutely,hourly,alerts&units=metric&appid=%s", lat, lon, apiKey), client);
            if (tempRaw != null) {
                JSONObject root = new JSONObject(tempRaw);
                JSONObject current = root.getJSONObject("current");
                JSONArray daily = root.getJSONArray("daily");

                String weatherId = current.getJSONArray("weather").getJSONObject(0).getString("id");

                data.currentTemp = new DecimalFormat("0.0").format(current.getDouble("temp")) + "°C";
                data.currentWeatherDesc = convertOwmIconToTd(context, weatherId);
                if (data.currentWeatherDesc == null) data.currentWeatherDesc = current.getJSONArray("weather").getJSONObject(0).getString("main");
                data.currentWeatherIcon = convertOwmIconToTIco(weatherId);
                ancillaryData[2].primaryValue = ((int)Math.round(current.getDouble("humidity"))) + "%";

                // Loop for daily forecast (0-4)
                Calendar cal = Calendar.getInstance();
                for (int i = 0; i < 5; i++) {
                    JSONObject day = daily.getJSONObject(i);

                    long ut = day.getLong("dt");
                    cal.setTimeInMillis(ut * 1000L);
                    String utStr = context.getString(R.string.weather_5day_header_c_format);
                    utStr = utStr.replace("{date}", cal.get(Calendar.DATE)+"");
                    utStr = utStr.replace("{week}", weekdayLabels[cal.get(Calendar.DAY_OF_WEEK)]);

                    int icon = convertOwmIconToTIco(day.getJSONArray("weather").getJSONObject(0).getString("id"));

                    String min = String.valueOf((int)Math.round(day.getJSONObject("temp").getDouble("min")));
                    String max = String.valueOf((int)Math.round(day.getJSONObject("temp").getDouble("max")));

                    fiveDayForecasts[i].date = utStr;
                    fiveDayForecasts[i].icon = icon;
                    fiveDayForecasts[i].minTemp = min + "°C";
                    fiveDayForecasts[i].maxTemp = max + "°C";
                }
            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            data.currentTemp = "-";
            data.currentWeatherDesc = "0";
            data.currentWeatherIcon = WeatherData.WEATHER_ICON_LOADING;
            failed = failed + "2";
        }

        data.ancillaryData = ancillaryData;
        data.fiveDayForecasts = fiveDayForecasts;
        data.lastUpdated = System.currentTimeMillis();
        data.status = failed.isEmpty() ? "O" : failed;

        if (failed.isEmpty()) {
            try (var os = context.openFileOutput("last_weather.json", Context.MODE_PRIVATE)) {
                os.write(Objects.requireNonNull(WeatherData.toJson(data)).getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) {}
            return data;
        } else {
            return new WeatherData();
        }
    }

    private String makeRequest(String url, OkHttpClient client) throws Exception {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Log.e("OWMWeatherProvider", "Failed to fetch response: HTTP " + response.code());
            }
        }
        return null;
    }

    private String getPm10Grade(double val) {
        if (val <= 30) return "blue";
        if (val <= 80) return "green";
        if (val <= 150) return "orange";
        return "red";
    }

    private String getPm25Grade(double val) {
        if (val <= 15) return "blue";
        if (val <= 35) return "green";
        if (val <= 75) return "orange";
        return "red";
    }

    private String convertOwmIconToTd(Context context, String owmIcon) {
        switch (owmIcon.charAt(0)) {
            case '2':
                return context.getString(R.string.weather_desc_thunderstorm);
            case '3':
                return context.getString(R.string.weather_desc_drizzle);
            case '5':
                if (owmIcon.equals("502") ||
                        owmIcon.equals("503") ||
                        owmIcon.equals("504") ||
                        owmIcon.equals("522") ||
                        owmIcon.equals("531")) return context.getString(R.string.weather_desc_heavyrain);
                return context.getString(R.string.weather_desc_rain);
            case '6':
                if (owmIcon.equals("602") ||
                        owmIcon.equals("622")) return context.getString(R.string.weather_desc_heavysnow);
                if (owmIcon.equals("611") ||
                        owmIcon.equals("612") ||
                        owmIcon.equals("613") ||
                        owmIcon.equals("615") ||
                        owmIcon.equals("616")) return context.getString(R.string.weather_desc_showersnow);
                return context.getString(R.string.weather_desc_snow);
            case '7':
                //noinspection IfCanBeSwitch
                if (owmIcon.equals("701") ||
                        owmIcon.equals("711") ||
                        owmIcon.equals("721") ||
                        owmIcon.equals("741")) return context.getString(R.string.weather_desc_foggy);
                if (owmIcon.equals("731") ||
                        owmIcon.equals("751") ||
                        owmIcon.equals("761")) return context.getString(R.string.weather_desc_sandstorm);
                if (owmIcon.equals("781")) return context.getString(R.string.weather_desc_tornado);
                return context.getString(R.string.weather_desc_cloudy_atmos);
            case '8':
                if (owmIcon.equals("800")) return context.getString(R.string.weather_desc_clear);
                if (owmIcon.equals("801") ||
                        owmIcon.equals("802")) return context.getString(R.string.weather_desc_cloudy);
                return context.getString(R.string.weather_desc_overcast);
        }
        return null;
    }

    private int convertOwmIconToTIco(String owmIcon) {
        switch (owmIcon.charAt(0)) {
            case '2':
                return WeatherData.WEATHER_ICON_THUNDERSTORM;
            case '3':
                return WeatherData.WEATHER_ICON_RAINY;
            case '5':
                if (owmIcon.equals("502") ||
                        owmIcon.equals("503") ||
                        owmIcon.equals("504") ||
                        owmIcon.equals("522") ||
                        owmIcon.equals("531")) return WeatherData.WEATHER_ICON_POURING;
                return WeatherData.WEATHER_ICON_RAINY;
            case '6':
                if (owmIcon.equals("611") ||
                        owmIcon.equals("612") ||
                        owmIcon.equals("613") ||
                        owmIcon.equals("615") ||
                        owmIcon.equals("616")) return WeatherData.WEATHER_ICON_RAINY_SNOWY;
                return WeatherData.WEATHER_ICON_SNOWY;
            case '7':
                return WeatherData.WEATHER_ICON_CLOUDY;
            case '8':
                if (owmIcon.equals("800")) return WeatherData.WEATHER_ICON_CLEAR;
                if (owmIcon.equals("801") ||
                        owmIcon.equals("802")) return WeatherData.WEATHER_ICON_PARTLY_CLOUDY;
                return WeatherData.WEATHER_ICON_CLOUDY;
        }
        return WeatherData.WEATHER_ICON_LOADING;
    }
}
