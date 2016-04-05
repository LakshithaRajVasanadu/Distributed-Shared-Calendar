import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class UIServer implements Runnable{
	Node node;
	
	public UIServer(Node node) {
		this.node = node;
	}

	@Override
	public void run() {
		try {

		
			ServerSocket serverSoc = new ServerSocket(node.uiPort);
			while(true) {
				System.out.println("[UISERVER] Waiting for client to connect");
				Socket receiverSoc = serverSoc.accept();
				
				new UIMessageHandler(node, receiverSoc).run();
			}
		} catch (IOException e) {
			
		} catch (Exception e) {
			
		}
	}
	
	
//	public static void main(String[] args) {
//		new UIServer().run();
//	}
	
}
