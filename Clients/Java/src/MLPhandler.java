import java.io.BufferedReader;
import java.io.DataOutputStream;


public class MLPhandler {
	DataOutputStream tx;
	BufferedReader rx;
	public MLPhandler(DataOutputStream tx, BufferedReader rx){
		this.tx = tx;
		this.rx = rx;
	}
	
}
