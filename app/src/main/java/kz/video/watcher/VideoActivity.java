 package kz.video.watcher;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

 public class VideoActivity extends AppCompatActivity {

    String path = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4";
    VideoView videoView;
    MediaController ctlr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoView = findViewById(R.id.video_view);
        Uri uri = Uri.parse(path);
        videoView.setVideoURI(uri);
        videoView.start();
        ctlr = new MediaController(this);
        ctlr.setMediaPlayer(videoView);
        videoView.setMediaController(ctlr);
        videoView.requestFocus();

        videoView = findViewById(R.id.video_view);
        videoView.setVideoPath(path);
        videoView.start();
    }
}
