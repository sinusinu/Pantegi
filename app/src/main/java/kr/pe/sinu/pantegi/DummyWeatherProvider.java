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

public class DummyWeatherProvider implements WeatherProvider {
    @Override
    public WeatherData fetchWeather(Context context, OkHttpClient client) {
        WeatherData ret = new WeatherData();

        ret.currentTemp = "12.3°C";
        ret.currentWeatherDesc = context.getString(R.string.weather_desc_overcast);
        ret.currentWeatherIcon = WeatherData.WEATHER_ICON_CLOUDY;

        ret.ancillaryData = new WeatherData.AncillaryData[3];

        ret.ancillaryData[0] = new WeatherData.AncillaryData();
        ret.ancillaryData[0].type = WeatherData.ANCILLARY_DATA_TYPE_WITH_INDICATOR;
        ret.ancillaryData[0].title = WeatherData.ANCILLARY_DATA_TITLE_PM10;
        ret.ancillaryData[0].primaryValue = "21";
        ret.ancillaryData[0].secondaryValue = "green";

        ret.ancillaryData[1] = new WeatherData.AncillaryData();
        ret.ancillaryData[1].type = WeatherData.ANCILLARY_DATA_TYPE_WITH_INDICATOR;
        ret.ancillaryData[1].title = WeatherData.ANCILLARY_DATA_TITLE_PM25;
        ret.ancillaryData[1].primaryValue = "7";
        ret.ancillaryData[1].secondaryValue = "blue";

        ret.ancillaryData[2] = new WeatherData.AncillaryData();
        ret.ancillaryData[2].type = WeatherData.ANCILLARY_DATA_TYPE_SIMPLE;
        ret.ancillaryData[2].title = WeatherData.ANCILLARY_DATA_TITLE_UVI;
        ret.ancillaryData[2].primaryValue = "Low";

        ret.fiveDayForecasts = new WeatherData.FiveDayForecast[5];

        ret.fiveDayForecasts[0] = new WeatherData.FiveDayForecast();
        ret.fiveDayForecasts[0].date = "S 1";
        ret.fiveDayForecasts[0].icon = WeatherData.WEATHER_ICON_CLEAR;
        ret.fiveDayForecasts[0].minTemp = "5°C";
        ret.fiveDayForecasts[0].maxTemp = "14°C";

        ret.fiveDayForecasts[1] = new WeatherData.FiveDayForecast();
        ret.fiveDayForecasts[1].date = "M 2";
        ret.fiveDayForecasts[1].icon = WeatherData.WEATHER_ICON_PARTLY_CLOUDY;
        ret.fiveDayForecasts[1].minTemp = "4°C";
        ret.fiveDayForecasts[1].maxTemp = "13°C";

        ret.fiveDayForecasts[2] = new WeatherData.FiveDayForecast();
        ret.fiveDayForecasts[2].date = "T 3";
        ret.fiveDayForecasts[2].icon = WeatherData.WEATHER_ICON_RAINY;
        ret.fiveDayForecasts[2].minTemp = "3°C";
        ret.fiveDayForecasts[2].maxTemp = "10°C";

        ret.fiveDayForecasts[3] = new WeatherData.FiveDayForecast();
        ret.fiveDayForecasts[3].date = "W 4";
        ret.fiveDayForecasts[3].icon = WeatherData.WEATHER_ICON_POURING;
        ret.fiveDayForecasts[3].minTemp = "3°C";
        ret.fiveDayForecasts[3].maxTemp = "9°C";

        ret.fiveDayForecasts[4] = new WeatherData.FiveDayForecast();
        ret.fiveDayForecasts[4].date = "T 5";
        ret.fiveDayForecasts[4].icon = WeatherData.WEATHER_ICON_CLOUDY;
        ret.fiveDayForecasts[4].minTemp = "3°C";
        ret.fiveDayForecasts[4].maxTemp = "10°C";

        ret.lastUpdated = 0;
        ret.status = "O";

        return ret;
    }
}