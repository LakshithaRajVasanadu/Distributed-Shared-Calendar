import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Calendar implements Serializable{
	
	List<Appointment> appointments;
	
	public Calendar() {
		this.appointments = new ArrayList<Appointment>();
	}
	
	public Calendar(Calendar cal) {
		this.appointments = new ArrayList<Appointment>();
		
		for(Appointment app: cal.appointments) {
			this.appointments.add(new Appointment(app));
		}
	}

	@Override
	public String toString() {
		
		String str = "Calendar[";
		for(Appointment a : appointments) {
			str += "(" + a + ")," ;
		}
		return "]" + str;
	}

	public List<Appointment> getAppointments() {
		return appointments;
	}

	public void setAppointments(List<Appointment> appointments) {
		this.appointments = appointments;
	}
	


}
