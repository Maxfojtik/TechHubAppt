import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.json.JSONException;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.github.lgooddatepicker.components.TimePickerSettings.TimeArea;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

public class Frame 
{
	static JFrame f;
	static JButton newAppt;
	static JButton deleteAppt;
//	static JButton printSchedule;
	static DatePicker datePicker;
	static JButton nextDay;
	static JButton prevDay;
	static JLabel hourLabelLabel;
	static JLabel nameLabel;
	static JLabel emailLabel;
	static JLabel noteLabel;
	static LinkedList<JComponent> hourComps = new LinkedList<JComponent>();
	static LinkedList<JTextArea> nameComps = new LinkedList<JTextArea>();
	static LinkedList<JTextArea> emailComps = new LinkedList<JTextArea>();
	static LinkedList<JTextArea> noteComps = new LinkedList<JTextArea>();
	static LinkedList<JComboBox<String>> dropComps = new LinkedList<JComboBox<String>>();
	static LinkedList<Integer> unsavedAppts = new LinkedList<>();
	static long lastUnsaved = 0;
	static void save(Date day, String combo)
	{
		for(int i = 0; i < unsavedAppts.size(); i++) 
		{
			String name = nameComps.get(unsavedAppts.get(i)).getText();
			String email = emailComps.get(unsavedAppts.get(i)).getText();
			if(email.equals(""))
			{
				email = null;
			}
			String note = noteComps.get(unsavedAppts.get(i)).getText();
			if(note.equals(""))
			{
				note = null;
			}
			String status = (String) dropComps.get(unsavedAppts.get(i)).getSelectedItem();
			if(combo!=null)
			{
				status = combo;
			}
			if(status.equals(""))
			{
				status = null;
			}
//			System.out.println("status: "+status);
			Date date = indexToTime(unsavedAppts.get(i), day);
			if(!name.trim().equals(""))
			{
				try {
					TechHubAppt.saveAppt(new TechHubAppt.Appt(name, email, note, date, status));
					dropComps.get(unsavedAppts.get(i)).setEnabled(true);
				} catch (JSONException | IOException | ParseException e) {
					e.printStackTrace();
				}
			}
			else
			{
				try {
					TechHubAppt.deleteIfExists(date);
				} catch (JSONException | IOException | ParseException e) {
					e.printStackTrace();
				}
				nameComps.get(unsavedAppts.get(i)).setText("");
				noteComps.get(unsavedAppts.get(i)).setText("");
				emailComps.get(unsavedAppts.get(i)).setText("");
				dropComps.get(unsavedAppts.get(i)).setSelectedIndex(0);
				dropComps.get(unsavedAppts.get(i)).setEnabled(false);
			}
			nameComps.get(unsavedAppts.get(i)).setEnabled(false);
			noteComps.get(unsavedAppts.get(i)).setEnabled(false);
			emailComps.get(unsavedAppts.get(i)).setEnabled(false);
		}
		f.setTitle("Tech Hub Appointments");
		unsavedAppts.clear();
		TechHubAppt.reHash();
	}
	static void logic()
	{
		for(int i = 0; i < Frame.nameComps.size(); i++)
		{
			JTextArea nameComp = Frame.nameComps.get(i);
			JTextArea noteComp = Frame.noteComps.get(i);
			JTextArea emailComp = Frame.emailComps.get(i);
			JComboBox<String> dropComp = Frame.dropComps.get(i);
			nameComp.setBackground(Color.white);
			noteComp.setBackground(Color.white);
			emailComp.setBackground(Color.white);
			dropComp.setBackground(Color.white);
		}
		if(datePicker.getDate().equals(LocalDate.now()))
		{
			int index = Frame.timeToIndex(new Date());
			if(index>=0 && index<=15)
			{
				JTextArea nameComp = Frame.nameComps.get(index);
				JTextArea noteComp = Frame.noteComps.get(index);
				JTextArea emailComp = Frame.emailComps.get(index);
				JComboBox<String> dropComp = Frame.dropComps.get(index);
				nameComp.setBackground(Color.lightGray);
				noteComp.setBackground(Color.lightGray);
				emailComp.setBackground(Color.lightGray);
				dropComp.setBackground(Color.lightGray);
			}
		}
		long delta = System.currentTimeMillis()-lastUnsaved;
		if(delta>0 && unsavedAppts.size()>0)
		{
			f.setTitle("Tech Hub Appointments (unsaved changes) Auto Saving in "+(60-delta/1000)+" seconds");
			if(delta>60000)
			{
				save(localDateToDate(datePicker.getDate()), null);
			}
		}
	}
	static void checkAskUnsaved()
	{
		if(unsavedAppts.size()>0)
		{
			Object[] options = {"Save","Discard"};
			int n = JOptionPane.showOptionDialog(null,
			    "Would you like to save these?",
			    "You have unsaved changes",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE,
			    null,
			    options,
			    options[0]);
			if(n==0)
			{
				save(localDateToDate(datePicker.getDate()), null);
			}
			else
			{
				unsavedAppts.clear();
				f.setTitle("Tech Hub Appointments");
			}
		}
	}
	static void init()
	{
		f = new JFrame("Tech Hub Appointments");
		f.setBounds(10,10,910,450);
		f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(null);
		f.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
				checkAskUnsaved();
			}
        });
		hourLabelLabel = new JLabel("Time");
		int yStart = 50;
		int deltaY = 20;
		hourLabelLabel.setBounds(10, yStart-deltaY, 100, deltaY);
		f.add(hourLabelLabel);
		nameLabel = new JLabel("Name");
		nameLabel.setBounds(110, yStart-deltaY, 150, deltaY);
		f.add(nameLabel);
		emailLabel = new JLabel("Name.Number (unused)");
		emailLabel.setBounds(270, yStart-deltaY, 150, deltaY);
		f.add(emailLabel);
		noteLabel = new JLabel("Note (opt)");
		noteLabel.setBounds(430, yStart-deltaY, 250, deltaY);
		f.add(noteLabel);
		nextDay = new JButton("Next Day");
		int frameWidth = 895;
		nextDay.setBounds(frameWidth-155, 5, 150, 25);
		nextDay.addActionListener(new NextDayListener());
		f.add(nextDay);
		prevDay = new JButton("Previous Day");
		prevDay.setBounds(5, 5, 150, 25);
		prevDay.addActionListener(new PrevDayListener());
		f.add(prevDay);
		datePicker = new DatePicker();
		datePicker.setDateToToday();
		int widthOfChoose = 160;
		datePicker.setBounds(frameWidth/2-widthOfChoose/2, 5, widthOfChoose, 25);
		datePicker.getComponentDateTextField().setEditable(false);
		datePicker.addDateChangeListener(new DateChangeListener() 
		{	
			@Override
			public void dateChanged(DateChangeEvent event) 
			{
				if(unsavedAppts.size()>0)
				{
					checkAskUnsaved();
//					unsavedAppts.clear();
//					f.setTitle("Tech Hub Appointments (discarded old data)");
				}
				showDay(TechHubAppt.getAppts(), event.getNewDate());
			}
		});
		f.add(datePicker);
		for(double i = 13; i < 16.9; i = i + 0.25)
		{
			int hour = (int)i;
			int minutes = (int) ((i-hour)*60);
			String minutesString = minutes!=0 ? String.valueOf(minutes) : "00";
			JLabel hourLabel = new JLabel((hour-12)+":"+minutesString +" PM");
			int line = (int)(yStart + (i-13)*(deltaY*4+10));
			hourLabel.setBounds(10, line, 100, deltaY);
			hourComps.add(hourLabel);
			f.add(hourLabel);
			
			JTextArea nameTA = new JTextArea(1,1);
			nameTA.getDocument().putProperty("filterNewlines", Boolean.TRUE);
			nameTA.setBounds(110, line, 150, deltaY);
			nameTA.addKeyListener(new ChangeListener(nameComps.size()));
			nameTA.addMouseListener(new ClickMouseListener());
			nameTA.setDisabledTextColor(Color.BLUE);
			f.add(nameTA);
			nameComps.add(nameTA);
			
			JTextArea emailTA = new JTextArea(1,1);
			emailTA.getDocument().putProperty("filterNewlines", Boolean.TRUE);
			emailTA.setBounds(270, line, 150, deltaY);
			emailTA.addKeyListener(new ChangeListener(emailComps.size()));
			emailTA.setDisabledTextColor(Color.BLUE);
			emailTA.addMouseListener(new ClickMouseListener());
			f.add(emailTA);
			emailComps.add(emailTA);
			
			JTextArea noteTA = new JTextArea(1,1);
			noteTA.getDocument().putProperty("filterNewlines", Boolean.TRUE);
			noteTA.setBounds(430, line, 350, deltaY);
			noteTA.addKeyListener(new ChangeListener(noteComps.size()));
			noteTA.setDisabledTextColor(Color.BLUE);
			noteTA.addMouseListener(new ClickMouseListener());
			f.add(noteTA);
			noteComps.add(noteTA);
			
			
			JComboBox<String> comboBox = new JComboBox<String>(new String[]{"", "Completed", "No show"}) ;
			comboBox.setSelectedIndex(0);
			comboBox.setBounds(790, line, 100, deltaY);
			//noteTA.addKeyListener(new ChangeListener(noteComps.size()));
			//comboBox.setDisabledTextColor(Color.BLUE);
			comboBox.addActionListener(new ComboListener(dropComps.size()));
//			comboBox.addMouseListener(new ClickMouseListener());
			f.add(comboBox);
			dropComps.add(comboBox);
		}
		showDay(TechHubAppt.getAppts(), datePicker.getDate());
		f.setVisible(true);
	}
	static int timeToIndex(Date d)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		int index = (int) (((cal.get(Calendar.HOUR_OF_DAY)-13)*4+cal.get(Calendar.MINUTE)/15));
		return index;
	}
	static Date indexToTime(int index, Date date)
	{
		int hour = index/4+13;
		int minute = index%4*15;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
//		cal.set(Calendar.YEAR, date.getYear());
//		cal.set(Calendar.DAY_OF_YEAR, date.getDayOfYear());
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		System.out.println(hour+"\t"+minute);
		return cal.getTime();
	}
	static void showDay(LinkedList<TechHubAppt.Appt> appts, LocalDate dayDate)
	{
		logic();
		for(int i = 0; i < nameComps.size(); i++)
		{
			nameComps.get(i).setText("");
			emailComps.get(i).setText("");
			noteComps.get(i).setText("");
			dropComps.get(i).setSelectedIndex(0);
			nameComps.get(i).setEnabled(false);
			emailComps.get(i).setEnabled(false);
			noteComps.get(i).setEnabled(false);
			dropComps.get(i).setEnabled(false);
		}
		if(dayDate!=null && appts!=null)
		{
			LinkedList<TechHubAppt.Appt> drawingAppts = new LinkedList<>();
			Calendar cal = Calendar.getInstance();
			for(int i = 0; i < appts.size(); i++)
			{
				cal.setTime(appts.get(i).date);
				int day = cal.get(Calendar.DAY_OF_YEAR);
				int year = cal.get(Calendar.YEAR);
				int thisDay = dayDate.getDayOfYear();
				int thisYear = dayDate.getYear();
				if(day == thisDay && year == thisYear)
				{
					drawingAppts.add(appts.get(i));
				}
			}
			for(int i = 0; i < drawingAppts.size(); i++)
			{
				TechHubAppt.Appt thisAppt = drawingAppts.get(i);
				int index = timeToIndex(thisAppt.date);
				nameComps.get(index).setText(thisAppt.name);
				emailComps.get(index).setText(thisAppt.email);
				noteComps.get(index).setText(thisAppt.note);
				dropComps.get(index).setEnabled(true);
				if(thisAppt.status!=null)
				{
					dropComps.get(index).setSelectedItem(thisAppt.status);
				}
				else
				{
					dropComps.get(index).setSelectedIndex(0);
				}
			}
		}
	}
	static void unsavedChanges(int index)
	{
		lastUnsaved = System.currentTimeMillis();
		if(!unsavedAppts.contains(index))
		{
			unsavedAppts.add(index);
			f.setTitle("Tech Hub Appointments (unsaved changes)");
		}
	}
	static void showPrevDay()
	{
		checkAskUnsaved();
		datePicker.setDate(getPrevDay(datePicker.getDate()));
	}
	static void showNextDay()
	{
		checkAskUnsaved();
		datePicker.setDate(getNextDay(datePicker.getDate()));
	}
	static LocalDate getNextDay(LocalDate old)
	{
		while(true)
		{
	        old = old.plusDays(1);
	        if (old.getDayOfWeek() != DayOfWeek.SUNDAY) 
	        {
	        	break;
	        }
		}
        return old;
	}
	static LocalDate getPrevDay(LocalDate old)
	{
		while(true)
		{
	        old = old.minusDays(1);
	        if (old.getDayOfWeek() != DayOfWeek.SUNDAY) 
	        {
	        	break;
	        }
		}
        return old;
	}
	static boolean ignoreCombos = false;
	static class NextDayListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			ignoreCombos = true;
			showNextDay();
			ignoreCombos = false;
		}
	}
	static class PrevDayListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			ignoreCombos = true;
			showPrevDay();
			ignoreCombos = false;
		}
	}
	static class ClickMouseListener implements java.awt.event.MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent e) {
			e.getComponent().setEnabled(true);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}
		
	}
	static class ComboListener implements ActionListener
	{
		int index;
		public ComboListener(int index)
		{
			this.index = index;
		}
		@Override
		public void actionPerformed(ActionEvent event) 
		{
			if((event.getModifiers()==16 || event.getModifiers()==4) && !ignoreCombos)
			{
				JComboBox<String> combo = (JComboBox<String>) event.getSource();
		        String selected = (String) combo.getSelectedItem();
				System.out.println("Saved as: "+selected);
				unsavedChanges(index);
				save(localDateToDate(datePicker.getDate()), selected);
			}
		}
	}
	static Date localDateToDate(LocalDate d)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, d.getYear());
		cal.set(Calendar.DAY_OF_YEAR, d.getDayOfYear());
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	static class ChangeListener implements KeyListener
	{
		int index;
		public ChangeListener(int index) {
			this.index = index;
		}
		@Override
		public void keyPressed(KeyEvent e) 
		{
			if(e.getKeyChar()=='\n')
			{
				save(localDateToDate(datePicker.getDate()), null);
			}
			else if(e.getKeyChar()=='\t')
			{
				if(nameComps.contains(e.getComponent()))
				{
					int index = nameComps.indexOf(e.getComponent());
					JTextArea ta = emailComps.get(index);
					ta.setEnabled(true);
					ta.requestFocus();
				}
				else if(emailComps.contains(e.getComponent()))
				{
					int index = emailComps.indexOf(e.getComponent());
					JTextArea ta = noteComps.get(index);
					ta.setEnabled(true);
					ta.requestFocus();
				}
				e.consume();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) 
		{
			
		}

		@Override
		public void keyTyped(KeyEvent e) 
		{
			unsavedChanges(index);
		}
	}
}
