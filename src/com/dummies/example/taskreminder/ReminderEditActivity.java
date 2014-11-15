package com.dummies.example.taskreminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.DatePicker;
import android.widget.Toast;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

@SuppressLint("SimpleDateFormat")
public class ReminderEditActivity extends Activity {

	private Button mDateButton;
	private EditText mTitleText;
	private EditText mBodyText;
	private Button mTimeButton;
	private Button Save;
	RemindersDbAdapter mDbHelper;
	ReminderManagaer rmr;
	private Calendar mCalender;
	private Long mRId;
	private static final int DATE_PICKER_DIALOG=0;
	private static final int TIME_PICKER_DIALOG=1;
	private static final String DATE_FORMAT="yyyy-MM-dd";
	private static final String TIME_FORMAT="HH:mm";
	public static final String DATE_TIME_FORMAT="yyyy-MM-dd HH:mm:ss";
		
		@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reminder_edit);
		
		mDbHelper = new RemindersDbAdapter(getApplicationContext());
		rmr = new ReminderManagaer(getApplicationContext());
		
		setContentView(R.layout.reminder_edit);
		mDateButton=(Button)findViewById(R.id.reminder_date);
		mTimeButton=(Button)findViewById(R.id.reminder_time);
		Save=(Button)findViewById(R.id.confirm);
		
		mCalender =Calendar.getInstance();
		mCalender.setTimeInMillis(System.currentTimeMillis());
		Log.w("Testing testing", (new Date(mCalender.getTimeInMillis())).toString());
		
		mTitleText=(EditText)findViewById(R.id.title);
		mBodyText=(EditText)findViewById(R.id.body);
		mRId= savedInstanceState!=null ? savedInstanceState.getLong(RemindersDbAdapter.KEY_ROWID) : null;
		registerButtonListenersAndSetDefaultText();
			
		}
	
		private void setRowIdFromIntent(){
			if(mRId == null){
				Bundle extras = getIntent().getExtras();
				mRId = extras!=null ? extras.getLong(RemindersDbAdapter.KEY_ROWID):null;
						
			}
		}
		
		public void saveState() {
			String title= mTitleText.getText().toString().trim();
			String body = mBodyText.getText().toString().trim();
			
			try{
				Date date = new Date(mCalender.getTimeInMillis());
				SimpleDateFormat sd = new SimpleDateFormat(DATE_TIME_FORMAT);
				String reminderDateTime = sd.format(date);
				if(mRId==null)
				{
					long id= mDbHelper.createReminder(title,body,reminderDateTime);
					if(id>0)	mRId=id;
				}else{
					
					mDbHelper.updateReminder(mRId,title, body, reminderDateTime);
				}
					
			}catch(Exception e){
				e.printStackTrace();
			}
			//其实每次返回的map都只有一个键值对
			HashMap<Long, Calendar>  map = mDbHelper.getLatestReminder();
			if(map!=null){
				for(Long key:map.keySet()){
					rmr.setReminder(key, map.get(key));
					Log.i("reminder", "setReminder-----"+key);
				}
			}
		}
			
	
	private void registerButtonListenersAndSetDefaultText() {
		// TODO Auto-generated method stub
		mDateButton.setOnClickListener(new View.OnClickListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
			showDialog(DATE_PICKER_DIALOG);	
			}
		});
		mTimeButton.setOnClickListener(new View.OnClickListener() {
			
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			showDialog(TIME_PICKER_DIALOG);	
			}
		});
		
//		updateDateButtonText();
//		updateTimeButtonText();
		
		Save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ReminderEditActivity.this);
				builder.setMessage("Are you sure you want to save the task?")
				.setTitle("Are you sure")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener(){

						public void onClick(DialogInterface dialog,int id){
														
							saveState();
							setResult(RESULT_OK);
							Toast.makeText(ReminderEditActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
							finish();
						}
					}).setNegativeButton("No",new DialogInterface.OnClickListener() {
													
						public void onClick(DialogInterface dialog,int id){
														
							dialog.cancel();
						}
					});
												
				builder.create().show();
			}
		});
	}
	
	protected Dialog onCreateDialog(int id){
		switch(id){
		case DATE_PICKER_DIALOG:
			return showDatePicker();
		case TIME_PICKER_DIALOG:
			return showTimePicker();
		}
		return null;
	}
	
	private DatePickerDialog showDatePicker(){
		DatePickerDialog dP = new DatePickerDialog(ReminderEditActivity.this,new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int mon, int day) {
				// TODO Auto-generated method stub
				mCalender.set(Calendar.YEAR,year);
				mCalender.set(Calendar.MONTH,mon);
				mCalender.set(Calendar.DAY_OF_MONTH,day);
				
				updateDateButtonText();
			}
		},mCalender.get(Calendar.YEAR),mCalender.get(Calendar.MONTH),mCalender.get(Calendar.DAY_OF_MONTH));
		return dP;
	}
	
	private TimePickerDialog showTimePicker(){
		TimePickerDialog tP = new TimePickerDialog(this,new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hour, int min) {
				// TODO Auto-generated method stub
				mCalender.set(Calendar.HOUR_OF_DAY,hour);
				mCalender.set(Calendar.MINUTE,min);
				mCalender.set(Calendar.SECOND, 0);
				
				updateTimeButtonText();
			}
		},mCalender.get(Calendar.HOUR_OF_DAY),mCalender.get(Calendar.MINUTE),true);
		return tP;
	}
	private void updateDateButtonText(){
		//SimpleDateFormat dF=new SimpleDateFormat(DATE_FORMAT);
		//String dateForButton=(String) dF.format(mCalender.getTime());
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		String dateForButton = dateFormat.format(new Date(mCalender.getTimeInMillis()));
		mDateButton.setText(dateForButton);	
	}
	
	private void updateTimeButtonText(){

		SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
		String timeForButton = dateFormat.format(new Date(mCalender.getTimeInMillis()));
		mTimeButton.setText(timeForButton);	
	}

	
	private void populateFields() throws java.text.ParseException {
		if(mRId!=null){
			Cursor cr = mDbHelper.fetchReminder(mRId);
			mTitleText.setText(cr.getString(cr.getColumnIndexOrThrow(RemindersDbAdapter.KEY_TITLE)));
			mBodyText.setText(cr.getString(cr.getColumnIndexOrThrow(RemindersDbAdapter.KEY_BODY)));
			
			
			//SimpleDateFormat dtf= new SimpleDateFormat(DATE_TIME_FORMAT);
			Date date=null;
			try{
				String ds = cr.getString(cr.getColumnIndexOrThrow(RemindersDbAdapter.KEY_DATE_TIME));
				date= getString(ds);
				
				mCalender.setTime(date);
			} catch(Exception e)
			{
				Log.e("ReminderEditActivity",e.getMessage(),e);
				e.printStackTrace();
			}
		
		}
		updateDateButtonText();
		updateTimeButtonText();
			
	}
	
	@Override 
	protected void onSaveInstanceState( Bundle outstate){
		super.onSaveInstanceState(outstate);
		if(mRId!=null){
			outstate.putLong(RemindersDbAdapter.KEY_ROWID, mRId);
		}
	}
	
	private Date getString(String datestring) throws java.text.ParseException{
	    SimpleDateFormat sd = new SimpleDateFormat(DATE_TIME_FORMAT);
	    sd.setTimeZone(TimeZone.getDefault());
	    return (Date)sd.parse(datestring);
	    
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		try{
		mDbHelper.close();
		}
		catch(ParseException e){
			e.printStackTrace();
		}
	}
	@Override
	protected void onResume(){
		super.onResume();
		try{
			try {
				mDbHelper.open();
			} catch (SQLiteException e) {
				e.printStackTrace();
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}catch(ParseException e){
			e.printStackTrace();
		}
		
		setRowIdFromIntent();
		
		try {
			populateFields();
		}catch (java.text.ParseException e) {
			e.printStackTrace();
		}
	}
	
}

