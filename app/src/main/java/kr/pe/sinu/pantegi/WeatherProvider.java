package kr.pe.sinu.pantegi;

import android.content.Context;

import okhttp3.OkHttpClient;

public interface WeatherProvider {
    /**
     * Called every hour on separate thread, so networking can be done directly.
     * @param context Context that can access to {@code getString}.
     * @param client Properly prepared OkHttp client.
     */
    public WeatherData fetchWeather(Context context, OkHttpClient client);
}
