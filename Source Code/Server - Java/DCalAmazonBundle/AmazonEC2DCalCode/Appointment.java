import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Appointment implements Serializable{
	
	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;

		Appointment app1 = this;
		Appointment app2 = (Appointment) obj;
		
		if(app1.name.equals(app2.name)) {
			if(app1.day.equals(app2.day)) {
				if(app1.startTime.equals(app2.startTime)) {
					if(app1.endTime.equals(app2.endTime)) {
						List<Integer> commonParticipants = listIntersection(app1.participants, app2.participants);
						if(commonParticipants.size() == app1.participants.size() && commonParticipants.size() == app2.participants.size()) {
							isEqual = true;
						}
					}
				}
			}
		}
		return isEqual;
	}
	
	public List<Integer> listIntersection(List<Integer> l1, List<Integer> l2) {
		List<Integer> newList = new ArrayList<Integer>();
		for(Integer i : l1) {
			if(l2.contains(i)) {
				newList.add(i);
			}
		}
		return newList;
	}

	@Override
	public String toString() {
		String str = "[";
		
		str += "Name:" + name;
		str += ",Day:" + day;
		str += ",StartTime:" + startTime;
		str += ",EndTime:" + endTime;
		str += ",Participants:(" ;
		for(int i : participants) {
			str += i + "," ;
		}
		return ")]" + str;
	}

	String name;
	String day;
	String startTime;
	String endTime;
	List<Integer> participants;
	
	boolean isAppointment = true;
	
	public Appointment(String name, String day, String startTime, String endTime, List<Integer> participants) {
		this.name = name;
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
		this.participants = participants;
	}
	
	public Appointment(Appointment app) {
		this.name = new String(app.name);
		this.day = new String(app.day);
		this.startTime = new String(app.startTime);
		this.endTime = new String(app.endTime);
		
		this.participants = new ArrayList<Integer>();
		for(Integer i: app.participants) {
			this.participants.add(i);
		}
	}
	
	public Appointment() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public List<Integer> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Integer> participants) {
		this.participants = participants;
	}

	public boolean isAppointment() {
		return isAppointment;
	}

	public void setAppointment(boolean isAppointment) {
		this.isAppointment = isAppointment;
	}
	
	

}
