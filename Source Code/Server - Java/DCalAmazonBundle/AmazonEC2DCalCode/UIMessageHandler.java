import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class UIMessageHandler{
	Socket clientSoc;
	Node n;

	public  UIMessageHandler(Node n, Socket clientSoc) {
		this.clientSoc = clientSoc;
		this.n = n;
	}
	
	//@Override
	public void run() {
		InputStream ios;
		OutputStream oos;
		
		try{
			ios = clientSoc.getInputStream();
			Scanner scan = new Scanner(ios);
			String message = scan.nextLine();
			
			String response = getResponseMessage(message);
			
			oos = clientSoc.getOutputStream();
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(oos)), true);
            out.println(response);
            out.flush();
			
			
		}catch(IOException e) {

		}
		finally {
			try {
				clientSoc.close();
			} catch (IOException e) {

			}
		}
		
	}
	
	
	public String getResponseMessage(String requestMsg) {
		System.out.println("[UIServer] Working on client request: " + requestMsg);
		requestMsg = requestMsg.trim();
		String response = "failure";
		
		try {
		String[] tokens = requestMsg.split(" ");
		if(tokens[0].equals("schedule")) {
			//System.out.println("Scheduling appointment from client Mobile...");
			
			Appointment appointment = n.parseAppointment(tokens);
			boolean canInsert = n.insertAppointment(appointment);
			if(canInsert == true) {
				response = "success";
			}
			
			if(canInsert == true) {
				System.out.println("[UIServer] Appointment " + appointment.name + " is valid. Trying to commit");
			} else {
				System.out.println("[UIServer] Appointment " + appointment.name + " is conflicting");
			}
		}
		else if(tokens[0].equals("cancel")) {
			Appointment appointment = n.parseAppointment(tokens);
			n.deleteAppointment(appointment);
			response = "success";
			System.out.println("[UIServer] Appointment " + appointment.name + " removed. Trying to commit");
		}
		} catch(Exception ex) {
		//	System.out.println("[UIServer] Some exception occurred..." + ex.getMessage());
		//	ex.printStackTrace();
			response = "failure";
		}
		
		
		return response;
		
	}
	

	
	
}
