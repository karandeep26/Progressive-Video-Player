package karan.socialcopstask;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.TrafficStats;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, VideoListener {
   final int REQUEST_CODE=123;
    VideoView videoView;
    HttpProxyCacheServer httpProxyCacheServer;
    String url;
    MediaController mediaController;
    ImageButton button;
     EditText enterUrl;
    RecycleViewAdapter recycleViewAdapter;
    RecyclerView recyclerView;
    ArrayList<Model> model;
    File root;
    ProgressBar progressBar, loading;
    int previousValue, newValue;
    boolean playingFromNetwork = true;
    private Handler mHandler = new Handler();
    private long mStartRX = 0;
    private long mStartTX = 0;
    TextView RX;
    TextView TX;
    View noContent;
    Resources res;
    LinearLayoutManager linearLayoutManager;
    Drawable drawable;
    DividerItemDecoration divider;
    File path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        initVariables();
        setViews();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
        else
        {
           hasPermission();
        }

    }


    @Override
    public void onClick(View v) {

        url = enterUrl.getText().toString();
        url = url.trim();
        if (url.isEmpty())
            url = "https://socialcops.com/video/main.mp4";
        if (httpProxyCacheServer.isCached(url)) {
            playingFromNetwork = false;
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Playing Offline", Toast.LENGTH_SHORT).show();
        } else {
            loading.setVisibility(View.VISIBLE);
            playingFromNetwork = true;
        }
        String proxyUrl = httpProxyCacheServer.getProxyUrl(url);
        registerCacheListener();


        videoView.setVideoPath(proxyUrl);
        videoView.start();

//        if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
//            Toast.makeText(this, "Device doesn't support monitoring", Toast.LENGTH_SHORT).show();
//        } else {
//            RX.setText("0");
//            TX.setText("0");
//
//            mHandler.postDelayed(mRunnable, 1000);
//        }


    }

    @Override
    public void playVideo(String fileName) {
        playingFromNetwork = false;
        progressBar.setVisibility(View.GONE);
        videoView.setVideoPath(fileName);
        videoView.start();


    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {

            long rxBytes = TrafficStats.getUidRxBytes(getApplicationInfo().uid) - mStartRX;
            RX.setText(Long.toString(rxBytes));
            long txBytes = TrafficStats.getUidTxBytes(getApplicationInfo().uid) - mStartTX;
            TX.setText(Long.toString(txBytes));
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private void registerCacheListener() {

        httpProxyCacheServer.registerCacheListener(new CacheListener() {
            @Override
            public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
                Log.d("Percentage", percentsAvailable + "");
                newValue = percentsAvailable;
                if (previousValue != newValue) {
                    previousValue = newValue;
                    progressBar.setProgress(percentsAvailable);
                    if (percentsAvailable == 100) {
                        Log.d("location",cacheFile.getAbsolutePath());
                        Toast.makeText(MainActivity.this, "Video Stored Offline", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                    }
                }
                if (!cacheFile.getAbsolutePath().contains("download")) {

                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(cacheFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                    model.add(new Model(bitmap, cacheFile.getAbsolutePath()));
                    recyclerView.setVisibility(View.VISIBLE);
                    recycleViewAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    noContent.setVisibility(View.GONE);
                    httpProxyCacheServer.unregisterCacheListener(this);

                }
            }
        }, url);
    }

    private void setVideoViewListeners() {
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                Toast.makeText(MainActivity.this, "Try some other Link", Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (playingFromNetwork && !httpProxyCacheServer.isCached(url))
                    progressBar.setVisibility(View.VISIBLE);
                else
                    progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                        mediaController.setAnchorView(videoView);
                    }
                });
            }
        });
    }

    private void setRecyclerViewOnLoad() {
           if (path.exists() && path.listFiles().length > 0) {
            File[] list = path.listFiles();
            for (File file : list) {
                if (file.getAbsolutePath().endsWith(".mp4")) {
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                    model.add(new Model(bitmap, file.getAbsolutePath()));
                    recycleViewAdapter.notifyDataSetChanged();

                }
            }
        }
        if (!model.isEmpty()) {
            findViewById(R.id.no_content).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

    }

    private void bindViews()
    {
        RX = (TextView) findViewById(R.id.RX);
        TX = (TextView) findViewById(R.id.TX);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        button = (ImageButton) findViewById(R.id.download);
        enterUrl = (EditText) findViewById(R.id.enter_url);
        recyclerView = (RecyclerView) findViewById(R.id.videos);
        loading = (ProgressBar) findViewById(R.id.loading);
        loading.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        noContent = findViewById(R.id.no_content);
        videoView = (VideoView) findViewById(R.id.videoView);

    }

    private void initVariables() {
        mStartRX = TrafficStats.getUidRxBytes(getApplicationInfo().uid);
        mStartTX = TrafficStats.getUidTxBytes(getApplicationInfo().uid);
        model = new ArrayList<>();
        res = getResources();
        drawable = res.getDrawable(R.drawable.circular);
        mediaController = new MediaController(this);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleViewAdapter = new RecycleViewAdapter(model, this);
        divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

    }

    private void setViews() {
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recycleViewAdapter);
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(100);
        progressBar.setMax(100);
        progressBar.setProgressDrawable(drawable);
        progressBar.setVisibility(View.GONE);
//        enterUrl.setHorizontalScrollBarEnabled(true);
//        enterUrl.setHorizontallyScrolling(true);
//        enterUrl.setMovementMethod(new ScrollingMovementMethod());
        videoView.setMediaController(mediaController);
        loading.setVisibility(View.GONE);
        recyclerView.addItemDecoration(divider);
        button.setOnClickListener(this);

    }
    void requestPermission()
    {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode!= Activity.RESULT_CANCELED&&grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           hasPermission();
        } else {
            Toast.makeText(this, "Restart app to grant permissions", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void hasPermission()
    {
        path=new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"Social Cops");
        httpProxyCacheServer = new HttpProxyCacheServer.Builder(this).cacheDirectory(path).build();
        setVideoViewListeners();
        setRecyclerViewOnLoad();
    }



}
