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
    public int currentIndex;
    Intent intent;
    int amount;
    String storagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        this.intent = getIntent();

        this.amount = intent.getIntExtra("cat.catcat.totalCats", -1);
        this.storagePath = intent.getStringExtra("cat.catcat.fullStoragePath");
        this.setPicture(0);

        this.quickToast("Enjoy your kittens!");
    }

    public void setPicture(int img){
        currentIndex = img;
        // updating text
        TextView currentKittenText = findViewById(R.id.currentKittenText);
        currentKittenText.setText("Kitty#"+String.valueOf(img+1)); // +1 for the normal people

        // setting the new image
        ImageView imageView = findViewById(R.id.imageView);
        BitmapDrawable imageToShow = new BitmapDrawable(getResources(), storagePath+img+".jpg");
        if(imageToShow.getBitmap() == null){
            this.quickToast("missing kitty");
        } else {
            imageView.setImageDrawable(imageToShow);
        }
    }

    public void returnToMainMenu(View view){
        finish();
    }

    public void nextPicture(View view){
        // increments the index and shows the picture
        currentIndex++;
        currentIndex = currentIndex % amount;
        setPicture(currentIndex);
    }

    public void prevPicture(View view){
        // makes sure that the index will not fall below 0
        if(currentIndex > 0){
            currentIndex--;
        } else{
            currentIndex = amount - 1;
        }

        // currentIndex = (currentIndex > 0)? (currentIndex - 1) : (amount - 1);

        // currentIndex = (currentIndex > 0)? currentIndex : amount;
        // currentIndex--;

        // currentIndex--;
        // if(currentIndex < 0) currentIndex = amount - 1;

        setPicture(currentIndex);
    }

    public void quickToast(String message){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
