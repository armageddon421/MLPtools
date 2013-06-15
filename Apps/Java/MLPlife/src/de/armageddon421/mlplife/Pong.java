package de.armageddon421.mlplife;

import java.io.IOException;

import de.armageddon421.mlp.MLPActiveListener;
import de.armageddon421.mlp.MLPClient;
import de.armageddon421.mlp.MLPReadyListener;

public class Pong {
	
	MLPClient	panel;
	
	long		starttime;
	
	
	double		p1pos, p2pos, p1h, p2h, ballx, bally, ballvx, ballvy;
	
	
	public Pong() {
		
		panel = new MLPClient("10.42.14.235", "Pong", (byte) 0);
		
		panel.addActiveListener(new MLPActiveListener() {
			
			@Override
			public void nowInactive() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void nowActive() {
				starttime = System.currentTimeMillis();
				ballx = panel.getWidth() / 2;
				bally = panel.getHeight() / 2;
				ballvx = 1;
				ballvy = 0.3;
				p1h = 6;
				p2h = 6;
				p1pos = panel.getHeight() / 2 - p1h / 2;
				p2pos = panel.getHeight() / 2 - p2h / 2;
				
			}
		});
		
		
		panel.addReadyListener(new MLPReadyListener() {
			
			@Override
			public void nowReady() {
				
				if (System.currentTimeMillis() > starttime + 10000) {
					try {
						panel.sendYield();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
				
				
				// PLAYER 1
				if (bally > p1pos + 3) {
					p1pos += 0.5;
				}
				else {
					p1pos -= 0.5;
				}
				
				
				if (p1pos > panel.getHeight() - p1h) {
					p1pos = panel.getHeight() - p1h;
				}
				if (p1pos < 0) {
					p1pos = 0;
				}
				
				
				// PLAYER 2
				if (bally > p2pos + 3) {
					p2pos += 0.5;
				}
				else {
					p2pos -= 0.5;
				}
				
				
				if (p2pos > panel.getHeight() - p2h) {
					p2pos = panel.getHeight() - p2h;
				}
				if (p2pos < 0) {
					p2pos = 0;
				}
				
				
				// BALL
				ballx += ballvx;
				bally += ballvy;
				
				if (bally < 0) {
					bally = 0;
					ballvy *= -1;
				}
				else if (bally > panel.getHeight() - 1) {
					bally = panel.getHeight() - 1;
					ballvy *= -1;
				}
				
				if (ballx < 2) {
					ballx = 2;
					ballvx *= -1;
				}
				else if (ballx > panel.getWidth() - 3) {
					ballx = panel.getWidth() - 3;
					ballvx *= -1;
				}
				
				
				byte buf[] = panel.getFrameBuffer();
				
				for (int i = 0; i < (panel.getWidth() * panel.getHeight()); i++) {
					buf[(i) * 3 + 0] *= 0.7;
					buf[(i) * 3 + 1] *= 0.7;
					buf[(i) * 3 + 2] *= 0.7;
					
				}
				
				for (int y = 0; y < 6; y++) {
					
					int p1 = (int) p1pos;
					buf[((y + p1) * 60 + 1) * 3 + 0] = (byte) (40);
					buf[((y + p1) * 60 + 1) * 3 + 1] = (byte) (40);
					buf[((y + p1) * 60 + 1) * 3 + 2] = (byte) (40);
					
					int p2 = (int) p2pos;
					buf[((y + p2) * 60 + panel.getWidth() - 2) * 3 + 0] = (byte) (40);
					buf[((y + p2) * 60 + panel.getWidth() - 2) * 3 + 1] = (byte) (40);
					buf[((y + p2) * 60 + panel.getWidth() - 2) * 3 + 2] = (byte) (40);
					
					
				}
				
				buf[((int) bally * 60 + (int) ballx) * 3 + 0] = (byte) (90);
				buf[((int) bally * 60 + (int) ballx) * 3 + 1] = (byte) (90);
				buf[((int) bally * 60 + (int) ballx) * 3 + 2] = (byte) (90);
				
				
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
		
		Pong pong = new Pong();
		
		
	}
	
}
