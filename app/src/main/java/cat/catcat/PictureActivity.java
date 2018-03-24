package cat.catcat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends AppCompatActivity {
    Intent intent;
    public int currentCat;
    int totalCats;
    String fullStoragePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        // getting intent and vars
        this.intent = getIntent();
        this.totalCats = intent.getIntExtra("cat.catcat.totalCats", -1);
        this.fullStoragePath = intent.getStringExtra("cat.catcat.fullStoragePath");

        // displays the cats
        this.setCat(0);
        this.quickToast("Enjoy your cats!");
    }

    public void setCat(int cat){
        currentCat = cat;
        // updating text
        TextView currentCatText = findViewById(R.id.currentCatText);
        currentCatText.setText("Cat#"+String.valueOf(cat + 1)); // +1 for the normal people

        // setting the new image
        ImageView imageView = findViewById(R.id.imageView);
        BitmapDrawable imageToShow = new BitmapDrawable(getResources(), fullStoragePath + cat + ".jpg");
        if(imageToShow.getBitmap() == null){
            this.quickToast("Missing cat!");
            imageView.setImageResource(R.mipmap.kittenapped);
        } else {
            imageView.setImageDrawable(imageToShow);
        }
    }

    public void nextCat(View view){
        // increments the index and shows the cat
        currentCat++;
        currentCat = currentCat % totalCats;
        setCat(currentCat);
    }

    public void prevCat(View view){
        // makes sure that the index will not fall below 0
        if(currentCat > 0){
            currentCat--;
        } else{
            currentCat = totalCats - 1;
        }

        setCat(currentCat);
    }

    public void quickToast(String message){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void returnToMainMenu(View view){
        finish();
    }
}
