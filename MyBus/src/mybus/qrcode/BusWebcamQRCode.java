package mybus.qrcode;

import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.github.sarxos.webcam.Webcam;

import mybus.qrcode.QRCode;
//for debugging only
public class BusWebcamQRCode {
	public static void main(String args[]) throws InterruptedException{
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.pack();
		frame.setVisible(true);
		
		Webcam webcam = Webcam.getWebcams().get(0);
		
		webcam.open();
		
		for(int i = 0; i < 1000; ++ i){
			frame.getContentPane().add(new JLabel(new ImageIcon(webcam.getImage())));
			frame.pack(); frame.repaint();
			
			Thread.sleep(200);
			frame.getContentPane().removeAll();
			
			try{
				String content = QRCode.readQRCode(webcam.getImage());
				System.out.println("*********************\r\n" + content + "\r\n");
				webcam.close();
				break;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		frame.setVisible(false);
		frame.dispose();
	}
}
