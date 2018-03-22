package nl.tue.robots.drones.fileIO;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class Images {

    public static BufferedImage convertImageColor(BufferedImage image, Color to, int alpha) {
        BufferedImage img = deepCopy(image);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color col = new Color(img.getRGB(x, y), true);
                int nAlpha = col.getAlpha();
                if (nAlpha < 50) {
                    //skip transparent pixels
                    continue;
                }
                if (alpha >= 0) {
                    nAlpha = alpha;
                }
                Color colTo = new Color(to.getRed(), to.getGreen(), to.getBlue(), nAlpha);
                img.setRGB(x, y, colTo.getRGB());
            }
        }
        return img;
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

}
