package kz.video.watcher;

public class Helper {
    static String url = "http://95.56.236.114:53000/";  //Ссылка на апи

    public static String getUrlLogin() {
        return url + "rpc/check_login";
    }

    public static String getUrlDeviceId() {
        return url + "rpc/get_device_id";
    }

    public static String getVideoInfo() {
        return url + "rpc/get_device_video";
    }

    public static String getActionUrl() {
        return url + "rpc/add_action";
    }

    public static void setUrl(String s) {
        url = s;
    }

    public static String getMobileInfo() {
        return url + "rpc/get_device_inter";
    }
}
