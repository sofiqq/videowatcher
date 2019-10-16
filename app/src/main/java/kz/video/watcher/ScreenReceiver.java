package kz.video.watcher;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Log.v("$$$$$$", "In Method:  ACTION_SCREEN_OFF");
            KeyguardManager km = (KeyguardManager) context
                    .getSystemService(Context.KEYGUARD_SERVICE);
            final KeyguardManager.KeyguardLock kl = km
                    .newKeyguardLock("MyKeyguardLock");
            kl.disableKeyguard();

            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "AppName:MyWakeLock");
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            Log.v("$$$$$$", "In Method:  ACTION_SCREEN_ON");
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            Log.v("$$$$$$", "In Method:  ACTION_USER_PRESENT");
//Handle resuming events
        }

    }
}
