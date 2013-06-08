import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Socket clientSocket = new Socket("localhost", 65000);
			DataOutputStream tx = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader rx = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			tx.writeBytes("test123\n");
			MLPhandler handle = new MLPhandler(tx, rx);
			
			clientSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
