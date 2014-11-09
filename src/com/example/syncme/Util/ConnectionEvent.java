package com.example.syncme.Util;

public class ConnectionEvent
{
	public ConnectionEvent(int id, boolean connected)
	{
		_id = id;
		_connected = connected;
	}
	public int _id;
	public boolean _connected;
}