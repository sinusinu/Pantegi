package kr.pe.sinu.pantegi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import kr.pe.sinu.pantegi.databinding.FragmentCalendarBinding;
import okhttp3.HttpUrl;

public class CalendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    BroadcastReceiver br;
    FragmentManager fm;
    SharedPreferences spCircledDays;
    int todayDate = 0;

    HolidayProvider hp;

    boolean[] circled = {
            false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            false, false, false
    };

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        return new CalendarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var activity = getActivity();
        fm = getParentFragmentManager();
        if (activity != null) spCircledDays = activity.getSharedPreferences("kr.pe.sinu.pantegi.circles", 0);
        // TODO: change me!
        hp = new MyHolidayProvider();
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Calendar now = Calendar.getInstance();
                if (now.get(Calendar.MINUTE) == 0 && (CalendarFragment.this.getActivity() instanceof MainActivity)) {
                    MainActivity mainActivity = (MainActivity)CalendarFragment.this.getActivity();
                    int[] nch = mainActivity.getNoCuckooHours();
                    int hour = now.get(Calendar.HOUR_OF_DAY);
                    boolean shouldCuckoo = true;
                    for (int j : nch) {
                        if (j == hour) {
                            shouldCuckoo = false;
                            break;
                        }
                    }
                    if (shouldCuckoo) {
                        mainActivity.cuckoo();
                    }
                }
                if (now.get(Calendar.DATE) != todayDate) updateCalendar(true);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        var activity = getActivity();
        if (activity != null) activity.registerReceiver(br, new IntentFilter("android.intent.action.TIME_TICK"));
    }

    @Override
    public void onPause() {
        super.onPause();
        var activity = getActivity();
        if (activity != null) activity.unregisterReceiver(br);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        updateCalendar(true);
        for (var wv : new TextView[] { binding.tvCalendarW1, binding.tvCalendarW2, binding.tvCalendarW3, binding.tvCalendarW4, binding.tvCalendarW5, binding.tvCalendarW6, binding.tvCalendarW7 }) {
            wv.setOnClickListener((v) -> {
                fm.beginTransaction().replace(R.id.fcv_main_right, SettingsFragment.class, (Bundle) null).commit();
            });
        }
        return binding.getRoot();
    }

    private void updateCalendar(boolean updateHolidays) {
        var activity = getActivity();
        if (activity == null) return;
        if (updateHolidays) {
            Thread tUpdateCalendar = new Thread(() -> {
                hp.update();
                activity.runOnUiThread(() -> {
                    internalUpdateCalendar(activity);
                });
            });
            tUpdateCalendar.start();
        } else {
            internalUpdateCalendar(activity);
        }
    }

    private void internalUpdateCalendar(Activity activity) {
        int paintFlags;
        TextView[] dates = {
                this.binding.tvCalendar11, this.binding.tvCalendar12, this.binding.tvCalendar13, this.binding.tvCalendar14, this.binding.tvCalendar15, this.binding.tvCalendar16, this.binding.tvCalendar17,
                this.binding.tvCalendar21, this.binding.tvCalendar22, this.binding.tvCalendar23, this.binding.tvCalendar24, this.binding.tvCalendar25, this.binding.tvCalendar26, this.binding.tvCalendar27,
                this.binding.tvCalendar31, this.binding.tvCalendar32, this.binding.tvCalendar33, this.binding.tvCalendar34, this.binding.tvCalendar35, this.binding.tvCalendar36, this.binding.tvCalendar37,
                this.binding.tvCalendar41, this.binding.tvCalendar42, this.binding.tvCalendar43, this.binding.tvCalendar44, this.binding.tvCalendar45, this.binding.tvCalendar46, this.binding.tvCalendar47,
                this.binding.tvCalendar51, this.binding.tvCalendar52, this.binding.tvCalendar53, this.binding.tvCalendar54, this.binding.tvCalendar55, this.binding.tvCalendar56, this.binding.tvCalendar57,
                this.binding.tvCalendar61, this.binding.tvCalendar62, this.binding.tvCalendar63, this.binding.tvCalendar64, this.binding.tvCalendar65, this.binding.tvCalendar66, this.binding.tvCalendar67
        };
//                for (TextView date : dates) date.setOnClickListener(null);

        var sundayColor = activity.getColor(R.color.calendar_sunday);
        var saturdayColor = activity.getColor(R.color.calendar_saturday);
        var weekdayColor = activity.getColor(R.color.fg_white);
        var shadowColor = activity.getColor(R.color.fg_shadow);

        final Calendar today = Calendar.getInstance();

//                Arrays.fill(this.circled, false);
//                String circledDaysOfThisMonthStr = this.spCircledDays.getString("c_" + today.get(1) + "_" + today.get(Calendar.MONTH), "[]");
//                try {
//                    JSONArray circledDaysOfThisMonth = new JSONArray(circledDaysOfThisMonthStr);
//                    for (int i = 0; i < circledDaysOfThisMonth.length(); i++) {
//                        this.circled[circledDaysOfThisMonth.getInt(i)] = true;
//                    }
//                } catch (JSONException e) {
//                    //noinspection CallToPrintStackTrace
//                    e.printStackTrace();
//                }

        todayDate = today.get(Calendar.DATE);
        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(Calendar.DATE, 1);
        int firstDayOfMonthWeek = firstDayOfMonth.get(7) - 1;
        for (int i = 0; i < firstDayOfMonthWeek; i++) {
            dates[i].setVisibility(View.INVISIBLE);
        }
        int thisDate = 0;
        while (thisDate < 42 - firstDayOfMonthWeek) {
            int holidayState = hp.getHoliday(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, thisDate + 1);
            if (thisDate >= firstDayOfMonth.getActualMaximum(Calendar.DATE)) {
                dates[thisDate + firstDayOfMonthWeek].setVisibility(View.INVISIBLE);
            } else {
                dates[thisDate + firstDayOfMonthWeek].setVisibility(View.VISIBLE);
                dates[thisDate + firstDayOfMonthWeek].setText(String.valueOf(thisDate + 1));
                final int day = thisDate;
//                        dates[thisDate + firstDayOfMonthWeek].setOnClickListener((v) -> {
//                            toggleCircle(today.get(Calendar.YEAR), today.get(Calendar.MONTH), day);
//                            updateCalendar();
//                        });
                TextView thisDayTextView = dates[thisDate + firstDayOfMonthWeek];
                if ((holidayState & HolidayProvider.SPECIAL_DAY) > 0) {
                    paintFlags = thisDayTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG;
                } else {
                    paintFlags = thisDayTextView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG;
                }
                thisDayTextView.setPaintFlags(paintFlags);
                if (thisDate + 1 == todayDate) {
//                            if (this.circled[thisDate]) {
//                                dates[thisDate + firstDayOfMonthWeek].setBackgroundResource(R.drawable.ic_circle_today);
//                            } else {
//                                dates[thisDate + firstDayOfMonthWeek].setBackgroundColor(getActivity().getColor(R.color.calendar_today_bg));
//                            }
                    dates[thisDate + firstDayOfMonthWeek].setBackgroundColor(getActivity().getColor(R.color.calendar_today_bg));
                    dates[thisDate + firstDayOfMonthWeek].setShadowLayer(0.0f, 0.0f, 0.0f, shadowColor);
                    if ((thisDate + firstDayOfMonthWeek) % 7 == 0 || holidayState == HolidayProvider.HOLIDAY) {
                        dates[thisDate + firstDayOfMonthWeek].setTextColor(sundayColor);
                    } else if ((thisDate + firstDayOfMonthWeek) % 7 != 6) {
                        dates[thisDate + firstDayOfMonthWeek].setTextColor(activity.getColor(R.color.calendar_today_fg));
                    } else {
                        dates[thisDate + firstDayOfMonthWeek].setTextColor(saturdayColor);
                    }
                } else {
//                            if (this.circled[thisDate]) {
//                                dates[thisDate + firstDayOfMonthWeek].setBackgroundResource(R.drawable.ic_circle);
//                            } else {
//                                dates[thisDate + firstDayOfMonthWeek].setBackground(null);
//                            }
                    dates[thisDate + firstDayOfMonthWeek].setBackground(null);
                    dates[thisDate + firstDayOfMonthWeek].setShadowLayer(2.0f, 2.0f, 2.0f, shadowColor);
                    if ((thisDate + firstDayOfMonthWeek) % 7 == 0 || holidayState == HolidayProvider.HOLIDAY) {
                        dates[thisDate + firstDayOfMonthWeek].setTextColor(sundayColor);
                    } else if ((thisDate + firstDayOfMonthWeek) % 7 == 6) {
                        dates[thisDate + firstDayOfMonthWeek].setTextColor(saturdayColor);
                    } else {
                        dates[thisDate + firstDayOfMonthWeek].setTextColor(weekdayColor);
                    }
                }
            }
            thisDate++;
        }
        // this make individual cells too long for rare 4-week months, which looks bad
        //this.binding.llCalendar5.setVisibility(today.getActualMaximum(Calendar.WEEK_OF_MONTH) > 4 ? View.VISIBLE : View.GONE);
        this.binding.llCalendar6.setVisibility(today.getActualMaximum(Calendar.WEEK_OF_MONTH) > 5 ? View.VISIBLE : View.GONE);
    }

    private void toggleCircle(int y, int m, int d) {
        String circledDaysOfThisMonthStr = this.spCircledDays.getString("c_" + y + "_" + m, "[]");
        try {
            JSONArray circledDaysOfThisMonth = new JSONArray(circledDaysOfThisMonthStr);
            ArrayList<Integer> listCircledDaysOfThisMonth = new ArrayList<>();
            boolean exist = false;
            for (int i = 0; i < circledDaysOfThisMonth.length(); i++) {
                int day = circledDaysOfThisMonth.getInt(i);
                if (d == day) {
                    exist = true;
                } else {
                    listCircledDaysOfThisMonth.add(day);
                }
            }
            if (!exist) {
                circledDaysOfThisMonth.put(d);
                this.spCircledDays.edit().putString("c_" + y + "_" + m, circledDaysOfThisMonth.toString()).apply();
            } else {
                JSONArray newCircledDaysOfThisMonth = new JSONArray();
                for (int i : listCircledDaysOfThisMonth) {
                    newCircledDaysOfThisMonth.put(i);
                }
                this.spCircledDays.edit().putString("c_" + y + "_" + m, newCircledDaysOfThisMonth.toString()).apply();
            }
            updateCalendar(false);
        } catch (JSONException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}