package cat.catcat;

import android.os.Process;

/**
 * Created by jiji on 24/03/18.
 */

public class getKittenRunnable {

    public void run(){
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        
    }
}
