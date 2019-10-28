 package kz.video.watcher.Activities;

import androidx.appcompat.app.AppCompatActivity;
import kz.video.watcher.Helper;
import kz.video.watcher.R;
import kz.video.watcher.StateBroadcastingVideoView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.warnyul.android.widget.FastVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

 public class VideoActivity extends AppCompatActivity {

    FastVideoView videoView;
    MediaController ctlr;
    private HttpProxyCacheServer proxy;
    private int stopPosition = 0;
    private int userId = 0;
    private int deviceId = 0;
    String verticalUrl = "";
    String horizontalUrl = "";
    final String HORIZONTAL_URL = "horizontal_url";
    final String VERTICAL_URL = "vertical_url";
     SharedPreferences sPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getPreferences(MODE_PRIVATE);
        setContentView(R.layout.activity_video);
        initUI();


        Intent intent = getIntent(); //Получение user_id и device_id из MainAcitivty
        userId = intent.getIntExtra("user", 0);
        deviceId = intent.getIntExtra("device", 0);

        horizontalUrl = sPref.getString(HORIZONTAL_URL, ""); //Получение horizontal_url и vertical_url из памяти телефона
        verticalUrl = sPref.getString(VERTICAL_URL,"");
        proxy = getProxy(this);
        if (horizontalUrl.equals("")) {
            getVideoInfo();
        } else {
            showVideo();
        }
        Log.e("ASD", "videoActivity user = " + userId + " device = " + deviceId);

    }

     public HttpProxyCacheServer getProxy(Context context) {
         return proxy == null ? (proxy = newProxy()) : proxy;
     }

     private HttpProxyCacheServer newProxy() {
         return new HttpProxyCacheServer.Builder(this)
                 .maxCacheSize(1024 * 1024 * 1024)
                 .build();
     }

    private void initUI() { //Объявление визуальных переменных
        videoView = findViewById(R.id.video_view);
        ctlr = new MediaController(this);
        ctlr.setVisibility(View.GONE);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                sendAction(8);
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
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                moveTaskToBack(true);
                return false;
            }
        });

    }

     @Override
     protected void onPause() {
         super.onPause();
         sendAction(7);
         stopPosition = videoView.getCurrentPosition();
         videoView.pause();
     }

     @Override
     protected void onResume() {
         super.onResume();
         sendAction(6);
         videoView.seekTo(stopPosition);
         videoView.start();
     }

     @Override
     protected void onDestroy() {
         super.onDestroy();
         sendAction(8);
     }

     private void showVideo() { //Показывает видео. Если новая ссылка, то отображает и кэширует другое видео.
        sendAction(6);
        getVideoInfo();
        ctlr.setMediaPlayer(videoView);
        videoView.setMediaController(ctlr);
        videoView.requestFocus();
        Log.e("ASD", "horizontal url = " + horizontalUrl);
        String videoPath = proxy.getProxyUrl(horizontalUrl);
        videoView.setVideoPath(videoPath);
        videoView.start();
    }

     void getVideoInfo() { //Запрос на get_device_video. Если запускается не первый раз, то проверяет ссылку на обновление
         new AsyncTask<String, String, String>() {

             @Override
             protected String doInBackground(String... params) {
                 try {
                     Log.e("ASD", Helper.getVideoInfo());
                     String paramString = "{\"user_id\":" + userId + ", \"device_id\":" + deviceId + "}";
                     //String paramString2 = "{\"user_id\":5,\"vendor_name\":\"Xiaomi2\",\"model_name\":\"Redmi Note 5\",\"ss_width\": 540,\"ss_height\": 340,\"android_ver\": \"6.0\",\"serial_num\": \"asdasdasd\",\"IMEI_1\": \"12321312313\",\"IMEI_2\": \"12321312314\"}";
                     String response = makePostRequest(Helper.getVideoInfo(), paramString
                             , getApplicationContext());
                     Log.e("ASD", paramString);
                     //Log.e("ASD", paramString2);
                     return response;
                 } catch (IOException ex) {
                     ex.printStackTrace();
                     return "";
                 }
             }

             @Override
             protected void onPostExecute(String s) {
                 super.onPostExecute(s);
                 try {
                     JSONObject ob = new JSONObject(s);
                     String hor = ob.getString("horizontal_url");
                     String ver = ob.getString("vertical_url");
                     SharedPreferences.Editor ed = sPref.edit();
                     ed.putString(HORIZONTAL_URL, hor);
                     ed.putString(VERTICAL_URL, ver);
                     ed.commit();
                     if (verticalUrl.equals("") && horizontalUrl.equals("")) {
                         verticalUrl = ver;
                         horizontalUrl = hor;
                         showVideo();
                     } else {
                         verticalUrl = ver;
                         horizontalUrl = hor;
                         sendAction(3);

                     }
                     horizontalUrl = hor;
                     verticalUrl = ver;
                     Log.e("ASD", "hor = " + hor + " ver = " + ver);
                     Log.e("ASD", "horizontal = " + horizontalUrl + " vertical = " + verticalUrl);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }

                 Log.e("ASD", "req mess VIDEO = " + s);

             }
         }.execute("");
     }

     public static String makePostRequest(String stringUrl, String payload,
                                          Context context) throws IOException {
         URL url = new URL(stringUrl);
         HttpURLConnection uc = (HttpURLConnection) url.openConnection();
         String line;
         StringBuffer jsonString = new StringBuffer();

         uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
         uc.setRequestProperty("Prefer", "params=single-object");
         uc.setRequestMethod("POST");
         uc.setDoInput(true);
         uc.setInstanceFollowRedirects(false);
         uc.connect();
         OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
         writer.write(payload);
         writer.close();
         try {
             BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
             while ((line = br.readLine()) != null) {
                 jsonString.append(line);
             }
             br.close();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         uc.disconnect();
         Log.e("ASD", "jsonToString = " + jsonString.toString());
         return jsonString.toString();
     }

     void sendAction(final int id) { //Запрос add_action. id - вид запроса:
//         1	Авторизация
//         2	Ошибка авторизации
//         3	Проверка видео
//         5	Окончание закачки видео
//         7	Прерываение воспроизведения видео
//         8	Окончание воспроизвдения видео
//         4	Начало закачки видео
//         6	Начало воспроизведения видео
         new AsyncTask<String, String, String>() {

             @Override
             protected String doInBackground(String... params) {
                 try {
                     Log.e("ASD", Helper.getActionUrl());
                     String paramString = "{ \"user_id\":" + userId +", \"device_id\":"+ deviceId + ", \"action_id\":" + id + ", \"action_param\":\"подключение однако\" }";
                     String response = makePostRequest(Helper.getActionUrl(), paramString
                             , getApplicationContext());
                     Log.e("ASD", paramString);
                     //Log.e("ASD", paramString2);
                     return response;
                 } catch (IOException ex) {
                     ex.printStackTrace();
                     return "";
                 }
             }

             @Override
             protected void onPostExecute(String s) {
                 super.onPostExecute(s);

                 Log.e("ASD", "req mess sendAction" + 7 +  " = " + s);

             }
         }.execute("");
     }

 }
