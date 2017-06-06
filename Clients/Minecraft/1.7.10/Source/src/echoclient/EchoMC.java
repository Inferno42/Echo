//Name: EchoMC
//Author: Christian Little (Inferno42)
//Description: EchoMC is a minecraft plugin designed to link the in-game chat from multiple servers.

package echoclient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

public class EchoMC extends JavaPlugin
{
	private Yaml reader;
	private String IP;
	private int Port;
	private String Identifier;
	private Socket s;
	private Thread ReadThread;
	private OutputStream out;
	private BufferedReader in;
	private ChatListener Chat;
	
	public void onEnable()
	{
		getLogger().info("Echo Client enabled.");
		reader = new Yaml();
		Startup();
	}
	
	public void Startup()
	{
		File path = new File("plugins//Echo//config.yml");
		if(!path.exists())
		{
			getLogger().info("ERROR!!! No config.yml found in plugins//Echo//");
			getLogger().info("Tried to open at" + path.getAbsolutePath());
		}
		else
		{
			ProcessYAML();
		}
	}
	
	public void ProcessYAML()
	{
		getLogger().info("Reading config.yml");
		
		File config = new File("plugins//Echo//config.yml");

		InputStream is;
		try 
		{
			//Get the contents of the YAML config file and load them.
			is = new FileInputStream(config);
			Map map = (Map) reader.load(is);
			
			//Grab the IP and Port for the Echo server and set the client's Identifier.
			IP = map.values().toArray()[0].toString();
			Port = (int) map.values().toArray()[1];
			Identifier = map.values().toArray()[2].toString();
			Identifier = "<" + Identifier + ">";
			
			//0.0.0.0 is the default IP listed in the config.
			if(IP.equals("0.0.0.0"))
			{
				getLogger().info("Echo Server IP not set. Please set it and do /echomc reset");
				return;
			}
			
			AttemptConnection();
		} 
		catch (FileNotFoundException e) 
		{
			getLogger().info("ERROR!!! Unable to open config.yml!");
			e.printStackTrace();
		}
	}

	
	public void AttemptConnection()
	{
		getLogger().info("Attempting to connect to Echo Server.");
		
		try 
		{
			//If the socket was already active, close it.
			//This being called means we have already opened the socket once.
			if(s != null)
			{
				s.close();
			}
			
			s = new Socket(IP, Port);
			
			//Use an OutputStream and BufferedReader(InputStreamReader) to output byte data (UTF-8) to the Echo server
			//and receive UTF-8 byte data from the server.
			//Note that the Echo server, per Java socket standards, ALWAYS communicates with UTF8 bytes and trailing newlines after messages.
			//If no newline trails the message sent, Java's readLine() will lock up the main plugin thread.
			out = s.getOutputStream();
			in = new BufferedReader(
		            new InputStreamReader(s.getInputStream()));

			//Authenticate client to server.
			if(in.readLine().equals("[IDENTIFY]"))
			{
				out.write(Identifier.getBytes());
				getLogger().info("Identified to Echo server as " + Identifier);
			}
			
			//Create a ChatListener and let it do its' job.
			//Bukkit automatically gives it its own thread.
			Chat = new ChatListener();
			Chat.Init(s, out);
			Bukkit.getPluginManager().registerEvents(Chat, this);
			
			//Use a Lambda Runnable thread to constantly read messages and broadcast them to the server.
			//This was originally intended to be much cleaner, but Java will not allow me to
			//implement both JavaPlugin and Runnable at the same time for a seperate class.
			Runnable r1 = () -> {

			          while(s.isConnected())
			          {
			 			try 
			 			{
			 				//Check if the socket is connected before even entering the try.
			 				//Really this is just safety padding and probably unnecessary.
			 				if(s.isConnected())
			 				{
			 					String msg = in.readLine();
			 					String trimmed = msg.trim();
			 					
			 					//Trim newlines from the end of messages being received.
			 					trimmed = msg.replaceAll("\n", "");
			 					
			 					//Hacky fix to prevent random new-lines being sent to the client by the server.
			 					//This was so far only observed on the Java client and not on Python-based test clients.
			 					if(msg.length() > 0)
			 						getServer().broadcastMessage(msg);
			 				}
						} 
			 			catch (IOException e) 
			 			{
							getLogger().info("Read Thread killed due to IOException. If echomc_reset was killed, ignore this message.");
							return;
						}
			          }
			          return;
			     
			};
			
			//Initiate and start the thread to read from the server.
			ReadThread = new Thread(r1);
			ReadThread.start();
			
		} 
		catch (UnknownHostException e1) 
		{
			getLogger().info("Unknown Host, unable to connect.");
			return;
		} 
		catch (ConnectException e1) 
		{
			getLogger().info("Unable to connect to server.");
		}
		catch (IOException e1) 
		{
			getLogger().info("Unknown Error.");
			e1.printStackTrace();
		}
	}
	
	public void onDisable()
	{
		
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		if(commandLabel.equalsIgnoreCase("echomc_reset"))
		{
			//If any of these Try/Catches fail, then the services were already non-existent.
			try
			{
				getLogger().info("Unhooking Chat Listener");
				HandlerList.unregisterAll(Chat);
			}
			//Catch is only here so that Java stays quiet.
			catch (NullPointerException e)
			{
				
			}
			
			try
			{
				getLogger().info("Interrupting Read Thread.");
				ReadThread.interrupt();
			}
			//Catch is only here so that Java stays quiet.
			catch (NullPointerException e)
			{
				
			}
			
			//Re-attempt startup.
			Startup();
		}
		
		if(commandLabel.equalsIgnoreCase("echomc"))
		{
			try
			{
				if(s.isConnected())
				{
					sender.sendMessage("Connection Status: Connected");
					sender.sendMessage("Echo Server IP: " + IP + ":" + Integer.toString(Port));
					sender.sendMessage("Identifier: " + Identifier);
				}
				else
				{
					sender.sendMessage("Connection Status: Disconnected");
					sender.sendMessage("Echo Server IP: ");
					sender.sendMessage("Identifier: ");
				}
			}
			catch (NullPointerException e)
			{
				getLogger().info("Socket is Null. Startup must have failed initially.");
			}
		}
		return true;
	}
}
