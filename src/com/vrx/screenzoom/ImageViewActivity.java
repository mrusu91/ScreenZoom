package com.vrx.screenzoom;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.polites.android.GestureImageView;

public class ImageViewActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    File img = new File(getFilesDir().getAbsolutePath(), "tmp.png");
    if (!img.exists())
      finish();
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bitmap = BitmapFactory.decodeFile(img.getAbsolutePath(), options);
    GestureImageView view = new GestureImageView(this);
    view.setImageBitmap(bitmap);
    view.setMaxScale(10.0f);
    setContentView(view);
  }
}