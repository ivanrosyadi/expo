package expo.modules.notifications.notifications;

import android.os.Bundle;

import org.unimodules.core.arguments.ReadableArguments;
import org.unimodules.core.interfaces.InternalModule;

import java.util.Collections;
import java.util.List;

import androidx.core.app.NotificationCompat;
import expo.modules.notifications.notifications.interfaces.NotificationMuffler;

import static expo.modules.notifications.notifications.ExpoNotificationBuilder.BADGE_EXTRA_KEY;

public class ExpoNotificationMuffler implements InternalModule, NotificationMuffler {
  private static final String SHOULD_SHOW_ALERT_KEY = "shouldShowAlert";
  private static final String SHOULD_PLAY_SOUND_KEY = "shouldPlaySound";
  private static final String SHOULD_SET_BADGE_KEY = "shouldSetBadge";

  @Override
  public List<? extends Class> getExportedInterfaces() {
    return Collections.singletonList(NotificationMuffler.class);
  }

  @Override
  public NotificationCompat.Builder muffle(NotificationCompat.Builder builder, ReadableArguments behavior) {
    if (!behavior.getBoolean(SHOULD_PLAY_SOUND_KEY)) {
      builder.setSound(null);
    }

    if (!behavior.getBoolean(SHOULD_SHOW_ALERT_KEY)) {
      builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    if (!behavior.getBoolean(SHOULD_SET_BADGE_KEY)) {
      Bundle extras = builder.getExtras();
      extras.remove(BADGE_EXTRA_KEY);
      builder.setExtras(extras);
    }

    return builder;
  }
}
