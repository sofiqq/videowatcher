 package kz.video.watcher.Activities;

import androidx.appcompat.app.AppCompatActivity;
import kz.video.watcher.R;
import kz.video.watcher.StateBroadcastingVideoView;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.warnyul.android.widget.FastVideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

 public class VideoActivity extends AppCompatActivity {

    String path = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
    FastVideoView videoView;
    MediaController ctlr;
    private static final boolean DISPLAY = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("ASD", "onCreate VideoActivity");
        setContentView(R.layout.activity_video);
        initUI();
        showVideo();
    }


    private void initUI() {
        videoView = findViewById(R.id.video_view);
        ctlr = new MediaController(this);
        ctlr.setVisibility(View.GONE);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Toast.makeText(getApplicationContext(), "END API Post Request", Toast.LENGTH_SHORT).show();
                showVideo();
            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.e("ASD", "" + height + " " + width);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(height,width);
        videoView.setLayoutParams(lp);
        float x = width / 2 - (height / 2);
        float y = height / 2 - (width / 2);
        videoView.setY(y);
        videoView.setX(x);
        Log.e("ASD", "" + x + " " + y);

//        videoView.setPlayPauseListener(new StateBroadcastingVideoView.PlayPauseListener() {
//            @Override
//            public void onPlay() {
//            }
//
//            @Override
//            public void onPause() {
//                Toast.makeText(getApplicationContext(), "PAUSE API post request", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void showVideo() {
        Toast.makeText(getApplicationContext(), "PLAY API post request", Toast.LENGTH_SHORT).show();
        ctlr.setMediaPlayer(videoView);
        videoView.setMediaController(ctlr);
        videoView.requestFocus();
        videoView.setVideoPath(path);
        videoView.start();
    }

     private static void downloadFile(String url, File outputFile) {
         try {
             URL u = new URL(url);
             URLConnection conn = u.openConnection();
             int contentLength = conn.getContentLength();

             DataInputStream stream = new DataInputStream(u.openStream());

             byte[] buffer = new byte[contentLength];
             stream.readFully(buffer);
             stream.close();

             DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
             fos.write(buffer);
             fos.flush();
             fos.close();
         } catch(FileNotFoundException e) {
             return; // swallow a 404
         } catch (IOException e) {
             return; // swallow a 404
         }
     }

 }
