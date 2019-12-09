 package kz.video.watcher.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kz.video.MobileInfo;
import kz.video.watcher.ActivityTask;
import kz.video.watcher.Helper;
import kz.video.watcher.MobileInfoAdapter;
import kz.video.watcher.OnSwipeTouchListener;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import static java.security.AccessController.getContext;

 public class VideoActivity extends AppCompatActivity {

    FastVideoView videoView;
    LinearLayout llPhone;
    RecyclerView rv;
    TextView tvName;
    TextView tvCost;

    MediaController ctlr;
    private HttpProxyCacheServer proxy;
    private int stopPosition = 0;
    private int userId = 0;
    private int deviceId = 0;
    String verticalUrl = "";
    String horizontalUrl = "";
    public static final String HORIZONTAL_URL = "horizontal_url";
    public static final String VERTICAL_URL = "vertical_url";
     SharedPreferences sPref;
     private int mOrientation=0;
     private boolean isTouched = true;
     int activity;
     OrientationEventListener mOrEventListener;
     private int playVideo;
     private int playIntro;
     private MobileInfo mobileInfo;
     private static MobileInfoAdapter adapter;
     private String deviceName = "";

     private final Handler handler = new Handler();
     private Runnable runnable;

     private void setDefaults() {
         mOrientation = 0;
         deviceId = 0;
         userId = 0;
         verticalUrl = "";
         horizontalUrl = "";
         runnable = new Runnable() {
             @Override
             public void run() {
                 isTouched = false;
                 if (playIntro == 1)
                    llPhone.animate().translationY(1300);
             }
         };
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getPreferences(MODE_PRIVATE);
        setContentView(R.layout.activity_video);
        setDefaults();
        initUI();
        activity = ActivityTask.getActivityId();
        activity++;
        ActivityTask.setActivity(activity);
        //handler.removeCallbacks(runnable);
        //handler.removeCallbacksAndMessages(null);
        Intent intent = getIntent(); //Получение user_id и device_id из MainAcitivty
        userId = intent.getIntExtra("user", 0);
        deviceId = intent.getIntExtra("device", 0);
        playIntro = intent.getIntExtra("play_intro", -1);
        mobileInfo = (MobileInfo) intent.getSerializableExtra("mobile_info");
        deviceName = intent.getStringExtra("device_name");
        Log.e("ASD", "VV mobile info spec size = " + mobileInfo.getTech_spec().size());
        playVideo = intent.getIntExtra("play_video", -1);
        Log.e("ASD", "play_inter = " + playIntro + " playvideo = " + playVideo);
        startOrientationChangeListener();
        horizontalUrl = sPref.getString(HORIZONTAL_URL, ""); //Получение horizontal_url и vertical_url из памяти телефона
        verticalUrl = sPref.getString(VERTICAL_URL,"");
        proxy = getProxy(this);
        if (horizontalUrl.equals("")) {
            // zdes' doljno biy' tol'ko getVideoInfo();
//            isTouched = false;
//            showVideo();
            getVideoInfo();
        } else {
            isTouched = false;
            showVideo();
        }
        Log.e("ASD", "videoActivity user = " + userId + " device = " + deviceId);
        llPhone.animate().translationY(1300);
        adapter = new MobileInfoAdapter(this);
        adapter.setList(mobileInfo);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        tvCost.setText(String.valueOf(mobileInfo.getPrice()) + " тг.");
        tvName.setText(Build.MODEL);
    }

     public HttpProxyCacheServer getProxy(Context context) {
         return proxy == null ? (proxy = newProxy()) : proxy;
     }

     private HttpProxyCacheServer newProxy() {
         return new HttpProxyCacheServer.Builder(this)
                 .maxCacheSize(1024 * 1024 * 1024)
                 .maxCacheFilesCount(1000)
                 .build();
     }
     private  void startOrientationChangeListener() {
             mOrEventListener = new OrientationEventListener(getApplicationContext()) {
                 @Override
                 public void onOrientationChanged(int rotation) {
                     Log.e("ASD", "rotation = " + rotation);
                     if (!isTouched) {
                         Log.e("ASD", "PHONE WAS TOUCHED");
                         isTouched = true;
                         if (playIntro == 1)
                            llPhone.animate().translationY(0);
                         if (playIntro == 0)
                             moveTaskToBack(true);
                     }
                     restart(5);

                 }
             };
             mOrEventListener.enable();
     }

     private void fillMobileInfo() {

     }

    private void initUI() { //Объявление визуальных переменных
        videoView = findViewById(R.id.video_view);
        ctlr = new MediaController(this);
        ctlr.setVisibility(View.GONE);
        llPhone = findViewById(R.id.ll_phone);
        tvName = findViewById(R.id.tv_name);
        tvCost = findViewById(R.id.tv_cost);
        llPhone.setRotation(90.0f);
        rv = findViewById(R.id.rv_spec);
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
//        videoView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                moveTaskToBack(true);
//                Log.e("ASD", "touch");
//                return false;
//            }
//        });
//        videoView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                moveTaskToBack(true);
//            }
//        });
        videoView.setOnTouchListener(new OnSwipeTouchListener(VideoActivity.this) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {
            }
            public void onSwipeLeft() {
                Intent i = new Intent(VideoActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra("open", 1);
                startActivity(i);
                //handler.removeCallbacks(runnable);
                //handler.removeCallbacksAndMessages(null);
                finish();
                //moveTaskToBack(true);
                return;
            }
            public void onSwipeBottom() {
            }

            @Override
            public void onDoubleTapL() {
                super.onDoubleTapL();
                moveTaskToBack(true);
            }

            @Override
            public void onSingle() {
                super.onSingle();
                moveTaskToBack(true);
            }
        });


    }

     @Override
     public void onBackPressed() {
         moveTaskToBack(true);
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
         handler.removeCallbacks(runnable);
         handler.removeCallbacksAndMessages(null);
         mOrEventListener.disable();
         stop();
     }

     private void showVideo() { //Показывает видео. Если новая ссылка, то отображает и кэширует другое видео.
         if (playVideo == 1) {
             sendAction(6);
             getVideoInfo();
             ctlr.setMediaPlayer(videoView);
             videoView.setMediaController(ctlr);
             videoView.requestFocus();
             Log.e("ASD", "horizontal url = " + horizontalUrl);
             String videoPath = proxy.getProxyUrl(horizontalUrl);
             videoView.setVideoURI(Uri.parse(videoPath));
             videoView.start();
         }
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
//         9    Телефон взят в руки
         new AsyncTask<String, String, String>() {

             @Override
             protected String doInBackground(String... params) {
                 try {
                     Log.e("ASD", Helper.getActionUrl());
                     String paramString = "{ \"user_id\":" + userId +", \"device_id\":"+ deviceId + ", \"action_id\":" + id + ", \"action_param\":\"подключение однако\" }";
                     Log.e("ASD", paramString);
                     String response = makePostRequest(Helper.getActionUrl(), paramString
                             , getApplicationContext());
                     if (id == 9 || id == 10)
                         Log.e("ASDD", "id = " + activity + " " + paramString);
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

     public void start(int secs) {
         handler.postDelayed(runnable, secs * 1000);
     }

     // to stop the handler
     public void stop() {
         handler.removeCallbacks(runnable);
         handler.removeCallbacksAndMessages(null);
     }

     // to reset the handler
     public void restart(int secs) {
         handler.removeCallbacks(runnable);
         handler.removeCallbacksAndMessages(null);
         handler.postDelayed(runnable, secs * 1000);
     }

 }
