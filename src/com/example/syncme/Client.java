package com.example.syncme;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.example.syncme.Util.TimeEntry;
import com.example.syncme.Util.ClientMode;;

public class Client {

	MainActivity _parent;
	String _ip;
	int _port;
	int _id;

	List<TimeEntry> _timeEntry = new ArrayList<TimeEntry>();
	
	public Client(MainActivity parent, String ip, int port, int id)
	{
		_parent = parent;
		_ip = ip;
		_port = port;
		_id = id;
		startClient();
	}


	void startClient()
	{
		try
		{
			new Thread(){
				public void run()
				{
					openReadConnection();
				}
			}.start();							
		}
		catch (Exception ex)
		{
			_parent._Print("Error..... " + ex.toString());
		}
	}


	void openReadConnection()
	{
		
		while(true)
		{		
			Socket s = null;
			try
			{	
				_parent._Print("Client Connecting.....");
				
				s = new Socket(_ip, _port);
				
				_parent._Print("Client Connected");
				
				sentInitMessage(s, ClientMode.readMode);

				while (true)
				{
					TimeEntry t = getTimeEntry(s);
					if (t._id == -1)
					{
						break;
					}
					_parent._Print("server sent " + t.toString());
					_parent._timeEntry(t);

				}
			}
			catch (Exception ex)
			{
				_parent._Print("Error openReadConnection..... " + ex.toString());
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			finally 
			{
				if (s != null)
				{
					try {
						s.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
				
	void sentInitMessage(Socket s, int mode) throws IOException
	{	
		DataOutputStream streamWriter = null;		 
		try {
			streamWriter = new DataOutputStream(s.getOutputStream());
			streamWriter.writeInt(mode);
			streamWriter.writeInt(_id);
			streamWriter.flush();		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw(e); 
		}
		finally
		{
			
		}
	}
	
	TimeEntry getTimeEntry(Socket s)
	{
		DataInputStream streamReader = null;
				
		TimeEntry t = new TimeEntry();
		try
		{				
			streamReader = new DataInputStream(s.getInputStream());
			t._id = streamReader.readInt();
			t._time = streamReader.readLong();			
		}
		catch (Exception ex)
		{
			
		}
		

		return t;
	}

	public void sendTimeEntry(TimeEntry t)
	{
		synchronized(_timeEntry)
		{
			_timeEntry.add(t);
		}
		
		
		class MyThread implements Runnable {
			
			Object lt;
			
			public MyThread(Object at) {
				lt = (TimeEntry)at;
			}		
			
			public void run() {
				_sendTimeEntry(lt);
			}
		}
		
		Runnable r = new MyThread(t);
		Thread thread = new Thread(r);	
		thread.start();
	}

	public void _sendTimeEntry(Object teObj)
	{
		TimeEntry t = (TimeEntry)teObj;
		Socket s = null;		
		try
		{
			s = new Socket(_ip, _port);
			_parent._Print("Client Connected to server");
			
			sentInitMessage(s, ClientMode.writeMode);

			_sendTimeEntry(s, t);

			synchronized(_timeEntry)
			{
				_timeEntry.remove(t);
			}
			
		}
		catch (Exception ex)
		{
			_parent._Print("Error openReadConnection..... " + ex.toString());				
		}
		finally
		{
			if (s != null)
			{
				try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}

	void _sendTimeEntry(Socket s, TimeEntry t) throws IOException
	{
		DataOutputStream streamWriter = null;
		try {
			streamWriter = new DataOutputStream(s.getOutputStream());
			streamWriter.writeInt(t._id);
			streamWriter.writeLong(t._time);
			streamWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw(e);
		}
			
	}
}
