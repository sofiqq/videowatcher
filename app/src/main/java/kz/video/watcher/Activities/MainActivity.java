package kz.video.watcher.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import kz.video.watcher.MobileInfo;
import kz.video.watcher.Device;
import kz.video.watcher.Helper;
import kz.video.watcher.Receivers.MyAdmin;
import kz.video.watcher.R;
import kz.video.watcher.Receivers.ScreenReceiver;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonPermissions;  //Все вьюшки
    Button buttonVideo;
    LinearLayout llPermissions;
    TextView tvSdk;
    TextView tvDevice;
    TextView tvModel;
    TextView tvProduct;
    TextView tvSerial;
    TextView tvImei;
    TextView tvUnique;
    LinearLayout llLogin;
    EditText etLogin;
    EditText etPassword;
    Button buttonLogin;
    Button buttonLogout;
    EditText etUrl;
    Button buttonConfirmUrl;
    ProgressBar pb;

    boolean permissionsActivated = false; //проверка прав на админа
    SharedPreferences sPref; //для хранения данных в памяти телефона

    final String PERMISSIONS = "LOCK_PERMISSIONS"; //ключи для сохранения переменных в памяти
    final String USER_ID = "USER_ID";
    final String DEVICE_ID = "DEVICE_ID";
    final String PLAYVIDEO = "PLAYVIDEO";
    final String PLAYINTRO = "PLAYINTRO";

    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private BroadcastReceiver mReceiver;
    private int userId = 0;
    private int deviceId = 0;
    private int playVideo;
    private int playIntro;
    private String imei1 = "", imei2 = "";
    private String url;
    private boolean isTouched = true;
    OrientationEventListener mOrEventListener;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private MobileInfo mobileInfo;

    private void setDefaults() {
        deviceId = 0;
        userId = 0;
        runnable = new Runnable() {
            @Override
            public void run() {
                isTouched = false;
                //handler.removeCallbacks(this);
                sendAction(10);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaults();
        initUI();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        sPref = getPreferences(MODE_PRIVATE);
        permissionsActivated = sPref.getBoolean(PERMISSIONS, false);
        userId = sPref.getInt(USER_ID, 0); //Достает из памяти user_id и device_id
        deviceId = sPref.getInt(DEVICE_ID, 0);
        playIntro = sPref.getInt(PLAYINTRO, -1);
        playVideo = sPref.getInt(PLAYVIDEO, -1);
        Gson gson = new Gson();
        String json = sPref.getString("mobile_info", "");
        Log.e("ASD", "MA mobile info str = " + json);
        mobileInfo = gson.fromJson(json, MobileInfo.class);
        url = sPref.getString("url", getString(R.string.default_url));
        Log.e("ASD", "url = " + url + " default url = " + getString(R.string.default_url));
        Helper.setUrl(url);
        etUrl.setHint(url);

        Log.e("ASD", "user id = " + userId + " device id = " + deviceId);
        if (userId != 0) { //Если user_id имеется, пропускает страничку авторизации
            llLogin.setVisibility(View.GONE);
            llPermissions.setVisibility(View.VISIBLE);
        }
        Intent i = getIntent();
        int open = i.getIntExtra("open", 0);
        if (permissionsActivated) {
            //buttonPermissions.setVisibility(View.GONE);
            buttonVideo.setVisibility(View.VISIBLE);
        }
        if (permissionsActivated && open == 0 && userId != 0 && deviceId != 0) { //Если права выданы, то откроется страница видео
            Log.e("ASD", "permission granted, load video");
            //buttonPermissions.setVisibility(View.GONE);
            buttonVideo.setVisibility(View.VISIBLE);
            pb.setVisibility(View.VISIBLE);
            buttonVideo.setEnabled(false);
            refreshDeviceId();
        }
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        Log.e("ASD", "launch mainactivity");
    }

    private  void startOrientationChangeListener() {
            mOrEventListener = new OrientationEventListener(getApplicationContext()) {
                @Override
                public void onOrientationChanged(int rotation) {
                    Log.e("ASD", "rotation = " + rotation);
                    if (!isTouched) {
                        Log.e("ASD", "PHONE WAS TOUCHED");
                        //Toast.makeText(getApplicationContext(), "PHONE IS TOUCHED, SEND ACTION 9", Toast.LENGTH_SHORT).show();
                        isTouched = true;
                        sendAction(9);
                    }
                    restart(5);
                }
            };
            mOrEventListener.enable();
    }

    public String getDeviceId(Context context, int sim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            imei = telephonyManager.getDeviceId(sim);
        }
        return imei;
    }

    private void initUI() { //Объявление визуальных переменных
        Log.e("ASD", "QWE");
        setContentView(R.layout.activity_main);
        buttonPermissions = findViewById(R.id.button_permissions);
        buttonVideo = findViewById(R.id.button_video);
        llPermissions = findViewById(R.id.ll_permissions);
        pb = findViewById(R.id.pb);
        tvSdk = findViewById(R.id.tv_sdk);
        tvDevice = findViewById(R.id.tv_device);
        tvImei = findViewById(R.id.tv_imei);
        tvModel = findViewById(R.id.tv_model);
        tvProduct = findViewById(R.id.tv_product);
        tvSerial = findViewById(R.id.tv_serial);
        tvUnique = findViewById(R.id.tv_unique);
        llLogin = findViewById(R.id.ll_login);
        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        buttonLogin = findViewById(R.id.button_login);
        buttonLogout = findViewById(R.id.button_logout);
        etUrl = findViewById(R.id.et_url);
        buttonConfirmUrl = findViewById(R.id.button_confirm_url);
        buttonLogout.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        buttonConfirmUrl.setOnClickListener(this);
        etLogin.setSingleLine();
        tvSdk.setText("SDK = " + Build.VERSION.SDK);
        tvDevice.setText("DEVICE = " + Build.MANUFACTURER);
        tvModel.setText("MODEL = " + Build.MODEL);
        tvProduct.setText("PRODUCT = " + Build.PRODUCT);
        tvSerial.setText("SERIAL = " + Device.getSerialNumber());
        tvImei.setText(" ");
        imei1 = getDeviceId(this, 0);
        imei2 = getDeviceId(this, 1);
        tvUnique.setText("UNIQUE ID = " + Device.getDeviceUniqueID(this));
        buttonVideo.setOnClickListener(this);
        buttonPermissions.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_permissions:
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Права для блокировки/разблокировки телефона");
                startActivityForResult(intent, RESULT_ENABLE);
                break;
            case R.id.button_video:
                pb.setVisibility(View.VISIBLE);
                buttonVideo.setEnabled(false);
                refreshDeviceId();
                break;
            case R.id.button_login:
                if (!etLogin.getText().equals("") && !etPassword.getText().equals("")) {
                    appointment();

                } else {
                    Toast.makeText(this, "Заполните оба поля", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_logout:
                userId = 0;
                deviceId = 0;
                sPref = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(USER_ID, userId);
                ed.putInt(DEVICE_ID, deviceId);
                ed.putString(VideoActivity.HORIZONTAL_URL, "");
                ed.putString(VideoActivity.VERTICAL_URL, "");
                llPermissions.setVisibility(View.GONE);
                llLogin.setVisibility(View.VISIBLE);
                ed.commit();
                break;
            case R.id.button_confirm_url:
                if (!etUrl.getText().toString().equals("")) {
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor edd = sPref.edit();
                    String ss = etUrl.getText().toString();
                    if (ss.charAt(ss.length() - 1) != '/')
                        ss += '/';
                    if (!ss.substring(0,4).equals("http"))
                        ss = "http://" + ss;
                    edd.putString("url", ss);
                    edd.commit();
                    Helper.setUrl(ss);
                    etUrl.setHint(ss);
                    etUrl.setText("");
                }
                break;
        }
    }

    private void showVideo() { //Запускает VideoActivity где играет видео
        startOrientationChangeListener();
        isTouched = false;
        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
        intent.putExtra("device", deviceId);
        intent.putExtra("user", userId);
        intent.putExtra("play_video", playVideo);
        intent.putExtra("play_intro", playIntro);
        intent.putExtra("mobile_info", mobileInfo);
        intent.putExtra("device_name", Build.MODEL);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Права уже активированы", Toast.LENGTH_SHORT).show();
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putBoolean(PERMISSIONS, true);
                    ed.commit();
                    buttonPermissions.setVisibility(View.INVISIBLE);
                    buttonVideo.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(MainActivity.this, "Ошибка активации прав", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("ASD", "in method: destroy main");
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        if (mOrEventListener != null)
            mOrEventListener.disable();
        stop();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("$````$", "In Method: onSaveInstanceState()");
        //if necessary,set a flag to check whether we have to restore or not
        //handle necessary savings…
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        Log.v("$````$", "In Method: onRestoreInstanceState()");
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //if any saved state, restore from it…
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("$$$$$$", "In Method: onResume()");
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //showVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("$$$$$$", "In Method: onPause()");
    }

    public void appointment() { //Запрос на авторизацию
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Log.e("ASD", Helper.getUrlLogin());
                    Log.e("ASD", "{\"username\":\"" + etLogin.getText().toString() + "\",\"password\":\"" + etPassword.getText().toString() + "\"}");
                    String response = makePostRequest(Helper.getUrlLogin(),
                            "{\"username\":\"" + etLogin.getText().toString() + "\",\"password\":\"" + etPassword.getText().toString() + "\"}", getApplicationContext());
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
                    JSONObject object = new JSONObject(s);
                    int user = object.getInt("user_id");
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putInt(USER_ID, user);
                    userId = user;
                    ed.commit();
                    llLogin.setVisibility(View.GONE);
                    llPermissions.setVisibility(View.VISIBLE);
                    sendPhoneInfo();
                    Log.e("ASD", "userId = " + userId);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Что-то пошло не так, проверьте введенные данные или попробуйте позже", Toast.LENGTH_SHORT).show();
                    sendAction(2);
                    e.printStackTrace();
                }
                Log.e("ASD", "req mess = " + s);

            }
        }.execute("");

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
                    String paramString = "{ \"user_id\":" + userId + ", \"device_id\":" + deviceId + ", \"action_id\":" + id + ", \"action_param\":\"подключение однако\" }";
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

                Log.e("ASD", "req mess sendAction" + 7 + " = " + s);

            }
        }.execute("");
    }

    void sendPhoneInfo() { //Запрос на получение device_id
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Log.e("ASD", Helper.getUrlDeviceId());
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;
                    String paramString = "{\"user_id\":" + userId + ",\"vendor_name\":\"" + Build.MANUFACTURER + "\",\"model_name\":\"" + Build.MODEL + "\",\"ss_width\":" + width + ",\"ss_height\":" + height + ",\"android_ver\": \"" + Build.VERSION.SDK + "\",\"serial_num\": \"" + Device.getSerialNumber() + "\",\"IMEI_1\": \"" + getDeviceId(getApplicationContext(), 0) + "\",\"IMEI_2\": \"" + getDeviceId(getApplicationContext(), 1) + "\"}";
                    Log.e("ASD", paramString);
                    //String paramString2 = "{\"user_id\":5,\"vendor_name\":\"Xiaomi2\",\"model_name\":\"Redmi Note 5\",\"ss_width\": 540,\"ss_height\": 340,\"android_ver\": \"6.0\",\"serial_num\": \"asdasdasd\",\"IMEI_1\": \"12321312313\",\"IMEI_2\": \"12321312314\"}";
                    String response = makePostRequest(Helper.getUrlDeviceId(), paramString
                            , getApplicationContext());

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
                    Log.e("ASD", "QWE");
                    JSONObject object = new JSONObject(s);
                    deviceId = object.getInt("device_id");
                    playIntro = object.getInt("play_inter");
                    playVideo = object.getInt("play_video");
                    Log.e("ASD", "play video = " + playVideo + ", " + "play intro = " + playIntro);
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putInt(DEVICE_ID, deviceId);
                    ed.putInt(PLAYINTRO, playIntro);
                    ed.putInt(PLAYVIDEO, playVideo);
                    ed.commit();
                    Log.e("ASD", "device id = " + deviceId);
                    sendAction(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("ASD", "req mess = " + s);

            }
        }.execute("");
    }

    void refreshDeviceId() { //Запрос на получение device_id
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Log.e("ASD", Helper.getUrlDeviceId());
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;
                    String paramString = "{\"user_id\":" + userId + ",\"vendor_name\":\"" + Build.MANUFACTURER + "\",\"model_name\":\"" + Build.MODEL + "\",\"ss_width\":" + width + ",\"ss_height\":" + height + ",\"android_ver\": \"" + Build.VERSION.SDK + "\",\"serial_num\": \"" + Device.getSerialNumber() + "\",\"IMEI_1\": \"" + getDeviceId(getApplicationContext(), 0) + "\",\"IMEI_2\": \"" + getDeviceId(getApplicationContext(), 1) + "\"}";
                    Log.e("ASD", paramString);
                    //String paramString2 = "{\"user_id\":5,\"vendor_name\":\"Xiaomi2\",\"model_name\":\"Redmi Note 5\",\"ss_width\": 540,\"ss_height\": 340,\"android_ver\": \"6.0\",\"serial_num\": \"asdasdasd\",\"IMEI_1\": \"12321312313\",\"IMEI_2\": \"12321312314\"}";
                    String response = makePostRequest(Helper.getUrlDeviceId(), paramString
                            , getApplicationContext());

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
                    Log.e("ASD", "QWE");
                    JSONObject object = new JSONObject(s);
                    deviceId = object.getInt("device_id");
                    playIntro = object.getInt("play_inter");
                    playVideo = object.getInt("play_video");
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putInt(DEVICE_ID, deviceId);
                    ed.putInt(PLAYINTRO, playIntro);
                    ed.putInt(PLAYVIDEO, playVideo);
                    ed.commit();
                    getMobileInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("ASD", "req mess = " + s);

            }
        }.execute("");
    }

    void getMobileInfo() { //Запрос на получение device_id
        Log.e("ASD", "GETMobileInfo");
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Log.e("ASD", Helper.getMobileInfo());
                    String paramString = "{\"user_id\":" + userId + ",\"device_id\":\"" + deviceId + "\"}";
                    Log.e("ASD", paramString);
                    //String paramString2 = "{\"user_id\":5,\"vendor_name\":\"Xiaomi2\",\"model_name\":\"Redmi Note 5\",\"ss_width\": 540,\"ss_height\": 340,\"android_ver\": \"6.0\",\"serial_num\": \"asdasdasd\",\"IMEI_1\": \"12321312313\",\"IMEI_2\": \"12321312314\"}";
                    String response = makePostRequest(Helper.getMobileInfo(), paramString
                            , getApplicationContext());

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
                JSONObject object = null;
                Log.e("ASD", "QWE s = " + s);
                try {
                    Gson gson = new Gson();
                    object = new JSONObject(s);
                    mobileInfo = gson.fromJson(object.toString(), MobileInfo.class);
                    JSONArray tec = object.getJSONArray("tech_spеc");
                    ArrayList<String> tecs = new ArrayList<>();
                    Log.e("ASD", "tec size = " + tec.length());
                    for (int i = 0; i < tec.length(); i++) {
                        tecs.add(tec.getString(i));
                    }
                    mobileInfo.setTech_spec(tecs);
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    String json = gson.toJson(mobileInfo);
                    ed.putString("mobile_info", json);
                    ed.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("ASD", "getmob name = " + mobileInfo.getPrice() + " tech spec size = " + mobileInfo.getTech_spec().size());
                Log.e("ASD", "req mess = " + s);
                buttonVideo.setEnabled(true);
                pb.setVisibility(View.GONE);
                showVideo();
            }
        }.execute("");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                tvSerial.setText(Build.getSerial());
            } else {
                //not granted
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        return jsonString.toString();
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
