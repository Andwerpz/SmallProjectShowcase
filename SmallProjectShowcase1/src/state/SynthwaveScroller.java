package state;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.Kernel;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.text.Element;
import javax.swing.text.html.ImageView;

import main.MainPanel;
import util.GraphicsTools;
import util.MathTools;
import util.PerlinNoise;
import util.Vector3D;

public class SynthwaveScroller extends State {

	// BACKGROUND
	Color background_color2 = new Color(232, 52, 229);
	Color background_color1 = new Color(10, 10, 30);
	//Color background_color1 = Color.BLACK;

	// SUN
	int sun_size = 300;
	int sun_pos_x = MainPanel.WIDTH / 2;
	int sun_pos_y = 300;
	Color sun_color1 = new Color(255, 255, 50);
	Color sun_color2 = new Color(255, 20, 195);

	// SUN LINES
	int sun_line_amt = 8;
	double sun_line_start = 0.3; // how far down the sun lines start
	int sun_line_max_size = 20; // sun lines will get bigger the farther down they are
	double sun_line_scroll_speed = 0.35;
	double sun_line_start_counter = 0;

	// LANDSCAPE
	Vector3D landscape_camera_pos = new Vector3D(0, 150, 150);
	double landscape_camera_start_z = 150;
	double landscape_camera_rot_x = Math.toRadians(-20);
	double landscape_camera_rot_y = 0;

	double landscape_cell_size = 100;
	double landscape_start_z = 0;
	double landscape_scroll_speed = 5;
	int landscape_width = 31;
	int landscape_length = 25;
	PerlinNoise landscape_noise1 = new PerlinNoise((int) (Math.random() * 1000), 1, 0.005, 1, 1);
	PerlinNoise landscape_noise2 = new PerlinNoise((int) (Math.random() * 1000), 1, 0.002, 1, 1);

	double[][][] landscape_screen_space = new double[landscape_length][landscape_width][4];

	Color landscape_line_color1 = new Color(219, 94, 235);
	Color landscape_line_color2 = new Color(255, 64, 240);

	// MUSIC
	Clip carpenter_brut;
	TargetDataLine carpenter_brut_line;
	
	int buffer_byte_size = 2048;
	float last_peak = 0;

	public SynthwaveScroller(StateManager gsm) {
		super(gsm);

		// All by Andwerp (2022) not taken from the internet or anything like that
		// everything i have is original
		try {
			
			File dir = new File("./");
			
			File file = new File(dir.getAbsolutePath() + "\\res\\Carpenter-Brut-Anarchy-Road.au");
			//File file = new File("C:\\-=+GAME+=-\\-- Github --\\SmallProjectShowcase\\SmallProjectShowcase1\\res\\Carpenter-Brut-Anarchy-Road.au");
			AudioInputStream in= AudioSystem.getAudioInputStream(file);
			AudioInputStream din = null;
			AudioFormat baseFormat = in.getFormat();
//			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
//			                                            baseFormat.getSampleRate(),
//			                                            16,
//			                                            baseFormat.getChannels(),
//			                                            baseFormat.getChannels() * 2,
//			                                            baseFormat.getSampleRate(),
//			                                            false);
			AudioFormat decodedFormat = new AudioFormat(44100f, 16, 1, true, false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			
			carpenter_brut = AudioSystem.getClip();
			carpenter_brut.open(din);
			
			carpenter_brut_line = AudioSystem.getTargetDataLine(decodedFormat);
			carpenter_brut_line.open(decodedFormat, buffer_byte_size);
			
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float volume = 0.8f;
		FloatControl gainControl = (FloatControl) carpenter_brut.getControl(FloatControl.Type.MASTER_GAIN);        
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * volume) + gainControl.getMinimum();
		gainControl.setValue(gain);
		
		carpenter_brut.loop(Clip.LOOP_CONTINUOUSLY);
		carpenter_brut_line.start();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {

		// upd sun lines
		sun_line_start_counter += sun_line_scroll_speed;

		// upd camera
		landscape_camera_pos.z += landscape_scroll_speed;
		if (landscape_camera_pos.z - landscape_camera_start_z - landscape_start_z > 100) {
			landscape_start_z += 100;
		}
	}

	@Override
	public void draw(Graphics g) {
		GraphicsTools.enableAntialiasing(g);

		// ---BACKGROUND---
		GradientPaint background_gradient = new GradientPaint(0, 0, background_color1, 0, MainPanel.HEIGHT,
				background_color2, false);
		Graphics2D g2D = (Graphics2D) g;
		g2D.setPaint(background_gradient);
		g2D.fillRect(0, 0, MainPanel.WIDTH, MainPanel.HEIGHT);

		// ---SUN---
		//sample from music
		byte[] buf = new byte[2048];
		float[] samples = new float[buffer_byte_size / 2];
		carpenter_brut_line.read(buf, 0, buf.length);
		double avg_amplitude = 0;
		double max_amplitude = 0;
		for(byte b : buf) {
			// convert bytes to samples here
            for(int i = 0, s = 0; i < b;) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768f;
            }
            
            float rms = 0f;
            float peak = 0f;
            for(float sample : samples) {

                float abs = Math.abs(sample);
                if(abs > peak) {
                    peak = abs;
                }

                rms += sample * sample;
            }

            rms = (float)Math.sqrt(rms / samples.length);

            if(last_peak > peak) {
                peak = last_peak * 0.875f;
            }

            last_peak = peak;
		}
		avg_amplitude /= 2048d;
		
		//System.out.println(last_peak);
		
		int sun_adj_size = sun_size;
		BufferedImage sun_image = new BufferedImage(sun_adj_size, sun_adj_size, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics_sun = sun_image.getGraphics();
		GraphicsTools.enableAntialiasing(graphics_sun);

		Graphics2D graphics_sun2D = (Graphics2D) graphics_sun;

		// drawing actual sun
		GradientPaint gradient_sun = new GradientPaint(sun_adj_size / 2, 0, sun_color1, sun_adj_size / 2, sun_adj_size, sun_color2,
				false);
		graphics_sun2D.setPaint(gradient_sun);

		graphics_sun2D.fillOval(0, 0, sun_adj_size, sun_adj_size);

		// drawing transparent lines in sun
		double sun_line_increment = ((double) sun_adj_size * (1d - sun_line_start)) / (double) (sun_line_amt - 1d);
		double sun_line_pos = sun_adj_size * sun_line_start + sun_line_start_counter % sun_line_increment;
		double sun_line_pixel_size_increment = sun_line_max_size / ((double) sun_adj_size * (1d - sun_line_start));

		graphics_sun2D.setComposite(AlphaComposite.Clear);

		for (int i = 0; i < sun_line_amt; i++) {
			double sun_line_cur_size = ((sun_line_pos - (sun_adj_size * sun_line_start)) * sun_line_pixel_size_increment);
			graphics_sun2D.fillRect(0, (int) (sun_line_pos - sun_line_cur_size / 2), sun_adj_size, (int) sun_line_cur_size);
			sun_line_pos += sun_line_increment;
		}

		// applying gaussian blur to sun
		// BufferedImage sun_blurred_image = blurredImage(sun_image, 10);

		// g2D.setComposite(AlphaComposite.Xor);
		// g.drawImage(sun_blurred_image, (MainPanel.WIDTH -
		// sun_blurred_image.getWidth()) / 2, (MainPanel.HEIGHT -
		// sun_blurred_image.getHeight()) / 2, null);

		g.drawImage(sun_image, (sun_pos_x * 2 - sun_image.getWidth()) / 2, (sun_pos_y * 2 - sun_image.getHeight()) / 2,
				null);

		// ---LANDSCAPE---

		// calc landscape
		Vector3D landscape_cur_camera_pos = new Vector3D(landscape_camera_pos);
		double landscape_total_width = landscape_cell_size * (landscape_width - 1);
		for (int i = 0; i < landscape_length; i++) {
			for (int j = 0; j < landscape_width; j++) {
				// compute real space coordinates
				double x = -landscape_total_width / 2 + j * landscape_cell_size;
				double z = landscape_cell_size * i + landscape_start_z;

				double centerBias = Math.min(500, (Math.pow((x * 1.5), 2)) / (Math.pow(landscape_cell_size / 2.5, 2)));
				double trigHills = Math.max(0, Math.cos(x / 300) + Math.sin(z / 300)) * centerBias;
				double noise = Math.max(0, Math.max(landscape_noise1.getHeight(x, z), landscape_noise2.getHeight(x, z)))
						* centerBias * 3;
				double heightBias = Math.min(4000, (Math.pow((x * 1), 2)) / Math.pow((1 * landscape_cell_size), 2));

				double y = noise + heightBias;
				double[] w = new double[1];

				// transform to screen space
				Vector3D camera_space_vec = MathTools.cameraTransform(new Vector3D(x, y, z), landscape_cur_camera_pos,
						landscape_camera_rot_x, landscape_camera_rot_y);
				Vector3D projected_vec = MathTools.projectPoint(camera_space_vec, w);
				Vector3D screen_space_vec = MathTools.scalePoint(projected_vec);

				// save
				landscape_screen_space[i][j][0] = screen_space_vec.x;
				landscape_screen_space[i][j][1] = screen_space_vec.y;
				landscape_screen_space[i][j][2] = screen_space_vec.z;
				landscape_screen_space[i][j][3] = w[0];

			}
		}

		// we can just draw landscape back to front, no need for calculating surface
		// normals.
		
		//we also need to draw from out to in, eg prioritizing high abs val x. 
		g2D.setStroke(new BasicStroke((float) 0.5));
		for (int i = landscape_length - 1; i > 0; i--) {
			int l = 0;
			int r = landscape_width - 2;
			for (int j = 0; j < landscape_width - 1; j++) {
				int next = j % 2 == 0? l++ : r--;
				
				double[] a = landscape_screen_space[i][next];
				double[] b = landscape_screen_space[i - 1][next];
				double[] c = landscape_screen_space[i - 1][next + 1];
				double[] d = landscape_screen_space[i][next + 1];

				if (a[3] < 0 || b[3] < 0 || c[3] < 0 || d[3] < 0) {
					continue;
				}

				g2D.setColor(Color.BLACK);
				g2D.fillPolygon(new int[] { (int) a[0], (int) b[0], (int) c[0] },
						new int[] { (int) a[1], (int) b[1], (int) c[1] }, 3);
				g2D.fillPolygon(new int[] { (int) c[0], (int) d[0], (int) a[0] },
						new int[] { (int) c[1], (int) d[1], (int) a[1] }, 3);
				g2D.drawLine((int) a[0], (int) a[1], (int) c[0], (int) c[1]);

				g2D.setColor(landscape_line_color1);
				g2D.drawLine((int) a[0], (int) a[1], (int) b[0], (int) b[1]);
				g2D.drawLine((int) b[0], (int) b[1], (int) c[0], (int) c[1]);
				g2D.drawLine((int) c[0], (int) c[1], (int) d[0], (int) d[1]);
				g2D.drawLine((int) d[0], (int) d[1], (int) a[0], (int) a[1]);
				
			}
		}

	}

	public static BufferedImage blurredImage(BufferedImage source, double radius) {
		if (radius == 0) {
			return source;
		}

		final int r = (int) Math.ceil(radius);
		final int rows = r * 2 + 1;
		final float[] kernelData = new float[rows * rows];

		final double sigma = radius / 3;
		final double sigma22 = 2 * sigma * sigma;
		final double sqrtPiSigma22 = Math.sqrt(Math.PI * sigma22);
		final double radius2 = radius * radius;

		double total = 0;
		int index = 0;
		double distance2;

		int x, y;
		for (y = -r; y <= r; y++) {
			for (x = -r; x <= r; x++) {
				distance2 = 1.0 * x * x + 1.0 * y * y;
				if (distance2 > radius2) {
					kernelData[index] = 0;
				} else {
					kernelData[index] = (float) (Math.exp(-distance2 / sigma22) / sqrtPiSigma22);
				}
				total += kernelData[index];
				++index;
			}
		}

		for (index = 0; index < kernelData.length; index++) {
			kernelData[index] /= total;
		}

		// We first pad the image so the kernel can operate at the edges.
		BufferedImage paddedSource = paddedImage(source, r);
		BufferedImage blurredPaddedImage = operatedImage(paddedSource,
				new ConvolveOp(new Kernel(rows, rows, kernelData), ConvolveOp.EDGE_ZERO_FILL, null));
		return blurredPaddedImage.getSubimage(r, r, source.getWidth(), source.getHeight());
	}

	/**
	 * Pads the given {@link BufferedImage} on all sides by the given padding
	 * amount.
	 *
	 * @param source  The source image.
	 * @param padding The amount to pad on all sides, in pixels.
	 * @return A new, padded image, or the source image if no padding is performed.
	 */
	public static BufferedImage paddedImage(BufferedImage source, int padding) {
		if (padding == 0) {
			return source;
		}

		BufferedImage newImage = newArgbBufferedImage(source.getWidth() + padding * 2,
				source.getHeight() + padding * 2);
		Graphics2D g = (Graphics2D) newImage.getGraphics();
		g.drawImage(source, padding, padding, null);
		return newImage;
	}

	public static BufferedImage operatedImage(BufferedImage source, BufferedImageOp op) {
		BufferedImage newImage = newArgbBufferedImage(source.getWidth(), source.getHeight());
		Graphics2D g = (Graphics2D) newImage.getGraphics();
		g.drawImage(source, op, 0, 0);
		return newImage;
	}

	/**
	 * Creates a new ARGB {@link BufferedImage} of the given width and height.
	 *
	 * @param width  The width of the new image.
	 * @param height The height of the new image.
	 * @return The newly created image.
	 */
	public static BufferedImage newArgbBufferedImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			carpenter_brut.stop();
			this.exit();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

}
