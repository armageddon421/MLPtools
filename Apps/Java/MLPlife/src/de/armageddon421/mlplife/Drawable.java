package de.armageddon421.mlplife;

import de.armageddon421.mlp.MLPClient;


public class Drawable {
	
	int[]	data;
	int		width;
	
	public Drawable(final int[] data, final int width) {
		
		this.data = data;
		this.width = width;
		
	}
	
	public void draw(final MLPClient panel, final int x, final int y, final double bri) {
		
		for (int i = 0; i < data.length / 3; i++) {
			int pp = (((y + i / width) * panel.getWidth()) + x + (i % width)) * 3;
			panel.getFrameBuffer()[pp + 0] += data[i * 3 + 0] * bri;
			panel.getFrameBuffer()[pp + 1] += data[i * 3 + 1] * bri;
			panel.getFrameBuffer()[pp + 2] += data[i * 3 + 2] * bri;
		}
	}
}
