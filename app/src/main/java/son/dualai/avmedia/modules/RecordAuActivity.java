package son.dualai.avmedia.modules;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import son.dualai.avmedia.R;

import static son.dualai.avmedia.core.GlobalConfig.AUDIO_FORMAT;
import static son.dualai.avmedia.core.GlobalConfig.CHANNEL_CONFIG;
import static son.dualai.avmedia.core.GlobalConfig.SAMPLE_RATE_INHZ;

/**
 * Created on 2019/2/14.
 */
public class RecordAuActivity extends Activity {


    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private static final String TAG = "jqd";

    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    /**
     * 被用户拒绝的权限列表
     */
    private List<String> mPermissionList = new ArrayList<>();
    private boolean isRecording;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private byte[] audioData;
    private FileInputStream fileInputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordau);

        checkPermissions();
    }

    public void record(View v){
        isRecording = true;
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ,CHANNEL_CONFIG,AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        final byte data[] = new byte[minBufferSize];
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        if (file.exists()) {
            file.delete();
        }

        audioRecord.startRecording();
        isRecording = true;



    }

    public void stopRecord(View v){
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            //recordingThread = null;
        }
    }

    public void play1(View v){

    }

    public void play2(View v){

    }

    public void convertToWav(View v){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, permissions[i] + " 权限被用户禁止！");
                }
            }
        }
    }


    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }
    }

}
