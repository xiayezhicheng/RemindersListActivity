package com.dummies.example.taskreminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class OnAlarmReceiver extends WakefulBroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		
		long rid = intent.getExtras().getLong(RemindersDbAdapter.KEY_ROWID);
		Log.i("reminder", "receiver------"+String.valueOf(rid));
		ReminderManagaer rmgr= new ReminderManagaer(context);
		RemindersDbAdapter db= new RemindersDbAdapter(context);
		
		try {
			db.open();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		HashMap<Long, Calendar>  map = db.getLatestReminder();
		if(map!=null){
			for(Long key:map.keySet()){
				rmgr.setReminder(key, map.get(key));
			}
		}
		
		Cursor cursor = db.fetchReminder(rid);
		String title = cursor.getString(cursor.getColumnIndex(RemindersDbAdapter.KEY_TITLE));
		String content = cursor.getString(cursor.getColumnIndex(RemindersDbAdapter.KEY_BODY));
		
		//如果时间没到，就不让它发送通知，AlarmManager管理的闹钟会有时间差
		String time = cursor.getString(cursor.getColumnIndex(RemindersDbAdapter.KEY_DATE_TIME));
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dmt = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
		Date date = null;
		try {
			date = dmt.parse(time);
			cal.setTime(date);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //现在的时间在设置的时间之后，这是合理的
		if(calendar.after(cal)){
		
			Intent in = new Intent (context, ReminderService.class);
			
			in.putExtra(RemindersDbAdapter.KEY_ROWID, (int)rid);
			in.putExtra(ReminderService.REMINDER_TITLE, title);
			in.putExtra(ReminderService.REMINDER_CONTENT, content);
			// Start the service, keeping the device awake while it is launching.
			startWakefulService(context, in);
			
			StaticWakeLock.lockOn(context);
		}
		
		db.close();
	}
}