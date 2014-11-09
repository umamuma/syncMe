package com.example.syncme.Util;

import java.util.Date;

public class TimeEntry
{
	public TimeEntry()
	{
		_id = -1;
		_time = 0;
	}

	public TimeEntry(int id, long time)
	{			
		_id = id;
		_time = time;
	}

	@Override 
	public String toString()
	{		
		Date d = new Date(_time);		
		return "(id: " + _id + ", " + d + " )" + _time;
	}
			
	
	public int _id;
	public long _time;
}