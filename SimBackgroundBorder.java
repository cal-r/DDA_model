/**
 * SimBackgroundBorder.java
 * 
 * Created on 10-Mar-2005
 * City University
 * BSc Computing with Distributed Systems
 * Project title: Simulating Animal Learning
 * Project supervisor: Dr. Eduardo Alonso 
 * @author Dionysios Skordoulis
 *
 * Modified in October-2009
 * The Centre for Computational and Animal Learning Research 
 * @supervisor Dr. Esther Mondragon 
 * email: e.mondragon@cal-r.org
 * @author Rocio Garcia Duran
 *
 * Modified in July-2011
 * The Centre for Computational and Animal Learning Research 
 * @supervisor Dr. Esther Mondragon 
 * email: e.mondragon@cal-r.org
 * @author Dr. Alberto Fernandez
 * email: alberto.fernandez@urjc.es
 *
 */

package simulator;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;

import javax.swing.ImageIcon;
import javax.swing.border.Border;

public class SimBackgroundBorder implements Border {

	// This method returns true if the specified image has transparent pixels
	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	// This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent
		// Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	/** Default image */
	private BufferedImage image = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_RGB);
	private boolean scale = NO_SCALE;

	public static final boolean SCALE = true;

	public static final boolean NO_SCALE = false;

	/**
	 * Constructs a border that scales the image to fit the bound
	 * 
	 * @param imaginea
	 */
	public SimBackgroundBorder(BufferedImage image) {
		this(image, SCALE);
	}

	/**
	 * @param The
	 *            background image
	 * @param scale
	 *            - if the background image should be scaled
	 */
	public SimBackgroundBorder(BufferedImage image, boolean scale) {
		this.scale = scale;
		this.image = image;
	}

	/**
	 * @param The
	 *            background image
	 */
	public SimBackgroundBorder(Image image) {
		this.image = toBufferedImage(image);
		this.scale = SCALE;
	}

	/**
	 * @param The
	 *            background image
	 * @param scale
	 *            - if the background image should be scaled
	 */
	public SimBackgroundBorder(Image image, boolean scale) {
		this.image = toBufferedImage(image);
		this.scale = scale;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(0, 0, 0, 0);
	}

	public BufferedImage getImage() {
		return image;
	}

	@Override
	public boolean isBorderOpaque() {
		return true; // because insets are empty
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		Graphics2D g2 = (Graphics2D) g;
		if (scale) {
			AffineTransform scale = new AffineTransform();
			scale.scale(((double) width) / ((double) image.getWidth()),
					((double) height) / ((double) image.getHeight()));
			g2.drawImage(image, scale, c);
		} else {
			g2.drawImage(image, g2.getTransform(), c);
		}
	}

}