package kr.pe.sinu.pantegi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.pe.sinu.pantegi.databinding.FragmentWeatherBinding;
import okhttp3.OkHttpClient;

public class WeatherFragment extends Fragment {
    private FragmentWeatherBinding binding;
    private BroadcastReceiver br;
    private WeatherProvider wp;
    private final OkHttpClient client = new OkHttpClient();
    private final String[] ancTitles = new String[5];
    private final int[] weatherIconResIds = new int[9];

    public WeatherFragment() {
        // Required empty public constructor
    }

    public static WeatherFragment newInstance(String param1, String param2) {
        return new WeatherFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ancTitles[0] = getString(R.string.weather_pm10_header);
        ancTitles[1] = getString(R.string.weather_pm25_header);
        ancTitles[2] = getString(R.string.weather_humidity_header);
        ancTitles[3] = getString(R.string.weather_wind_header);
        ancTitles[4] = getString(R.string.weather_uvi_header);

        weatherIconResIds[0] = R.drawable.ic_dots;
        weatherIconResIds[1] = R.drawable.ic_clear;
        weatherIconResIds[2] = R.drawable.ic_partlycloudy;
        weatherIconResIds[3] = R.drawable.ic_cloudy;
        weatherIconResIds[4] = R.drawable.ic_rainy;
        weatherIconResIds[5] = R.drawable.ic_mixedweather;
        weatherIconResIds[6] = R.drawable.ic_snowy;
        weatherIconResIds[7] = R.drawable.ic_pouring;
        weatherIconResIds[8] = R.drawable.ic_thunderstorm;

        // TODO: change me!
        wp = new MyWeatherProvider();

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateDisplay(false);
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        updateDisplay(true);
        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) getActivity().unregisterReceiver(this.br);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) getActivity().registerReceiver(this.br, new IntentFilter("android.intent.action.TIME_TICK"));
    }

    private void updateDisplay(boolean forceUpdateWeather) {
        updateClock();
        if (forceUpdateWeather || Calendar.getInstance().get(Calendar.MINUTE) == 0) updateWeather();
    }

    private void updateClock() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(getString(R.string.weather_date_sdf_format), Locale.getDefault());
        binding.tvWeatherDate.setText(df.format(date));
        SimpleDateFormat tf = new SimpleDateFormat(getString(R.string.weather_time_sdf_format), Locale.getDefault());
        binding.tvWeatherTime.setText(tf.format(date));
    }

    private void updateWeather() {
        @SuppressLint("SetTextI18n")
        Thread thread = new Thread(() -> {
            var activity = getActivity();
            if (activity != null) {
                var dingus = wp.fetchWeather(activity, client);
                activity.runOnUiThread(() -> {
                    binding.tvWeatherNowTemp.setText(dingus.currentTemp);
                    binding.tvWeatherNowDesc.setText(dingus.currentWeatherDesc);
                    binding.ivWeatherNowIcon.setImageResource(weatherIconResIds[dingus.currentWeatherIcon]);
                    LinearLayout[] llAncs = { binding.llWeatherAnc1, binding.llWeatherAnc2, binding.llWeatherAnc3 };
                    TextView[] tvAncTitles = { binding.tvWeatherAnc1Title, binding.tvWeatherAnc2Title, binding.tvWeatherAnc3Title };
                    TextView[] tvAncValues = { binding.tvWeatherAnc1Value, binding.tvWeatherAnc2Value, binding.tvWeatherAnc3Value };
                    TextView[] tvAncSubtexts = { binding.tvWeatherAnc1Subtext, binding.tvWeatherAnc2Subtext, binding.tvWeatherAnc3Subtext };
                    if (dingus.ancillaryData != null) {
                        for (int i = 0; i < 3; i++) {
                            if (i < dingus.ancillaryData.length) {
                                llAncs[i].setVisibility(View.VISIBLE);
                                tvAncTitles[i].setText(ancTitles[dingus.ancillaryData[i].title]);
                                tvAncValues[i].setText(dingus.ancillaryData[i].primaryValue);
                                switch (dingus.ancillaryData[i].type) {
                                    case WeatherData.ANCILLARY_DATA_TYPE_SIMPLE:
                                        tvAncSubtexts[i].setVisibility(View.GONE);
                                        break;
                                    case WeatherData.ANCILLARY_DATA_TYPE_WITH_INDICATOR:
                                        tvAncSubtexts[i].setVisibility(View.VISIBLE);
                                        tvAncSubtexts[i].setText(R.string.weather_subtext_indicator);
                                        switch (dingus.ancillaryData[i].secondaryValue) {
                                            case "blue":
                                                tvAncSubtexts[i].setTextColor(activity.getColor(R.color.weather_subtext_indicator_blue));
                                                break;
                                            case "green":
                                                tvAncSubtexts[i].setTextColor(activity.getColor(R.color.weather_subtext_indicator_green));
                                                break;
                                            case "yellow":
                                                tvAncSubtexts[i].setTextColor(activity.getColor(R.color.weather_subtext_indicator_yellow));
                                                break;
                                            case "orange":
                                                tvAncSubtexts[i].setTextColor(activity.getColor(R.color.weather_subtext_indicator_orange));
                                                break;
                                            case "red":
                                                tvAncSubtexts[i].setTextColor(activity.getColor(R.color.weather_subtext_indicator_red));
                                                break;
                                            default:
                                                break;
                                        }
                                        break;
                                    case WeatherData.ANCILLARY_DATA_TYPE_WITH_SUBTEXT:
                                        tvAncSubtexts[i].setVisibility(View.VISIBLE);
                                        tvAncSubtexts[i].setText(dingus.ancillaryData[i].secondaryValue);
                                        tvAncSubtexts[i].setTextColor(activity.getColor(R.color.fg_white));
                                        break;
                                }
                            } else {
                                llAncs[i].setVisibility(View.GONE);
                            }
                        }
                    } else {
                        for (int i = 0; i < 3; i++) llAncs[i].setVisibility(View.GONE);
                    }
                    LinearLayout[] llDays = { binding.llWeatherDay1, binding.llWeatherDay2, binding.llWeatherDay3, binding.llWeatherDay4, binding.llWeatherDay5 };
                    TextView[] tvDayHeaders = { binding.tvWeatherDay1Header, binding.tvWeatherDay2Header, binding.tvWeatherDay3Header, binding.tvWeatherDay4Header, binding.tvWeatherDay5Header };
                    ImageView[] ivDayIcons = { binding.ivWeatherDay1Icon, binding.ivWeatherDay2Icon, binding.ivWeatherDay3Icon, binding.ivWeatherDay4Icon, binding.ivWeatherDay5Icon };
                    TextView[] tvDayMaxes = { binding.tvWeatherDay1Max, binding.tvWeatherDay2Max, binding.tvWeatherDay3Max, binding.tvWeatherDay4Max, binding.tvWeatherDay5Max };
                    TextView[] tvDayMins = { binding.tvWeatherDay1Min, binding.tvWeatherDay2Min, binding.tvWeatherDay3Min, binding.tvWeatherDay4Min, binding.tvWeatherDay5Min };
                    if (dingus.fiveDayForecasts != null) {
                        for (int i = 0; i < 5; i++) {
                            if (i < dingus.fiveDayForecasts.length) {
                                llDays[i].setVisibility(View.VISIBLE);
                                tvDayHeaders[i].setText(dingus.fiveDayForecasts[i].date);
                                ivDayIcons[i].setImageResource(weatherIconResIds[dingus.fiveDayForecasts[i].icon]);
                                tvDayMaxes[i].setText(dingus.fiveDayForecasts[i].maxTemp);
                                tvDayMins[i].setText(dingus.fiveDayForecasts[i].minTemp);
                            } else {
                                llDays[i].setVisibility(View.GONE);
                            }
                        }
                    } else {
                        for (int i = 0; i < 5; i++) llDays[i].setVisibility(View.GONE);
                    }
                    SimpleDateFormat df = new SimpleDateFormat(getString(R.string.weather_last_updated_time_format), Locale.getDefault());
                    String lus = df.format(new Date(dingus.lastUpdated));
                    binding.tvWeatherUpdateInfo.setText(String.format(getString(R.string.weather_last_updated_format), lus, dingus.status));
                });
            }
        });
        thread.start();
    }
}