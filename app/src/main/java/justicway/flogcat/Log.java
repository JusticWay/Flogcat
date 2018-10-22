package justicway.flogcat;

import android.graphics.Color;

/**
 * Created by William on 2015/12/28.
 */
public class Log {
    // Define
    public static int ERROR   = 1;
    public static int WARN    = 2;
    public static int DEBUG	  = 3;
    public static int INFO	  = 4;
    public static int VERBOSE = 5;
    public static int CUSTOMIZE = 6;
    // Parameter
    public static boolean debug = true;// show general log
    // static method
    public static void e(String tag,String message){
        e(tag, message, null, true, android.os.Process.myPid());
    }
    public static void e(String tag,String message,Throwable throwable){
        e(tag, message, throwable, true, android.os.Process.myPid());
    }
    public static void e(String tag,String message,Throwable throwable, boolean isSetConsole,int threadId){
        if(debug)android.util.Log.e(tag,message,throwable);
        if(isSetConsole){
            // add in console buffer
            message = message + '\n' + android.util.Log.getStackTraceString(throwable);
            Console.getInstance().add(tag,message,threadId,ERROR,Color.RED);
        }
    }

    public static void w(String tag,String message){
        w(tag, message, null, true, android.os.Process.myPid());
    }
    public static void w(String tag,String message,Throwable throwable){
        w(tag, message, throwable, true, android.os.Process.myPid());
    }
    public static void w(String tag,String message,Throwable throwable, boolean isSetConsole,int threadId){
        if(debug)android.util.Log.w(tag, message, throwable);
        if(isSetConsole){
            // add in console buffer
            message = message + '\n' + android.util.Log.getStackTraceString(throwable);
            Console.getInstance().add(tag,message,threadId,WARN,Color.YELLOW);
        }
    }

    public static void i(String tag,String message){
        i(tag, message, null, true, android.os.Process.myPid());
    }
    public static void i(String tag,String message,Throwable throwable){
        i(tag, message, throwable, true, android.os.Process.myPid());
    }
    public static void i(String tag,String message,Throwable throwable, boolean isSetConsole,int threadId){
        if(debug)android.util.Log.i(tag, message, throwable);
        if(isSetConsole){
            // add in console buffer
            message = message + '\n' + android.util.Log.getStackTraceString(throwable);
            Console.getInstance().add(tag,message,threadId,INFO,Color.BLUE);
        }
    }

    public static void d(String tag,String message){
        d(tag, message, null, true, android.os.Process.myPid());
    }
    public static void d(String tag,String message,Throwable throwable){
        d(tag, message, throwable, true, android.os.Process.myPid());
    }
    public static void d(String tag,String message,Throwable throwable, boolean isSetConsole,int threadId){
        if(debug)android.util.Log.d(tag, message, throwable);
        if(isSetConsole){
            // add in console buffer
            message = message + '\n' + android.util.Log.getStackTraceString(throwable);
            Console.getInstance().add(tag,message,threadId,DEBUG,Color.GREEN);
        }
    }

    public static void v(String tag,String message){
        v(tag, message, null, true, android.os.Process.myPid());
    }
    public static void v(String tag,String message,Throwable throwable){
        v(tag, message, throwable, true, android.os.Process.myPid());
    }
    public static void v(String tag,String message,Throwable throwable, boolean isSetConsole,int threadId){
        if(debug)android.util.Log.v(tag, message, throwable);
        if(isSetConsole){
            // add in console buffer
            message = message + '\n' + android.util.Log.getStackTraceString(throwable);
            Console.getInstance().add(tag,message,threadId,VERBOSE,Color.LTGRAY);
        }
    }

    public static void c(String tag,String message){
        c(tag, message, null, true, android.os.Process.myPid(),Console.DEFAULT_COLOR);
    }
    public static void c(String tag,String message,int color){
        c(tag, message, null, true, android.os.Process.myPid(),color);
    }
    public static void c(String tag,String message,Throwable throwable,int color){
        c(tag, message, throwable, true, android.os.Process.myPid(),color);
    }
    public static void c(String tag,String message,Throwable throwable, boolean isSetConsole,int threadId,int color){
        if(debug)android.util.Log.v(tag, message, throwable);
        if(isSetConsole){
            // add in console buffer
            message = message + '\n' + android.util.Log.getStackTraceString(throwable);
            Console.getInstance().add(tag,message,threadId,CUSTOMIZE,color);
        }
    }

}
