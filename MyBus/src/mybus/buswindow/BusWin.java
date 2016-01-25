package mybus.buswindow;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import mybus.qrcode.QRCode;

import com.github.sarxos.webcam.Webcam;

public class BusWin {

	private JFrame frame;
	private JPanel mainPanel;
	private JLabel resultLabel;

	private static Analyser analyser = null;
	private static String QRCodeContent = "unmodified";

	// private final Action scan = new OpenCam();

	/**
	 * Launch the application Cong , Logconf
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BusWin window = new BusWin();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public BusWin() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("BUS System");
		frame.getContentPane().setLayout(null);

		// Main Panel
		mainPanel = new JPanel();
		mainPanel.setBorder(null);
		mainPanel.setBounds(29, 6, 271, 135);
		frame.getContentPane().add(mainPanel);
		mainPanel.setLayout(null);

		JLabel scLabel = new JLabel("Scan Ticket");
		scLabel.setFont(new Font("Georgia", Font.BOLD, 16));
		scLabel.setBounds(66, 6, 128, 21);
		mainPanel.add(scLabel);
		scLabel.setHorizontalAlignment(SwingConstants.CENTER);

		JButton startBtn = new JButton("Start");
		startBtn.setBounds(74, 39, 120, 29);
		mainPanel.add(startBtn);

		resultLabel = new JLabel("Click start to scan");
		resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
		resultLabel.setBounds(40, 80, 200, 38);
		mainPanel.add(resultLabel);

		
		frame.setBounds(100, 100, 323, 169);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// start button event
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread scanner = new Thread(new Scan(resultLabel));
				scanner.start();
			}
		});
	}

	private class Scan implements Runnable {
		JLabel resultLabel = null;

		Scan(JLabel resultView) {
			this.resultLabel = resultView;
		}

		@Override
		public void run() {
			JFrame frame = new JFrame();
			frame.getContentPane().setLayout(new FlowLayout());
			frame.pack();
			frame.setVisible(true);

			Webcam webcam = Webcam.getWebcams().get(0);

			webcam.open();

			for (int i = 0; i < 1000; ++i) {
				frame.getContentPane().add(
						new JLabel(new ImageIcon(webcam.getImage())));
				frame.pack();
				frame.repaint();

				try {
					Thread.sleep(200);
					frame.getContentPane().removeAll();
					QRCodeContent = QRCode.readQRCode(webcam.getImage());
					System.out.println("*********************\r\n"
							+ QRCodeContent + "\r\n**********************");

					webcam.close();
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			frame.setVisible(false);
			frame.dispose();

			try {
				analyser = new Analyser(QRCodeContent);
				String result = analyser.validateTicket();
				resultLabel.setText(result);
				System.out.println("result: " + result);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
