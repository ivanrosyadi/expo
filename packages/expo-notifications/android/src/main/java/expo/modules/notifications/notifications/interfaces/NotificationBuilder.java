package expo.modules.notifications.notifications.interfaces;

import org.json.JSONObject;

import androidx.core.app.NotificationCompat;

public interface NotificationBuilder {
  NotificationCompat.Builder build(NotificationCompat.Builder builder, JSONObject object);
}
