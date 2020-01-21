package expo.modules.notifications.notifications;

import android.os.Bundle;
import android.provider.Settings;

import org.json.JSONObject;
import org.unimodules.core.interfaces.InternalModule;

import java.util.Collections;
import java.util.List;

import androidx.core.app.NotificationCompat;
import expo.modules.notifications.notifications.interfaces.NotificationBuilder;

public class ExpoNotificationBuilder implements InternalModule, NotificationBuilder {
  private static final String CONTENT_TITLE_KEY = "title";
  private static final String CONTENT_TEXT_KEY = "body";
  private static final String SOUND_KEY = "sound";
  private static final String BADGE_KEY = "badge";

  /* package */ static final String BADGE_EXTRA_KEY = "badge";

  @Override
  public List<? extends Class> getExportedInterfaces() {
    return Collections.singletonList(NotificationBuilder.class);
  }

  @Override
  public NotificationCompat.Builder build(NotificationCompat.Builder builder, JSONObject object) {
    builder.setContentTitle(object.optString(CONTENT_TITLE_KEY));

    builder.setContentText(object.optString(CONTENT_TEXT_KEY));

    if (object.optBoolean(SOUND_KEY)) {
      builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    }

    if (object.has(BADGE_KEY)) {
      Bundle extras = builder.getExtras();
      extras.putInt(BADGE_EXTRA_KEY, object.optInt(BADGE_KEY));
      builder.setExtras(extras);
      builder.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
    }

    return builder;
  }
}
