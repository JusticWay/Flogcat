package justicway.flogcat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author  Justicway
 */
public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private int clickConter =0;
    private boolean isTracklifecycle = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // key point init here
        Console.getInstance().init(this,isTracklifecycle);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickConter++;
                // flogcat.justicway.flogcat.Log replace import android.util.Log;
                Log.c(TAG,"onClick(): "+Integer.toString(clickConter)+" times",randonColor());
                Snackbar.make(view, "Click action and make a log", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new Timer().schedule(new TimerTask() {
            int countdown = 20;
            @Override
            public void run() {
                if (countdown<0) {
                    this.cancel();
                    return;
                }
                Log.i("TimerTask","countdown ="+Integer.toString(countdown) );
                countdown--;
            }
        },0,1000);
    }

    private int randonColor(){
        return (int)(Math.random()*0x1000000) ^ Color.BLACK;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Console.REQUEST_CONSOLE_PERMISSION_CODE:
                //init again
                Console.getInstance().init(this,isTracklifecycle);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // deinit
        Console.getInstance().deInit();
    }
}

