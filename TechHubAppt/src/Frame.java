import javax.swing.JButton;
import javax.swing.JFrame;

public class Frame 
{
	static JFrame f;
	static JButton newAppt;
	static JButton deleteAppt;
	static JButton printSchedule;
	static JButton viewAppts;
	static void init()
	{
		f = new JFrame("Tech Hub Appointments");
		f.setBounds(10,10,800,600);
	}
}
