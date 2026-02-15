package kr.pe.sinu.pantegi;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import kr.pe.sinu.pantegi.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    FragmentManager fm;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm = getParentFragmentManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        binding.ivSettingsBack.setOnClickListener(view -> {
            fm.beginTransaction().replace(R.id.fcv_main_right, CalendarFragment.class, null).commit();
        });

        var activity = getActivity();
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity)activity;

            binding.btnSettingsBackgroundBlank.setOnClickListener(v -> mainActivity.setBackground(MainActivity.BACKGROUND_TYPE_BLANK));
            binding.btnSettingsBackgroundWallpaper.setOnClickListener(v -> mainActivity.setBackground(MainActivity.BACKGROUND_TYPE_WALLPAPER));
            binding.btnSettingsBackgroundWallpaperDim.setOnClickListener(v -> mainActivity.setBackground(MainActivity.BACKGROUND_TYPE_WALLPAPER_DIM));
            binding.btnSettingsBackgroundWallpaperChange.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.settings_background_wallpaper_change)));
            });
            binding.btnSettingsBackgroundVideos.setOnClickListener(v -> {
                if (mainActivity.isVideoBackgroundAvailable()) mainActivity.setBackground(MainActivity.BACKGROUND_TYPE_VIDEOS);
                else Toast.makeText(mainActivity, R.string.settings_background_videos_unavailable, Toast.LENGTH_LONG).show();
            });
            binding.btnSettingsBackgroundVideosDim.setOnClickListener(v -> {
                if (mainActivity.isVideoBackgroundAvailable()) mainActivity.setBackground(MainActivity.BACKGROUND_TYPE_VIDEOS_DIM);
                else Toast.makeText(mainActivity, R.string.settings_background_videos_unavailable, Toast.LENGTH_LONG).show();
            });

            binding.btnSettingsEnableCuckoo.setOnClickListener(v -> {
                if (mainActivity.isCuckooAvailable()) {
                    mainActivity.setCuckooEnabled(true);
                    mainActivity.cuckoo();
                } else {
                    Toast.makeText(activity, R.string.settings_cuckoo_unavailable, Toast.LENGTH_LONG).show();
                }
            });
            binding.btnSettingsDisableCuckoo.setOnClickListener(v -> mainActivity.setCuckooEnabled(false));

            CheckBox[] boxes = {this.binding.cbSettingsHour0, this.binding.cbSettingsHour1, this.binding.cbSettingsHour2, this.binding.cbSettingsHour3, this.binding.cbSettingsHour4, this.binding.cbSettingsHour5, this.binding.cbSettingsHour6, this.binding.cbSettingsHour7, this.binding.cbSettingsHour8, this.binding.cbSettingsHour9, this.binding.cbSettingsHour10, this.binding.cbSettingsHour11, this.binding.cbSettingsHour12, this.binding.cbSettingsHour13, this.binding.cbSettingsHour14, this.binding.cbSettingsHour15, this.binding.cbSettingsHour16, this.binding.cbSettingsHour17, this.binding.cbSettingsHour18, this.binding.cbSettingsHour19, this.binding.cbSettingsHour20, this.binding.cbSettingsHour21, this.binding.cbSettingsHour22, this.binding.cbSettingsHour23};
            int[] noCuckooHours = mainActivity.getNoCuckooHours();
            final boolean[] noCuckooHoursBool = new boolean[24];
            for (int noCuckooHour : noCuckooHours) {
                noCuckooHoursBool[noCuckooHour] = true;
                boxes[noCuckooHour].setChecked(false);
            }
            this.binding.cbSettingsHour0.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[0] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour1.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[1] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour2.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[2] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour3.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[3] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour4.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[4] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour5.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[5] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour6.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[6] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour7.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[7] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour8.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[8] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour9.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[9] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour10.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[10] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour11.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[11] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour12.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[12] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour13.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[13] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour14.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[14] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour15.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[15] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour16.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[16] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour17.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[17] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour18.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[18] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour19.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[19] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour20.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[20] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour21.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[21] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour22.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[22] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
            this.binding.cbSettingsHour23.setOnCheckedChangeListener((b, v) -> {
                noCuckooHoursBool[23] = !v;
                updateNoCuckooHours(noCuckooHoursBool);
            });
        }

        return binding.getRoot();
    }

    private void updateNoCuckooHours(boolean[] noCuckooHoursBool) {
        if (getActivity() instanceof MainActivity) {
            int nchCount = 0;
            for (int i = 0; i < 23; i++) {
                if (noCuckooHoursBool[i]) {
                    nchCount++;
                }
            }
            int[] nch = new int[nchCount];
            int nchPtr = 0;
            for (int i2 = 0; i2 < 23; i2++) {
                if (noCuckooHoursBool[i2]) {
                    nch[nchPtr] = i2;
                    nchPtr++;
                }
            }
            MainActivity mainActivity = (MainActivity)getActivity();
            mainActivity.setNoCuckooHours(nch);
        }
    }
}