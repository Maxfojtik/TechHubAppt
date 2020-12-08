import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

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
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}
	static LinkedList<Appt> loadAppts(String path) throws IOException, JSONException, ParseException
	{
		String rawJSON = readFile(path);
		JSONArray jsonArray = new JSONArray(rawJSON);
		LinkedList<Appt> tempList = new LinkedList<Appt>();
		for(int i = 0; i < jsonArray.length(); i++)
		{
			JSONObject apptObj = jsonArray.getJSONObject(i);
			String name = apptObj.getString("name");
			String email = null;
			if(apptObj.has("email"))
			{
				email = apptObj.getString("email");
			}
			String note = null;
			if(apptObj.has("note"))
			{
				note = apptObj.getString("note");
			}
//			DateFormat df = new 
			Date date = df.parse(apptObj.getString("date"));
			Appt tempAppt = new Appt(name, email, note, date);
			tempList.add(tempAppt);
		}
		return tempList;
	}
	static void saveAppt(String path, Appt appt) throws IOException, JSONException, ParseException
	{
		String rawJSON = readFile(path);
		JSONArray jsonArray = new JSONArray(rawJSON);
		LinkedList<Appt> tempList = new LinkedList<Appt>();
		for(int i = 0; i < jsonArray.length(); i++)
		{
			JSONObject apptObj = jsonArray.getJSONObject(i);
			String name = apptObj.getString("name");
			String email = null;
			if(apptObj.has("email"))
			{
				email = apptObj.getString("email");
			}
			String note = null;
			if(apptObj.has("note"))
			{
				note = apptObj.getString("note");
			}
			Date date = df.parse(apptObj.getString("date"));
			Appt tempAppt = new Appt(name, email, note, date);
			tempList.add(tempAppt);
		}
	}
	public static void main(String args[]) throws URISyntaxException
	{
		df = new SimpleDateFormat("MM/dd/YYYY HH:mm", Locale.ENGLISH);
		myPath = new File(TechHubAppt.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
	}
	static class Appt
	{
		String name;
		String email;
		String note;
		Date date;
		public Appt(String name, String email, String note, Date date)
		{
			this.name = name;
			this.email = email;
			this.note = note;
			this.date = date;
		}
	}
}
