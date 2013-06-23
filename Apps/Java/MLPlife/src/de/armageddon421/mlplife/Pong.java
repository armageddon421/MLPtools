package de.armageddon421.mlplife;

import java.io.IOException;

import de.armageddon421.mlp.MLPActiveListener;
import de.armageddon421.mlp.MLPClient;
import de.armageddon421.mlp.MLPReadyListener;

public class Pong {
	
	MLPClient	panel;
	
	long		starttime;
	
	
	double		p1pos, p2pos, p1h, p2h, ballx, bally, ballvx, ballvy;
	int			p1score, p2score;
	
	Drawable	number[];
	
	private void createNumbers() {
		number = new Drawable[10];
		
		number[0] = new Drawable(new int[] { 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 0, 0, 0, 255, 255, 255,
				255, 255, 255, 0, 0, 0, 255, 255, 255,
				255, 255, 255, 0, 0, 0, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255 }, 3);
		number[1] = new Drawable(new int[] { 0, 0, 0, 0, 0, 0, 255, 255, 255,
				0, 0, 0, 0, 0, 0, 255, 255, 255,
				0, 0, 0, 0, 0, 0, 255, 255, 255,
				0, 0, 0, 0, 0, 0, 255, 255, 255,
				0, 0, 0, 0, 0, 0, 255, 255, 255 }, 3);
		number[2] = new Drawable(new int[] { 255, 255, 255, 255, 255, 255, 255, 255, 255,
				0, 0, 0, 0, 0, 0, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 0, 0, 0, 0, 0, 0,
				255, 255, 255, 255, 255, 255, 255, 255, 255 }, 3);
		number[3] = new Drawable(new int[] { 255, 255, 255, 255, 255, 255, 255, 255, 255,
				0, 0, 0, 0, 0, 0, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255,
				0, 0, 0, 0, 0, 0, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255 }, 3);
	}
	
	public Pong() {
		
		createNumbers();
		
		panel = new MLPClient("10.42.14.235", "Pong", (byte) 0);
		
		panel.addActiveListener(new MLPActiveListener() {
			
			@Override
			public void nowInactive() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void nowActive() {
				starttime = System.currentTimeMillis();
				placeBall();
				
				p1h = 6;
				p2h = 6;
				p1pos = panel.getHeight() / 2 - p1h / 2;
				p2pos = panel.getHeight() / 2 - p2h / 2;
				
				p1score = p2score = 0;
				
			}
		});
		
		
		panel.addReadyListener(new MLPReadyListener() {
			
			@Override
			public void nowReady() {
				
				if (System.currentTimeMillis() > starttime + 60000 || p1score >= 3 || p2score >= 3) {
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
				if (ballvx > 0) {
					if (bally > p2pos + 4) {
						p2pos += 0.5;
					}
					else if (bally < p2pos + 2) {
						p2pos -= 0.5;
					}
				}
				else {
					if (panel.getHeight() / 2 > p2pos + 4) {
						p2pos += 0.5;
					}
					else if (panel.getHeight() / 2 < p2pos + 2) {
						p2pos -= 0.5;
					}
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
				
				
				// Hit Left
				if (ballx < 2) {
					ballx = 2;
					
					if (bally >= p1pos && bally <= p1pos + p1h) {
						ballvx *= -1.0;
						ballvy += (p1pos + p1h / 2 - bally) * -0.13;
					}
					else {
						p2score++;
						placeBall();
					}
					
					
				} // Hit right
				else if (ballx > panel.getWidth() - 3) {
					ballx = panel.getWidth() - 3;
					if (bally >= p2pos && bally <= p2pos + p2h) {
						ballvx *= -1.0;
						ballvy += (p2pos + p2h / 2 - bally) * -0.13;
					}
					else {
						p1score++;
						placeBall();
					}
				}
				
				if (Math.abs(ballvy) > 2) {
					ballvy = Math.signum(ballvy) * 2;
				}
				
				
				byte buf[] = panel.getFrameBuffer();
				
				// Fade out
				for (int i = 0; i < (panel.getWidth() * panel.getHeight()); i++) {
					buf[(i) * 3 + 0] *= 0.7;
					buf[(i) * 3 + 1] *= 0.9;
					buf[(i) * 3 + 2] *= 0.7;
					
				}
				
				// border fade
				for (int x = 0; x < 2; x++) {
					for (int y = 0; y < panel.getHeight(); y++) {
						buf[(y * panel.getWidth() + x) * 3 + 0] *= 0.5;
						buf[(y * panel.getWidth() + x) * 3 + 1] *= 0.6;
						buf[(y * panel.getWidth() + x) * 3 + 2] *= 0.5;
						
						buf[(y * panel.getWidth() + panel.getWidth() - x - 1) * 3 + 0] *= 0.5;
						buf[(y * panel.getWidth() + panel.getWidth() - x - 1) * 3 + 1] *= 0.6;
						buf[(y * panel.getWidth() + panel.getWidth() - x - 1) * 3 + 2] *= 0.5;
					}
				}
				
				// Pedals
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
				
				// ball
				buf[((int) bally * 60 + (int) ballx) * 3 + 0] = (byte) (90);
				buf[((int) bally * 60 + (int) ballx) * 3 + 1] = (byte) (90);
				buf[((int) bally * 60 + (int) ballx) * 3 + 2] = (byte) (90);
				
				// background
				for (int x = 0; x < panel.getWidth(); x++) {
					for (int y = 0; y < panel.getHeight(); y++) {
						double bri = Math.random() * 1.7 + 0.7;
						if (y % 2 == 0) {
							bri = 0;
						}
						buf[(y * panel.getWidth() + x) * 3 + 0] += 0;
						buf[(y * panel.getWidth() + x) * 3 + 1] += bri;
						buf[(y * panel.getWidth() + x) * 3 + 2] += 0;
						
						if ((y - 1) % 4 <= 1 && (x == panel.getWidth() / 2 || x == panel.getWidth() / 2 - 1)) {
							buf[(y * panel.getWidth() + x) * 3 + 0] = 15;
							buf[(y * panel.getWidth() + x) * 3 + 1] = 15;
							buf[(y * panel.getWidth() + x) * 3 + 2] = 15;
						}
						
					}
				}
				
				// Scores
				number[p1score].draw(panel, panel.getWidth() / 2 - 6, 1, 0.03);
				number[p2score].draw(panel, panel.getWidth() / 2 + 3, 1, 0.03);
				
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
	
	private void placeBall() {
		ballx = panel.getWidth() / 2;
		bally = panel.getHeight() / 2;
		if (Math.random() < 0.5) {
			ballvx = 1;
		}
		else {
			ballvx = -1;
		}
		ballvy = (Math.random() - 0.5) * 0.7;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		
		Pong pong = new Pong();
		
		
	}
	
}
