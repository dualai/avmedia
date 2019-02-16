package son.dualai.avmedia.modules.onlyh264;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.socks.library.KLog;

import java.io.IOException;

import son.dualai.avmedia.R;
import son.dualai.avmedia.modules.PmsActivity;
import son.dualai.avmedia.modules.muxer.MuxerActivity;
import son.dualai.avmedia.util.Log;

public class To264Activity extends PmsActivity implements SurfaceHolder.Callback,Camera.PreviewCallback{

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button muxerButton;

    private Camera camera;

    int width = 1280;
    int height = 720;
    int framerate = 30;
    H264Encoder encoder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to264);

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        muxerButton = (Button) findViewById(R.id.go_muxer);
        muxerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(To264Activity.this, MuxerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (supportH264Codec()) {
            Log.d("support H264 hard codec");
        } else {
            Log.d("not support H264 hard codec");
        }
    }


    private boolean supportH264Codec() {
        // 遍历支持的编码格式信息
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

                String[] types = codecInfo.getSupportedTypes();
                for (int i = 0; i < types.length; i++) {
                    KLog.d(types[i]);
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("enter surfaceCreated method");
        // 目前设定的是，当surface创建后，就打开摄像头开始预览
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(width, height);

        try {
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

        encoder = new H264Encoder(width, height, framerate);
        encoder.startEncoder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("enter surfaceChanged method");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("enter surfaceDestroyed method");

        // 停止预览并释放资源
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera = null;
        }

        if (encoder != null) {
            encoder.stopEncoder();
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (encoder != null) {
            encoder.putData(bytes);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
