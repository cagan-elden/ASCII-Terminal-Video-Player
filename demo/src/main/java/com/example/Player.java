package com.example;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class Player {
    public static void main(String[] args) {
        try {
            File sobelFile = new File("vidSrc/sobel.mp4");
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(sobelFile.getAbsoluteFile());

            frameGrabber.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            int frameNum = frameGrabber.getLengthInFrames();

            // Frame scaler (Do not play with these!)
            int powershellWidth = 160;
            int sobelHeight = frameGrabber.getImageWidth();
            int sobelWidth = frameGrabber.getImageHeight();
            int powershellHeight = (int) (((double) powershellWidth / sobelWidth) * sobelHeight * 0.5);

            BufferedImage scaledImage = new BufferedImage(powershellWidth, powershellHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaledImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            StringBuilder frameOnTerminal = new StringBuilder((powershellWidth+1) * powershellHeight);

            int[] RGB;
            for (int frameId=0; frameId < frameNum-1; frameId++) {
                Frame frame = frameGrabber.grabImage();
                BufferedImage image = converter.convert(frame);

                g2d.drawImage(image, 0, 0, powershellWidth, powershellHeight, null); // do not touch this line too

                frameOnTerminal.setLength(0);

                RGB = new int[]{0, 0, 0};
                long fpsMilli = 40;
                for (int y=0; y < powershellHeight; y++) {
                    for (int x=0; x < powershellWidth; x++) {
                        int pixel = scaledImage.getRGB(x, y);

                        RGB[0] = (pixel >> 16) & 0xFF;
                        RGB[1] = (pixel >> 8) & 0xFF;
                        RGB[2] = pixel & 0xFF;

                        if (RGB[2] < 90)  frameOnTerminal.append(" ");
                        if (RGB[2] >= 90 && RGB[2] < 127) frameOnTerminal.append("*");
                        if (RGB[2] >= 127) frameOnTerminal.append("%");
                    }
                    frameOnTerminal.append("\n");
                }

                try { TimeUnit.MILLISECONDS.sleep(fpsMilli); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

                System.out.print("\033[H\033[2J");
                System.out.print(frameOnTerminal.toString());
                System.out.flush();
            }

        } catch (IOException e) {
            System.out.println("Encountered an error: " + e.getMessage());
        }
    }
}
