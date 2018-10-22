package justicway.flogcat;
import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by William on 2018/10/22.
 * @catelogy Console
 *
 */
public class Console {
    // Define
    public static final int REQUEST_CONSOLE_PERMISSION_CODE = 9999;
    public static final int DEFAULT_BUFFER_SIZE = 500;
    public static final int DEFAULT_COLOR = Color.WHITE;
    private static final String TAG_NAME = "Console";
    // Parameter
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private static Console self = new Console(DEFAULT_BUFFER_SIZE);
    private static Handler handler ;
    private Context appContext,baseContext;
    private ArrayList<ConsoleMsg> logList;
    private LogAdapter adapter;
    private AppLifecycleCallbacks lifecycleCallback;
    private int bufferSize;
    private boolean isInit;
    private static boolean isTracklifecycle = true;
    private static boolean toLast , isFullScreen;
    private ConsoleCallBack callback;
    // View
    private LinearLayout popLayout;
    private RelativeLayout controlLayout;
    private ListView listView;
    private Button switchButton,lastButton,clearButton,takeScreenShotButton,fullScreenButton;

    // Constructor
    private Console(int size){
        this.bufferSize = size;
        this.logList = new ArrayList<ConsoleMsg>();
        this.isInit = false;
        this.toLast = true;
        this.isFullScreen = false;
    }
    /**
     * @return singleton Console Object
     */
    public static Console getInstance(){
        return self;
    }
    /**
     * add <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     * @param context
     * @return
     */
    public boolean init(Context context){
        return init(context,-1,false);// default draw
    }
    /**
     * add <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
     * @param context
     * @param isTracklifecycle AutoLog ActivityLifwCycle
     * @return
     */
    public boolean init(Context context,boolean isTracklifecycle){
        return init(context,-1,isTracklifecycle);// default draw
    }
    /**
     * @param context
     * @param row_layout : android: view_id
     * @return
     */
    public boolean init(Context context,int row_layout ,boolean isTracklifecycle){
        this.isTracklifecycle = isTracklifecycle;
        if(isInit)return false;
        this.appContext = context.getApplicationContext(); // class case Application
        this.baseContext = context;// class case Activity
        try{
            // main Thread
            handler = new Handler(baseContext.getMainLooper());
            wm = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
            params = new WindowManager.LayoutParams();
            // View init
            popLayout = new LinearLayout(appContext);
            popLayout.setClickable(false);
            controlLayout = new RelativeLayout(appContext);
            controlLayout.setClickable(false);
            switchButton = new Button(appContext);
            lastButton = new Button(appContext);
            clearButton = new Button(appContext);
            takeScreenShotButton= new Button(appContext);
            fullScreenButton= new Button(appContext);
            listView = new ListView(appContext);

            popLayout.setOrientation(LinearLayout.VERTICAL);
            popLayout.setClickable(false);
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            listView.setLayoutParams(lparams);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(dp(44),dp(40));
            switchButton.setLayoutParams(rlp);
            rlp = new RelativeLayout.LayoutParams(dp(44),dp(40));
            rlp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            lastButton.setLayoutParams(rlp);
            rlp = new RelativeLayout.LayoutParams(dp(44),dp(40));
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            clearButton.setLayoutParams(rlp);
            rlp = new RelativeLayout.LayoutParams(dp(44),dp(40));
            rlp.leftMargin = dp(60);
            takeScreenShotButton.setLayoutParams(rlp);
            rlp = new RelativeLayout.LayoutParams(dp(44),dp(40));
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            rlp.rightMargin = dp(60);
            fullScreenButton.setLayoutParams(rlp);

            listView.setBackgroundColor(Color.argb(77, 50, 50, 50));// 30% transparent gray
            listView.setDivider(null);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    if (handler != null) handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onLongClickListView(logList.get(position).toJsonMessage());
                        }
                    });
                    return false;
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    if (handler != null) handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onClickListView(logList.get(position).toJsonMessage());
                        }
                    });
                }
            });

            switchButton.setText("On");
            switchButton.setTextSize(10);
            switchButton.setBackgroundDrawable(getBitmapDrawable());
            switchButton.setTextColor(Color.RED);

            clearButton.setText("Clean");
            clearButton.setTextSize(9);
            clearButton.setBackgroundDrawable(getBitmapDrawable());
            clearButton.setTextColor(Color.RED);
            clearButton.setVisibility(View.INVISIBLE);

            lastButton.setText("Stop");
            lastButton.setTextSize(8);
            lastButton.setBackgroundDrawable(getBitmapDrawable());
            lastButton.setTextColor(Color.RED);
            lastButton.setVisibility(View.INVISIBLE);

            takeScreenShotButton.setText("takeShot");
            takeScreenShotButton.setTextSize(7);
            takeScreenShotButton.setBackgroundDrawable(getBitmapDrawable());
            takeScreenShotButton.setTextColor(Color.BLUE);
            takeScreenShotButton.setVisibility(View.INVISIBLE);

            fullScreenButton.setText("100%");
            fullScreenButton.setTextSize(8);
            fullScreenButton.setBackgroundDrawable(getBitmapDrawable());
            fullScreenButton.setTextColor(Color.BLUE);
            fullScreenButton.setVisibility(View.INVISIBLE);

            if(Build.VERSION.SDK_INT >= 26) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;;
            }else{
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
            params.format = PixelFormat.RGBA_8888; // transparent background
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.width = dp(40);//cssW_limit(10, dp(45), dp(100));// screen-width 10% range 45~100 dp
            params.height = dp(40);
            params.x = -1*cssW(50);
            params.y = cssH(50);
            controlLayout.addView(takeScreenShotButton);
            controlLayout.addView(fullScreenButton);
            controlLayout.addView(lastButton);
            controlLayout.addView(clearButton);
            controlLayout.addView(switchButton);
            popLayout.addView(controlLayout);
            popLayout.addView(listView);

            // permission check
            if(Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + context.getPackageName()));
                ((Activity)context).startActivityForResult(intent,REQUEST_CONSOLE_PERMISSION_CODE);
                return false;
            }
            // runtime permission request/checked
            if (Build.VERSION.SDK_INT >= 23) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                for (String str : permissions) {
                    if (context.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                        ((Activity)context).requestPermissions(permissions, REQUEST_CONSOLE_PERMISSION_CODE);
                    }
                }// end of for-loop
                if( !Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + context.getPackageName()));
                    ((Activity)context).startActivityForResult(intent,REQUEST_CONSOLE_PERMISSION_CODE);
                    return false;
                }
            }// runtime permission check

            wm.addView(popLayout, params);
            wm.updateViewLayout(popLayout, params);

            // set action
            switchButton.setOnClickListener(new View.OnClickListener() {
                boolean onoff = false;
                @Override
                public void onClick(View v) {
                    onoff = !onoff;
                    if (onoff) {
                        params.width = cssW(100); // screen-width 100%
                        if(isFullScreen)params.height = cssH(100); // screen-height 45%
                        else params.height = cssH(40); // screen-height 45%
                        switchButton.setText("Off");
                        clearButton.setVisibility(View.VISIBLE);
                        lastButton.setVisibility(View.VISIBLE);
                        takeScreenShotButton.setVisibility(View.VISIBLE);
                        fullScreenButton.setVisibility(View.VISIBLE);

                        wm.updateViewLayout(popLayout, params);
                    } else {
                        params.width = dp(40);//cssW_limit(10, dp(45), dp(100));// screen-width 10% range 45~100 dp
                        params.height = dp(40);
                        switchButton.setText("On");
                        clearButton.setVisibility(View.INVISIBLE);
                        lastButton.setVisibility(View.INVISIBLE);
                        takeScreenShotButton.setVisibility(View.INVISIBLE);
                        fullScreenButton.setVisibility(View.INVISIBLE);

                        wm.updateViewLayout(popLayout, params);
                    }
                }
            });
            switchButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deInit();
                    return false;
                }
            });

            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clean();
                }
            });
            lastButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toLast = !toLast;
                    if(toLast){
                        lastButton.setText("Stop");
                    }else{
                        lastButton.setText("Update");
                    }
                }
            });
            takeScreenShotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeScreenshot();
                }
            });
            fullScreenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isFullScreen = !isFullScreen;
                    if(isFullScreen){
                        fullScreenButton.setText("40%");
                        params.height = cssH(100); // screen-height 100%
                        wm.updateViewLayout(popLayout, params);
                    }else{
                        fullScreenButton.setText("100%");
                        params.height = cssH(40); // screen-height 40%
                        wm.updateViewLayout(popLayout, params);
                    }
                }
            });

            switchButton.setOnTouchListener(new View.OnTouchListener() {
                int lastX, lastY;
                int paramX, paramY;

                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            lastX = (int) event.getRawX();
                            lastY = (int) event.getRawY();
                            paramX = params.x;
                            paramY = params.y;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int dx = (int) event.getRawX() - lastX;
                            int dy = (int) event.getRawY() - lastY;
                            params.x = paramX + dx;
                            params.y = paramY + dy;
                            // update View (x,y)
                            wm.updateViewLayout(popLayout, params);
                            break;
                        case MotionEvent.ACTION_UP:
                            if (Math.abs(event.getRawX() - lastX) < 4 && Math.abs(event.getRawY() - lastY) < 4)
                                switchButton.performClick();
                            break;
                    }
                    return true;
                }
            });

            // default save log lifecycle
            lifecycleCallback = new AppLifecycleCallbacks();
            if(isTracklifecycle)((Application)appContext).registerActivityLifecycleCallbacks(lifecycleCallback);

            // init adapter
            if (adapter == null) {
                adapter = new LogAdapter(baseContext,row_layout);// R.draw.row_layout);
                listView.setAdapter(adapter);
            }
            synchronized (this){
                isInit = true;
            }
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public void deInit(){
        synchronized (this){
            isInit = false;
        }
        self = new Console(DEFAULT_BUFFER_SIZE);
        wm.removeView(popLayout);
        popLayout = null;
        adapter = null;
        callback = null;
        ((Application)appContext).unregisterActivityLifecycleCallbacks(lifecycleCallback);
        System.gc();
    }

    public ArrayList<ConsoleMsg> getLogList() {
        return logList;
    }

    /**
     * @param set
     * @return true : success
     * @return false : not init
     */
    public static boolean setCallBack(ConsoleCallBack set){
        if(self==null)return false;
        self.callback = set;
        return true;
    }

    public void clean(){
        logList.clear();
        if(handler!=null&&isInit)handler.post(new Runnable() {
            @Override
            public void run() {
                if(callback!=null)callback.onClean();
                if (adapter != null) adapter.notifyDataSetChanged();

            }
        });
    }

    private boolean  add(final ConsoleMsg msg){
        if(self==null)return false;
        if(handler!=null&&isInit) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // main thread
                    if (logList.size() >= bufferSize) {
                        try {
                            if (callback != null)
                                callback.onFlush(self.logList.get(0).toJsonMessage());
                        } catch (IndexOutOfBoundsException e) {
                        }// pop message
                        logList.remove(0);
                    }
                    // add
                    logList.add(msg);
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (toLast && listView != null) listView.setSelection(logList.size() - 1);
                    if (logList.size() >= bufferSize && callback != null) callback.onFull();
                }
            });

            return true;
        }else{
            return false;
        }
    }

    public boolean add(String tag,String message,int threadId,int kind,int color){
        ConsoleMsg msg = new ConsoleMsg();
        msg.timestemp = System.currentTimeMillis();
        msg.threadID = threadId;
        msg.tagName = tag;
        msg.message = message;
        msg.kind = kind;
        msg.color = color;
        return add(msg);
    }
    public boolean add(String tag,String message,int threadId,int kind){
        ConsoleMsg msg = new ConsoleMsg();
        msg.timestemp = System.currentTimeMillis();
        msg.threadID = threadId;
        msg.tagName = tag;
        msg.message = message;
        msg.kind = kind;
        msg.color = DEFAULT_COLOR;
        return add(msg);
    }

    private void shareScreenshot(Context context,File imageFile) {
        Intent intent = new Intent();
        Uri uri = Uri.fromFile(imageFile);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/jpeg");
        String str = "Post Log ScreenShot image to ...";
        context.startActivity(Intent.createChooser(intent,str));
        Log.c(TAG_NAME,"shareScreenshot()",Color.YELLOW);
    }
    /**
     *  add <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     */
    private void takeScreenshot() {

        try {
            Long timeStempLong = System.currentTimeMillis()/1000;
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/screenShot_"+timeStempLong.toString()+".jpg";
            // create bitmap screen capture
            View view = popLayout.getRootView();
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            File imageFile = new File(mPath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            // share
            shareScreenshot(this.baseContext,imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    // default icon
    private BitmapDrawable getBitmapDrawable(){
        String textLogo = "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";
        byte[] imageAsBytes = Base64.decode(textLogo.getBytes(), Base64.DEFAULT);
        BitmapDrawable bdrawable = new BitmapDrawable(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
        return bdrawable;
    }
    /**
     * @param dp
     * @return dp to real screen px
     */
    private int dp(int dp){
        return (int)(appContext.getResources().getDisplayMetrics().density*dp+0.5f);
    }
    private int cssW(int percent){
        if(percent<=0)return 0;
        return (int)(appContext.getResources().getDisplayMetrics().widthPixels*percent/100);
    }
    private int cssH(int percent){
        if(percent<=0)return 0;
        return (int)(appContext.getResources().getDisplayMetrics().heightPixels*percent/100);
    }

    private int cssW_limit(int percent,int max,int min){
        if(percent<=0)return 0;
        int real = (int)(appContext.getResources().getDisplayMetrics().widthPixels*percent/100);
        if(real<min)return min;
        if(real>max)return max;
        return real;
    }
    private int cssH_limit(int percent,int max,int min){
        if(percent<=0)return 0;
        int real = (int)(appContext.getResources().getDisplayMetrics().heightPixels*percent/100);
        if(real<min)return min;
        if(real>max)return max;
        return real;
    }

    public interface ConsoleCallBack{
        void onClean();
        void onFull();
        void onFlush(String target);
        void onClickListView(String msg);
        void onLongClickListView(String msg);
    }

    private class ConsoleMsg implements Serializable {
        long timestemp;
        int threadID;
        String tagName;
        String message;
        int kind;
        int color;
        public String toJsonMessage() {
            String msg = message.replace("\"","\\\"");// message have json
            return "{time:"+timestemp+",pid:"+threadID+",tag:\""+tagName+"\",msg:\""+msg+"\"}";
        }
    }

    static class LogAdapter extends ArrayAdapter<ConsoleMsg> {
        private int layoutResourceId;
        private Context context;
        private ArrayList<ConsoleMsg> data ;
        class ViewHolder{
            public TextView timeStemp,thread,tag,message;
        }
        public LogAdapter(Context context, int resource) {
            super(context, -1);
            this.context = context;
            this.layoutResourceId = resource;
            this.data = Console.getInstance().getLogList();
            self.adapter = this;
        }

        @Override
        public void add(ConsoleMsg object) {
            this.data.add(object);
            // Maybe different thread add
            if(handler!=null)handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                    try{ if(toLast)self.listView.setSelection(self.logList.size() - 1);}catch (Exception e){}
                }
            });
        }

        @Override
        public int getCount() {
            return this.data.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            int timeStemp_id =-1,thread_id=-1,tag_id=-1,message_id=-1;
            if(row==null){// new View
                if(layoutResourceId!=-1) {// use layout.xml
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    row = inflater.inflate(layoutResourceId, parent, false);
                    ViewHolder holder = new ViewHolder();
//                    holder.timeStemp = (TextView)row.findViewById(R.id.timestempTextView);
//                    holder.thread =  (TextView)row.findViewById(R.id.threadTextView);
//                    holder.tag =  (TextView)row.findViewById(R.id.tagTextView);
//                    holder.message =  (TextView)row.findViewById(R.id.messageTextView);
                    row.setTag(holder);
                }else{// layout programmatically
                    LinearLayout root = new LinearLayout(context);
                    ViewHolder holder = new ViewHolder();
                    root.setOrientation(LinearLayout.HORIZONTAL);
                    holder.timeStemp = new TextView(context);
                    holder.timeStemp.setTextSize(9);
                    holder.timeStemp.setGravity(Gravity.LEFT | Gravity.CENTER);
                    holder.thread = new TextView(context);
                    holder.thread.setTextSize(9);
                    holder.thread.setGravity(Gravity.LEFT | Gravity.CENTER);
                    holder.tag = new TextView(context);
                    holder.tag.setTextSize(12);
                    holder.tag.setGravity(Gravity.LEFT | Gravity.CENTER);
                    holder.tag.setPadding(5, 0, 0, 0);
                    holder.message = new TextView(context);
                    holder.message.setTextSize(12);
                    holder.message.setGravity(Gravity.LEFT | Gravity.CENTER);
                    holder.message.setPadding(5, 0, 0, 0);
                    root.addView(holder.timeStemp);
                    root.addView(holder.thread);
                    root.addView(holder.tag);
                    root.addView(holder.message);
                    root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 45));//ViewGroup.LayoutParams.WRAP_CONTENT));

                    row = root;
                    row.setTag(holder);
                }
            }
            // find Views
            ViewHolder holder = (ViewHolder)row.getTag();
            //set View status
            int color = data.get(position).color;
            Date dateTime = new Date(data.get(position).timestemp);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
            holder.timeStemp.setText("" + df.format(dateTime));
            holder.timeStemp.setTextColor(color);
            holder.thread.setText("(" + data.get(position).threadID + ")");
            holder.thread.setTextColor(color);
            holder.tag.setText("[" + data.get(position).tagName+"]");
            holder.tag.setTextColor(color);
            holder.message .setText("" + data.get(position).message);
            holder.message.setTextColor(color);

            return row;
        }

    }
    private class AppLifecycleCallbacks implements Application.ActivityLifecycleCallbacks{
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (adapter == null) {
                adapter = new LogAdapter(activity, -1);// R.draw.row_layout);
                listView.setAdapter(adapter);
            }
            Log.d(TAG_NAME + "-" + activity.getLocalClassName(), "onCreate()");
        }
        @Override
        public void onActivityStarted(Activity activity) {
            Log.d(TAG_NAME + "-" + activity.getLocalClassName(), "onStart()");
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.d(TAG_NAME + "-" + activity.getLocalClassName(), "onResume()");
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.d(TAG_NAME + "-" + activity.getLocalClassName(), "onPause()");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.d(TAG_NAME + "-" + activity.getLocalClassName(), "onStop()");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            Log.d(TAG_NAME + "-" + activity.getLocalClassName(), "onSaveInstanceState()");
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.d(TAG_NAME + "-" + activity.getLocalClassName(), "onDestroy()");
        }
    }


}