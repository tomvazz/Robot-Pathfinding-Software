import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;

public class GuiSetUp {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiSetUp window = new GuiSetUp();
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
	public GuiSetUp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Robot Pathfinding Software");
		frame.getContentPane().setBackground(new Color(105, 105, 105));
		frame.setBounds(200, 100, 1000, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		Visualizer obj = new Visualizer();
		obj.gui(frame);
	}
}
