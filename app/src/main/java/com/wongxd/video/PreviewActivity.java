package com.wongxd.video;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.tangyx.video.ffmpeg.FFmpegCommands;
import com.tangyx.video.ffmpeg.FFmpegRun;
import com.wongxd.video.utils.FileUtils;

import static com.tangyx.video.ffmpeg.FFmpegRun.execute;


/**
 * Created by wongxd on 2017/9/8.
 */

public class PreviewActivity extends AppCompatActivity {
    private static final String TAG = "PreviewActivity";
    private VideoView videoView;
    private int duration = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_perview);
        videoView = (VideoView) findViewById(R.id.video);
        final TextView tvNext = (TextView) findViewById(R.id.tv_next);
        ImageView ivBack = (ImageView) findViewById(R.id.iv_back);

        final String videoPath = getIntent().getStringExtra("path");

        videoView.setVideoPath(videoPath);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                duration = mp.getDuration();
                mp.start();
                mp.setLooping(true);
            }
        });

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(outVideo) || duration == 0) {
                    tvNext.setVisibility(View.GONE);
                    return;
                }
                Intent i = new Intent(PreviewActivity.this, MakeVideoActivity.class);
                i.putExtra("path", outVideo);
                i.putExtra("time", duration);
                startActivity(i);
                finish();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        reverseVideo();
    }

    private String outVideo;

    /**
     * 视频反转
     */
    private void reverseVideo() {
        final ProgressDialog pb = new ProgressDialog(PreviewActivity.this);
        final String inVideo = getIntent().getStringExtra("path");

        FileUtils mFileUtils = new FileUtils(this);
        String mTargetPath = mFileUtils.getStorageDirectory();
        outVideo = mTargetPath + "/反转-" + System.currentTimeMillis() + "-video.mp4";


        String[] commands = FFmpegCommands.reverseAudioAndVideo(inVideo, outVideo);
        execute(commands, new FFmpegRun.FFmpegRunListener() {
            @Override
            public void onStart() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setMessage("视频反转中");
                        pb.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pb.setCancelable(false);
                        pb.show();
                    }
                });
                Log.e(TAG, "reverseVideo ffmpeg start...");
            }

            @Override
            public void onEnd(int result) {
                Log.e(TAG, "reverseVideo ffmpeg end...");

                Toast.makeText(PreviewActivity.this, result == 0 ? "视频反转成功" : "视频反转失败", Toast.LENGTH_SHORT).show();
                if (pb.isShowing()) pb.dismiss();
                if (result == 0) {
                    videoView.setVideoPath(outVideo);
                    videoView.start();
                }

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
    }
}
