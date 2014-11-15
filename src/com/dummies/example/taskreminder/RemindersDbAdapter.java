package com.dummies.example.taskreminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.ParseException;

import android.content.ContentValues;
import android.content.Context;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class RemindersDbAdapter {
	private static final String DATABASE_NAME="data";
	private static final String DATABASE_TABLE="reminders";
	private static final int DATABASE_VERSION=1;
	static final String KEY_TITLE="title";
	static final String KEY_BODY="body";
	static final String KEY_DATE_TIME="reminder_date_time";
	static final String KEY_ROWID="_id";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private static final String DATABASE_CREATE =
			"create table " + DATABASE_TABLE +"("
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_TITLE + " text not null, "
			+ KEY_BODY +  " text not null, "
			+ KEY_DATE_TIME + " text not null);";
	private final Context mCtx;
	
	public RemindersDbAdapter(Context ctx){
		this.mCtx = ctx;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			try{
			db.execSQL(DATABASE_CREATE);
			}
			catch(ParseException e){
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			//Not used
		}
		
	}
	public void open() throws android.database.sqlite.SQLiteException, java.text.ParseException {
		
		mDbHelper = new DatabaseHelper(mCtx , DATABASE_NAME, null, DATABASE_VERSION);
		
		try{
			mDb = mDbHelper.getWritableDatabase();
		}catch(Exception e){
			mDb=mDbHelper.getReadableDatabase();
		}
		
	}
	public void close() {
		try{
		mDbHelper.close();
		}
		catch(ParseException e){
			e.printStackTrace();
		}
	}
	public long createReminder(String title,String body, String reminderDateTime){
		ContentValues initialValues= new ContentValues();
		
		initialValues.put(KEY_TITLE,title);
		initialValues.put(KEY_BODY,body);
		initialValues.put(KEY_DATE_TIME,reminderDateTime);
		return mDb.insert(DATABASE_TABLE,null,initialValues);
		
		
	}
	public boolean deleteReminder(long rowId){
		
		return mDb.delete(DATABASE_TABLE,KEY_ROWID+"="+rowId,null)>0;
		
		
	}
	public synchronized Cursor fetchAllReminders(){
		
		return mDb.query(DATABASE_TABLE,new String[] {KEY_ROWID, KEY_TITLE, KEY_BODY, KEY_DATE_TIME},null,null,null,null,null);
		
		
		
	}
	
	public synchronized Cursor fetchReminder(long rowId) throws android.database.sqlite.SQLiteException{
		
		Cursor mC = mDb.query(DATABASE_TABLE,new String[] {KEY_ROWID, KEY_TITLE, KEY_BODY, KEY_DATE_TIME},KEY_ROWID+"="+rowId,null,null,null,null);
		if(mC!=null){
			mC.moveToFirst();
		}
		return mC;
		
		
	}
	public int updateReminder(long rowId, String t, String b, String r){
		
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, t);
		args.put(KEY_BODY, b);
		args.put(KEY_DATE_TIME, r);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID+"="+rowId, null);
		
	}
	
	//找到最近的提醒
	public HashMap<Long, Calendar> getLatestReminder(){
		HashMap<Long, Calendar> calMap = new HashMap<Long, Calendar>();
		Cursor csr =fetchAllReminders();
		if(csr!=null){
			csr.moveToFirst();
		
			int rowIdColumnIndex =csr.getColumnIndex(RemindersDbAdapter.KEY_ROWID);
			int dateTimeColumnIndex = csr.getColumnIndex(RemindersDbAdapter.KEY_DATE_TIME);
			
			Calendar latestCal = Calendar.getInstance();
			Long latestRid = null;
			boolean isFirst = true;
			while(csr.isAfterLast()==false){
				Long rid = csr.getLong(rowIdColumnIndex);
				String dateime = csr.getString(dateTimeColumnIndex);
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat dmt = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
				Date date = null;
				try {
					date = dmt.parse(dateime);
					cal.setTime(date);
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
				Calendar calendar = Calendar.getInstance();
		        calendar.setTimeInMillis(System.currentTimeMillis());
				if(cal.after(calendar)){
					
					if(isFirst){
						latestCal = cal;
						latestRid = rid;
						isFirst = false;
					}else if(cal.before(latestCal)){
						latestCal = cal;
						latestRid = rid;
					}
				}
				csr.moveToNext();
			}
			if(csr!=null)  csr.close();
			calMap.put(latestRid, latestCal);
		}
		return calMap;
		
	}
}

