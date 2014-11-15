package com.dummies.example.taskreminder;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;

public class OnBootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		
		ReminderManagaer rmgr= new ReminderManagaer(context);
		RemindersDbAdapter db= new RemindersDbAdapter(context);
		try {
			db.open();
			HashMap<Long, Calendar>  map = db.getLatestReminder();
			if(map!=null){
				for(Long key:map.keySet()){
					rmgr.setReminder(key, map.get(key));
				}
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		db.close();
	}
}



