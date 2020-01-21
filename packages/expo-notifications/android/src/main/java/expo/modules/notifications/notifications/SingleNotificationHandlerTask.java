package expo.modules.notifications.notifications;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.unimodules.core.ModuleRegistry;
import org.unimodules.core.Promise;
import org.unimodules.core.arguments.ReadableArguments;
import org.unimodules.core.interfaces.services.EventEmitter;

import java.util.UUID;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import expo.modules.notifications.notifications.interfaces.NotificationBuilder;
import expo.modules.notifications.notifications.interfaces.NotificationMuffler;

/* package */ class SingleNotificationHandlerTask {
  private final static Handler HANDLER = new Handler(Looper.getMainLooper());

  private final static String HANDLE_NOTIFICATION_EVENT_NAME = "onHandleNotification";
  private final static String HANDLE_NOTIFICATION_TIMEOUT_EVENT_NAME = "onHandleNotificationTimeout";

  private final static String NOTIFICATION_DATA_KEY = "notification";

  private final static int SECONDS_TO_TIMEOUT = 3;

  private Context mContext;
  private EventEmitter mEventEmitter;
  private RemoteMessage mRemoteMessage;
  private NotificationsHandler mDelegate;
  private NotificationBuilder mBuilder;
  private NotificationMuffler mMuffler;
  private String mIdentifier;

  private Runnable mTimeoutRunnable = new Runnable() {
    @Override
    public void run() {
      SingleNotificationHandlerTask.this.handleTimeout();
    }
  };

  /* package */ SingleNotificationHandlerTask(Context context, ModuleRegistry moduleRegistry, RemoteMessage remoteMessage, NotificationsHandler delegate) {
    mContext = context;
    mBuilder = moduleRegistry.getModule(NotificationBuilder.class);
    mEventEmitter = moduleRegistry.getModule(EventEmitter.class);
    mMuffler = moduleRegistry.getModule(NotificationMuffler.class);
    mRemoteMessage = remoteMessage;
    mDelegate = delegate;

    mIdentifier = remoteMessage.getMessageId();
    if (mIdentifier == null) {
      mIdentifier = UUID.randomUUID().toString();
    }
  }

  /* package */ String getIdentifier() {
    return mIdentifier;
  }

  /* package */ void start() {
    Bundle eventBody = new Bundle();
    eventBody.putString("id", getIdentifier());
    eventBody.putBundle("notification", RemoteMessageSerializer.toBundle(mRemoteMessage));
    mEventEmitter.emit(HANDLE_NOTIFICATION_EVENT_NAME, eventBody);

    HANDLER.postDelayed(mTimeoutRunnable, SECONDS_TO_TIMEOUT * 1000);
  }

  /* package */ void stop() {
    finish();
  }

  /* package */ void handleResponse(final ReadableArguments behavior, final Promise promise) {
    HANDLER.post(new Runnable() {
      @Override
      public void run() {
        try {
          PushNotificationChannel.ensureIsSetup(mContext);
          Notification notification = getNotification(behavior);
          if (notification == null) {
            throw new NullPointerException();
          }
          NotificationManagerCompat.from(mContext).notify(getIdentifier(), 0, notification);
          // set badge count
          promise.resolve(null);
        } catch (JSONException e) {
          promise.reject("ERR_NOTIFICATION_FORMAT", "Malformed 'notification' object.", e);
        } catch (NullPointerException e) {
          promise.reject(e);
        } finally {
          finish();
        }
      }
    });
  }

  private void handleTimeout() {
    Bundle eventBody = new Bundle();
    eventBody.putString("id", getIdentifier());
    eventBody.putBundle("notification", RemoteMessageSerializer.toBundle(mRemoteMessage));
    mEventEmitter.emit(HANDLE_NOTIFICATION_TIMEOUT_EVENT_NAME, eventBody);

    finish();
  }

  private void finish() {
    HANDLER.removeCallbacks(mTimeoutRunnable);
    mDelegate.onTaskFinished(this);
  }

  private Notification getNotification(ReadableArguments behavior) throws JSONException {
    String notificationSpecString = mRemoteMessage.getData().get(NOTIFICATION_DATA_KEY);
    if (notificationSpecString == null) {
      return null;
    }
    JSONObject notificationSpec = new JSONObject(notificationSpecString);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, PushNotificationChannel.getChannelId());
    builder.setSmallIcon(mContext.getApplicationInfo().icon);
    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
    builder.setVibrate(new long[0]);
    mBuilder.build(builder, notificationSpec);
    mMuffler.muffle(builder, behavior);
    return builder.build();
  }
}
