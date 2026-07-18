package com.example;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class App 
{
    public static void main( String[] args )
    {
        try {
            File videoFile = new File("vidSrc/sample.mp4");

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFile.getAbsoluteFile());

            frameGrabber.start();

            Java2DFrameConverter converter  = new Java2DFrameConverter();
            int frameNum = frameGrabber.getLengthInFrames();

            frameGrabber.setFrameNumber(0);
            Frame frameDetailAccessor = frameGrabber.grab();

            // setting up a recorder to record each frame
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("vidSrc/sobel.mp4", frameDetailAccessor.imageWidth, frameDetailAccessor.imageHeight);

            recorder.setFormat("mp4");
            recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
            recorder.setPixelFormat(org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P);
            recorder.setFrameRate(frameGrabber.getFrameRate());
            recorder.setVideoBitrate(2000000);

            recorder.start();

            frameGrabber.setFrameNumber(0);

            // edge-detection implementation frame-by-frame
            int[] RGB;
            int[] RGBSum;

            int[][] horizontalKernel = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
            };

            int[][] verticalKernel = {
                {-1,-2,-1},
                { 0, 0, 0},
                { 1, 2, 1}
            };

            for (int frameId=0; frameId < frameNum-1; frameId++) {
                // helps to check if java is working or not (DON'T DELETE!)
                if (frameId % 10 == 0) {
                    System.out.println("Processing frame: " + frameId + " / " + frameNum);
                }

                frameGrabber.setFrameNumber(frameId);
                Frame frame = frameGrabber.grabImage();

                /*
                    image          : frame
                    grayscaleImage : grayscale-version of the image to prepare for blurriness
                    blurredImage   : blurred-grayscale to make edge-detection easier
                    imageWithEdges : using blurred-image, the image generated which can be understood by computer
                */

                BufferedImage image = converter.convert(frame);

                int width   = image.getWidth();
                int height  = image.getHeight();
                int imgType = image.getType();

                BufferedImage grayscaleImage = new BufferedImage(width, height, imgType);
                BufferedImage blurredImage   = new BufferedImage(width, height, imgType);
                BufferedImage imageWithEdges = new BufferedImage(width, height, imgType);

                // grayscale-convertor
                RGB = new int[]{0, 0, 0};
                for (int y=0; y < height; y++) {
                    for (int x=0; x < width; x++) {
                        int pixel = image.getRGB(x, y);

                        RGB[0] = (pixel >> 16) & 0xff;
                        RGB[1] = (pixel >> 8)  & 0xff;
                        RGB[2] = pixel & 0xff;

                        int avgPix = (RGB[0]+RGB[1]+RGB[2])/3;
                        int newPix = (avgPix << 16) | (avgPix << 8) | avgPix;

                        grayscaleImage.setRGB(x, y, newPix);
                    }
                }

                // blur-convertor
                RGBSum = new int[]{0, 0, 0};
                RGB    = new int[]{0, 0, 0};
                for (int y=1; y < height-1; y++) {
                    for (int x=1; x < width-1; x++) {

                        for (int ky=-1; ky <= 1; ky++) {
                            for (int kx=-1; kx <= 1; kx++) {
                                int pixel = grayscaleImage.getRGB(x+kx, y+ky);

                                RGBSum[0] = (pixel >> 16) & 0xFF;
                                RGBSum[1] = (pixel >> 8)  & 0xFF;
                                RGBSum[2] = pixel & 0xFF;
                            }
                        }

                        RGB[0] = RGBSum[0]/9;
                        RGB[1] = RGBSum[1]/9;
                        RGB[2] = RGBSum[2]/9;

                        int newPix = (RGB[0] << 16) | (RGB[1] << 8) | RGB[2];
                        blurredImage.setRGB(x, y, newPix);
                    }
                }

                // sobel-convertor
                for (int y=1; y < height-1; y++) {
                    for (int x=1; x < width-1; x++) {

                        int gradientX=0, gradientY=0;
                        for (int ky=-1; ky <= 1; ky++) {
                            for (int kx=-1; kx <= 1; kx++) {
                                int pixel     = blurredImage.getRGB(x+kx, y+ky);
                                int luminance = pixel & 0xFF;

                                gradientX += luminance*horizontalKernel[ky+1][kx+1];
                                gradientY += luminance*verticalKernel[ky+1][kx+1];
                            }
                        }

                        int magnitude = (int) Math.sqrt((gradientX*gradientX) + (gradientY*gradientY));

                        if (magnitude > 255) magnitude=255;
                        if (magnitude < 0)   magnitude=0;

                        int newPixel = (magnitude << 16) | (magnitude << 8) | magnitude;
                        imageWithEdges.setRGB(x, y, newPixel);
                    }
                }

                Frame frameWithEdges = converter.convert(imageWithEdges);
                recorder.record(frameWithEdges);
            }

            frameGrabber.stop();
            frameGrabber.release();

            recorder.stop();
            recorder.release();
        } catch (IOException e) {
            System.out.println("Encountered an error: " + e.getMessage());
        }
    }
}
