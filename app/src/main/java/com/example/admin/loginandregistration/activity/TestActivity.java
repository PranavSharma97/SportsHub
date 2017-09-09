package com.example.admin.loginandregistration.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.R;
import com.example.admin.loginandregistration.helper.RoundImage;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_design_profile_screen_xml_ui_design);

        ImageView imageView1 = (ImageView) findViewById(R.id.imageDownloaded);
        RoundImage roundedImage;
        Bitmap bm;
        bm = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.man);
        roundedImage = new RoundImage(bm);
        imageView1.setImageDrawable(roundedImage);
    }
}
