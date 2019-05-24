package park.haneol.project.logger.component;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import park.haneol.project.logger.BuildConfig;
import park.haneol.project.logger.R;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.util.ActivityObserver;
import park.haneol.project.logger.util.Database;

public class ShareActivity extends Activity {

    private static String CHANNEL_ID = BuildConfig.APPLICATION_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && ClipDescription.MIMETYPE_TEXT_PLAIN.equals(type)) {

            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (text != null) {

                Database database = new Database(this);
                LogItem item = database.insert(text);

                MainActivity activity = ActivityObserver.getInstance().getActivity();
                if (activity != null) {
                    activity.onTextShared(item);
                } else {
                    setTextNotification(item, text);
                    Toast.makeText(this, getString(R.string.get_share_message), Toast.LENGTH_SHORT).show();
                }

            }
        }

        finish();
    }

    private void setTextNotification(LogItem item, String text) {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra("id", item.getId());
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_offline_pin_white_24dp)
                .setContentTitle(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setWhen(item.getTime() * 60000L)
                .setAutoCancel(true)
                .setShowWhen(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(item.getId(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            CharSequence name = getString(R.string.app_name);
            //String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            //channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
