package expo.modules.notifications.notifications.interfaces;

import android.app.Notification;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;

public interface NotificationBuilder {
  NotificationBuilder setRemoteMessage(RemoteMessage remoteMessage) throws JSONException;

  NotificationBuilder setAllowedBehavior(NotificationBehavior behavior);

  Notification build();
}
