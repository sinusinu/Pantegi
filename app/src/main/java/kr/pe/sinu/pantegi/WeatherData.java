package kr.pe.sinu.pantegi;

import androidx.annotation.IntDef;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WeatherData {
    public static final int WEATHER_ICON_LOADING = 0;
    public static final int WEATHER_ICON_CLEAR = 1;
    public static final int WEATHER_ICON_PARTLY_CLOUDY = 2;
    public static final int WEATHER_ICON_CLOUDY = 3;
    public static final int WEATHER_ICON_RAINY = 4;
    public static final int WEATHER_ICON_RAINY_SNOWY = 5;
    public static final int WEATHER_ICON_SNOWY = 6;
    public static final int WEATHER_ICON_POURING = 7;
    public static final int WEATHER_ICON_THUNDERSTORM = 8;

    @IntDef({
            WEATHER_ICON_LOADING,
            WEATHER_ICON_CLEAR,
            WEATHER_ICON_PARTLY_CLOUDY,
            WEATHER_ICON_CLOUDY,
            WEATHER_ICON_RAINY,
            WEATHER_ICON_RAINY_SNOWY,
            WEATHER_ICON_SNOWY,
            WEATHER_ICON_POURING,
            WEATHER_ICON_THUNDERSTORM,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface WeatherIcon {}

    /**
     * This ancillary data must only show the primary value.
     */
    public static final int ANCILLARY_DATA_TYPE_SIMPLE = 0;
    /**
     * This ancillary data must show the colored indicator.
     */
    public static final int ANCILLARY_DATA_TYPE_WITH_INDICATOR = 1;
    /**
     * This ancillary data must show the subtext.
     */
    public static final int ANCILLARY_DATA_TYPE_WITH_SUBTEXT = 2;

    @IntDef({
            ANCILLARY_DATA_TYPE_SIMPLE,
            ANCILLARY_DATA_TYPE_WITH_INDICATOR,
            ANCILLARY_DATA_TYPE_WITH_SUBTEXT,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AncillaryDataType {}

    public static final int ANCILLARY_DATA_TITLE_PM10 = 0;
    public static final int ANCILLARY_DATA_TITLE_PM25 = 1;
    public static final int ANCILLARY_DATA_TITLE_HUMIDITY = 2;
    public static final int ANCILLARY_DATA_TITLE_WIND = 3;
    public static final int ANCILLARY_DATA_TITLE_UVI = 4;

    @IntDef({
            ANCILLARY_DATA_TITLE_PM10,
            ANCILLARY_DATA_TITLE_PM25,
            ANCILLARY_DATA_TITLE_HUMIDITY,
            ANCILLARY_DATA_TITLE_WIND,
            ANCILLARY_DATA_TITLE_UVI,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AncillaryDataTitle {}

    /**
     * Current temperature. Will be displayed as is. Include unit as well.
     */
    public String currentTemp = "-";
    /**
     * Description of current weather. Will be displayed as is.
     */
    public String currentWeatherDesc = "-";
    /**
     * Icon of current weather. Must be one of {@code WEATHER_ICON_*}.
     */
    @WeatherIcon
    public int currentWeatherIcon = WEATHER_ICON_LOADING;

    /**
     * Ancillary data, up to 3 entries.
     */
    public AncillaryData[] ancillaryData;

    public static class AncillaryData {
        /**
         * Display type of this ancillary data. Must be one of {@code ANCILLARY_DATA_TYPE_*}.
         */
        @AncillaryDataType
        public int type = ANCILLARY_DATA_TYPE_SIMPLE;
        /**
         * Title of this ancillary data. Must be one of {@code ANCILLARY_DATA_TITLE_*}.
         */
        @AncillaryDataTitle
        public int title = ANCILLARY_DATA_TITLE_PM10;
        /**
         * Value of this ancillary data, shown as is.
         */
        public String primaryValue = "-";
        /**
         * Subtext of this ancillary data.
         * If {@code type} is {@code ANCILLARY_DATA_TYPE_WITH_INDICATOR}, must be one of {@code blue}, {@code green}, {@code yellow}, {@code orange}, or {@code red}.
         * If {@code type} is {@code ANCILLARY_DATA_TYPE_WITH_SUBTEXT}, shown as is.
         */
        public String secondaryValue = "-";
    }

    /**
     * Five-day forecast, up to 5 entries.
     */
    public FiveDayForecast[] fiveDayForecasts;

    public static class FiveDayForecast {
        /**
         * Date of this day's forecast, shown as is. Keep it short.
         */
        public String date = "-";
        /**
         * Icon of this day's weather. Must be one of {@code WEATHER_ICON_*}.
         */
        @WeatherIcon
        public int icon = WEATHER_ICON_LOADING;
        /**
         * Highest temperature of this day's forecast, shown as is. Keep it short, include unit as well.
         */
        public String maxTemp = "-";
        /**
         * Lowest temperature of this day's forecast, shown as is. Keep it short, include unit as well.
         */
        public String minTemp = "-";
    }

    // diagnostics
    /**
     * Unix timestamp of the time this data has been fetched.
     */
    public long lastUpdated = 0;
    /**
     * Could be a fetch error indicator, but anything is fine.
     */
    public String status = "-";

    public static WeatherData fromJson(String jsonString) {
        WeatherData data = new WeatherData();
        try {
            JSONObject json = new JSONObject(jsonString);

            // Current weather
            data.currentTemp = json.optString("current_temp", "-");
            data.currentWeatherDesc = json.optString("current_weather_desc", "-");
            data.currentWeatherIcon = json.optInt("current_weather_icon", WEATHER_ICON_LOADING);

            // Ancillary data
            JSONArray ancillaryArray = json.optJSONArray("ancillary_data");
            if (ancillaryArray != null) {
                int len = Math.min(ancillaryArray.length(), 3);
                data.ancillaryData = new AncillaryData[len];
                for (int i = 0; i < len; i++) {
                    JSONObject obj = ancillaryArray.optJSONObject(i);
                    if (obj == null) continue;

                    AncillaryData a = new AncillaryData();
                    a.type = obj.optInt("type", ANCILLARY_DATA_TYPE_SIMPLE);
                    a.title = obj.optInt("title", 0);
                    a.primaryValue = obj.optString("primary_value", "-");
                    a.secondaryValue = obj.optString("secondary_value", "-");
                    data.ancillaryData[i] = a;
                }
            }

            // Five-day forecasts
            JSONArray forecastArray = json.optJSONArray("five_day_forecasts");
            if (forecastArray != null) {
                int len = Math.min(forecastArray.length(), 5);
                data.fiveDayForecasts = new FiveDayForecast[len];
                for (int i = 0; i < len; i++) {
                    JSONObject obj = forecastArray.optJSONObject(i);
                    if (obj == null) continue;

                    FiveDayForecast f = new FiveDayForecast();
                    f.date = obj.optString("date", "-");
                    f.icon = obj.optInt("icon", WEATHER_ICON_LOADING);
                    f.maxTemp = obj.optString("max_temp", "-");
                    f.minTemp = obj.optString("min_temp", "-");
                    data.fiveDayForecasts[i] = f;
                }
            }

            // Diagnostics
            data.lastUpdated = json.optLong("last_updated", 0L);
            data.status = json.optString("status", "-");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return null;
        }
        return data;
    }

    public static String toJson(WeatherData data) {
        try {JSONObject json = new JSONObject();

            // Current weather
            json.put("current_temp", data.currentTemp);
            json.put("current_weather_desc", data.currentWeatherDesc);
            json.put("current_weather_icon", data.currentWeatherIcon);

            // Ancillary data
            if (data.ancillaryData != null) {
                JSONArray ancillaryArray = new JSONArray();
                for (AncillaryData a : data.ancillaryData) {
                    if (a == null) continue;
                    JSONObject obj = new JSONObject();
                    obj.put("type", a.type);
                    obj.put("title", a.title);
                    obj.put("primary_value", a.primaryValue);
                    obj.put("secondary_value", a.secondaryValue);
                    ancillaryArray.put(obj);
                }
                json.put("ancillary_data", ancillaryArray);
            }

            // Five-day forecasts
            if (data.fiveDayForecasts != null) {
                JSONArray forecastArray = new JSONArray();
                for (FiveDayForecast f : data.fiveDayForecasts) {
                    if (f == null) continue;
                    JSONObject obj = new JSONObject();
                    obj.put("date", f.date);
                    obj.put("icon", f.icon);
                    obj.put("max_temp", f.maxTemp);
                    obj.put("min_temp", f.minTemp);
                    forecastArray.put(obj);
                }
                json.put("five_day_forecasts", forecastArray);
            }

            // Diagnostics
            json.put("last_updated", data.lastUpdated);
            json.put("status", data.status);

            return json.toString();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return null;
        }
    }
}