package kz.video.watcher;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button buttonPermissions;
    Button buttonVideo;

    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);

        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

// Customized BroadcastReceiver class
//Will be defined soon..

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
        buttonVideo.setOnClickListener(this);
        buttonPermissions.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_permissions:
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
                startActivityForResult(intent, RESULT_ENABLE);
                break;
            case R.id.button_video:
                Intent i = new Intent(MainActivity.this, VideoActivity.class);
                startActivity(i);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Права уже активированы", Toast.LENGTH_SHORT).show();

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
        //if any saved state, restore from it…
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
