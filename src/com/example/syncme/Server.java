package com.example.syncme;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.example.syncme.Util.ConnectionEvent;
import com.example.syncme.Util.TimeEntry;
import com.example.syncme.Util.ClientMode;;


public class Server {

	MainActivity _parent;
	String _ip;
	int _port;
	int _id;

	List<TimeEntry> _timeEntry = new ArrayList<TimeEntry>();
	
	public Server(MainActivity parent, String ip, int port, int id)
	{
		_parent = parent;
		_ip = ip;
		_port = port;
		_id = id;
		startServer();
	}
	
	
	void startServer()
	{
		try
		{
			new Thread(){
				public void run()
				{
					_startServer();
				}
			}.start();							
		}
		catch (Exception ex)
		{
			_parent._Print("Error..... " + ex.toString());
		}
	}


	void _startServer()
	{
		 Socket socket = null;
		 ServerSocket serverSocket;
		 try {
			serverSocket = new ServerSocket(_port);
			
			while (!Thread.currentThread().isInterrupted()) {
				
				try {
		
					socket = serverSocket.accept();
		
					class ClientConnectThread implements Runnable
					{
						Socket socket;
						ClientConnectThread(Socket aSocket)
						{
							socket = aSocket;	
						}
						
						public void run()
						{
							try {
								HandleClientComm(socket);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					new Thread(new ClientConnectThread(socket)).start();				
		
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	class InitMessage
	{
		int mode;
		int id;
	}

	void HandleClientComm(Socket tcpclnt) throws IOException
	{
		InitMessage im = new InitMessage();
					
		try
		{
			_parent._Print("Connection accepted from " + tcpclnt.getRemoteSocketAddress().toString());
			
			getInitMessage(tcpclnt, im);

			switch (im.mode)
			{
				case ClientMode.err:
					break;
				case ClientMode.readMode:
					acceptReadClient(tcpclnt, im.id);
					break;
				case ClientMode.writeMode:
					acceptWriteClient(tcpclnt, im.id);
					break;
			}				
		}
		catch (Exception ex)
		{
			_parent._Print("caught from" + im.id + " in listenToClient");
			_parent._Print(ex.getMessage());
		}
		finally
		{
			tcpclnt.close();
		}
	}

	void acceptReadClient(Socket s, int id)
	{
		try
		{
			_parent._Print("read from " + id);
			_parent._connectionEvent(new ConnectionEvent(id, true));
			
			synchronized(_timeEntry)
			{
				if (_timeEntry.size() > 0)
				{
					TimeEntry t = _timeEntry.get(_timeEntry.size() -1);
					sendTimeEntry(s, t);
					_parent._Print("sending latest " + t.toString() + " to " + id);
				}
			}
			while (true)
			{
				synchronized(_timeEntry)
				{
					_timeEntry.wait();
					if (_timeEntry.size() > 0)
					{
						TimeEntry t = _timeEntry.get(_timeEntry.size() -1);						
						sendTimeEntry(s, t);
						_parent._Print("sending new " + t.toString() + " to " + id);
					}
				}
			}					
		}
		catch (InterruptedException ex)
		{
			
		}
		catch (IOException ex)
		{
			
		}
		finally
		{
			_parent._connectionEvent(new ConnectionEvent(id, false));
		}
	}

	void acceptWriteClient(Socket s, int id)
	{
		_parent._Print("write from " + id);
		while (true)
		{
			TimeEntry t;
			try
			{
				t = getTimeEntry(s);
				if (t._id == -1)
				{
					break;
				}
				_parent._Print("client " +id + "sent " + t.toString());
				synchronized(_timeEntry)
				{
					_timeEntry.add(t);
					/*while(_timeEntry.Count > 5)
					{
						_timeEntry.Remove(_timeEntry.First());
					}*/
					_timeEntry.notifyAll();					
				}
			}
			catch (Exception ex)
			{
				_parent._Print("caught from " + id + " in write");
				break;
			}
			finally
			{

			}
		}				
	}

	public void sendTimeEntry(Socket s, TimeEntry t) throws IOException
	{
		boolean connected  = s.isConnected();
		boolean closed  = s.isClosed();
		boolean outputShutdown  = s.isOutputShutdown();
		
		if(outputShutdown == true || connected == false || closed == true)
			throw(new IOException());
		
		DataOutputStream streamWriter = null;
		//try {
			streamWriter = new DataOutputStream(s.getOutputStream());
			streamWriter.writeInt(t._id);
			streamWriter.writeLong(t._time);
			streamWriter.flush();
		//} catch (IOException e) {
		//	e.printStackTrace();
		//	throw(e);
		//}
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

	void getInitMessage(Socket s, InitMessage im) throws IOException
	{
		DataInputStream streamReader = null;
								
		streamReader = new DataInputStream(s.getInputStream());		
		im.mode = streamReader.readInt();		
		//streamReader = new DataInputStream(s.getInputStream());
		im.id = streamReader.readInt();		
	}	
}
