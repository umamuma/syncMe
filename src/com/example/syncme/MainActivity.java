package com.example.syncme;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.syncme.Util.ConnectionEvent;
import com.example.syncme.Util.MessageType;
import com.example.syncme.Util.TimeEntry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	//SectionsPagerAdapter mSectionsPagerAdapter;

	@Override
	protected void onStop() {
		saveData();
		super.onStop();
	}

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	

	//ViewPager mViewPager;
	Client mClient;
	Server mServer;
	List<String> mText = new ArrayList<String>();
	SparseIntArray mConnectedIDs = new SparseIntArray();
	private boolean isRunning = false;
	private Chronometer chronometer;
	
	Handler mHandler = new Handler(Looper.getMainLooper())
	 {
			@Override
		    public void handleMessage(Message inputMessage) {
								
				Boolean bool;
				Button b;
				switch (inputMessage.what) {
                // The decoding is done
                case MessageType.TIME_ENTRY_RECEIVED:
                	TimeEntry te = (TimeEntry) inputMessage.obj;
                	Print("UI: got " + te.toString());
                	timeEntry(te);
                    break;
                case MessageType.PRINT_STRING:
                	String s= (String) inputMessage.obj;
                	Print(s);
                    break;
                case MessageType.CONNECTION_EVENT:
                	ConnectionEvent ce = (ConnectionEvent)inputMessage.obj;
                	connectionEvent(ce);
                	break;
                case MessageType.CLIENT_STATE:
                	bool = (Boolean)inputMessage.obj;
                	b = (Button)findViewById(R.id.connectButton);
            		b.setEnabled(bool.booleanValue());
            		break;
                case MessageType.SERVER_STATE:
                	bool = (Boolean)inputMessage.obj;
                	b = (Button)findViewById(R.id.serverButton);
            		b.setEnabled(bool.booleanValue());
            		break;
                default:
                    /*
                     * Pass along other messages from the UI
                     */
                    super.handleMessage(inputMessage);
				}
            }
	};
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		createShortCut();		
		
		chronometer = (Chronometer) findViewById(R.id.chronometer1);
		
		loadData();
		
		TextView mTextView = (TextView)findViewById(R.id.messageLog);
		mTextView.setMovementMethod(new ScrollingMovementMethod());	
			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void _Print(String s)
	{		
		Message completeMessage =
                mHandler.obtainMessage(MessageType.PRINT_STRING, s);
        completeMessage.sendToTarget();
	}
	
	
	public void _timeEntry(TimeEntry te)
	{		
		Message completeMessage =
                mHandler.obtainMessage(MessageType.TIME_ENTRY_RECEIVED, te);
        completeMessage.sendToTarget();
	}
	
	public void timeEntry(TimeEntry te)
	{		
		Chronometer chronometer = (Chronometer)findViewById(R.id.chronometer1);
				
		long etime = SystemClock.elapsedRealtime();
		Date date = new Date();				
		long timeEntryChronnoTime = te._time - (date.getTime() - etime); 
				
		long chTime = chronometer.getBase();
		
		if(isRunning == false || chTime < timeEntryChronnoTime)
		{
			Print("chronometer updated");
			chronometer.setBase(timeEntryChronnoTime);
			chronometer.start();
			isRunning = true;
			saveData();		
		}
		else if(chTime == te._time && te._id == getID())
		{
			Print("chronometer ping");			
		}
		else
		{
			Print("---- chronometer has newer time ----");
		}
		
	}
	
	int mLineNumber = 0;
	public void Print(String s)
	{	
		TextView mTextView = (TextView)findViewById(R.id.messageLog);
		while(mText.size() > 70)
		{
			mText.remove(0);
		}
		mText.add(s);
		mLineNumber++;
		
		int count = mLineNumber - mText.size();
		String newText = "";
		for(String line : mText)
		{
			newText = newText + count + ": " + line + '\n';
			count++;
		}
				
    	mTextView.setText(newText);
    	    	 
    	
    	final MainActivity ma = this;
        // if there is no need to scroll, scrollAmount will be <=0
    	mTextView.post(new Runnable() {    		
    		
    		MainActivity _ma = ma;
    		
    		public void run()
    		{ 
    			try{
    				TextView _textView = (TextView)_ma.findViewById(R.id.messageLog);
        			int scrollAmount = _textView.getLayout().getLineTop(_textView.getLineCount()) - _textView.getHeight();
        			if (scrollAmount > 0)
        				_textView.scrollTo(0, scrollAmount);
        			else
        				_textView.scrollTo(0, 0);	
    			}
    			catch(Exception e)
    			{
    				
    			}    			    			   			    			
    		}
    	});        
	}
		
	public void _connectionEvent(ConnectionEvent ce)
	{
		Message completeMessage =
                mHandler.obtainMessage(MessageType.CONNECTION_EVENT, ce);
        completeMessage.sendToTarget();
	}
	
	public void _clientConnectionState(Boolean state)
	{
		Message completeMessage =
                mHandler.obtainMessage(MessageType.CLIENT_STATE, state);
        completeMessage.sendToTarget();
	}
	
	public void _serverConnectionState(Boolean state)
	{
		Message completeMessage =
                mHandler.obtainMessage(MessageType.SERVER_STATE, state);
        completeMessage.sendToTarget();
	}
	
	public void connectionEvent(ConnectionEvent ce)
	{
		Integer i = mConnectedIDs.get(ce._id);
		if(ce._connected == false)
		{
			if(i == null)
			{
				Print("attempt to remove none existing client id:" + ce._id);
				return;
			}
			
			i = i+1;
			if(i.intValue() == 1)
			{
				mConnectedIDs.removeAt(ce._id);				
			}
			else
			{
				mConnectedIDs.put(ce._id, i);	
			}			
			Print("client id:" + ce._id + " disconnected ( " + i.toString() + ")");
		}
		else //if(ce._connected == true)
		{
			if(i == null)
			{
				i = 0;								
			}
			
			i = i+1;
			
			mConnectedIDs.put(ce._id, i);					
			Print("client id:" + ce._id + " connected (" + i.toString() + ")");
		}		
	}
	
	int getID()
	{
		EditText idText = (EditText)findViewById(R.id.idBox);
		int id = Integer.parseInt(idText.getText().toString());
		return id;
	}
	
	int getPort()
	{
		EditText portText = (EditText)findViewById(R.id.portBox);
		int port = Integer.parseInt(portText.getText().toString()); 
		return port;
	}
	
	
	String getIP()
	{
		EditText urlText = (EditText)findViewById(R.id.ipBox);
		String stringUrl = urlText.getText().toString();
		return stringUrl; 
	}
	
	public void startClick(View view)
	{
		if(mClient == null)
			connectClick(null);
		Date date = new Date();
		TimeEntry t = new TimeEntry();
			 
		t._id = getID();
		t._time = date.getTime();
		
		timeEntry(t);
		
		mClient.sendTimeEntry(t);
		
	}
	
	public void connectClick(View view) {
		saveData();
		Button b = (Button)findViewById(R.id.connectButton);
		b.setEnabled(false);
		
		String stringUrl = getIP();
				
		int port = getPort(); 
				
		EditText idText = (EditText)findViewById(R.id.idBox);
		int id = Integer.parseInt(idText.getText().toString());
		
	    mClient = new Client(this, stringUrl, port, id);
	}
	
	public void serverClick(View view) {
		saveData();
		Button b = (Button)findViewById(R.id.serverButton);
		b.setEnabled(false);
				
		String stringUrl = getIP();
		int port = getPort();				
		int id = getID();
		
	    mServer = new Server(this, stringUrl, port, id);
	}
	
	public void createShortCut() {

		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    boolean isAppInstalled = appPreferences.getBoolean("isAppInstalled", false);

	 	if(!isAppInstalled){

	        Intent HomeScreenShortCut= new Intent(getApplicationContext(), MainActivity.class);

	        HomeScreenShortCut.setAction(Intent.ACTION_MAIN);
	        HomeScreenShortCut.putExtra("duplicate", false);

	        
	        Intent addIntent = new Intent();
	        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, HomeScreenShortCut);
	        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
	        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
	                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
	                        R.drawable.ic_launcher));
	        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT"); 
	        getApplicationContext().sendBroadcast(addIntent);


	        //Make preference true
	        SharedPreferences.Editor editor = appPreferences.edit();
	        editor.putBoolean("isAppInstalled", true);
	        editor.putString("appName", getString(R.string.app_name));
	        editor.commit();
	    }
	}	
	
	private void saveData()
	{
		EditText urlText = (EditText)findViewById(R.id.ipBox);
		String stringUrl = urlText.getText().toString();
		
		EditText portText = (EditText)findViewById(R.id.portBox);
		String stringPort= portText.getText().toString(); 
		
		EditText idText = (EditText)findViewById(R.id.idBox);
		String stringId= idText.getText().toString();
		
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString("stringUrl", stringUrl);
        editor.putString("stringPort", stringPort);
        editor.putString("stringId", stringId);        
                
        editor.putBoolean("isRunning", isRunning);
	    
	    if(isRunning == true)
	    {
	    	long base = chronometer.getBase();
	    	editor.putLong("base", base);	        	    	
	    }
		
		
        editor.commit();        
	}
	
	private void loadData()
	{			
		EditText urlText = (EditText)findViewById(R.id.ipBox);
		String stringUrl = urlText.getText().toString();
		
		EditText portText = (EditText)findViewById(R.id.portBox);
		String stringPort= portText.getText().toString(); 
		
		EditText idText = (EditText)findViewById(R.id.idBox);
		String stringId= idText.getText().toString();
		
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);		
		stringUrl = appPreferences.getString("stringUrl", stringUrl);
		stringPort = appPreferences.getString("stringPort", stringPort);
		stringId = appPreferences.getString("stringId", stringId);
	
		
		urlText.setText(stringUrl);
		portText.setText(stringPort);
		idText.setText(stringId);		
		
		
	    isRunning = appPreferences.getBoolean("isRunning", false);
		if(isRunning == true)
		{
			long base = appPreferences.getLong("base", 0);
			chronometer.setBase(base);
			chronometer.start();
		}
	}
}
