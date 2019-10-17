package kz.video.watcher.Activities;

import androidx.appcompat.app.AppCompatActivity;
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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button buttonPermissions;
    Button buttonVideo;
    LinearLayout llPermissions;
    LinearLayout llVideo;
    //StateBroadcastingVideoView videoView;
    TextView tvSdk;
    TextView tvDevice;
    TextView tvModel;
    TextView tvProduct;
    TextView tvSerial;
    TextView tvImei;
    TextView tvUnique;

    boolean permissionsActivated = false;
    SharedPreferences sPref;
    final String PERMISSIONS = "LOCK_PERMISSIONS";
    String path = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
    MediaController ctlr;

    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private BroadcastReceiver mReceiver;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        sPref = getPreferences(MODE_PRIVATE);
        permissionsActivated = sPref.getBoolean(PERMISSIONS, false);
        if (permissionsActivated) {
            Log.e("ASD", "permission granet, load video");
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

    private void initUI() {
        setContentView(R.layout.activity_main);
        buttonPermissions = findViewById(R.id.button_permissions);
        buttonVideo = findViewById(R.id.button_video);
        llPermissions = findViewById(R.id.ll_permissions);
        llVideo = findViewById(R.id.ll_video);
        //videoView = findViewById(R.id.video_view);
        tvSdk = findViewById(R.id.tv_sdk);
        tvDevice = findViewById(R.id.tv_device);
        tvImei = findViewById(R.id.tv_imei);
        tvModel = findViewById(R.id.tv_model);
        tvProduct = findViewById(R.id.tv_product);
        tvSerial = findViewById(R.id.tv_serial);
        tvUnique = findViewById(R.id.tv_unique);
        tvSdk.setText("SDK = " + Build.VERSION.SDK);
        tvDevice.setText("DEVICE = " + Build.DEVICE);
        tvModel.setText("MODEL = " + Build.MODEL);
        tvProduct.setText("PRODUCT = " + Build.PRODUCT);
        tvSerial.setText("SERIAL = " + getSerialNumber());
        tvImei.setText("IMEI = " + getIMEI(this));
        tvUnique.setText("UNIQUE ID = " + getDeviceUniqueID(this));
        buttonVideo.setOnClickListener(this);
        buttonPermissions.setOnClickListener(this);
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
//                Intent i = new Intent(MainActivity.this, VideoActivity.class);
//                startActivity(i);
                showVideo();
                break;
        }
    }

    private void showVideo() {
        //Toast.makeText(getApplicationContext(), "PLAY API post request", Toast.LENGTH_SHORT).show();
//        llPermissions.setVisibility(View.GONE);
//        llVideo.setVisibility(View.VISIBLE);
//        videoView = findViewById(R.id.video_view);
//        Uri uri = Uri.parse(path);
//        videoView.setVideoURI(uri);
//        videoView.start();
//        ctlr = new MediaController(this);
//        ctlr.setMediaPlayer(videoView);
//        videoView.setMediaController(ctlr);
//        videoView.requestFocus();
//        videoView = findViewById(R.id.video_view);
//        videoView.setVideoPath(path);
//        videoView.start();
//        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Intent intent = new Intent (MainActivity.this, VideoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
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
    public void onDestroy()
    {
        super.onDestroy();
        Log.v("$$$$$$", "In Method: onDestroy()");

        if (mReceiver != null)
        {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }

    @Override
    public void  onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("$````$", "In Method: onSaveInstanceState()");
        //if necessary,set a flag to check whether we have to restore or not
        //handle necessary savings…
    }

    @Override
    public void onRestoreInstanceState(Bundle inState)
    {
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

    public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = null;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        return serialNumber;
    }

    public String getIMEI(Activity activity) {
        TelephonyManager telephonyManager = (TelephonyManager) activity
                .getSystemService(Context.TELEPHONY_SERVICE);
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
        return telephonyManager.getDeviceId();
    }

    public String getDeviceUniqueID(Activity activity){
        String device_unique_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return device_unique_id;
    }

}
