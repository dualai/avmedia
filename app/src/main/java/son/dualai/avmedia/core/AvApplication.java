package son.dualai.avmedia.core;

import android.app.Application;

import com.socks.library.KLog;

/**
 * Created on 2019/2/14.
 */
public class AvApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KLog.init(true,"avmedia");
    }
}
