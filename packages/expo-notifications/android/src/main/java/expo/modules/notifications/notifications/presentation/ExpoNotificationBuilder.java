package expo.modules.notifications.notifications.presentation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.unimodules.core.ModuleRegistry;

import androidx.core.app.NotificationCompat;
import expo.modules.notifications.notifications.interfaces.NotificationBehavior;
import expo.modules.notifications.notifications.interfaces.NotificationBuilder;
import expo.modules.notifications.notifications.interfaces.NotificationChannelsManager;
import expo.modules.notifications.notifications.presentation.effectors.BadgeSettingNotificationEffector;
import expo.modules.notifications.notifications.presentation.effectors.SoundPlayingNotificationEffector;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ExpoNotificationBuilder implements NotificationBuilder {
  private static final String REMOTE_MESSAGE_DATA_NOTIFICATION_KEY = "notification";

  private static final String CONTENT_TITLE_KEY = "title";
  private static final String CONTENT_TEXT_KEY = "body";
  private static final String SOUND_KEY = "sound";
  private static final String BADGE_KEY = "badge";

  private static final long[] NO_VIBRATE_PATTERN = new long[]{0, 0};

  private final Context mContext;
  private final ModuleRegistry mModuleRegistry;

  private JSONObject mNotificationRequest;
  private NotificationBehavior mAllowedBehavior;
  private int mSmallIcon;

  public ExpoNotificationBuilder(Context context, ModuleRegistry moduleRegistry) {
    mContext = context;
    mModuleRegistry = moduleRegistry;
    mSmallIcon = context.getApplicationInfo().icon;
  }

  @Override
  public ExpoNotificationBuilder setRemoteMessage(RemoteMessage remoteMessage) throws JSONException {
    String notificationRequestString = remoteMessage.getData().get(REMOTE_MESSAGE_DATA_NOTIFICATION_KEY);
    mNotificationRequest = new JSONObject(notificationRequestString);
    return this;
  }

  @Override
  public NotificationBuilder setAllowedBehavior(NotificationBehavior behavior) {
    mAllowedBehavior = behavior;
    return this;
  }

  protected NotificationCompat.Builder createBuilder() {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, getChannelId());

    // If the notification has no user-facing content
    // we want it not to be shown - without a small icon it will fail to be presented.
    if (hasUserFacingContent()) {
      builder.setSmallIcon(mSmallIcon);
    }

    if (mNotificationRequest.has(CONTENT_TITLE_KEY)) {
      builder.setContentTitle(mNotificationRequest.optString(CONTENT_TITLE_KEY));
    }
    if (mNotificationRequest.has(CONTENT_TEXT_KEY)) {
      builder.setContentText(mNotificationRequest.optString(CONTENT_TEXT_KEY));
    }

    if (shouldShowAlert()) {
      // Display as a heads-up notification
      builder.setPriority(NotificationCompat.PRIORITY_HIGH);
    } else {
      // Do not display as a heads-up notification, but show in the notification tray
      builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    if (shouldPlaySound()) {
      builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
      builder.setDefaults(NotificationCompat.DEFAULT_ALL); // sets default vibration too
    } else {
      builder.setSound(null);
      builder.setDefaults(0);
      builder.setVibrate(NO_VIBRATE_PATTERN);
    }

    if (!hasUserFacingContent() && shouldPlaySound()) {
      // If the notification has no user-facing content, it will fail to be presented.
      // If it should play sound, we still want to play the sound. An effector will do it for us.
      Bundle extras = builder.getExtras();
      extras.putParcelable(SoundPlayingNotificationEffector.EXTRAS_SOUND_URI_KEY, Settings.System.DEFAULT_NOTIFICATION_URI);
      builder.setExtras(extras);
    }

    if (shouldSetBadge()) {
      Bundle extras = builder.getExtras();
      extras.putInt(BadgeSettingNotificationEffector.EXTRAS_BADGE_KEY, getBadgeCount());
      builder.setExtras(extras);
    }

    return builder;
  }

  @Override
  public Notification build() {
    return createBuilder().build();
  }

  protected String getChannelId() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Returning null on incompatible platforms won't be an error.
      return null;
    }

    NotificationChannelsManager channelsManager = mModuleRegistry.getSingletonModule("NotificationChannelsManager", NotificationChannelsManager.class);
    if (channelsManager == null) {
      // We need a channel ID, but we can't access the provider. Let's use system-provided one as a fallback.
      Log.w("ExpoNotificationBuilder", "Using `NotificationChannel.DEFAULT_CHANNEL_ID` as channel ID for push notification. " +
          "Please provide a NotificationChannelsManager to provide builder with a fallback channel ID.");
      return NotificationChannel.DEFAULT_CHANNEL_ID;
    }

    return channelsManager.getFallbackNotificationChannel().getId();
  }

  private boolean hasUserFacingContent() {
    return mNotificationRequest.has(CONTENT_TITLE_KEY) || mNotificationRequest.has(CONTENT_TEXT_KEY);
  }

  private int getBadgeCount() {
    return mNotificationRequest.optInt(BADGE_KEY);
  }

  private boolean shouldShowAlert() {
    return (mAllowedBehavior == null || mAllowedBehavior.shouldShowAlert()) && hasUserFacingContent();
  }

  private boolean shouldPlaySound() {
    return (mAllowedBehavior == null || mAllowedBehavior.shouldPlaySound()) && mNotificationRequest.optBoolean(SOUND_KEY);
  }

  private boolean shouldSetBadge() {
    return (mAllowedBehavior == null || mAllowedBehavior.shouldSetBadge()) && mNotificationRequest.has(BADGE_KEY);
  }
}
