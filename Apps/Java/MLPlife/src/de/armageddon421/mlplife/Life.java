package de.armageddon421.mlplife;

import java.io.IOException;

import de.armageddon421.mlp.MLPActiveListener;
import de.armageddon421.mlp.MLPClient;
import de.armageddon421.mlp.MLPReadyListener;

public class Life {
	
	MLPClient					panel;
	
	int							world[];
	int							world2[];
	int							blur[];
	int							sums[];
	int							framecounter;
	boolean						endFixed;
	
	
	long						lastCycle;
	long						endtime;
	
	private static final int	numSums	= 10;
	
	
	public Life() {
		
		panel = new MLPClient("10.42.14.235", "Game of Life!", (byte) 0);
		
		
		panel.addActiveListener(new MLPActiveListener() {
			
			@Override
			public void nowInactive() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void nowActive() {
				setupLife(panel.getWidth(), panel.getHeight());
				endtime = System.currentTimeMillis() + 120000;
			}
		});
		
		
		panel.addReadyListener(new MLPReadyListener() {
			
			@Override
			public void nowReady() {
				
				if (System.currentTimeMillis() > lastCycle + 100) {
					lastCycle = System.currentTimeMillis();
					lifeCycle();
				}
				else {
					beautyCycle();
				}
				
				byte buf[] = panel.getFrameBuffer();
				
				if (System.currentTimeMillis() > endtime) {
					try {
						panel.sendYield();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
				else {
					
					for (int y = 0; y < panel.getHeight(); y++) {
						for (int x = 0; x < panel.getWidth(); x++) {
							int cell = getCell(world, x, y);
							byte tmp = 0;
							if (cell >= 1) {
								cell -= 1;
								if (cell > 255) {
									cell = 255;
								}
								buf[(y * panel.getWidth() + x) * 3 + 0] = (byte)
										(127 - cell / 2);
								buf[(y * panel.getWidth() + x) * 3 + 1] = (byte)
										(127 + cell / 2);
								buf[(y * panel.getWidth() + x) * 3 + 2] = (byte)
										(127 - cell / 2);// - cell);
								
							}
							else {
								cell /= -2;
								if (cell > 30) {
									cell = 30;
								}
								
								
								int bl = getCell(blur, x, y);
								
								if (bl > 10) {
									bl = 10;
								}
								
								buf[(y * panel.getWidth() + x) * 3 + 0] = (byte) (30 - cell + bl);
								buf[(y * panel.getWidth() + x) * 3 + 1] = (byte) (0);
								buf[(y * panel.getWidth() + x) * 3 + 2] = (byte) (0);
							}
							
							
						}
					}
					
					// buf[40 * 60 * 3 - 3 + 1] = (byte) 100;
					
					
					// try {
					// Thread.sleep(500);
					// } catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					// e1.printStackTrace();
					// }
					
					
					try {
						panel.sendFrame();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
	
	void setupLife(final int width, final int height) {
		
		world = new int[width * height];
		world2 = new int[width * height];
		blur = new int[width * height];
		sums = new int[numSums];
		
		framecounter = 0;
		endFixed = false;
		
		fillRandom();
	}
	
	int getCell(final int world[], final int x, final int y) {
		return world[((y % panel.getHeight() + panel.getHeight()) % panel.getHeight()) * panel.getWidth()
				+ ((x % panel.getWidth() + panel.getWidth()) % panel.getWidth())];
	}
	
	void setCell(final int world[], final int x, final int y, final int value) {
		world[((y % panel.getHeight() + panel.getHeight()) % panel.getHeight()) * panel.getWidth()
				+ ((x % panel.getWidth() + panel.getWidth()) % panel.getWidth())] = value;
	}
	
	void fillPattern() {
		
		// glider
		setCell(world, 2, 4, 1);
		setCell(world, 3, 4, 1);
		setCell(world, 4, 4, 1);
		setCell(world, 4, 3, 1);
		setCell(world, 3, 2, 1);
		
		// block
		setCell(world, 20, 2, 1);
		setCell(world, 20, 3, 1);
		setCell(world, 21, 2, 1);
		setCell(world, 20, 3, 1);
		
		// line
		setCell(world, 30, 3, 1);
		setCell(world, 31, 3, 1);
		setCell(world, 32, 3, 1);
	}
	
	void fillRandom() {
		
		
		for (int i = 0; i < world.length; i++) {
			
			if (Math.random() < 0.10) {
				world[i] = 1;
			} else {
				world[i] = 0;
			}
			
		}
		
		
	}
	
	int neighborsAlive(final int world[], final int x, final int y) {
		int sum = 0;
		if (getCell(world, x + 1, y) > 0) {
			sum++;
		}
		if (getCell(world, x - 1, y) > 0) {
			sum++;
		}
		if (getCell(world, x + 1, y + 1) > 0) {
			sum++;
		}
		if (getCell(world, x - 1, y + 1) > 0) {
			sum++;
		}
		if (getCell(world, x + 1, y - 1) > 0) {
			sum++;
		}
		if (getCell(world, x - 1, y - 1) > 0) {
			sum++;
		}
		if (getCell(world, x, y + 1) > 0) {
			sum++;
		}
		if (getCell(world, x, y - 1) > 0) {
			sum++;
		}
		return sum;
	}
	
	void addRadialBlur(final int px, final int py, final double radius, final double intensity) {
		for (int y = 0; y < panel.getHeight(); y++) {
			for (int x = 0; x < panel.getWidth(); x++) {
				double dist = Math.sqrt(Math.pow((px - x), 2) + Math.pow((py - y), 2));
				dist = radius - dist;
				if (dist > 0) {
					setCell(blur, x, y, (int) (dist * intensity) + getCell(blur, x, y));
				}
				
			}
		}
	}
	
	void lifeCycle() {
		// addRadialBlur(10, 10, 10.0, 5.0);
		
		for (int y = 0; y < panel.getHeight(); y++) {
			for (int x = 0; x < panel.getWidth(); x++) {
				int cell = getCell(world, x, y);
				int neighbors = neighborsAlive(world, x, y);
				if (neighbors == 3) {
					if (cell <= 0) {
						setCell(world2, x, y, 1);
						addRadialBlur(x, y, 4.0, 0.8);
					}
					else {
						setCell(world2, x, y, cell + 1);
					}
				}
				else if (cell >= 1 && neighbors == 2) {
					setCell(world2, x, y, cell + 1);
				}
				else {
					if (cell <= 0) {
						setCell(world2, x, y, cell - 1);
					}
					else {
						setCell(world2, x, y, 0);
						addRadialBlur(x, y, 4.0, 1.0);
					}
				}
			}
		}
		
		
		int index = framecounter % numSums;
		sums[index] = 0;
		for (int y = 0; y < panel.getHeight(); y++) {
			for (int x = 0; x < panel.getWidth(); x++) {
				blur[y * panel.getWidth() + x] *= 0.9;
				if (blur[y * panel.getWidth() + x] < 0) {
					blur[y * panel.getWidth() + x] = 0;
				}
				
				if (getCell(world2, x, y) > 0) {
					int value = (y * panel.getWidth() + x) + 1;
					sums[index] ^= value;
				}
			}
		}
		
		
		if (!endFixed) {
			for (int i = 0; i < numSums; i++) {
				
				if (i != index && sums[i] > 0 && sums[i] == sums[index]) {
					endFixed = true;
					endtime = System.currentTimeMillis() + 3000;
				}
				// System.out.printf("%10d ", sums[i]);
				
			}
			// System.out.println();
		}
		
		
		int temp[];
		temp = world;
		world = world2;
		world2 = temp;
		
		framecounter++;
		
	}
	
	void beautyCycle() {
		
		for (int y = 0; y < panel.getHeight(); y++) {
			for (int x = 0; x < panel.getWidth(); x++) {
				int cell = getCell(world, x, y);
				if (cell >= 1) {
					setCell(world, x, y, cell + 1);
				}
				else {
					setCell(world, x, y, cell - 1);
				}
				// blur[y * panel.getWidth() + x] *= 0.9;
				// if (blur[y * panel.getWidth() + x] < 0) {
				// blur[y * panel.getWidth() + x] = 0;
				// }
			}
		}
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		
		Life life = new Life();
		
		
	}
	
}
