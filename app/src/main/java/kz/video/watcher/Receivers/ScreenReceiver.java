package kz.video.watcher.Receivers;

import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;
import kz.video.watcher.Activities.MainActivity;
import kz.video.watcher.R;

import static android.content.Context.WINDOW_SERVICE;

public class ScreenReceiver extends BroadcastReceiver {

    private static final boolean DISPLAY = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
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
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.v("$$$$$$", "In Method:  ACTION_SCREEN_ON");

            if (Build.VERSION.SDK_INT >= 29)
                showNotification(context);
            else {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(i);
            }
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {

            Log.v("$$$$$$", "In Method:  ACTION_USER_PRESENT");
//Handle resuming events
        }

    }

    private void showNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Video")
            .setContentText("Продолжить просмотр")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        manager.notify(87, builder.build());
        }



}
