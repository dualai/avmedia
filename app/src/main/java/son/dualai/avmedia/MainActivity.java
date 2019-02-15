package son.dualai.avmedia;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import son.dualai.avmedia.modules.RecordAuActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        OutputStream
//        ByteArrayOutputStream
//        FileOutputStream
//        OutputStreamWriter

        startActivity(new Intent(this,RecordAuActivity.class));

    }
}
