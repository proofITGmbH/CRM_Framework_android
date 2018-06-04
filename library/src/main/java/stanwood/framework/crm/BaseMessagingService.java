package stanwood.framework.crm;


import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import stanwood.framework.crm.utils.UrlUtils;

/**
 * Class responsible for receiving push notifications, extracting the data and notifying @{@link BaseNotificationHandler}
 */
public abstract class BaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "BaseMessagingService";
    private static final String IN_APP_TYPE = "inapp";

    @NonNull
    public abstract BaseNotificationHandler createBaseNotificationHandler();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        BaseNotificationHandler instance = createBaseNotificationHandler();

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String title = remoteMessage.getData().get(this.getString(R.string.title_key));
            String message = remoteMessage.getData().get(this.getString(R.string.message_key));
            String bigMessage = remoteMessage.getData().get(this.getString(R.string.big_message_key));
            String link = remoteMessage.getData().get(this.getString(R.string.deeplink_key));
            String messageIdText = remoteMessage.getData().get(this.getString(R.string.messageId_key));
            String messageType = remoteMessage.getData().get(this.getString(R.string.message_type_key));
            String targetScreen = remoteMessage.getData().get(this.getString(R.string.target_screen));


            if (!TextUtils.isEmpty(messageType) && IN_APP_TYPE.equalsIgnoreCase(messageType)) {
                //Handle inapp messages
                if (TextUtils.isEmpty(link)) {
                    throw new IllegalArgumentException("Link can not be empty for inapp messages. Remember to override link_key from crm.xml");
                }

                if (TextUtils.isEmpty(targetScreen)) {
                    throw new IllegalArgumentException("targetScreen can not be empty for inapp messages. Remember to override target_screen from crm.xml");
                }
                Crm.getInstance().saveInApp(targetScreen, link);

            } else {
                //Handle Notification part
                int messageId = TextUtils.isEmpty(messageIdText) ? 1 : Integer.valueOf(messageIdText);
                if (TextUtils.isEmpty(title)) {
                    throw new IllegalArgumentException("Title can not be empty. Remember to override title_key from crm.xml");
                }

                if (TextUtils.isEmpty(message)) {
                    throw new IllegalArgumentException("Message can not be empty. Remember to override message_key from crm.xml");
                }

                Map<String, String> params = UrlUtils.getQueryMap(link);
                instance.setNotificationData(title, message, bigMessage, params, messageId);
                instance.createNotification();
            }
        } else if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Map<String, String> params = null;

            if (notification.getLink() != null) {
                params = UrlUtils.getQueryMap(notification.getLink().toString());
            }

            instance.setNotificationData(notification.getTitle(), notification.getBody(), null, params, 1);
            instance.createNotification();
        }
    }
}
