package expo.modules.notifications.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class PushNotificationChannel {
  private static final String PUSH_NOTIFICATION_CHANNEL_ID = "expo-notifications-push-channel";
  private static final String PUSH_NOTIFICATION_CHANNEL_NAME = "Push notifications";

  public static void ensureIsSetup(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && find(context) != null) {
      create(context);
    }
  }

  public static String getChannelId() {
    return PUSH_NOTIFICATION_CHANNEL_ID;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private static NotificationChannel find(Context context) {
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    return manager.getNotificationChannel(PUSH_NOTIFICATION_CHANNEL_ID);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private static void create(Context context) {
    NotificationChannel channel = new NotificationChannel(PUSH_NOTIFICATION_CHANNEL_ID, PUSH_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
    channel.setShowBadge(true);
    channel.enableVibration(true);
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    manager.createNotificationChannel(channel);
  }
}
