package expo.modules.notifications.notifications.interfaces;

import org.unimodules.core.arguments.ReadableArguments;

import androidx.core.app.NotificationCompat;

public interface NotificationMuffler {
  NotificationCompat.Builder muffle(NotificationCompat.Builder builder, ReadableArguments behavior);
}
