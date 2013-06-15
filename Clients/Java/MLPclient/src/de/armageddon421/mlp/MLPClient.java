package de.armageddon421.mlp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MLPClient {
	
	private Socket								socket;
	private DataOutputStream					dout;
	private DataInputStream						din;
	
	private final String						name;
	private final byte							mode;
	
	private int									width, height;
	private byte								active;
	
	private final ArrayList<MLPActiveListener>	activeListeners;
	private final ArrayList<MLPReadyListener>	readyListeners;
	
	private byte								frameBuffer[];
	
	public MLPClient(final String ip, final String name, final byte mode) {
		this.name = name;
		this.mode = mode;
		
		activeListeners = new ArrayList<MLPActiveListener>();
		readyListeners = new ArrayList<MLPReadyListener>();
		width = 0;
		height = 0;
		active = 0;
		
		
		try {
			socket = new Socket(ip, 1254);
			
			dout = new DataOutputStream(socket.getOutputStream());
			din = new DataInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			
			sendLogin();
			receivePackage(); // Should be the 'I' Package, containing display
								// width and height
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Receive a Package from the Server. This should be looped (for example in
	 * a Thread) to generate Events.
	 * 
	 * @throws IOException
	 *             when reading from the socket fails
	 */
	public void receivePackage() throws IOException {
		
		byte type = din.readByte();
		int length = din.readInt();
		
		
		switch (type) {
			case 'I':
				width = din.readInt();
				height = din.readInt();
				frameBuffer = new byte[width * height * 3];
				break;
			
			case 'A':
				byte newActive = din.readByte();
				if (active != newActive) {
					active = newActive;
					for (MLPActiveListener al : activeListeners) {
						if (active == 1) {
							al.nowActive();
						}
						else {
							al.nowInactive();
						}
					}
				}
				if (active == 1) {
					// System.out.println("Activated");
				} else {
					// System.out.println("Deactivated");
				}
				
				break;
			
			case 'R':
				for (MLPReadyListener rl : readyListeners) {
					rl.nowReady();
				}
				// System.out.println("Ready");
				break;
			
			default:
				byte data[] = new byte[length];
				din.readFully(data);
				break;
		
		}
		
		
	}
	
	private void send(final byte type, final byte[] data) throws IOException {
		
		dout.writeByte(type);
		if (data == null) {
			dout.writeInt(0);
			
		}
		else {
			dout.writeInt(data.length);
			dout.write(data);
		}
		
		dout.flush();
	}
	
	/**
	 * Send name an mode to the server.
	 * 
	 * @throws IOException
	 */
	private void sendLogin() throws IOException {
		
		
		ByteBuffer bb = ByteBuffer.allocate(name.length() + 1);
		
		bb.put(mode);
		bb.put(name.getBytes());
		
		send((byte) 'L', bb.array());
		
	}
	
	/**
	 * Propose to the server to display a different client now.
	 * 
	 * @throws IOException
	 */
	public void sendYield() throws IOException {
		
		send((byte) 'Y', null);
		
	}
	
	/**
	 * Sends the contents of the frameBuffer to the server for them to be
	 * displayed.
	 * 
	 * @throws IOException
	 */
	public void sendFrame() throws IOException {
		
		send((byte) 'D', frameBuffer);
		
	}
	
	public void addActiveListener(final MLPActiveListener al) {
		activeListeners.add(al);
	}
	
	/**
	 * Add a Ready listener. You should react either by sending a Frame or by
	 * Yielding.
	 * 
	 * @param rl
	 */
	public void addReadyListener(final MLPReadyListener rl) {
		readyListeners.add(rl);
	}
	
	
	public final String getName() {
		return name;
	}
	
	public final byte getMode() {
		return mode;
	}
	
	public final int getWidth() {
		return width;
	}
	
	public final int getHeight() {
		return height;
	}
	
	public final byte getActive() {
		return active;
	}
	
	public byte[] getFrameBuffer() {
		return frameBuffer;
	}
	
	/*
	 * public static void main(final String args[]) {
	 * 
	 * final MLPClient cl = new MLPClient("10.42.14.235", "Penis lol", (byte)
	 * 1);
	 * 
	 * System.out.printf("%d %d", cl.width, cl.height);
	 * 
	 * for (int x = 0; x < cl.width; x++) { for (int y = 0; y < cl.height; y++)
	 * { byte buf[] = cl.getFrameBuffer();
	 * 
	 * buf[(y * cl.width + x) * 3 + 0] = 40; buf[(y * cl.width + x) * 3 + 1] =
	 * 00; buf[(y * cl.width + x) * 3 + 2] = 00; } }
	 * 
	 * cl.addReadyListener(new MLPReadyListener() {
	 * 
	 * int counter = 0;
	 * 
	 * @Override void nowReady() {
	 * 
	 * if (cl.active == 1) { try {
	 * 
	 * 
	 * counter++;
	 * 
	 * //if ((counter % 20) == 0) { cl.sendYield(); } else {
	 * 
	 * Thread.sleep(100); cl.sendFrame(); // }
	 * 
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } } } });
	 * 
	 * 
	 * while (true) {
	 * 
	 * try { cl.receivePackage(); } catch (IOException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 */
	
	/*
	 * int ctr = 0; while (true) { byte type = din.readByte();
	 * System.out.println(type); din.skip(4);
	 * 
	 * if (type == 'R') { ByteBuffer bb = ByteBuffer.allocate(2400 * 3 + 5);
	 * bb.order(ByteOrder.LITTLE_ENDIAN); bb.put((byte) 'D'); bb.putInt(2400 *
	 * 3); for (int i = 0; i < 2400; i++) {
	 * 
	 * // byte val = (byte) (i % 60); if ((i % 59) == (ctr % (59))) {
	 * bb.put((byte) 60); bb.put((byte) 30); bb.put((byte) 30); } else {
	 * 
	 * bb.put((byte) 0); bb.put((byte) 0); bb.put((byte) 0); }
	 * 
	 * } dout.write(bb.array()); dout.flush(); ctr++; }
	 */
	
	// }
}
