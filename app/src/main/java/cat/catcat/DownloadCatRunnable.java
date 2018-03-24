package cat.catcat;

import android.graphics.drawable.BitmapDrawable;
import android.os.Process;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

// i have no idea what i'm doing halp, multi threading is hard :(
// but at least it works

public class DownloadCatRunnable implements Runnable{
    String kittenURL;
    String storageLocation;
    int minHoz, maxHoz, minVert, maxVert;


    public DownloadCatRunnable(String saveTo, int minHoz, int maxHoz, int minVert, int maxVert){
        this.storageLocation = saveTo;
        this.minHoz = minHoz;
        this.maxHoz = maxHoz;
        this.minVert = minVert;
        this.maxVert = maxVert;
    }

    public void run(){
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        int tryCount = 0;
        try {
            boolean validImage = false;
            while(!validImage){
                kittenURL = this.getNewURL();
                // getting image
                InputStream in = new BufferedInputStream(new URL(kittenURL).openStream());
                OutputStream out = new BufferedOutputStream(new FileOutputStream(storageLocation));
                int i;
                while((i = in.read()) != -1){
                    out.write(i);
                }
                in.close();
                out.flush();
                out.close();

                // TODO requires better error handling, still get blank images sometimes, might be fixed :)
                BitmapDrawable bd = new BitmapDrawable(android.content.res.Resources.getSystem(), storageLocation);
                if(bd.getBitmap() == null && tryCount <= 3){
                    System.out.println(kittenURL + "\ndoes not return a valid image!\n" +
                            "Attempt: "+String.valueOf(tryCount+1));
                    tryCount++;
                } else{
                    validImage = true;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getNewURL(){
        // generates image sizes
        int currentHoz = (int) (Math.random() * maxHoz);
        int currentVert = (int) (Math.random() * maxVert);

        // makes sure that images are of decent quality
        if(currentHoz < minHoz){
            currentHoz = currentHoz + minHoz;
        }
        if(currentVert < minVert){
            currentVert = currentVert + minVert;
        }
        // caps the size to user
        if(currentHoz > maxHoz){
            currentHoz = maxHoz;
        }
        if(currentVert > maxVert){
            currentVert = maxVert;
        }

        // constructing URL
        String URL = "http://placekitten.com/g/"+currentHoz+"/"+currentVert;

        return URL;
    }
}
