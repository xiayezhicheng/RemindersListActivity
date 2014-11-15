package com.dummies.example.taskreminder;

import java.util.Calendar;
import java.util.HashMap;

import org.apache.http.ParseException;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class RemindersListActivity extends ListActivity {

	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;
	
	private RemindersDbAdapter rdb;
	ReminderManagaer rmgr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) throws android.database.sqlite.SQLiteException {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminders_list);
		
		rmgr= new ReminderManagaer(getApplicationContext());
		try{
			
			rdb = new RemindersDbAdapter(getApplicationContext());
			rdb.open();
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		fillData();
		registerForContextMenu(getListView());
	}
	
	@SuppressWarnings("deprecation")
	private void fillData() {
		
		Cursor c = rdb.fetchAllReminders();
		startManagingCursor(c);
		//create array to specify fields we want(title)
		String [] from = new String[]{RemindersDbAdapter.KEY_TITLE};
		
		//and array of fields we want to bind in the view
		int [] to = new int[]{R.id.text1};
		
		//now create cursor and set it to display
		SimpleCursorAdapter r= new SimpleCursorAdapter(this,R.layout.reminder_row,c,from,to);
		setListAdapter(r);
		
	}
	protected void onListItemClick(ListView l,View v,int position, long id){
		super.onListItemClick(l,v,position,id);
		Intent i=new Intent(this,ReminderEditActivity.class);
		i.putExtra(RemindersDbAdapter.KEY_ROWID, id);
		startActivityForResult(i,ACTIVITY_EDIT);

	}
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi=getMenuInflater();
		mi.inflate(R.menu.list_menu_item_longpress, menu);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater mi=getMenuInflater();
		mi.inflate(R.menu.list_menu, menu);
		return true;
	}
	@Override
	public boolean onMenuItemSelected(int fId, MenuItem item){
		switch(item.getItemId()){
		case R.id.menu_insert:
			createReminder();
			return true;
		}
		return super.onMenuItemSelected(fId, item);
	}
	
	private void createReminder(){
		Intent i = new Intent(this,ReminderEditActivity.class);
		startActivityForResult(i,ACTIVITY_CREATE);
	}
	@Override
	public boolean onContextItemSelected( MenuItem item) throws android.database.sqlite.SQLiteException{
		switch(item.getItemId()){
		case R.id.menu_delete:
			
			final AdapterContextMenuInfo info= (AdapterContextMenuInfo) item.getMenuInfo();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(RemindersListActivity.this);
			builder.setMessage("Are you sure you want to Delete the task?")
				.setTitle("Are you sure")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog,int id){
						
						try{
							rdb.deleteReminder(info.id);
							fillData();
							//重新设置提醒
							HashMap<Long, Calendar>  map = rdb.getLatestReminder();
							if(map!=null){
								for(Long key:map.keySet()){
									rmgr.setReminder(key, map.get(key));
								}
							}
							
						}catch(ParseException e){
							e.printStackTrace();
						}
					}
			}).setNegativeButton("No",new DialogInterface.OnClickListener() {
												
					public void onClick(DialogInterface dialog,int id){

						dialog.cancel();
					}
			});
											
			builder.create().show();
			
			return true;
		}
		return super.onContextItemSelected(item);
	}
	@Override
	protected void onActivityResult(int reqCode,int resCode,Intent i){
		super.onActivityResult(reqCode, resCode, i);
		fillData();
	}
	
	@Override
	protected void onDestroy() {
		if(rdb!=null){
			rdb.close();
		}
		super.onDestroy();
	}
	
}
