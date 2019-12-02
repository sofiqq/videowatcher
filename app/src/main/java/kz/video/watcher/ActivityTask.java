package kz.video.watcher;

public class ActivityTask {
    static int activity = 0;

    public static void setActivity(int id) {
        activity = id;
    }

    public static int getActivityId() {
        return activity;
    }
}
