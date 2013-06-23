package de.armageddon421.mlplife;

import java.io.IOException;

import de.armageddon421.mlp.MLPActiveListener;
import de.armageddon421.mlp.MLPClient;
import de.armageddon421.mlp.MLPReadyListener;

public class Test {
	
	MLPClient	panel;
	
	long		starttime;
	
	int			tmp;
	
	
	public Test() {
		
		panel = new MLPClient("10.42.14.235", "Game of Life!", (byte) 0);
		
		panel.addActiveListener(new MLPActiveListener() {
			
			@Override
			public void nowInactive() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void nowActive() {
				starttime = System.currentTimeMillis();
				
			}
		});
		
		
		panel.addReadyListener(new MLPReadyListener() {
			
			@Override
			public void nowReady() {
				
				
				byte buf[] = panel.getFrameBuffer();
				
				
				for (int y = 0; y < 40; y++) {
					for (int x = 0; x < 60; x++) {
						if ((y * 60 + x) % 2400 == tmp % 2400) {// || ((-x +
																// 100) % 10) ==
																// tmp % 10
							// || ((-y + 100) % 10) == tmp % 10) {
							buf[(y * 60 + x) * 3 + 0] = (byte) (Math.random() * 0 + 250);
							buf[(y * 60 + x) * 3 + 1] = (byte) (Math.random() * 0 + 250);
							buf[(y * 60 + x) * 3 + 2] = (byte) (Math.random() * 0 + 250);
						}
						else {
							buf[(y * 60 + x) * 3 + 0] = (byte) (Math.random() * 2 + 0);
							buf[(y * 60 + x) * 3 + 1] = (byte) (Math.random() * 0 + 0);
							buf[(y * 60 + x) * 3 + 2] = (byte) (Math.random() * 0 + 0);
						}
						
					}
					
				}
				tmp++;
				
				
				try {
					panel.sendFrame();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		
		try {
			while (true) {
				panel.receivePackage();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		
		Test test = new Test();
		
		
	}
	
}
