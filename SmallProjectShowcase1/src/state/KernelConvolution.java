package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import main.MainPanel;
import util.GraphicsTools;

public class KernelConvolution extends State{
	
	BufferedImage img;
	
	BufferedImage result;
	
	// 1 coefficient
	int[][] ridge = new int[][] {
			{0, 1, 0},
			{1, -4, 1},
			{0, 1, 0}
	};
	
	// 1 coefficient
	int[][] sharpen = new int[][] {
			{0, -1, 0},
			{-1, 5, -1},
			{0, -1, 0}
	};
	
	// 1 / 16 coefficient
	int[][] blur = new int[][] {
		{1, 2, 1},
		{2, 4, 2},
		{1, 2, 1}
	};
	
	int[][] emboss = new int[][] {
		{-2, -1, 0}, 
		{-1, 1, 1}, 
		{0, 1, 2}
	};
	
	int[][] sobelVert = new int[][] {
		{1, 2, 1},
		{0, 0, 0},
		{-1, -2, -1}
	};
	
	int[][] sobelHoriz = new int[][] {
		{1, 0, -1},
		{2, 0, -2},
		{1, 0, -1}
	};
	
	int[][] sobelSum = new int[][] {
		{2, 2, 0},
		{2, 0, -2},
		{0, -2, -2}
	};
	
	double coefficient = 1;

	public KernelConvolution(StateManager gsm) {
		super(gsm);
		
		this.img = GraphicsTools.loadImage("/bike.png");
		//this.result = convoluteImage(img, sobelHoriz, coefficient);
		this.result = this.sobelEdgeDetect(img);
	}
	
	public BufferedImage convoluteImage(BufferedImage img, int[][] kernel, double coefficient) {
		BufferedImage ans = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		for(int i = 0; i < img.getWidth() - kernel.length; i++) {
			for(int j = 0; j < img.getHeight() - kernel[0].length; j++) {
				double[] rgb = new double[3];
				int[][][] channels = new int[3][kernel.length][kernel[0].length];
				
				//load color channels
				for(int r = 0; r < kernel.length; r++) {
					for(int c = 0; c < kernel[0].length; c++) {
						int color = img.getRGB(i + r, j + c);
						channels[2][r][c] = (color & 0xff);
						channels[1][r][c] = ((color & 0xff00) >> 8);
						channels[0][r][c] = ((color & 0xff0000) >> 16);
					}
				}
				
				//System.out.println("color channels loaded");
				
				for(int k = 0; k < 3; k++) {
					int[][] convChannel = matrixCompMult(kernel, channels[k]);
					//System.out.println((k + 1) + " convolution done");
					
					for(int r = 0; r < kernel.length; r++) {
						for(int c = 0; c < kernel[0].length; c++) {
							rgb[k] += convChannel[r][c];
						}
					}
					
					rgb[k] *= coefficient;
					rgb[k] = Math.min(255, rgb[k]);
					rgb[k] = Math.max(0, rgb[k]);
				}
				
				//System.out.println("convolution done");
				
				int rgbInt = ((int) rgb[0] << 16) + ((int) rgb[1] << 8) + (int) rgb[2];
				
				ans.setRGB(i, j, rgbInt);
			}
		}
		
		return ans;
	}
	
	public BufferedImage sobelEdgeDetect(BufferedImage img) {
		BufferedImage ans = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		for(int i = 0; i < img.getWidth() - sobelHoriz.length; i++) {
			for(int j = 0; j < img.getHeight() - sobelHoriz.length; j++) {
				double[] rgbHoriz = new double[3];
				double[] rgbVert = new double[3];
				int[][][] channels = new int[3][sobelHoriz.length][sobelHoriz[0].length];
				
				//load color channels
				for(int r = 0; r < sobelHoriz.length; r++) {
					for(int c = 0; c < sobelHoriz[0].length; c++) {
						int color = img.getRGB(i + r, j + c);
						channels[2][r][c] = (color & 0xff);
						channels[1][r][c] = ((color & 0xff00) >> 8);
						channels[0][r][c] = ((color & 0xff0000) >> 16);
					}
				}
				
				//System.out.println("color channels loaded");
				
				for(int k = 0; k < 3; k++) {
					int[][] convChannel = matrixCompMult(sobelHoriz, channels[k]);
					//System.out.println((k + 1) + " convolution done");
					
					for(int r = 0; r < sobelHoriz.length; r++) {
						for(int c = 0; c < sobelHoriz[0].length; c++) {
							rgbHoriz[k] += convChannel[r][c];
						}
					}
				}
				
				for(int k = 0; k < 3; k++) {
					int[][] convChannel = matrixCompMult(sobelVert, channels[k]);
					//System.out.println((k + 1) + " convolution done");
					
					for(int r = 0; r < sobelVert.length; r++) {
						for(int c = 0; c < sobelVert[0].length; c++) {
							rgbVert[k] += convChannel[r][c];
						}
					}
				}
				
				double[] rgb = new double[3];
				for(int k = 0; k < 3; k++) {
					rgb[k] = Math.sqrt(rgbHoriz[k] * rgbHoriz[k] + rgbVert[k] * rgbVert[k]);
					rgb[k] = Math.min(255, rgb[k]);
					rgb[k] = Math.max(0, rgb[k]);
				}
				
				//System.out.println("convolution done");
				
				int rgbInt = ((int) rgb[0] << 16) + ((int) rgb[1] << 8) + (int) rgb[2];
				
				ans.setRGB(i, j, rgbInt);
			}
		}
		
		return ans;
	}
	
	//assuming the matrices have proper dimensions
	public static int[][] matrixMult(int[][] a, int[][] b) {
		if(a.length != b[0].length || a[0].length != b.length) {
			System.out.println("unequal dimensions");
			return null;
		}
		
		int[][] ans = new int[a.length][b[0].length];
		for(int i = 0; i < ans.length; i++) {
			for(int j = 0; j < ans[0].length; j++) {
				//calc current cell's ans
				for(int k = 0; k < a.length; k++) {
					ans[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		
		return ans;
	}
	
	//ans[r][c] = a[r][c] * b[r][c]
	public static int[][] matrixCompMult(int[][] a, int[][] b){
		if(a.length != b.length || a[0].length != b[0].length) {
			System.out.println("unequal dimensions");
			return null;
		}
		
		int[][] ans = new int[a.length][a[0].length];
		for(int i = 0; i < ans.length; i++) {
			for(int j = 0; j < ans[0].length; j++) {
				ans[i][j] = a[i][j] * b[i][j];
			}
		}
		
		return ans;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(this.img, 0, 0, null);
		g.drawRect(MainPanel.WIDTH - this.result.getWidth(), MainPanel.HEIGHT - this.result.getHeight(), this.result.getWidth(), this.result.getHeight());
		g.drawImage(this.result, MainPanel.WIDTH - this.result.getWidth(), MainPanel.HEIGHT - this.result.getHeight(), null);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
