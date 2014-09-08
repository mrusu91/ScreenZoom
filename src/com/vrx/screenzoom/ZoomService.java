package com.vrx.screenzoom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.vrx.screenzoom.Shaker.Callback;

public class ZoomService extends Service implements Callback {
  private Shaker shaker;
  private int mode;
  OutputStreamWriter osw;
  InputStream isw;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    shaker = new Shaker(this, 3.25d, 500, this);
    Process process;
    try {
      process = Runtime.getRuntime().exec("su");
      osw = new OutputStreamWriter(process.getOutputStream());
      isw = process.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    mode = intent.getExtras().getInt("mode");
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    shaker.close();
    if (osw != null)
      try {
        osw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public void shakingStarted() {
    if (isRunning(this))
      return;
    if (osw == null)
      return;
    File shot = new File(getFilesDir().getAbsolutePath(), "shot" + mode);
    File img = new File(getFilesDir().getAbsolutePath(), "tmp.png");
    if (!shot.exists()) {
      System.err.println("Shot file not found! ");
      return;
    }
    try {
      System.out.println(shot.getAbsolutePath() + " " + img.getAbsolutePath()
          + "\n");
      osw.write(shot.getAbsolutePath() + " " + img.getAbsolutePath() + "\n");
      osw.flush();
      Thread.sleep(3000); // give time to finish
      Intent intent = new Intent(this, ImageViewActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void shakingStopped() {
  }

  public boolean isRunning(Context ctx) {
    ActivityManager activityManager = (ActivityManager) ctx
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningTaskInfo> tasks = activityManager
        .getRunningTasks(Integer.MAX_VALUE);
    for (RunningTaskInfo task : tasks) {
      if (task.topActivity.getClassName().equals(
          ImageViewActivity.class.getName()))
        return true;
    }

    return false;
  }
}
