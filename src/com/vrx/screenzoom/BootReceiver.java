package com.vrx.screenzoom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    int mode = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getInt("mode", 0);
    if (mode != 0) {
      Intent myIntent = new Intent(context, ZoomService.class);
      intent.putExtra("mode", mode);
      context.startService(myIntent);
    }
  }
}
