package echoclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener
{
	//Storage a local copy of the active socket and OutputStream.
	private Socket s;
	private OutputStream out;
	
	public void Init(Socket sock, OutputStream outstream)
	{
		s = sock;
		out = outstream;
	}
	
	@EventHandler
	public void onChatEvent(AsyncPlayerChatEvent evt)
	{
			String msg = evt.getPlayer().getDisplayName() + ": " + evt.getMessage();
			try 
			{
				if(s.isConnected())
				{
					out.write(msg.getBytes());
				}
			} 
			catch (IOException e) 
			{
				//This usually shouldn't be hit. Again, just safety netting in case.
				e.printStackTrace();
			}
		
	}
}
