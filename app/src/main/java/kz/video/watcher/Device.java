package kz.video.watcher;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;

public class Device {
    /**
     * @return The device's serial number, visible to the user in {@code Settings > About phone/tablet/device > Status
     * > Serial number}, or {@code null} if the serial number couldn't be found
     */

    public static String getDeviceUniqueID(Activity activity){
        String device_unique_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return device_unique_id;
    }

}
