package su.geocaching.android.controller.managers;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

public class MemoryManager {
    
    private static int UPDATE_INTERVAL = 1000; // Milliseconds
    
    private final Timer memoryTimer;
    private final TimerTask memoryReporterTask;
    
    public MemoryManager(Context context) {
        
        final Toast memoryToast = Toast.makeText(context, "Memory usage", Toast.LENGTH_SHORT);
        memoryToast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
        
        memoryReporterTask = new TimerTask() {
            private Handler updateUI = new Handler(){
                @Override
                public void dispatchMessage(Message msg) {
                    super.dispatchMessage(msg);
                    if (memoryToast != null) {
                        memoryToast.cancel();
                        memoryToast.setText(String.format("%d Kb", msg.arg1 / 1024));
                        memoryToast.show();
                    }
                }
            };            
            
            @Override
            public void run() {                
                Message m = new Message();
                m.arg1 = (int) Debug.getNativeHeapAllocatedSize();
                updateUI.sendMessage(m);  
            }
        };        
        
        memoryTimer = new Timer();
    };

    public void showMemoryToast() {
        memoryTimer.schedule(memoryReporterTask, 0, UPDATE_INTERVAL);   
    }
}
