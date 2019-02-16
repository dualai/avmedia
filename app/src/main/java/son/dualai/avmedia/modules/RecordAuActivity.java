package son.dualai.avmedia.modules;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import son.dualai.avmedia.R;
import son.dualai.avmedia.util.FileUtil;
import son.dualai.avmedia.util.Log;
import son.dualai.avmedia.util.PcmToWavUtil;

import static son.dualai.avmedia.core.GlobalConfig.AUDIO_FORMAT;

/**
 * Created on 2019/2/14.
 */


//    ByteArrayStream相关
//    https://blog.csdn.net/yhl_jxy/article/details/79287693
public class RecordAuActivity extends PmsActivity {
    private boolean isRecording;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private byte[] audioData;
    private FileInputStream fileInputStream;
    File file;

    private Button recordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordau);
        file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "avmedia_record.pcm");
        Log.d(file.getAbsolutePath());

        recordBtn = findViewById(R.id.recordBtn);

//        String str = "helloworld,goodmorning";
//        ByteArrayOutputStream bos = null;
//        ByteArrayInputStream bis = null;
//        bis = new ByteArrayInputStream(str.getBytes());
//        bos = new ByteArrayOutputStream();
//        int temp = -1;
//        while( (temp = bis.read()) != -1 ) //依次读取内存
//        {
//            //接收字符
//            char c = (char) temp;
//            bos.write(Character.toUpperCase(c)); //输出
//        }
//
//        String newStr = bos.toString();
//        Log.d(newStr);

//        try {
//            // 把数据写入字节数组
//            String str2 = "abcdefghijklmnopqrstuvwxyz";
//            byte[] bytes = str2.getBytes();
//            // 把字节数组放入到ByteArrayInputStream，转化为输入流
//            InputStream input = new ByteArrayInputStream(bytes);
//            // 读取第一个字节
//            int data = input.read();
//            StringBuilder sb = new StringBuilder();
//            while (data != -1) {
//                // 将读取的字节拼成字符串
//                sb.append((char) data);
//                // 读取下一个字节
//                data = input.read();
//            }
//            Log.d("内容:" + sb.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void deleteRecord(View v) {
        if (file != null && file.exists()) {
            Log.d("存在文件，删除...");
            file.delete();
        }
    }

    public void record(View v) {
        isRecording = true;
        final int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AUDIO_FORMAT, minBufferSize);

        final byte data[] = new byte[minBufferSize];
        if (!file.mkdirs()) {
            Log.e("Directory not created");
        }
        if (file.exists()) {
            file.delete();
        }

        audioRecord.startRecording();
        isRecording = true;
        v.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (outStream != null) {
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, minBufferSize);
                        Log.d("record,read,"+read);
                        if (read >= 0) {
                            try {
                                outStream.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                try {
                    if(outStream != null) {
                        Log.d("close file output stream");
                        outStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public void stopRecord(View v) {
        isRecording = false;
        recordBtn.setEnabled(true);
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    /**
     * stream模式
     *
     * @param v
     */
    public void play1(View v) {

        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize = AudioTrack.getMinBufferSize(44100, channelConfig, AUDIO_FORMAT);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioTrack = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    new AudioFormat.Builder().setSampleRate(44100)
                            .setEncoding(AUDIO_FORMAT)
                            .setChannelMask(channelConfig)
                            .build(),
                    minBufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
            );
        } else {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                    channelConfig, AUDIO_FORMAT,
                    audioData.length, AudioTrack.MODE_STREAM);
        }

        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                Log.d("1 onMarkerReached");
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                Log.d("1 onPeriodicNotification");
            }
        });

        audioTrack.play();
        isStartPlay = true;

        if (!file.exists() || !file.canRead()) {
            return;
        }

        try {
            fileInputStream = new FileInputStream(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] tempBuffer = new byte[minBufferSize];
                    try {
                        while(isStartPlay && fileInputStream.available() > 0){
                            Log.d("fileInputStream.available():"+fileInputStream.available());
                            int read = fileInputStream.read(tempBuffer);
                            Log.d("read,"+read);
                            if(isStartPlay && read > 0) {
                                audioTrack.write(tempBuffer, 0, read);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        fileInputStream.close();
                        Log.d("play1,close fileInput...");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * static模式
     *
     * @param v
     */
    public void play2(View v) {
// static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                InputStream in = getResources().openRawResource(R.raw.ding);
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int b = -1;
                    Log.d("start get data...");
                    while (in.available() > 0){
                        if((b = in.read()) != -1) {
                            out.write(b);
                        }
                    }
                    Log.d("get the data");
                    audioData = out.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }


            @Override
            protected void onPostExecute(Void v) {
                Log.i("Creating track...audioData.length = " + audioData.length);

                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioTrack = new AudioTrack(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build(),
                            new AudioFormat.Builder().setSampleRate(22050)
                                    .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build(),
                            audioData.length,
                            AudioTrack.MODE_STATIC,
                            AudioManager.AUDIO_SESSION_ID_GENERATE);
                } else {
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 22050,
                            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT,
                            audioData.length, AudioTrack.MODE_STATIC);
                }

                Log.d("Writing audio data...");
                audioTrack.write(audioData, 0, audioData.length);
                Log.d("Starting playback");
                audioTrack.play();
                Log.d("Playing");
            }

        }.execute();

    }


    /**
     * 停止播放
     */
    private boolean isStartPlay;
    public void stopPlay(View v) {
        if (audioTrack != null) {
            Log.d("stop play...");
            isStartPlay = false;
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    public void convertToWav(View v) {
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_MONO, AUDIO_FORMAT);

        File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "avmedia_record.wav");
        if (wavFile.exists()) {
            wavFile.delete();
        }
        pcmToWavUtil.pcmToWav(file.getAbsolutePath(), wavFile.getAbsolutePath());

    }

}
