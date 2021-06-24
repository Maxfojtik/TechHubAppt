import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import static java.nio.file.StandardWatchEventKinds.*;
import javax.swing.JTextArea;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TechHubAppt 
{
	static DateFormat df;
	static String myPath;
	static LinkedList<Appt> getAppts()
	{
		try {
			return loadAppts(myPath);
		} catch (JSONException | IOException | ParseException e) {
			return null;
		}
	}
	static String readFile(String path) throws IOException
	{
		try
		{
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	static FileOutputStream lockFileOutputStream;
	static FileLock lock;
	static LinkedList<Appt> loadAppts(String path) throws IOException, JSONException, ParseException
	{
		String rawJSON = readFile(path);
		LinkedList<Appt> tempList = new LinkedList<Appt>();
		if(rawJSON!=null)
		{
			JSONArray jsonArray = new JSONArray(rawJSON);
			for(int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject apptObj = jsonArray.getJSONObject(i);
				String name = apptObj.getString("name");
				String email = null;
				String status = null;
				if(apptObj.has("email"))
				{
					email = apptObj.getString("email");
				}
				if(apptObj.has("status"))
				{
					status = apptObj.getString("status");
				}
				String note = null;
				if(apptObj.has("note"))
				{
					note = apptObj.getString("note");
				}
	//			DateFormat df = new 
				Date date = df.parse(apptObj.getString("date"));
				Appt tempAppt = new Appt(name, email, note, date, status);
				tempList.add(tempAppt);
			}
		}
		return tempList;
	}
	static void saveAppt(Appt appt) throws JSONException, IOException, ParseException
	{
		saveAppt(myPath, appt);
	}
	static LinkedList<Appt> deleteIfExists(LinkedList<Appt> in, Date date)
	{
		for(int i = 0; i < in.size(); i++)
		{
			if(in.get(i).date.equals(date))
			{
				System.out.println("Deleted Appt at "+date);
				in.remove(i);
				i--;
			}
		}
		return in;
	}
	static void deleteIfExists(Date date) throws IOException, JSONException, ParseException
	{
		LinkedList<Appt> tempList = loadAppts(myPath);
		deleteIfExists(tempList, date);
		JSONArray output = new JSONArray();
		for(int i = 0; i < tempList.size(); i++)
		{
			JSONObject apptObj = new JSONObject();
			Appt thisAppt = tempList.get(i);
			apptObj.put("name", thisAppt.name);
			if(thisAppt.email!=null)
			{
				apptObj.put("email", thisAppt.email);
			}
			if(thisAppt.note!=null)
			{
				apptObj.put("note", thisAppt.note);
			}
			if(thisAppt.status!=null)
			{
				apptObj.put("status", thisAppt.note);
			}
			apptObj.put("date", df.format(thisAppt.date));
			output.put(apptObj);
		}
		FileWriter file = new FileWriter(myPath);
        file.write(output.toString());
        file.close();
        System.out.println("Saved "+output.length()+" appts");
	}
	static void saveAppt(String path, Appt appt) throws IOException, JSONException, ParseException
	{
		LinkedList<Appt> tempList = loadAppts(path);
		deleteIfExists(tempList, appt.date);
		tempList.add(appt);
		JSONArray output = new JSONArray();
		for(int i = 0; i < tempList.size(); i++)
		{
			JSONObject apptObj = new JSONObject();
			Appt thisAppt = tempList.get(i);
			apptObj.put("name", thisAppt.name);
			if(thisAppt.email!=null)
			{
				apptObj.put("email", thisAppt.email);
			}
			if(thisAppt.note!=null)
			{
				apptObj.put("note", thisAppt.note);
			}
			if(thisAppt.status!=null)
			{
				apptObj.put("status", thisAppt.status);
			}
			apptObj.put("date", df.format(thisAppt.date));
			output.put(apptObj);
		}
		FileWriter file = new FileWriter(path);
        file.write(output.toString());
        file.close();
        System.out.println("Saved "+output.length()+" appts");
	}
	static String oldHash;
	static boolean needReload()
	{
		String newHash = hash(new File("./appts.json"));
		System.out.println(newHash+":"+oldHash);
		return !newHash.equals(oldHash);
	}
	static void reHash()
	{
		oldHash = hash(new File("./appts.json"));
	}
	static boolean fileChanged(WatchKey key, WatchService watcher)
	{
		key = watcher.poll();
		if(key!=null)
		{
		    for (WatchEvent<?> event: key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();
		        if (kind == OVERFLOW) {
		            continue;
		        }
		        final Path changed = (Path) event.context();
	            System.out.println(changed);
	            if (changed.endsWith("appts.json")) {
	                return true;
	            }
		    }
		    boolean valid = key.reset();
		    if (!valid) {
		        return false;
		    }
		}
		return false;
	}
	public static void main(String args[]) throws URISyntaxException, JSONException, IOException, ParseException, InterruptedException
	{
		File folder = new File("./Users");
		folder.mkdir();
		File file = new File("./Users/"+System.getProperty("user.name")+".txt");
		file.createNewFile();
        file.deleteOnExit();
		df = new SimpleDateFormat("HH:mm MM/dd/yyyy", Locale.ENGLISH);
		myPath = new File("./appts.json").getPath();
		Frame.init();
		long loopNum = 0;
		reHash();
		WatchService watcher = FileSystems.getDefault().newWatchService();
		Path dir = Paths.get("./");
		WatchKey key = null;
		try {
		    key = dir.register(watcher, ENTRY_MODIFY);
		} catch (IOException x) {
		    System.err.println(x);
		    System.exit(1);
		}
		while(true)
		{
			if(loopNum++%100==0 || fileChanged(key, watcher))
			{
				if(needReload() && Frame.unsavedAppts.size()==0)
				{
					System.out.println("Needs reload");
					Frame.showDay(TechHubAppt.getAppts(), Frame.datePicker.getDate());
					oldHash = hash(new File("./appts.json"));
				}
			}
			Frame.logic();
			File f = new File("./Users/plsclose.txt");
			if(f.exists())
			{
				System.exit(0);
			}
			Thread.sleep(100);
		}
//		Frame.showDay(appts);
//		saveAppt(myPath, new Appt("Max", null, null, new Date()));
	}

	static String hash(File f)
	{
		try {
			FileInputStream fis = new FileInputStream(f);
			MessageDigest digest = MessageDigest.getInstance("MD5");
			//Create byte array to read data in chunks
		    byte[] byteArray = new byte[1024];
		    int bytesCount = 0; 
		      
		    //Read file data and update in message digest
		    while ((bytesCount = fis.read(byteArray)) != -1) {
		    	digest.update(byteArray, 0, bytesCount);
		    };
		     
		    //close the stream; We don't need it now.
		    fis.close();
			byte[] hash = digest.digest();
			//This bytes[] has bytes in decimal format;
		    //Convert it to hexadecimal format
		    StringBuilder sb = new StringBuilder();
		    for(int i=0; i< hash.length ;i++)
		    {
		        sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
		    }
		     
		    //return complete hash
		   return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		return "";
	}
	static class Appt
	{
		String name;
		String email;
		String note;
		String status;
		Date date;
		public Appt(String name, String email, String note, Date date, String status)
		{
			this.name = name;
			this.email = email;
			this.note = note;
			this.date = date;
			this.status = status;
		}
	}
}
