package com.minhduc5a12.chess.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.constants.GameConstants;

public class ImageLoader {
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ImageLoader.class);

    public static Image getImage(String path, int width, int height) {
        return imageCache.computeIfAbsent(path, k -> {
            try {
                java.net.URL imageUrl = ImageLoader.class.getClassLoader().getResource(path);
                if (imageUrl == null) throw new IOException("Cannot find image: " + path);
                Image img = ImageIO.read(imageUrl);
                return img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            } catch (IOException e) {
                log.error("Cannot load image: {}", path, e);
                return null;
            }
        });
    }

    public static Image getSvg(String svgPath, int width, int height, Color color) {
        try {
            InputStream svgStream = ImageLoader.class.getClassLoader().getResourceAsStream(svgPath);
            if (svgStream == null) {
                log.error("Cannot find SVG: {}", svgPath);
                throw new IOException("Cannot find SVG: " + svgPath);
            }

            class CustomImageTranscoder extends ImageTranscoder {
                private BufferedImage image;

                @Override
                public BufferedImage createImage(int w, int h) {
                    image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    return image;
                }

                @Override
                public void writeImage(BufferedImage img, TranscoderOutput output) {
                }

                public BufferedImage getImage() {
                    return image;
                }
            }

            CustomImageTranscoder transcoder = new CustomImageTranscoder();
            transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);

            TranscoderInput input = new TranscoderInput(svgStream);
            transcoder.transcode(input, null);

            BufferedImage renderedImage = transcoder.getImage();
            if (renderedImage == null) {
                throw new IllegalStateException("Failed to render SVG: " + svgPath);
            }

            if (color != null) {
                BufferedImage coloredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = coloredImage.createGraphics();

                g2d.drawImage(renderedImage, 0, 0, null);
                g2d.setComposite(AlphaComposite.SrcAtop);
                g2d.setColor(color);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();

                return coloredImage;
            }

            return renderedImage;
        } catch (IOException | IllegalStateException e) {
            log.error("Cannot load SVG: {}", svgPath, e);
            return null;
        } catch (org.apache.batik.transcoder.TranscoderException e) {
            log.error("Error during SVG transcoding: {}", svgPath, e);
            return null;
        }
    }

    public static void preloadImages() {
        String[] chessPieceName = {"white_pawn.png", "black_pawn.png", "white_rook.png", "black_rook.png", "white_knight.png", "black_knight.png", "white_bishop.png", "black_bishop.png", "white_queen.png", "black_queen.png", "white_king.png", "black_king.png"};
        for (String name : chessPieceName) {
            getImage("images/pieces/" + name, 95, 95);
        }
        getImage("images/chessboard.png", GameConstants.Board.BOARD_WIDTH, GameConstants.Board.BOARD_HEIGHT);
    }

    public static Image rotateImage(Image original, double degrees) {
        int w = original.getWidth(null);
        int h = original.getHeight(null);
        BufferedImage rotated = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        g2d.rotate(Math.toRadians(degrees), w / 2.0, h / 2.0);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        return rotated.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }
}