package com.vrx.screenzoom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends Activity implements OnClickListener {

  Button btnStart, btnStop;
  RadioGroup rgRoms;
  RadioButton rbStock, rbCustom;
  SharedPreferences prefs;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Root root = new Root();
    if (!root.isDeviceRooted()) {
      AlertDialog dialog = new AlertDialog.Builder(this)
          .setMessage(R.string.no_root)
          .setPositiveButton(R.string.ok,
              new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                  finish();
                }
              }).create();
      dialog.setCancelable(false);
      dialog.setCanceledOnTouchOutside(false);
      dialog.show();
    }
    prefs = getSharedPreferences("settings", MODE_PRIVATE);
    if (!prefs.getBoolean("ready", false)) {
      new InstallTask().execute();
    } else {
      initLayout();
    }
  }

  private void initLayout() {
    setContentView(R.layout.activity_main);
    btnStart = (Button) findViewById(R.id.btn_start);
    btnStop = (Button) findViewById(R.id.btn_stop);
    rgRoms = (RadioGroup) findViewById(R.id.rg_rom);
    rbStock = (RadioButton) findViewById(R.id.rb_stock);
    rbCustom = (RadioButton) findViewById(R.id.rb_custom);

    btnStart.setOnClickListener(this);
    btnStop.setOnClickListener(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (prefs.getBoolean("ready", false)) {
      int mode = prefs.getInt("mode", 0);
      setUpControls(mode);
    }
  }

  private void setUpControls(int mode) {
    if (mode == 0) {
      btnStop.setVisibility(View.GONE);
      rbStock.setChecked(true);
      btnStart.setVisibility(View.VISIBLE);
    } else {
      btnStart.setVisibility(View.GONE);
      btnStop.setVisibility(View.VISIBLE);
      if (mode == 1)
        rbStock.setChecked(true);
      else
        rbCustom.setChecked(true);
    }
  }

  private void startZoomService(int mode) {
    Intent intent = new Intent(this, ZoomService.class);
    intent.putExtra("mode", mode);
    startService(intent);
  }

  private void stopZoomService() {
    stopService(new Intent(this, ZoomService.class));
  }

  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.btn_start:
      int rbId = rgRoms.getCheckedRadioButtonId();
      int mode = (rbId == R.id.rb_stock) ? 1 : 2;
      prefs.edit().putInt("mode", mode).commit();
      setUpControls(mode);
      startZoomService(mode);
      break;
    case R.id.btn_stop:
      prefs.edit().putInt("mode", 0).commit();
      setUpControls(0);
      stopZoomService();
      break;
    default:
      break;
    }
  }

  private class InstallTask extends AsyncTask<Void, Void, Boolean> {
    ProgressDialog progress;

    @Override
    protected void onPreExecute() {
      progress = new ProgressDialog(MainActivity.this);
      progress.setCancelable(false);
      progress.setCanceledOnTouchOutside(false);
      progress.setMessage(getResources().getString(R.string.installing));
      progress.show();
      super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      String appDirectory = getFilesDir().getAbsolutePath();
      AssetManager assetManager = getAssets();
      InputStream in;
      OutputStream out;
      OutputStreamWriter rootedOut;
      try {
        Process process = Runtime.getRuntime().exec("su");
        rootedOut = new OutputStreamWriter(process.getOutputStream());
        String[] files = assetManager.list("shots");
        for (String file : files) {
          in = assetManager.open("shots/" + file);
          File outpuFile = new File(appDirectory, file);
          out = new FileOutputStream(outpuFile);
          byte[] buffer = new byte[1024];
          int read;
          while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
          }
          in.close();
          out.flush();
          out.close();
          makeExecutable(rootedOut, outpuFile);
        }
        rootedOut.flush();
        rootedOut.close();
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return false;
    }

    private void makeExecutable(OutputStreamWriter rootedOut, File file)
        throws IOException, InterruptedException {
      rootedOut.write("/system/bin/chmod 744 " + file.getAbsolutePath() + "\n");
    }

    @Override
    protected void onPostExecute(Boolean result) {
      progress.dismiss();
      if (result) {
        prefs.edit().putBoolean("ready", true).commit();
        initLayout();
      } else {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
            .setMessage(R.string.install_error)
            .setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                  public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                  }
                }).create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
      }
      super.onPostExecute(result);
    }
  }
}