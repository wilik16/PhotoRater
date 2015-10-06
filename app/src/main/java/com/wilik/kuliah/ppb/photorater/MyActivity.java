package com.wilik.kuliah.ppb.photorater;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class MyActivity extends Activity {

    LinearLayout layout_galeri;
    String path_gambar;
    String path_tujuan;
    ImageView gambar_full;
    RatingBar ratingbar;
    TextView textview_rating, textview_main;
    SQLiteDatabase db;
    String lokasi_gambar;
    boolean ada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        layout_galeri = (LinearLayout) findViewById(R.id.layout_galeri);
        gambar_full = (ImageView) findViewById(R.id.imageview_full);
        ratingbar = (RatingBar) findViewById(R.id.ratingbar);
        textview_rating = (TextView) findViewById(R.id.textview_rating);
        textview_main = (TextView) findViewById(R.id.textview_main);

        ratingbar.setVisibility(View.GONE);

        path_gambar = Environment.getExternalStorageDirectory().getAbsolutePath();
        path_tujuan = path_gambar + "/Gambar/";

        File file_tujuan = new File(path_tujuan);
        File[] files = file_tujuan.listFiles();
        for(File file : files) {
            layout_galeri.addView(insertPhoto(file.getAbsolutePath()));
        }
        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                textview_rating.setText(ratingBar.getRating() + "");
                if(ada)
                    db.execSQL("UPDATE photo_rater SET rate='"+ratingBar.getRating()+"' WHERE location='"+lokasi_gambar+"';");
                else
                    db.execSQL("INSERT INTO photo_rater VALUES ('"+lokasi_gambar+"','"+ratingBar.getRating()+"');");
                ada = true;
            }
        });

        db=openOrCreateDatabase("DBTest", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS photo_rater(location VARCHAR,rate VARCHAR);");
    }

    private View insertPhoto(final String path) {
        Bitmap bm = decodeContohBitmapDariAlamatPath(path,220,220);

        LinearLayout layout_gambar = new LinearLayout(getApplicationContext());
        layout_gambar.setLayoutParams(new LinearLayout.LayoutParams(220,220));
        layout_gambar.setGravity(Gravity.CENTER);

        final ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new ActionBar.LayoutParams(220,200));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5, 5, 5, 5);
        imageView.setLayoutParams(lp);

        imageView.setImageBitmap(bm);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap gambar = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                gambar_full.setImageBitmap(gambar);
                lokasi_gambar = path;

                ratingbar.setVisibility(View.VISIBLE);

                Cursor c = db.query("photo_rater", new String[] {"location", "rate"}, "location = ?", new String[]{path}, null, null, null, null);
                if(c.getCount()==0) {
                    ada = false;
                    ratingbar.setRating(0);
                    return;
                }
                try{
                    if (c.moveToFirst())
                        ratingbar.setRating(Float.parseFloat(c.getString(1)));
                }finally {
                    c.close();
                }
                ada = true;
            }
        });

        layout_gambar.addView(imageView);

        return layout_gambar;
    }

    private Bitmap decodeContohBitmapDariAlamatPath(String path, int lebar, int tinggi) {
        Bitmap bm = null;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = hitungUkuranSample(options, lebar, tinggi);

        options.inJustDecodeBounds = false;

        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    private int hitungUkuranSample(BitmapFactory.Options options, int lebarDiperlukan, int tinggiDiperlukan) {
        final int lebar = options.outWidth;
        final int tinggi = options.outHeight;
        int sampleResize = 1;

        if (tinggi>tinggiDiperlukan || lebar>lebarDiperlukan)
            sampleResize = Math.round((float)tinggi/(float)tinggiDiperlukan);
        else
            sampleResize = Math.round((float)lebar/(float)lebarDiperlukan);

        return sampleResize;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
