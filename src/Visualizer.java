import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

//Pathfinding A* Algorithm Visualization Tool

/*
METHODS 

gui - user interface is setup with all the buttons and the grid

executeselection - menuselection string is initialized to "start"; whenever user clicks another option, this method is called and 
menuselection changes, a certain line of code will be executed for each. printlocation method is called here

keyevents - when menuselection == "draw", which means draw button was pressed, walls can be drawn with arrow keys

printlocation - prints location array into terminal, shows which nodes are open, closed, empty, start, end (o, c, e, s, e)

runsimulation - This method is called every time interval from timer task.
Scans all directions of current parent node, only takes in empty squares (.), the end square (e), and open 
squares (o). When inside, the visual grid is updated (open nodes) and the cost for the node in that direction is calculated 
by calling the calculatecost method. If end(e) is scanned in one of the directions, the draw route method is called. After 
scanning all directions, nopathchecker is called to see if there are any more open nodes. After this, the current parent is 
displayed, and the next lowestcost is calculated and new parent node is updated to coordinates of lowestcost. 

calculatecost - calculates the gcost, hcost and fcost of given node. The direction of the passed in node from parent node
is also passed through here and "current" array is updated for given node (eg. south (s), northwest (nw))
If node is already open and new fcost is more expensive (which would lead to more expensive route), the old costs are retained 
for that open node. The location is set at open (o) for each node that passes through here, (parent node will be closed after 
in runsimulation method)

fcostdisplay - all the fcosts for each open and closed node is displayed when user clicks on the numbers button

arrowdisplay - the direction from parent node (current/path) is displayed for each open and closed node when user clicks on
arrows button.

drawroute - once the end is hit, this method is called and will trace back the best possible route using the "current" array full
of directions.

nopathchecker - this is called in the runsimulation method after all directions are scanned (after open nodes are marked as open
and before parent node is set as closed). Checks to see if there are any open nodes left. If not, there is no possible 
route and message will be displayed as such. Also, email is sent to admin about error

*/

public class Visualizer implements KeyListener{
	
	public ImageIcon grayblock;
	
	public JButton[][] grid = new JButton[43][27];
	public JLabel[][] gridtext = new JLabel[43][27];
	public JLabel[][] nodetext = new JLabel[43][27];
	public JButton startbtn = new JButton();
	public JButton endbtn = new JButton();
	public JButton playbtn = new JButton();
	public JButton drawbtn = new JButton();
	public JButton restartbtn = new JButton();
	public JButton numbers = new JButton();
	public JButton arrows = new JButton();
	public JButton mapbtn = new JButton();
	
	//start and end coordinates
	public int startx = 0;
	public int starty = 0;
	public int endx = 0;
	public int endy = 0;
	public int parentx = 0;
	public int parenty = 0;
	public int wallx = 0;
	public int wally = 0;
	
	/*
	s = start
	e = end
	w = wall
	o = open
	c = closed 
	*/
	public String[][] location = new String[43][27];
	public String[][] current = new String[43][27];
	public String[][] maplayout = {
			{".",".",".",".",".",".",".",".",".",".","w","w","w","w","w","w","w","w",".",".",".","w",".",".",".",".",".",".",".","w",".",".",".",".","w","w","w","w",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w","w","w","w","w","w","w","w",".",".",".","w",".",".",".","w",".",".",".","w",".",".",".",".","w","w","w","w",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".",".",".","w","w",".","w","w","w",".","w","w","w","w","w",".","w",".",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w","w","w","w","w",".",".","w","w",".","w","w",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".","w","w","w","w","w",".",".",".","w",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".",".",".",".",".",".",".",".","w","w","w","w",".","w",".",".",".","w",".",".",".","w",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".","w","w","w","w","w","w","w","w",".",".",".",".","w",".",".",".","w",".",".","w","w",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".",".","w",".",".","w",".",".",".",".",".",".","w",".","w",".",".",".","w",".",".","w",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w","w",".","w","w",".",".","w","w","w","w",".",".",".",".",".",".","w",".",".",".",".",".","w",".",".","w",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".",".",".",".",".",".",".",".",".",".",".","w",".","w","w","w","w","w",".","w","w",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".",".",".",".","w","w","w","w",".","w",".",".",".",".","w",".","w",".",".",".",".",".","w",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".","w",".",".","w",".","w",".",".",".",".","w",".","w",".",".","w",".",".","w",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w","w","w","w","w",".",".","w",".",".","w",".","w",".",".",".",".",".",".","w",".",".","w",".",".","w",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".","w",".","w","w",".","w",".","w","w","w","w",".","w",".","w","w",".",".","w",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".","w",".",".",".","w",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".","w",".",".",".","w",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".","w","w","w","w","w",".",".","w","w","w","w",".",".","w",".",".",".",".","w","w",".","w",".","w","w","w","w","w","w","w","w","w","w"}, 
			{".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".",".","w",".",".","w","w","w","w",".","w",".",".","w",".","w",".",".",".","w",".",".",".",".","."}, 
			{".",".",".","w","w","w","w","w","w","w","w","w","w","w","w",".",".","w",".",".","w",".",".","w",".",".",".",".","w","w","w","w",".","w","w",".","w","w",".",".",".",".","."}, 
			{"w","w","w","w",".",".",".",".",".",".","w",".",".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".",".",".",".",".",".",".",".",".",".","w",".",".",".",".","."}, 
			{".",".",".","w",".",".","w",".",".",".","w",".",".",".","w",".",".","w",".",".",".",".",".",".",".","w","w",".",".",".",".",".",".",".",".",".",".","w","w",".","w","w","w"}, 
			{".",".",".","w",".",".","w","w","w",".","w",".",".",".","w",".",".","w",".",".",".",".",".",".",".","w",".",".",".","w",".",".",".",".",".",".",".",".",".",".",".",".","."}, 
			{"w","w",".","w",".",".",".",".","w","w","w","w",".",".","w",".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".",".",".",".",".",".",".",".",".",".",".","."}, 
			{".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".",".","w","w","w","w","w",".",".",".","w",".",".","w","w","w",".","w","w",".","w","w",".",".",".",".",".","."}, 
			{".",".",".","w","w",".","w","w","w","w",".","w",".",".","w",".",".",".",".",".",".","w","w","w","w","w",".",".",".","w",".",".","w",".",".",".","w","w","w",".","w","w","w"}, 
			{".",".",".","w",".",".","w",".",".",".",".","w","w","w","w",".",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".",".","w",".",".",".","w",".",".",".",".",".","."}, 
			{".",".",".","w",".",".","w",".",".",".",".","w","w","w","w","w","w","w","w",".",".",".",".",".",".",".",".",".",".",".",".",".","w",".",".",".","w",".",".",".",".",".","."} 
	};
	
	public int[][] gcost = new int[43][27]; //distance from start
	public int[][] hcost = new int[43][27]; //distance from end
	public int[][] fcost = new int[43][27]; //total
	
	public String menuselection = "start";
	public int playcount = 0;
	public int displaynums = 0;
	public int displayarrows = 0;
	public int speed = 10;
	
	public int pause = 0;
	Timer t = new Timer();
	TimerTask tt = new TimerTask() {
		public void run() {
			if (pause == 0) {
				runsimulation();
			}
		}
	};
	
	public void gui(JFrame f) {
		
		f.addKeyListener(this);
		f.setFocusable(true);
		
		//initialize location, hcost and gcost array
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				location[a][b] = ".";
				current[a][b] = " ";
				gcost[a][b] = 0;
				hcost[a][b] = 0;
				fcost[a][b] = 0;
			}
		}
		
		grayblock = new ImageIcon(this.getClass().getResource("/grayblock.png"));
		
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				nodetext[a][b] = new JLabel("");
				nodetext[a][b].setFont(new Font("Futura", Font.PLAIN, 10));
				nodetext[a][b].setHorizontalAlignment(SwingConstants.CENTER);
				nodetext[a][b].setBounds(((a*23)+6), (649-(b*23)), 22, 22);
				f.getContentPane().add(nodetext[a][b]);
			}
		}
		
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				gridtext[a][b] = new JLabel("");
				gridtext[a][b].setFont(new Font("Webdings", Font.PLAIN, 16));
				gridtext[a][b].setHorizontalAlignment(SwingConstants.CENTER);
				gridtext[a][b].setBounds(((a*23)+6), (649-(b*23)), 22, 22);
				f.getContentPane().add(gridtext[a][b]);
			}
		}
		
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				grid[a][b] = new JButton(grayblock);
				grid[a][b].setBounds(((a*23)+6), (649-(b*23)), 22, 22);
				grid[a][b].setFocusable(false);
				f.getContentPane().add(grid[a][b]);
			}
		}
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				int x = a;
				int y = b;
				grid[a][b].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						executeselection(x,y);
					}
				});
			}
		}
		
		numbers = new JButton("123");
		numbers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (displaynums == 0) {
					displaynums = 1;
					fcostdisplay();
					displayarrows = 0;
				} else {
					displaynums = 0;
					for (int a = 0; a < 43; a++) {
						for (int b = 0; b < 27; b++) {
							nodetext[a][b].setText("");
						}
					}
				}
			}
		});
		numbers.setForeground(Color.GRAY);
		numbers.setFont(new Font("futura", Font.PLAIN, 10));
		numbers.setBounds(6, 8, 34, 34);
		numbers.setFocusable(false);
		f.getContentPane().add(numbers);
		
		arrows = new JButton("G");
		arrows.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (displayarrows == 0) {
					displayarrows = 1;
					arrowsdisplay();
					displaynums = 0;
				} else {
					displayarrows = 0;
					for (int a = 0; a < 43; a++) {
						for (int b = 0; b < 27; b++) {
							nodetext[a][b].setText("");
						}
					}
				}
			}
		});
		arrows.setForeground(Color.GRAY);
		arrows.setFont(new Font("Wingdings 3", Font.PLAIN, 20));
		arrows.setBounds(45, 8, 34, 34);
		arrows.setFocusable(false);
		f.getContentPane().add(arrows);
		
		playbtn = new JButton("u");
		playbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pause = 0;
				menuselection = "play";
				executeselection(0,0);
			}
		});
		playbtn.setForeground(new Color(65, 105, 225));
		playbtn.setFont(new Font("Wingdings 3", Font.PLAIN, 20));
		playbtn.setBounds(450, 5, 100, 40);
		playbtn.setFocusable(false);
		f.getContentPane().add(playbtn);
		
		endbtn = new JButton("n");
		endbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menuselection = "end";
			}
		});
		endbtn.setForeground(new Color(148, 0, 211));
		endbtn.setFont(new Font("Webdings", Font.PLAIN, 14));
		endbtn.setBounds(375, 8, 50, 34);
		endbtn.setFocusable(false);
		f.getContentPane().add(endbtn);
		
		startbtn = new JButton("n");
		startbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menuselection = "start";
			}
		});
		startbtn.setForeground(new Color(147, 112, 219));
		startbtn.setFont(new Font("Webdings", Font.PLAIN, 14));
		startbtn.setBounds(300, 8, 50, 34);
		startbtn.setFocusable(false);
		f.getContentPane().add(startbtn);
		
		drawbtn = new JButton("!");
		drawbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menuselection = "draw";
			}
		});
		drawbtn.setForeground(Color.BLACK);
		drawbtn.setFont(new Font("Wingdings", Font.PLAIN, 18));
		drawbtn.setBounds(575, 8, 50, 34);
		drawbtn.setFocusable(false);
		f.getContentPane().add(drawbtn);
		
		restartbtn = new JButton("P");
		restartbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pause = 1;
				menuselection = "restart";
				executeselection(0,0);
			}
		});
		restartbtn.setForeground(new Color(70, 130, 180));
		restartbtn.setFont(new Font("Wingdings 3", Font.PLAIN, 16));
		restartbtn.setBounds(650, 8, 50, 34);
		restartbtn.setFocusable(false);
		f.getContentPane().add(restartbtn);
		
		mapbtn = new JButton("l");
		mapbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int c = 0; c < 43; c++) {
					for (int d = 0; d < 27; d++) {
						gridtext[c][d].setFont(new Font("Webdings", Font.PLAIN, 15));
						gridtext[c][d].setText("");
						nodetext[c][d].setText("");
						location[c][d] = maplayout[d][c];
						current[c][d] = " ";
						gcost[c][d] = 0;
						hcost[c][d] = 0;
						fcost[c][d] = 0;
					}
				}
				
				for (int m = 0; m < 43; m++) {
					for (int n = 0; n < 43; n++) {
						if (location[n][m] == "w") {
							gridtext[n][m].setText("g");
							gridtext[n][m].setFont(new Font("Webdings", Font.PLAIN, 22));
							gridtext[n][m].setForeground(Color.DARK_GRAY);
						}
					}
				}
			}
		});
		mapbtn.setForeground(Color.GRAY);
		mapbtn.setFont(new Font("Webdings", Font.PLAIN, 20));
		mapbtn.setBounds(960, 8, 34, 34);
		mapbtn.setFocusable(false);
		f.getContentPane().add(mapbtn);
		
	}
	
	public void executeselection(int a, int b) {
		
		if (menuselection == "start") {
			gridtext[startx][starty].setText("");
			location[startx][starty] = ".";
			
			startx = a;
			starty = b;
			parentx = a;
			parenty = b;
			gridtext[a][b].setText("n");
			gridtext[a][b].setForeground(new Color(147, 112, 219));
			location[a][b] = "s";
		}
		
		if (menuselection == "end") {
			gridtext[endx][endy].setText("");
			location[endx][endy] = ".";
			
			endx = a;
			endy = b;
			gridtext[a][b].setText("n");
			gridtext[a][b].setForeground(new Color(148, 0, 211));
			location[a][b] = "e";
		}
		
		if (menuselection == "play") {
			playcount++;
			if (playcount == 1) {
				t.schedule(tt, 100, speed);
			}
		}
		
		if (menuselection == "draw") {
			wallx = a;
			wally = b;
			gridtext[a][b].setText("g");
			gridtext[a][b].setFont(new Font("Webdings", Font.PLAIN, 22));
			gridtext[a][b].setForeground(Color.DARK_GRAY);
			location[a][b] = "w";
		}
		
		if (menuselection == "restart") {
			for (int c = 0; c < 43; c++) {
				for (int d = 0; d < 27; d++) {
					gridtext[c][d].setFont(new Font("Webdings", Font.PLAIN, 15));
					gridtext[c][d].setText("");
					nodetext[c][d].setText("");
					location[c][d] = ".";
					current[c][d] = " ";
					gcost[c][d] = 0;
					hcost[c][d] = 0;
					fcost[c][d] = 0;
				}
			}
		}
		
		printlocations();
		
	}
	
	
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		int spacecode = e.getKeyCode();
		if (menuselection == "draw") {
			if (spacecode == KeyEvent.VK_UP && (wally != 26)) {
				gridtext[wallx][wally+1].setText("g");
				gridtext[wallx][wally+1].setFont(new Font("Webdings", Font.PLAIN, 22));
				gridtext[wallx][wally+1].setForeground(Color.DARK_GRAY);
				location[wallx][wally+1] = "w";
				wally = wally+1;
				printlocations();
			}
			if (spacecode == KeyEvent.VK_DOWN && (wally != 0)) {
				gridtext[wallx][wally-1].setText("g");
				gridtext[wallx][wally-1].setFont(new Font("Webdings", Font.PLAIN, 22));
				gridtext[wallx][wally-1].setForeground(Color.DARK_GRAY);
				location[wallx][wally-1] = "w";
				wally = wally-1;
				printlocations();
			}
			if (spacecode == KeyEvent.VK_LEFT && (wallx != 0)) {
				gridtext[wallx-1][wally].setText("g");
				gridtext[wallx-1][wally].setFont(new Font("Webdings", Font.PLAIN, 22));
				gridtext[wallx-1][wally].setForeground(Color.DARK_GRAY);
				location[wallx-1][wally] = "w";
				wallx = wallx-1;
				printlocations();
			}
			if (spacecode == KeyEvent.VK_RIGHT && (wallx != 42)) {
				gridtext[wallx+1][wally].setText("g");
				gridtext[wallx+1][wally].setFont(new Font("Webdings", Font.PLAIN, 22));
				gridtext[wallx+1][wally].setForeground(Color.DARK_GRAY);
				location[wallx+1][wally] = "w";
				wallx = wallx+1;
				printlocations();
			}
		}
		
	}
	public void keyReleased(KeyEvent e) {}
	
	public void printlocations() { //printing in terminal

		System.out.println("\n");
		for (int b = 26; b >= 0; b--) {
			for (int a = 0; a < 43; a++) {
				System.out.print("\""+location[a][b]+"\",");
				if (a == 42) {
					System.out.print(" \n");
				}
			}
		}
		System.out.println("Parent Node (" + parentx + ", " + parenty + ")");

	}
	
	public void runsimulation() {
		
		//Scans all directions, only takes in empty squares (.), the end square (e), and open squares (o)
		//When inside, the visual grid is updated and the cost is calculated
		
		//northwest
		if (parentx - 1 >= 0 && parenty + 1 <= 26 && (location[parentx-1][parenty+1] == "." || location[parentx-1][parenty+1] == "e" || location[parentx-1][parenty+1] == "o")) {
			if (location[parentx-1][parenty+1] != "e") {
				gridtext[parentx-1][parenty+1].setText("n");
				gridtext[parentx-1][parenty+1].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx-1,parenty+1,"nw");
		}
		//north
		if (parenty + 1 <= 26 && (location[parentx][parenty+1] == "." || location[parentx][parenty+1] == "e" || location[parentx][parenty+1] == "o")) {
			if (location[parentx][parenty+1] != "e") {
				gridtext[parentx][parenty+1].setText("n");
				gridtext[parentx][parenty+1].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx,parenty+1,"n");
		}
		//northeast
		if (parentx + 1 <= 42 && parenty + 1 <= 26 && (location[parentx+1][parenty+1] == "." || location[parentx+1][parenty+1] == "e" || location[parentx+1][parenty+1] == "o")) {
			if (location[parentx+1][parenty+1] != "e") {
				gridtext[parentx+1][parenty+1].setText("n");
				gridtext[parentx+1][parenty+1].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx+1,parenty+1,"ne");
		}
		//west
		if (parentx - 1 >= 0 && (location[parentx-1][parenty] == "." || location[parentx-1][parenty] == "e" || location[parentx-1][parenty] == "o")) {
			if (location[parentx-1][parenty] != "e") {
				gridtext[parentx-1][parenty].setText("n");
				gridtext[parentx-1][parenty].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx-1,parenty,"w");
		}
		//east
		if (parentx + 1 <= 42 && (location[parentx+1][parenty] == "." || location[parentx+1][parenty] == "e" || location[parentx+1][parenty] == "o")) {
			if (location[parentx+1][parenty] != "e") {
				gridtext[parentx+1][parenty].setText("n");
				gridtext[parentx+1][parenty].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx+1,parenty,"e");
		}
		//southwest
		if (parentx - 1 >= 0 && parenty - 1 >= 0 && (location[parentx-1][parenty-1] == "." || location[parentx-1][parenty-1] == "e" || location[parentx-1][parenty-1] == "o")) {
			if (location[parentx-1][parenty-1] != "e") {
				gridtext[parentx-1][parenty-1].setText("n");
				gridtext[parentx-1][parenty-1].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx-1,parenty-1,"sw");
		}
		//south
		if (parenty - 1 >= 0 && (location[parentx][parenty-1] == "." || location[parentx][parenty-1] == "e" || location[parentx][parenty-1] == "o")) {
			if (location[parentx][parenty-1] != "e") {
				gridtext[parentx][parenty-1].setText("n");
				gridtext[parentx][parenty-1].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx,parenty-1,"s");
		}
		//southeast
		if (parentx + 1 <= 42 && parenty - 1 >= 0 && (location[parentx+1][parenty-1] == "." || location[parentx+1][parenty-1] == "e" || location[parentx+1][parenty-1] == "o")) {
			if (location[parentx+1][parenty-1] != "e") {
				gridtext[parentx+1][parenty-1].setText("n");
				gridtext[parentx+1][parenty-1].setForeground(new Color(102, 205, 170));
			} else { // hits end
				drawroute(parentx,parenty);
				pause = 1;
			}
			calculatecost(parentx+1,parenty-1,"se");
		}
		
		nopathchecker();
		
		
		if (parentx != startx || parenty != starty) {
			gridtext[parentx][parenty].setFont(new Font("Webdings", Font.PLAIN, 22));
			gridtext[parentx][parenty].setForeground(new Color(32, 178, 170)); // parent is displayed
		}
		if (pause == 1) {
			gridtext[parentx][parenty].setFont(new Font("Webdings", Font.PLAIN, 22));
			gridtext[parentx][parenty].setForeground(new Color(65, 105, 225));
		}
		//node with the lowest cost is calculated
		int lowestcost = 10000;
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				if (fcost[a][b] < lowestcost && fcost[a][b] != 0 && location[a][b] != "c") {
					lowestcost = fcost[a][b];
					parentx = a;
					parenty = b;
				} else if (fcost[a][b] == lowestcost && fcost[a][b] != 0 && location[a][b] != "c") {
					if (hcost[a][b] < hcost[parentx][parenty]) {
						lowestcost = fcost[a][b];
						parentx = a;
						parenty = b;
					}
				}
			}
		}
		location[parentx][parenty] = "c"; // parent node is closed
		
		printlocations();
		
	}
	
	public void calculatecost(int a, int b, String direction) {
		
		// holds costs of already open nodes
		int gcostholder = 10000;
		int hcostholder = 10000;
		int fcostholder = 10000;
		String currentholder = " ";
		if (location[a][b] == "o") {
			gcostholder = gcost[a][b];
			hcostholder = hcost[a][b];
			fcostholder = fcost[a][b];
			currentholder = current[a][b];
		}
		
		// distance from point to parent node plus gcost of parent node (to follow a path)
		gcost[a][b] = gcost[parentx][parenty] + (int)(10 * Math.sqrt( (Math.pow(parentx-a, 2)) + (Math.pow(parenty-b, 2)) )); 
		// distance from point to end
		hcost[a][b] = (int)(10 * Math.sqrt( (Math.pow(endx-a, 2)) + (Math.pow(endy-b, 2)) ));
		fcost[a][b] = gcost[a][b] + hcost[a][b];
		
		// set direction from parent
		current[a][b] = direction;
		
		// if new fcost ends up being a more expensive route, set costs to old costs
		if (location[a][b] == "o") {
			if (fcost[a][b] > fcostholder) {
				gcost[a][b] = gcostholder;
				hcost[a][b] = hcostholder;
				fcost[a][b] = fcostholder;
				current[a][b] = currentholder;
			}
		}
		
		
		// sets node to open (closed nodes will be set later in runsimulation method)
		if (location[a][b] != "e") {
			location[a][b] = "o";
		}
		
		// if number display mode is hit
		if (displaynums == 1) {
			fcostdisplay();
		}
		// if arrow display mode is hit
		if (displayarrows == 1) {
			arrowsdisplay();
		}
		
	}
	
	// if displaying the numbers is clicked on, this is run
	public void fcostdisplay() {
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				if (location[a][b] == "c" || location[a][b] == "o") {
					nodetext[a][b].setText(String.valueOf(fcost[a][b]));
					nodetext[a][b].setFont(new Font("Futura", Font.PLAIN, 7));
				}
			}
		}
	}
	
	// if displaying the arrows is clicked on, this is run
	public void arrowsdisplay() {
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				if (location[a][b] == "c" || location[a][b] == "o") {
					
					// an arrow is pointed to whatever direction is stored in current array for that node
					
					if (current[a][b] == "nw") {
						nodetext[a][b].setText("m");
					}
					if (current[a][b] == "n") {
						nodetext[a][b].setText("i");
					}
					if (current[a][b] == "ne") {
						nodetext[a][b].setText("l");
					}
					if (current[a][b] == "w") {
						nodetext[a][b].setText("g");
					}
					if (current[a][b] == "e") {
						nodetext[a][b].setText("f");
					}
					if (current[a][b] == "sw") {
						nodetext[a][b].setText("k");
					}
					if (current[a][b] == "s") {
						nodetext[a][b].setText("h");
					}
					if (current[a][b] == "se") {
						nodetext[a][b].setText("j");
					}
					
					nodetext[a][b].setFont(new Font("Wingdings 3", Font.PLAIN, 10));
					
				}
			}
		}
	}
	
	// once the end is hit, this method is called to draw the optimal route
	public void drawroute(int a, int b) {
		
		int x = a;
		int y = b;
		for (int t = 0; t < 1200; t++){ // directions go opposite since tracing back from end
			if (current[x][y] == "nw") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				x = x+1;
				y = y-1;
			} else if (current[x][y] == "n") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				y = y-1;
			} else if (current[x][y] == "ne") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				x = x-1;
				y = y-1;
			} else if (current[x][y] == "w") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				x = x+1;
			} else if (current[x][y] == "e") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				x = x-1;
			} else if (current[x][y] == "sw") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				x = x+1;
				y = y+1;
			} else if (current[x][y] == "s") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				y = y+1;
			} else if (current[x][y] == "se") {
				gridtext[x][y].setForeground(new Color(65, 105, 225));
				x = x-1;
				y = y+1;
			}
			
			if (location[x][y] == "s") {
				break;
			}
		}
		
		
	}
	
	// checks if there is no possible path
	public void nopathchecker() { 
		
		int opencounter = 0;
		for (int a = 0; a < 43; a++) {
			for (int b = 0; b < 27; b++) {
				if (location[a][b] == "o") {
					opencounter++;
				}
			}
		}
		if (opencounter == 0) {
			// Send email
			EmailNotification e = new EmailNotification();
			e.sendEmail();
			
			pause = 1;
			JOptionPane.showMessageDialog(null, "There is no possible path");
		}
	}

}
