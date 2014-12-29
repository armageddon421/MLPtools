package de.armageddon421.mlplife;

import java.io.IOException;

import de.armageddon421.mlp.MLPActiveListener;
import de.armageddon421.mlp.MLPClient;
import de.armageddon421.mlp.MLPReadyListener;
import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OnlineChecker {

    MLPClient panel;
    long starttime;
    boolean firstTimeOver;
    boolean beforeReachable;

    public OnlineChecker(String ip) {

        panel = new MLPClient(ip, "Online Checker", (byte) 0);

        panel.addActiveListener(new MLPActiveListener() {

            @Override
            public void nowInactive() {
                System.out.println("inactive");
            }

            @Override
            public void nowActive() {
                System.out.println("active");
                starttime = System.currentTimeMillis();
            }
        });

        panel.addReadyListener(new MLPReadyListener() {

            @Override
            public void nowReady() {

                if (System.currentTimeMillis() > starttime + 60000) {
                    try {
                        panel.sendYield();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return;

                }

                int panelWidth = panel.getWidth();
                int panelHeight = panel.getHeight();

                BufferedImage img = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);

                boolean reachable = false;

                try {
                    Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
                    int returnVal = p1.waitFor();
                    reachable = (returnVal == 0);
                    p1.destroy();
                } catch (UnknownHostException ex) {
                    Logger.getLogger(OnlineChecker.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(OnlineChecker.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OnlineChecker.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (reachable) {
                    System.out.println("Inet is up");
                    img.getGraphics().drawString("UP", 5, 25);
                } else {
                    System.out.println("Inet is down");
                    img.getGraphics().drawString("DOWN", 5, 25);
                }

                if (!firstTimeOver || reachable != beforeReachable) {

                    byte buffer[] = panel.getFrameBuffer();

                    for (int y = 0; y < panelHeight; y++) {
                        for (int x = 0; x < panelWidth; x++) {
                            if (img.getRGB(x, y) != -16777216) {
                                if (reachable) {
                                    buffer[(y * panelWidth + x) * 3 + 1] = (byte) 0xFF;
                                } else {
                                    buffer[(y * panelWidth + x) * 3 + 0] = (byte) 0xFF;
                                }
                            } else {
                                buffer[(y * panelWidth + x) * 3 + 0] = (byte) 0;
                                buffer[(y * panelWidth + x) * 3 + 1] = (byte) 0;
                                buffer[(y * panelWidth + x) * 3 + 2] = (byte) 0;
                            }
                        }
                    }
                }
                try {
                    panel.sendFrame();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                firstTimeOver = true;
                beforeReachable = reachable;
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

    // 
    /**
     * @param args
     */
    public static void main(final String[] args) {
        String ip = "151.217.8.29";
        if (args.length == 1) {
            ip = args[0];
        }

        OnlineChecker checker = new OnlineChecker(ip);
    }
}
