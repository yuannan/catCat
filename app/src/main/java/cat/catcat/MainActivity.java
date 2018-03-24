package cat.catcat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Process;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // number of cats
    int totalCats;
    int newCats;
    // download resolution
    int maxHoz;
    int maxVert;
    int minHoz = 100;
    int minVert = 100;
    // picture storage
    String storageLocation;
    String folderName;
    String fullStoragePath;
    // user fail safes
    boolean validInputs = false;
    boolean downloading = false;
    // progress bar
    ProgressBar pb;
    // debugging
    boolean verbose = true;
    ArrayList<String> errors = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // user introduction and global var inits
        this.quickToast("Welcome to catCat!");
        this.updateStorage();
        this.updateProgressBar();
        this.refreshMainUI();
    }

    public void updateStorage(){
        // allocating file cache
        this.storageLocation = getFilesDir().getAbsolutePath();
        this.folderName = "poor_conditions";
        this.fullStoragePath = storageLocation + "/" + folderName + "/";
        // ensures the location exists
        File saveLocation = new File (fullStoragePath);
        saveLocation.mkdirs();

        if(verbose) System.out.println(fullStoragePath);
    }

    public void updateTotalCats(){
        File dir = new File(fullStoragePath);
        totalCats = dir.listFiles().length;
    }

    public void updateInputBoxes() {
        EditText amountBox = findViewById(R.id.amountBox);
        EditText hozBox = findViewById(R.id.hozBox);
        EditText vertBox = findViewById(R.id.vertBox);
        try {
            newCats = Integer.valueOf(amountBox.getText().toString());
            maxHoz = Integer.valueOf(hozBox.getText().toString());
            maxVert = Integer.valueOf(vertBox.getText().toString());
            validInputs = true;
        } catch(Exception e){
            this.quickToast("Invalid Inputs!");
            validInputs = false;
        }
    }

    public void updateProgressBar(){
        pb = findViewById(R.id.downloadProgress);
        pb.setProgress(0);
    }

    public void refreshMainUI(){
        this.updateTotalCats();
        this.refreshRandomMainUIImage();
        this.refreshGeneralMessage();
    }

    public void refreshRandomMainUIImage(){
        ImageView mainMenuImage = findViewById(R.id.mainMenuImage);
        BitmapDrawable imageToShow;

        // shows a random cat you have or a default picture
        if(totalCats == 0) {
            mainMenuImage.setImageResource(R.mipmap.kittenapped);
        } else{
            int img = (int) (Math.random() * totalCats);
            imageToShow = new BitmapDrawable(getResources(), fullStoragePath + img + ".jpg");
            mainMenuImage.setImageDrawable(imageToShow);
        }
    }

    public void refreshGeneralMessage(){
        // "funny" user messages based on the totalCats of cats
        if(totalCats == 0){
            setGeneralMessageText("You have no cats! Get some!");
        } else if(totalCats < 0){
            setGeneralMessageText("how...");
        } else if(totalCats > 9000){
            setGeneralMessageText("nice.");
        } else if(totalCats > 50){
            setGeneralMessageText(totalCats+" CATS?!1? MY APP ISN'T THAT GOOD!");
        } else{
            setGeneralMessageText("Cat Count: " + totalCats);
        }
    }

    public void setGeneralMessageText(String message){
        TextView messageText = findViewById(R.id.generalMessageText);
        messageText.setText(message);
    }

    public void setDownloadingState(boolean state){
        downloading = state;
    }

    public void purgeImageView(View view){
        this.purgeCachedImages();
        this.refreshMainUI();
    }

    public void purgeCachedImages(){
        File dir = new File(fullStoragePath);
        if (dir.isDirectory()) {
            for(File img : dir.listFiles()){
                img.delete();
            }
        }
        this.quickToast("All cats killed...");
    }

    public void downloadMoreCats(View view){
        if(downloading) {
            this.quickToast("Already getting new cats!");
        } else{
            validInputs = false;
            this.updateInputBoxes();
            if (validInputs) {
                new AsyncDownloadCats().execute(fullStoragePath, newCats, totalCats, minHoz, maxHoz, minVert, maxVert);
                this.quickToast("Downloading new cats...");
            }
        }
    }

    // async downloading to prevent UI freeze up, very bad code but at least it works :')
    private class AsyncDownloadCats extends AsyncTask<Object, Integer, Integer> {
        @Override
        protected Integer doInBackground(Object... objects) {
            // declare vars needed
            String fullStoragePath = (String) objects[0];
            int amount = (int) objects[1];
            int currentIndex = (int) objects[2];
            int minHoz = (int) objects[3];
            int maxHoz = (int) objects[4];
            int minVert = (int) objects[5];
            int maxVert = (int) objects[6];

            MainActivity.this.pb.setMax(amount);
            // downloading cats
            for (int cat = 0; cat < amount; cat++) {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                DownloadCatRunnable newCat = new DownloadCatRunnable(fullStoragePath + String.valueOf(cat + currentIndex) + ".jpg", minHoz, maxHoz, minVert, maxVert);
                newCat.run();
                publishProgress(cat);
            }

            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity.this.setDownloadingState(true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            MainActivity.this.pb.setProgress(values[0] + 1);
            // Animated progress bar for API 24+
            // MainActivity.this.pb.setProgress(values[0] + 1, true);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            // direct function calls to talk back to main
            MainActivity.this.setDownloadingState(false);
            MainActivity.this.refreshMainUI();
            MainActivity.this.quickToast("Cats downloaded!");
        }
    }

    public void viewCats(View view){
        this.updateTotalCats();
        if(totalCats == 0){
            this.quickToast("No cats found!");
        } else {
            Intent intent = new Intent(this, PictureActivity.class);
            intent.putExtra("cat.catcat.totalCats", totalCats);
            intent.putExtra("cat.catcat.fullStoragePath", fullStoragePath);
            startActivity(intent);
        }
    }

    public void quickToast(String message){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void exitProgram(View view){
        System.exit(0);
    }
}
