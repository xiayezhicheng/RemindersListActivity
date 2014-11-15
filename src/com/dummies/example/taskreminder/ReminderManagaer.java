package com.dummies.example.taskreminder;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderManagaer{

	private Context C;
	private AlarmManager AM;
	
	public ReminderManagaer(Context cxt){
	
		C=cxt;
		AM=(AlarmManager)cxt.getSystemService(Context.ALARM_SERVICE);
	}
	
	@SuppressLint("NewApi")
	public void setReminder(Long taskId, Calendar when){
		if(taskId!=null){
			
			Intent i = new Intent(C,OnAlarmReceiver.class);
			i.putExtra(RemindersDbAdapter.KEY_ROWID, (long)taskId);
			
			PendingIntent pi = PendingIntent.getBroadcast(C,0,i,PendingIntent.FLAG_CANCEL_CURRENT);
			
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){

				AM.setExact(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(),pi);
			}else{
				
				AM.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(),pi);
			}
		}
	}
}