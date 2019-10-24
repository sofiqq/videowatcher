package kz.video.watcher;

public class Helper {
    static String url = "http://95.56.236.114:53000/";

    public static String getUrlLogin() {
        return url + "rpc/check_login";
    }

    public static String getUrlDeviceId() {
        return url + "rpc/get_device_id";
    }

    public static String getVideoInfo() {
        return url + "rpc/get_device_video";
    }
}
