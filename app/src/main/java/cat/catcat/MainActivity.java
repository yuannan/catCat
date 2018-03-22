package cat.catcat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public int totalCats;
    int newCats;
    int maxHoz;
    int maxVert;
    int minHoz = 100;
    int minVert = 100;
    String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
    String folderName = "catCat";
    public String storagePath = sdCard + "/" + folderName + "/";

    boolean verbose = true;
    ArrayList<String> errors = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // custom code
        this.quickToast("Welcome to catCat!");
        this.updateGeneralCountMessage();
        randomMainMenuImage();

    }

    public void randomMainMenuImage(){
        this.updateTotalCats();
        ImageView mainMenuImage = findViewById(R.id.mainMenuImage);
        BitmapDrawable imageToShow;

        if(totalCats == 0) {
            mainMenuImage.setImageResource(R.mipmap.kittenapped);
        } else{
            int img = (int) (Math.random() * totalCats);
            imageToShow = new BitmapDrawable(getResources(), storagePath+img+".jpg");
            mainMenuImage.setImageDrawable(imageToShow);
        }
    }

    public void updateGeneralCountMessage(){
        this.updateTotalCats();
        if(totalCats == 0){
            updateGeneralMessage("You have no cats! Get some!");
        } else if(totalCats < 0){
            updateGeneralMessage("how...");
        } else if(totalCats > 9000){
            updateGeneralMessage("nice.");
        } else if(totalCats > 50){
            updateGeneralMessage(totalCats+" CATS?!1? MY APP ISN'T THAT GOOD!");
        } else{
            updateGeneralMessage("Kitty Kount: " + totalCats);
        }
    }

    public void updateTotalCats(){
        File dir = new File(storagePath);
        totalCats = dir.listFiles().length;
    }

    public void updateGeneralMessage(String message){
        TextView messageText = findViewById(R.id.generalMessageText);
        messageText.setText(message);
    }

    public void purgeImageView(View view){
        this.purgePreviousImages();
        this.updateGeneralCountMessage();
    }

    public void purgePreviousImages(){
        File dir = new File(storagePath);
        if (dir.isDirectory()) {
            String[] pictures = dir.list();
            for (int i = 0; i < pictures.length; i++){
                new File(dir, pictures[i]).delete();
            }
        }
        this.quickToast("All cats killed...");
        this.randomMainMenuImage();
    }
    public String getNewImageURL(){
        this.updateInputBoxes();
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
        // this.quickToast(URL);

        return URL;
    }

    public void downloadMoreCats(View view){
        this.updateInputBoxes();

        /*  TODO Multi-threading may be needed
            app freeze upon download button press
            logcat: "The application may be doing too much work on its main thread."
        */
        for(int img = 0; img < newCats; img++){
            int tryCount = 0;
            try {
                // getting temp picture location
                File saveLocation = new File (storagePath);
                saveLocation.mkdirs(); // makes dirs
                String filename = String.valueOf(img + totalCats) + ".jpg"; // generate file name

                boolean validImage = false;
                while(!validImage){
                    // getting image
                    String imageURL = this.getNewImageURL();
                    InputStream in = new BufferedInputStream(new URL(imageURL).openStream());
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(storagePath+filename));
                    int i;
                    while((i = in.read()) != -1){
                        out.write(i);
                    }
                    in.close();
                    out.flush();
                    out.close();

                    // TODO requires better error handling, still get blank images sometimes, might be fixed :)
                    BitmapDrawable bd = new BitmapDrawable(getResources(), storagePath+filename);
                    if(bd.getBitmap() == null){
                        errors.add(imageURL + "\ndoes not return a valid image!\nAttempt: "+tryCount);
                        tryCount++;
                    } else{
                        validImage = true;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        this.randomMainMenuImage();
        this.updateGeneralCountMessage();
        if(verbose) {
            this.quickToast("All images downloaded");
            this.spewErrors();
        }
    }

    public void quickToast(String message){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void spewErrors(){
        for(int e = 0; e < errors.size(); e++){
            try {
                this.quickToast(errors.get(e));
                Thread.sleep(100);
            }
            catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }
        }
        errors.clear();
    }

    public void updateInputBoxes() {
        EditText amountBox = findViewById(R.id.amountBox);
        EditText hozBox = findViewById(R.id.hozBox);
        EditText vertBox = findViewById(R.id.vertBox);

        newCats = Integer.valueOf(amountBox.getText().toString());
        maxHoz = Integer.valueOf(hozBox.getText().toString());
        maxVert = Integer.valueOf(vertBox.getText().toString());
    }

    public void exitProgram(View view){
        this.quickToast("bye.");
        System.exit(0);
    }

    public void viewPictures(View view){
        Intent intent = new Intent(this, PictureActivity.class);
        intent.putExtra("cat.catcat.totalCats", totalCats);
        intent.putExtra("cat.catcat.storagePath", storagePath);
        startActivity(intent);
    }
}
