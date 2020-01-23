package expo.modules.notifications.notifications.channels;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import org.unimodules.core.interfaces.SingletonModule;

import androidx.annotation.RequiresApi;
import expo.modules.notifications.R;
import expo.modules.notifications.notifications.interfaces.NotificationChannelsManager;

public class ExpoNotificationChannelsManager implements SingletonModule, NotificationChannelsManager {
  private final static String SINGLETON_NAME = "NotificationChannelsManager";

  private final static String FALLBACK_CHANNEL_ID = "expo_notifications_fallback_notification_channel";

  @RequiresApi(api = Build.VERSION_CODES.N)
  private final static int FALLBACK_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

  private Context mContext;

  public ExpoNotificationChannelsManager(Context context) {
    mContext = context;
  }

  @Override
  public String getName() {
    return SINGLETON_NAME;
  }

  @Override
  public NotificationChannel getFallbackNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      return null;
    }

    NotificationChannel channel = getNotificationManager().getNotificationChannel(FALLBACK_CHANNEL_ID);
    if (channel != null) {
      return channel;
    }

    return createFallbackChannel();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  protected NotificationChannel createFallbackChannel() {
    NotificationChannel channel = new NotificationChannel(FALLBACK_CHANNEL_ID, getFallbackChannelName(), FALLBACK_CHANNEL_IMPORTANCE);
    channel.setShowBadge(true);
    channel.enableVibration(true);
    getNotificationManager().createNotificationChannel(channel);
    return channel;
  }

  protected String getFallbackChannelName() {
    return mContext.getString(R.string.fallback_channel_name);
  }

  private NotificationManager getNotificationManager() {
    return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
  }
}
