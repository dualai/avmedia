package son.dualai.avmedia.util;

public class Log {
    public static void d(String msg){
        android.util.Log.d("avmedia",msg);
    }

    public static void d(String TAG,String msg){
        android.util.Log.d(TAG,msg);
    }

    public static void i(String msg){
        android.util.Log.i("avmedia",msg);
    }

    public static void i(String TAG,String msg){
        android.util.Log.i(TAG,msg);
    }

    public static void e(String msg){
        android.util.Log.e("avmedia",msg);
    }

    public static void e(String TAG,String msg){
        android.util.Log.e(TAG,msg);
    }
}
