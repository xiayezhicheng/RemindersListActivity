package com.dummies.example.taskreminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class ReminderService extends IntentService {

	public static final String REMINDER_TITLE = "reminder_title";
	public static final String REMINDER_CONTENT = "reminder_content";
	
	public ReminderService() {
		super("ReminderService");

	}

	@Override
	protected void onHandleIntent(Intent i) {

		NotificationManager mgr= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		Intent ni= new Intent(this, ReminderEditActivity.class);
		
		PendingIntent pi = PendingIntent.getActivity(this,0,ni,PendingIntent.FLAG_CANCEL_CURRENT);
		
		int id = i.getIntExtra(RemindersDbAdapter.KEY_ROWID,0);
		String title = i.getStringExtra(REMINDER_TITLE);
		String content = i.getStringExtra(REMINDER_CONTENT);
		
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setTicker(title)
		.setContentTitle(title)
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(content))
		.setContentText(content)
		.setWhen(System.currentTimeMillis())
		.setAutoCancel(true)
		.setDefaults(Notification.DEFAULT_ALL);
		
		mBuilder.setContentIntent(pi);
		mgr.notify(id,mBuilder.build());
		
		OnAlarmReceiver.completeWakefulIntent(i);
		// Release the wake lock provided by the BroadcastReceiver.
		StaticWakeLock.lockOff(this);
	}
}
