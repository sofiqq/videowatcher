package kz.video.watcher.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import kz.video.watcher.Device;
import kz.video.watcher.Helper;
import kz.video.watcher.Receivers.MyAdmin;
import kz.video.watcher.R;
import kz.video.watcher.Receivers.ScreenReceiver;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonPermissions;
    Button buttonVideo;
    LinearLayout llPermissions;
    //StateBroadcastingVideoView videoView;
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

    boolean permissionsActivated = false;
    SharedPreferences sPref;
    final String PERMISSIONS = "LOCK_PERMISSIONS";
    final String USER_ID = "USER_ID";
    String path = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
    MediaController ctlr;

    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private BroadcastReceiver mReceiver;
    Context context;
    private int userId = -1;
    private String imei1 = "", imei2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        sPref = getPreferences(MODE_PRIVATE);
        permissionsActivated = sPref.getBoolean(PERMISSIONS, false);
        getVideoInfo();
        userId = sPref.getInt(USER_ID, -1);
        if (userId != -1) {
            llLogin.setVisibility(View.GONE);
            llPermissions.setVisibility(View.VISIBLE);
        }
        if (permissionsActivated) {
            Log.e("ASD", "permission granted, load video");
            buttonPermissions.setVisibility(View.INVISIBLE);
            buttonVideo.setVisibility(View.VISIBLE);
//            Intent i = new Intent(MainActivity.this, VideoActivity.class);
//            startActivity(i);
            showVideo();
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

    public String getDeviceId(Context context, int sim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
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

    private void initUI() {
        setContentView(R.layout.activity_main);
        buttonPermissions = findViewById(R.id.button_permissions);
        buttonVideo = findViewById(R.id.button_video);
        llPermissions = findViewById(R.id.ll_permissions);
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
        buttonLogin.setOnClickListener(this);
        etLogin.setSingleLine();
        tvSdk.setText("SDK = " + Build.VERSION.SDK);
        tvDevice.setText("DEVICE = " + Build.MANUFACTURER);
        tvModel.setText("MODEL = " + Build.MODEL);
        tvProduct.setText("PRODUCT = " + Build.PRODUCT);
        tvSerial.setText("SERIAL = " + Device.getSerialNumber());
        tvImei.setText("IMEI = " + getDeviceId(this, 0));
        imei1 = getDeviceId(this, 0);
        imei2 = getDeviceId(this, 1);
        tvUnique.setText("UNIQUE ID = " + Device.getDeviceUniqueID(this));
        buttonVideo.setOnClickListener(this);
        buttonPermissions.setOnClickListener(this);
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                Toast.makeText(getApplicationContext(), "END API Post Request", Toast.LENGTH_SHORT).show();
//                showVideo();
//            }
//        });
//        videoView.setPlayPauseListener(new StateBroadcastingVideoView.PlayPauseListener() {
//            @Override
//            public void onPlay() {
//                //Toast.makeText(getApplicationContext(), "PLAY API post request", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onPause() {
//                Toast.makeText(getApplicationContext(), "PAUSE API post request", Toast.LENGTH_SHORT).show();
//            }
//        });
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
                showVideo();
                break;
            case R.id.button_login:
                if (!etLogin.getText().equals("") && !etPassword.getText().equals("")) {
                    HashMap<String, String> mapLogin = new HashMap<String, String>();
                    mapLogin.put("username", etLogin.getText().toString());
                    mapLogin.put("passwprd", etPassword.getText().toString());
                    appointment();

                } else {
                    Toast.makeText(this, "Заполните оба поля", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showVideo() {
        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
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
        Log.v("$$$$$$", "In Method: onDestroy()");

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

    public void appointment() {
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    String response = makePostRequest(Helper.getUrlLogin(),
                            "{\"username\":\"" + etLogin.getText().toString() + "\",\"password\":\"" + etPassword.getText().toString() + "\"}", getApplicationContext());
                    Log.e("ASD", "{\"username\":\"" + etLogin.getText().toString() + "\",\"password\":\"" + etPassword.getText().toString() + "\"}");
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
                    Toast.makeText(context, "Что-то пошло не так, проверьте введенные данные или попробуйте позже", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                Log.e("ASD", "req mess = " + s);

            }
        }.execute("");

    }

    void sendPhoneInfo() {
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
                    //String paramString2 = "{\"user_id\":5,\"vendor_name\":\"Xiaomi2\",\"model_name\":\"Redmi Note 5\",\"ss_width\": 540,\"ss_height\": 340,\"android_ver\": \"6.0\",\"serial_num\": \"asdasdasd\",\"IMEI_1\": \"12321312313\",\"IMEI_2\": \"12321312314\"}";
                    String response = makePostRequest(Helper.getUrlDeviceId(), paramString
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

                Log.e("ASD", "req mess = " + s);

            }
        }.execute("");
    }

    void getVideoInfo() {
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Log.e("ASD", Helper.getVideoInfo());
                    String paramString = "{\"user_id\":5, \"device_id\":8}";
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

}
