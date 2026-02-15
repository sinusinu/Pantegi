package kr.pe.sinu.pantegi;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

import kr.pe.sinu.pantegi.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public static final int BACKGROUND_TYPE_CURRENT = -1;
    public static final int BACKGROUND_TYPE_BLANK = 0;
    public static final int BACKGROUND_TYPE_WALLPAPER = 1;
    public static final int BACKGROUND_TYPE_WALLPAPER_DIM = 2;
    public static final int BACKGROUND_TYPE_VIDEOS = 3;
    public static final int BACKGROUND_TYPE_VIDEOS_DIM = 4;

    ActivityMainBinding binding;
    private SharedPreferences sp;

    File[] bgVideos;
    int currentVideoId = 0;
    boolean shouldJumpToRandomPos = true;

    SoundPool soundPool;
    int cuckoo = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fl_main), (v, insets) -> {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            return ViewCompat.onApplyWindowInsets(v, insets);
        });

        this.sp = getSharedPreferences("kr.pe.sinu.pantegi.prefs", Context.MODE_PRIVATE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fcv_main_right, CalendarFragment.class, null)
                .commit();

        var filesDir = getExternalFilesDir(null);
        var videosDir = new File(filesDir, "bg_videos");
        if (!videosDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            videosDir.mkdirs();
        }
        bgVideos = videosDir.listFiles((file, s) -> s.endsWith(".mp4"));

        if (bgVideos != null && bgVideos.length > 0) {
            binding.vvMain.setOnPreparedListener((mp) -> {
                mp.setVolume(0f, 0f);
                if (shouldJumpToRandomPos) {
                    binding.vvMain.seekTo(new Random().nextInt(mp.getDuration() / 10 * 9));
                    shouldJumpToRandomPos = false;
                } else {
                    binding.vvMain.seekTo(0);
                }
                binding.vvMain.start();
            });

            binding.vvMain.setOnCompletionListener((mp) -> {
                currentVideoId++;
                if (currentVideoId == bgVideos.length) currentVideoId = 0;
                binding.vvMain.setVideoURI(Uri.fromFile(bgVideos[currentVideoId]));
                this.binding.vvMain.start();
            });
        }

        setBackground(BACKGROUND_TYPE_CURRENT);

        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
        var cuckooAudio = new File(filesDir, "chime.mp3");
        if (cuckooAudio.exists()) {
            cuckoo = this.soundPool.load(cuckooAudio.getAbsolutePath(), 1);
        }
    }

    public void setVideoBackground(int index) {
        if (bgVideos != null && bgVideos.length > 0) {
            File videoFile = bgVideos[index];
            binding.vvMain.setVideoURI(Uri.fromFile(videoFile));
            binding.vvMain.start();
        }
    }

    public void setBackground(int type) {
        if (type != BACKGROUND_TYPE_CURRENT) {
            sp.edit().putInt("background_type", type).apply();
        } else {
            type = sp.getInt("background_type", BACKGROUND_TYPE_WALLPAPER_DIM);
        }
        switch (type) {
            case BACKGROUND_TYPE_BLANK:
                binding.vvMain.setVisibility(View.GONE);
                binding.vMainDimOverlay.setVisibility(View.VISIBLE);
                binding.vMainDimOverlay.setBackgroundColor(getColor(R.color.black));
                binding.vvMain.stopPlayback();
                break;
            case BACKGROUND_TYPE_WALLPAPER:
                binding.vvMain.setVisibility(View.GONE);
                binding.vMainDimOverlay.setVisibility(View.GONE);
                binding.vvMain.stopPlayback();
                break;
            case BACKGROUND_TYPE_WALLPAPER_DIM:
                binding.vvMain.setVisibility(View.GONE);
                binding.vMainDimOverlay.setVisibility(View.VISIBLE);
                binding.vMainDimOverlay.setBackgroundColor(getColor(R.color.bg_dim_overlay));
                binding.vvMain.stopPlayback();
                break;
            case BACKGROUND_TYPE_VIDEOS:
                if (isVideoBackgroundAvailable()) {
                    if (binding.vvMain.getVisibility() != View.VISIBLE) {
                        binding.vvMain.setVisibility(View.VISIBLE);
                        setVideoBackground(new Random().nextInt(bgVideos.length));
                    }
                    binding.vMainDimOverlay.setVisibility(View.GONE);
                } else {
                    binding.vvMain.setVisibility(View.GONE);
                }
                break;
            case BACKGROUND_TYPE_VIDEOS_DIM:
                if (isVideoBackgroundAvailable()) {
                    if (binding.vvMain.getVisibility() != View.VISIBLE) {
                        binding.vvMain.setVisibility(View.VISIBLE);
                        setVideoBackground(new Random().nextInt(bgVideos.length));
                    }
                    binding.vMainDimOverlay.setVisibility(View.VISIBLE);
                    binding.vMainDimOverlay.setBackgroundColor(getColor(R.color.bg_dim_overlay));
                } else {
                    binding.vvMain.setVisibility(View.GONE);
                }
                break;
        }
    }

    public boolean isVideoBackgroundAvailable() {
        return bgVideos.length > 0;
    }

    public void setCuckooEnabled(boolean enabled) {
        this.sp.edit().putBoolean("cuckoo_enabled", enabled).apply();
    }

    public int[] getNoCuckooHours() {
        String noHourStr = this.sp.getString("cuckoo_nohour", "");
        if (noHourStr.isEmpty()) {
            return new int[0];
        }
        String[] splitNoHourStr = noHourStr.split(",");
        int[] noHour = new int[splitNoHourStr.length];
        for (int i = 0; i < splitNoHourStr.length; i++) {
            noHour[i] = Integer.parseInt(splitNoHourStr[i]);
        }
        return noHour;
    }

    public void setNoCuckooHours(int[] noCuckooHours) {
        if (noCuckooHours.length == 0) {
            this.sp.edit().putString("cuckoo_nohour", "").apply();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int noCuckooHour : noCuckooHours) {
            sb.append(noCuckooHour).append(',');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        this.sp.edit().putString("cuckoo_nohour", sb.toString()).apply();
    }

    public boolean isCuckooAvailable() {
        return cuckoo != -1;
    }

    public void cuckoo() {
        if (isCuckooAvailable() && this.sp.getBoolean("cuckoo_enabled", false)) {
            this.soundPool.play(this.cuckoo, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
}
