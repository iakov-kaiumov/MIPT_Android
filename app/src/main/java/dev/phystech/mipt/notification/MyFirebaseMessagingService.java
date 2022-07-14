package dev.phystech.mipt.notification;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Objects;

import dev.phystech.mipt.R;
import dev.phystech.mipt.ui.activities.SplashActivity;
import dev.phystech.mipt.utils.ChatUtils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static String ACTION_MESSAGE = "dev.phystech.mipt.MESSAGE";
    public static String EXT_CONV_ID = "EXT_CONV_ID";
    public static String EXT_EXTERNAL_URL = "EXT_EXTERNAL_URL";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if(isAppIsInBackground(getApplicationContext())) {

            String title = "";
            String text = "";

            String convid = "";
            String externalUrl = "";

            if (!remoteMessage.getData().isEmpty()) {
                title = remoteMessage.getData().get("title");
                text = remoteMessage.getData().get("body");

                convid = remoteMessage.getData().get("convid");
                externalUrl = remoteMessage.getData().get("externalUrl");
            }

            if (title == null || title.equals("")) title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
            if (text == null || text.equals("")) text = Objects.requireNonNull(remoteMessage.getNotification()).getBody();

            Intent intent = new  Intent(this, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .setAction(ACTION_MESSAGE)
                    .putExtra(EXT_CONV_ID, convid)
                    .putExtra(EXT_EXTERNAL_URL, externalUrl);
//                .setAction(Intent.ACTION_VIEW);

//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            final String CHANNEL_ID = "dev.phystech.mipt.ANDROID";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Heads Up Notification", NotificationManager.IMPORTANCE_HIGH);
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_app_icon))
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat.from(this).notify(0, notification.build());
        }

        super.onMessageReceived(remoteMessage);
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        }
        else
        {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
}
