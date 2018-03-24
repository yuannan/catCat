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
    public int totalCats;
    int newCats;
    int maxHoz;
    int maxVert;
    int minHoz = 100;
    int minVert = 100;
    String storageLocation;
    String folderName;
    public String fullStoragePath;
    boolean validInputs = false;
    boolean downloading = false;

    boolean verbose = true;
    ArrayList<String> errors = new ArrayList();

    // progress bar
    public ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pb = findViewById(R.id.downloadProgress);

        this.quickToast("Welcome to catCat!");
        this.updateStorage();
        this.refreshUI();
    }

    public void updateStorage(){
        this.storageLocation = getFilesDir().getAbsolutePath();
        this.folderName = "poor_conditions";
        this.fullStoragePath = storageLocation + "/" + folderName + "/";
        // ensures the location exists
        File saveLocation = new File (fullStoragePath);
        saveLocation.mkdirs();

        if(verbose) System.out.println(fullStoragePath);
    }

    public void randomMainMenuImage(){
        ImageView mainMenuImage = findViewById(R.id.mainMenuImage);
        BitmapDrawable imageToShow;

        if(totalCats == 0) {
            mainMenuImage.setImageResource(R.mipmap.kittenapped);
        } else{
            int img = (int) (Math.random() * totalCats);
            imageToShow = new BitmapDrawable(getResources(), fullStoragePath +img+".jpg");
            mainMenuImage.setImageDrawable(imageToShow);
        }
    }

    public void updateGeneralCountMessage(){
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
        File dir = new File(fullStoragePath);
        totalCats = dir.listFiles().length;
    }

    public void updateGeneralMessage(String message){
        TextView messageText = findViewById(R.id.generalMessageText);
        messageText.setText(message);
    }

    public void purgeImageView(View view){
        this.purgePreviousImages();
        this.refreshUI();
    }

    public void purgePreviousImages(){
        File dir = new File(fullStoragePath);
        if (dir.isDirectory()) {
            String[] pictures = dir.list();
            for (int i = 0; i < pictures.length; i++){
                new File(dir, pictures[i]).delete();
            }
        }
        this.quickToast("All cats killed...");

        this.refreshUI();
    }

    public void refreshUI(){
        this.updateTotalCats();
        this.randomMainMenuImage();
        this.updateGeneralCountMessage();
    }

    public void downloadMoreCats(View view){
        if(downloading) {
            this.quickToast("Already getting new kittens!\nSlow down you greedy pig!");
        } else{
            validInputs = false;
            this.updateInputBoxes();
            if (validInputs) {
                new AsyncDownloadKittens().execute(fullStoragePath, newCats, totalCats, minHoz, maxHoz, minVert, maxVert);
                this.quickToast("Downloading new kittens...");
            }
        }
    }

    public void quickToast(String message){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void spewErrors(){
        for(int e = 0; e < errors.size(); e++){
            System.out.println(errors.get(e));
        }
        errors.clear();
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

    public void exitProgram(View view){
        this.quickToast("bye.");
        System.exit(0);
    }

    public void viewPictures(View view){
        this.updateTotalCats();
        Intent intent = new Intent(this, PictureActivity.class);
        intent.putExtra("cat.catcat.totalCats", totalCats);
        intent.putExtra("cat.catcat.fullStoragePath", fullStoragePath);
        startActivity(intent);
    }

    public void setDownloadingState(boolean bol){
        downloading = bol;
    }

    // async downloading to prevent UI freeze up, bad code but at least it works :')
    private class AsyncDownloadKittens extends AsyncTask<Object, Integer, Integer> {
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

            // downloading kittens
            MainActivity.this.pb.setMax(amount);
            for (int kitty = 0; kitty < amount; kitty++) {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                GetKittenRunnable newKitty = new GetKittenRunnable(fullStoragePath + String.valueOf(kitty + currentIndex) + ".jpg", minHoz, maxHoz, minVert, maxVert);
                newKitty.run();
                publishProgress(kitty);
            }

            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            MainActivity.this.pb.setProgress(values[0] + 1);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity.this.setDownloadingState(true);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            // direct function calls to talk back to main
            MainActivity.this.setDownloadingState(false);
            MainActivity.this.refreshUI();
            MainActivity.this.quickToast("Kittens downloaded!");
        }
    }
}
