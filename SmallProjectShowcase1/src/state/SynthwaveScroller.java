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
import java.util.ArrayList;

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
import util.Vec3;
import util.WavFile;

public class SynthwaveScroller extends State {

	// BACKGROUND
	Color background_color2 = new Color(232, 52, 229);
	Color background_color1 = new Color(10, 10, 30);

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
	double sun_line_scroll_speed = 0.05;
	double sun_line_start_counter = 0;

	// LANDSCAPE
	Vec3 landscape_camera_pos = new Vec3(0, 150, 150);
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

	Color landscape_line_color1 = new Color(235, 100, 235);
	Color landscape_line_color2 = new Color(255, 230, 240);

	// MUSIC
	WavFile carpenter_brut;
	TargetDataLine carpenter_brut_line;

	int sample_rate;
	float last_peak = 0;
	int bit_depth = 65536; //16 bit

	ArrayList<Float> waveform = new ArrayList<>();

	public SynthwaveScroller(StateManager gsm) {
		super(gsm);

		try {
			carpenter_brut = new WavFile(GraphicsTools.loadFile("EvenIfTheWholeWorldWereToLaughAtMe.wav"));
			sample_rate = carpenter_brut.getSampleRate();
		}
		catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		carpenter_brut.play();

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

		//---MUSIC---
		//System.out.println(carpenter_brut.getAudioFormat());
		int cur_frame = carpenter_brut.getClip().getFramePosition();
		//System.out.println(carpenter_brut.getSampleInt(cur_frame));
		int num_sample = 1000;
		float cur_sample = 0;
		int num_discarded = 0;
		for (int i = cur_frame; i < Math.min(cur_frame + num_sample, carpenter_brut.getFramesCount()); i++) {
			float cur = bit_depth / 2 - Math.abs(carpenter_brut.getSampleInt(i) - (bit_depth / 2));
			//System.out.println(cur);
			cur = cur / (bit_depth / 2); //normalizing
			cur_sample += cur;
		}
		cur_sample /= num_sample == num_discarded ? 1f : (float) (num_sample - num_discarded);
		last_peak = (float) Math.max(last_peak * 0.85, cur_sample);
		waveform.add(cur_sample);

		// ---BACKGROUND---
		GradientPaint background_gradient = new GradientPaint(0, 0, background_color1, 0, MainPanel.HEIGHT, background_color2, false);
		Graphics2D g2D = (Graphics2D) g;
		g2D.setPaint(background_gradient);
		g2D.fillRect(0, 0, MainPanel.WIDTH, MainPanel.HEIGHT);

		// ---SUN---
		int sun_adj_size = sun_size + (int) (last_peak * 50f);
		BufferedImage sun_image = new BufferedImage(sun_adj_size, sun_adj_size, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics_sun = sun_image.getGraphics();
		GraphicsTools.enableAntialiasing(graphics_sun);

		Graphics2D graphics_sun2D = (Graphics2D) graphics_sun;

		// drawing actual sun
		GradientPaint gradient_sun = new GradientPaint(sun_adj_size / 2, 0, sun_color1, sun_adj_size / 2, sun_adj_size, sun_color2, false);
		graphics_sun2D.setPaint(gradient_sun);

		graphics_sun2D.fillOval(0, 0, sun_adj_size, sun_adj_size);

		// drawing transparent lines in sun
		double sun_line_increment = (sun_adj_size * (1d - sun_line_start)) / (sun_line_amt - 1d);
		double sun_line_pos = sun_adj_size * sun_line_start + sun_line_start_counter % sun_line_increment;
		double sun_line_pixel_size_increment = sun_line_max_size / (sun_adj_size * (1d - sun_line_start));

		graphics_sun2D.setComposite(AlphaComposite.Clear);

		for (int i = 0; i < sun_line_amt; i++) {
			double sun_line_cur_size = ((sun_line_pos - (sun_adj_size * sun_line_start)) * sun_line_pixel_size_increment);
			graphics_sun2D.fillRect(0, (int) (sun_line_pos - sun_line_cur_size / 2), sun_adj_size, (int) sun_line_cur_size);
			sun_line_pos += sun_line_increment;
		}

		g.drawImage(sun_image, (sun_pos_x * 2 - sun_image.getWidth()) / 2, (sun_pos_y * 2 - sun_image.getHeight()) / 2, null);

		// ---LANDSCAPE---

		//color lines based off of sound
		Color line_color = new Color(235, Math.min(235, 70 + (int) (200f * cur_sample)), 235);

		// calc landscape
		Vec3 landscape_cur_camera_pos = new Vec3(landscape_camera_pos);
		double landscape_total_width = landscape_cell_size * (landscape_width - 1);
		for (int i = 0; i < landscape_length; i++) {
			for (int j = 0; j < landscape_width; j++) {
				// compute real space coordinates
				double x = -landscape_total_width / 2 + j * landscape_cell_size;
				double z = landscape_cell_size * i + landscape_start_z;

				double centerBias = Math.min(500, (Math.pow((x * 1.5), 2)) / (Math.pow(landscape_cell_size / 2.5, 2)));
				double trigHills = Math.max(0, Math.cos(x / 300) + Math.sin(z / 300)) * centerBias;
				double noise = Math.max(0, Math.max(landscape_noise1.getHeight(x, z), landscape_noise2.getHeight(x, z))) * centerBias * 3;
				double heightBias = Math.min(4000, (Math.pow((x * 1), 2)) / Math.pow((1 * landscape_cell_size), 2));

				double y = noise + heightBias;
				double[] w = new double[1];

				// transform to screen space
				Vec3 camera_space_vec = MathTools.cameraTransform(new Vec3(x, y, z), landscape_cur_camera_pos, landscape_camera_rot_x, landscape_camera_rot_y);
				Vec3 projected_vec = MathTools.projectVector(camera_space_vec, w);
				Vec3 screen_space_vec = MathTools.scaleVector(projected_vec);

				// save
				landscape_screen_space[i][j][0] = screen_space_vec.x;
				landscape_screen_space[i][j][1] = screen_space_vec.y;
				landscape_screen_space[i][j][2] = screen_space_vec.z;
				landscape_screen_space[i][j][3] = w[0];

			}
		}

		// we can just draw landscape back to front, no need for calculating surface normals.
		//we also need to draw from out to in, eg prioritizing high abs val x. 
		g2D.setStroke(new BasicStroke((float) 0.5));
		for (int i = landscape_length - 1; i > 0; i--) {
			int l = 0;
			int r = landscape_width - 2;
			for (int j = 0; j < landscape_width - 1; j++) {
				int next = j % 2 == 0 ? l++ : r--;

				double[] a = landscape_screen_space[i][next];
				double[] b = landscape_screen_space[i - 1][next];
				double[] c = landscape_screen_space[i - 1][next + 1];
				double[] d = landscape_screen_space[i][next + 1];

				if (a[3] < 0 || b[3] < 0 || c[3] < 0 || d[3] < 0) {
					continue;
				}

				g2D.setColor(Color.BLACK);
				g2D.fillPolygon(new int[] { (int) a[0], (int) b[0], (int) c[0], (int) d[0] }, new int[] { (int) a[1], (int) b[1], (int) c[1], (int) d[1] }, 4);

				g2D.setColor(line_color);
				g2D.drawLine((int) d[0], (int) d[1], (int) a[0], (int) a[1]);
				if (j == landscape_width - 2) {
					g2D.drawLine((int) a[0], (int) a[1], (int) b[0], (int) b[1]);
					g2D.drawLine((int) c[0], (int) c[1], (int) d[0], (int) d[1]);
				}
				else if (j % 2 == 0) {
					g2D.drawLine((int) a[0], (int) a[1], (int) b[0], (int) b[1]);
				}
				else {
					g2D.drawLine((int) c[0], (int) c[1], (int) d[0], (int) d[1]);
				}
			}
		}

		//draw waveform
		//		g2D.setStroke(new BasicStroke());
		//		g2D.setColor(Color.GREEN);
		//		for(int i = 0; i < waveform.size(); i++) {
		//			g.fillRect(i, 0, 1, (int) (waveform.get(i) * 200));
		//		}

		if (waveform.size() >= MainPanel.WIDTH) {
			waveform.clear();
		}
	}

	@Override
	protected void _exit() {
		this.carpenter_brut.stop();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
