package com.sit.adefy;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class AdefyReminder extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    if(intent.getStringExtra("title") != null && intent.getStringExtra("desc") != null && intent.getStringExtra("url") != null) {

      // Show notification...
      NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
          .setSmallIcon(R.drawable.ic_launcher)
          .setContentTitle(intent.getStringExtra("title"))
          .setContentText(intent.getStringExtra("desc"));

      if(intent.getStringExtra("icon") != null) {
        Bitmap bitmap = BitmapFactory.decodeFile(intent.getStringExtra("icon"));
        mBuilder.setLargeIcon(bitmap);
      }

      Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("url")));
      PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);

      mBuilder.setContentIntent(contentIntent);
      mBuilder.setOnlyAlertOnce(true);
      mBuilder.setVibrate(new long[] {200, 300, 200, 300, 200, 300, 200, 600});

      NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      mNotificationManager.notify(0, mBuilder.build());
    }
  }
}
