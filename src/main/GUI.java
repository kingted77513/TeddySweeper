/*
 * 使用GUI，方便觀察和測試 
 */
package main;
import game.Loader;
import game.Board;
import game.Referee;
import game.UI;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;

public final class GUI extends JFrame{
	private static final int GRID_WORLD_SIZE = 14;	// 32 
	private static final int AREA_WORLD_SIZE = 12;	// 24 
	public static void main(String args[]){
		System.out.println("This is GUI");
		GUI teddyMineSweeper = new GUI();
		teddyMineSweeper.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		//右上角的x的功能為關閉
	}	// end main

	private Container container;
	private GridBagConstraints constraints;
	private GridBagLayout layout;

	private UI ui;
	private JLabel showMineNuberLabel, setup_Area, board_Size, game_Rule, hint_Area, use_Method, auto_Run, hint_info, game_Start;
	private JTextField userMapX, userMapY, userMineNumber, inputField;
	private JTextArea outputMessage;
	private JPanel communicationArea, showMineNumber, setAreaBox;
	private int mapX, mapY, mineNumber;
	private String order, rule;

	private JButton start, one_step, many_steps, hint, dig;
	private JCheckBox singlePoint_Method, CSP_Method, CSP_Method_Plus, Teddy_Method, Teddy_Method_Plus;	//, FOPJ_Method, FOPJ_Method_Plus

	private JRadioButton beginnerButton, intermediateButton, expertButton, customizeButton, standardRulesButton, hardRulesButton, hintedRulesButton;

	JButton map[][];

	//讀檔寫檔
	//Create a file chooser
	final JFileChooser fc;

	public GUI(){
		// program toptext
		super("TeddySweeper");

		ui = new UI();
		fc = new JFileChooser();

		// get content pane
		this.container = getContentPane();

		// set Container's layout
		this.layout = new GridBagLayout();
		this.container.setLayout(layout);

		this.constraints = new GridBagConstraints();

		reset_SetArea();

		// set file menu
		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);
		JMenu fileMenu = new JMenu("File");
		fileMenu.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		JMenuItem safeItem = new JMenuItem("Safe");		//存檔
		safeItem.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		safeItem.setMnemonic('s');
		fileMenu.add(safeItem);
		safeItem.addActionListener(
				new ActionListener(){
					public void actionPerformed (ActionEvent event){
						int returnVal = fc.showSaveDialog(GUI.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							//This is where a real application would open the file.
							order = UI.order.safemap + " " + file.getPath() + Loader.ioFile;

							if (ui.isGameOver() == true && ui.isWin() == false){
								order += " true";
							}else {
								order += " false";
							}

							outputMessage.append("\n" + order);
							outputMessage.append("\n" + ui.communication(order));
							reShowMap();
						}
					}
				}
				);

		JMenuItem loadItem = new JMenuItem("Load");		//讀檔
		loadItem.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		loadItem.setMnemonic('l');
		fileMenu.add(loadItem);
		loadItem.addActionListener(
				new ActionListener(){
					public void actionPerformed (ActionEvent event){
						int returnVal = fc.showOpenDialog(GUI.this);
						File file = null;
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							file = fc.getSelectedFile();
							//This is where a real application would open the file.
							order = UI.order.loadmap + " " + file.getPath();
							outputMessage.append("\n" + order);
							outputMessage.append("\n" + ui.communication(order));
							mapX = ui.getMapX();
							mapY = ui.getMapY();

							reset_Map();
							reShowMap();
						}
					}
				}
				);

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		exitItem.setMnemonic('x');
		fileMenu.add(exitItem);
		exitItem.addActionListener(
				new ActionListener() {
					public void actionPerformed (ActionEvent event){
						System.exit(0);
					}
				}
				);

		bar.add(fileMenu);
		//		 end set file menu

		this.setSize(1000, 900);		//設定原始視窗大小
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); //設定最大化顯示
		this.setVisible(true);
	}	// end GUI_old

	private void reset_Map(){
		container.removeAll();
		container.setForeground(Color.BLACK);
		//		set board map area--------------------
		this.map = new JButton[mapY][mapX];
		Dig_Handler diger = new Dig_Handler();
		for (int y = 0  ; y < mapY ; y++){
			for (int x = 0 ; x < mapX ; x++){
				this.map[y][x] = new JButton (" ");
				this.map[y][x].setToolTipText(x + ", " + y);
				//				this.map[y][x].setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.WORLD_SIZE));	//設定字體的大小。
				//				this.map[y][x].setFont(new Font("symbol " ,Font.BOLD ,GUI.WORLD_SIZE));		//設定字體的大小。
				this.map[y][x].setBorder(BorderFactory.createLineBorder(new Color(38, 38, 38)));		// 設定框線
				this.map[y][x].setBackground(Color.GRAY);
				this.map[y][x].addMouseListener(diger);		//加入事件處理
				this.constraints.weightx = Integer.MAX_VALUE;
				this.constraints.weighty = Integer.MAX_VALUE;
				this.constraints.fill = GridBagConstraints.BOTH;
				this.addComponent(map[y][x], Math.abs(y-mapY), x, 1, 1);
			}
		}

		this.showMineNumber = new JPanel();
		this.showMineNumber.setLayout(new FlowLayout());
		JLabel mineJLabel = new JLabel("mine: ");
		mineJLabel.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.GRID_WORLD_SIZE));	//設定字體的大小。
		mineJLabel.setForeground(Color.WHITE);
		this.showMineNumber.add(mineJLabel);
		this.showMineNuberLabel = new JLabel(String.valueOf(this.mineNumber));
		this.showMineNuberLabel.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.GRID_WORLD_SIZE));	//設定字體的大小。
		this.showMineNuberLabel.setForeground(Color.WHITE);
		this.showMineNumber.add(this.showMineNuberLabel);
		this.showMineNumber.setBackground(Color.BLACK);
		this.constraints.fill = GridBagConstraints.HORIZONTAL;
		addComponent(this.showMineNumber, this.mapY+1, 0, this.mapX+1, 1);
		//		--------------------set board map area
		reset_SetArea();

		this.container.validate();		//使其有效，使用重畫具有立即性
		this.container.repaint();		//重畫
	}

	private void reset_SetArea(){
		//		set communicate with AI area--------------------
		this.communicationArea = new JPanel();
		this.communicationArea.setLayout(new BorderLayout());

		this.outputMessage = new JTextArea("show AI message", 5, 5);
		outputMessage.append("\n" + this.order);
		this.outputMessage.setEditable(false);		//設定訊息列為不可編輯
		this.outputMessage.setLineWrap(true);	//指令過長時可換行
		this.outputMessage.setCaretPosition(this.outputMessage.getText().length());		//使使JTextArea 的滾動條隨著記錄的增加自動滾動到最後一行

		JScrollPane outputMesScroll = new JScrollPane(this.outputMessage);	//使用JScrollPane以提供捲軸功能
		outputMesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);	//設定總是出現捲軸

		this.communicationArea.add(outputMesScroll, BorderLayout.NORTH);

		this.inputField = new JTextField();

		this.inputField.addActionListener(		// set the inputField action
				new ActionListener(){
					public void actionPerformed (ActionEvent event){
						order = inputField.getText();
						outputMessage.append("\n" + order);
						outputMessage.append("\n" + ui.communication(order));
						inputField.setText("");		//消除輸入的指令，方便輸入指令

						reShowMap();
					}
				}
				);

		this.communicationArea.add(new JScrollPane(this.inputField), BorderLayout.SOUTH);
		this.constraints.weighty = Integer.MAX_VALUE;
		this.constraints.fill = GridBagConstraints.HORIZONTAL;
		this.addComponent(this.communicationArea, this.mapY+2, 0, this.mapX+1, 1);
		//			container.add(this.communicationArea, BorderLayout.SOUTH);

		//	--------------------set communicate with AI area

		//		set setup area--------------------
		//		 	宣告設定地圖大小的按鍵
		this.setAreaBox = new JPanel();
		this.setAreaBox.setLayout(new BoxLayout(this.setAreaBox, BoxLayout.Y_AXIS));
//		this.setAreaBox.setLayout(leftlayout);
		
		//			setAreaBox.setBackground(Color.yellow);
//		this.constraints.weightx = 0;
		//			constraints.fill = GridBagConstraints.VERTICAL;
		this.setup_Area = new JLabel("-----Setup Area-----");
		this.setup_Area.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		this.board_Size = new JLabel("Board Size");
		this.board_Size.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		this.setAreaBox.add(this.setup_Area);
		this.setAreaBox.add(this.board_Size);

		this.beginnerButton = new JRadioButton("Beginner", true);		//預設為Beginner
		this.intermediateButton =  new JRadioButton("Intermediate", false);
		this.expertButton =  new JRadioButton("Expert", false);
		this.customizeButton =  new JRadioButton("Customize", false);

		this.customizeButton.addItemListener(		//使用者自行定義地圖大小
				new ItemListener(){
					public void itemStateChanged (ItemEvent event){
						userMapX.setEditable(true);
						userMapY.setEditable(true);
						userMineNumber.setEditable(true);
					}
				}
		);

		//			create logical relationship between JRadioRuttons，使只能選擇一個按鈕
		ButtonGroup sizeRadioGroup = new ButtonGroup();
		sizeRadioGroup.add(this.beginnerButton);
		sizeRadioGroup.add(this.intermediateButton);
		sizeRadioGroup.add(this.expertButton);
		sizeRadioGroup.add(this.customizeButton);

		setAreaBox.add(this.beginnerButton);
		setAreaBox.add(this.intermediateButton);
		setAreaBox.add(this.expertButton);
		setAreaBox.add(this.customizeButton);

		JPanel xSet = new JPanel(new FlowLayout());
		JPanel ySet = new JPanel(new FlowLayout());
		JPanel mSet = new JPanel(new FlowLayout());

		this.userMapX = new JTextField(2);
		this.userMapX.setEditable(false);		//預設為不可編輯
		this.userMapY = new JTextField(2);
		this.userMapY.setEditable(false);
		this.userMineNumber = new JTextField(2);
		this.userMineNumber.setEditable(false);

		xSet.add(new JLabel("Width: "));
		xSet.add(this.userMapX);
		ySet.add(new JLabel("Length: "));
		ySet.add(this.userMapY);
		mSet.add(new JLabel("Mine: "));
		mSet.add(this.userMineNumber);

		this.setAreaBox.add(xSet);
		this.setAreaBox.add(ySet);
		this.setAreaBox.add(mSet);

		//			宣告設定規則的按鍵
		this.game_Rule = new JLabel("Game Rule");
		this.game_Rule.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		this.setAreaBox.add(this.game_Rule);
		this.standardRulesButton = new JRadioButton("The first move can be a mine.", false);
		this.hardRulesButton =  new JRadioButton("The first move isn't a mine.", true);
		this.hintedRulesButton =  new JRadioButton("The first move is zero.", false);

		//			create logical relationship between JRadioRuttons，使只能選擇一個按鈕
		ButtonGroup ruleRadioGroup = new ButtonGroup();
		ruleRadioGroup.add(this.standardRulesButton);
		ruleRadioGroup.add(this.hardRulesButton);
		ruleRadioGroup.add(this.hintedRulesButton);

		setAreaBox.add(this.standardRulesButton);
		setAreaBox.add(this.hardRulesButton);
		setAreaBox.add(this.hintedRulesButton);

		this.game_Start = new JLabel("-----Game Start-----");
		this.game_Start.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		this.setAreaBox.add(this.game_Start);		// 分隔線

		this.start = new JButton("Start");
		this.start.addActionListener(		// set the start action
				new ActionListener(){
					public void actionPerformed (ActionEvent event){
						boolean isLegalSizeSet = true;
						// 設定地圖大小
						if (customizeButton.isSelected() == true){	// 設定使用者所設定的大小
							try {
								mapX = Integer.parseInt(userMapX.getText());
								mapY = Integer.parseInt(userMapY.getText());
								mineNumber = Integer.parseInt(userMineNumber.getText());
								isLegalSizeSet = true;
							}catch (NumberFormatException nfe){
								isLegalSizeSet = false;
								JOptionPane.showMessageDialog(null, "wrong size set");
							}
						}else if (beginnerButton.isSelected() == true){
							mapX = 9;
							mapY = 9;
							mineNumber = 10;
							isLegalSizeSet = true;
						}else if (intermediateButton.isSelected() == true){
							mapX = 16;
							mapY = 16;
							mineNumber = 40;
							isLegalSizeSet = true;
						}else if (expertButton.isSelected() == true){
							mapX = 30;
							mapY = 16;
							mineNumber = 99;
							isLegalSizeSet = true;
						}

						if (standardRulesButton.isSelected() == true){
							rule = Referee.rule_1;
						}else if (hardRulesButton.isSelected() == true){
							rule = Referee.rule_2;
						}else if (hintedRulesButton.isSelected() == true){
							rule = Referee.rule_3;
						}

						if (isLegalSizeSet == true){
							order = UI.order.reset + " " + mapX + " " + mapY + " " + mineNumber + " " + rule + " 0" + " 0";
							outputMessage.append("\n" + order);
							outputMessage.append("\n" + ui.communication(order));
							//								JOptionPane.showMessageDialog(null, order);
							reset_Map();
						}else {
							JOptionPane.showMessageDialog(null, "worng set! please set angin");
						}

					}
				}
		);
		this.setAreaBox.add(this.start);

		this.addComponent(this.setAreaBox, 0, this.mapX+1, 1, this.mapY+2);

		this.hint_Area = new JLabel("-----Hint Area-----");
		this.hint_Area.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		this.setAreaBox.add(this.hint_Area);
		
		//			宣告設定使用方法的按鍵
		this.use_Method = new JLabel("Use Method");
		this.use_Method.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		//this.setAreaBox.add(this.use_Method);
		
		this.singlePoint_Method = new JCheckBox ("Single Point", false);
		//setAreaBox.add(this.singlePoint_Method);
		this.CSP_Method = new JCheckBox("CSP", false);
		this.CSP_Method_Plus = new JCheckBox("CSP + Guess", false);
		//setAreaBox.add(this.CSP_Method);
		//setAreaBox.add(this.CSP_Method_Plus);
		
		this.Teddy_Method = new JCheckBox ("Teddy Method", true);
		this.Teddy_Method_Plus = new JCheckBox("Teddy Method + Guess", true);
		//			this.FOPJ_Method = new JCheckBox("FOPJ", false);
		//			this.FOPJ_Method_Plus = new JCheckBox("FOPJ + Guess", false);
		setAreaBox.add(this.Teddy_Method);
		setAreaBox.add(this.Teddy_Method_Plus);
		//			setAreaBox.add(this.FOPJ_Method);
		//			setAreaBox.add(this.FOPJ_Method_Plus);

		this.auto_Run = new JLabel("Auto Run");
		this.auto_Run.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		setAreaBox.add(this.auto_Run);
		
//		JPanel autoRunBlock = new JPanel();
//		Box autoRunBlock = Box.createHorizontalBox();
		Box autoRunBlock = Box.createVerticalBox();
		
		
		
		AutoRun autohandler = new AutoRun();
		this.one_step = new JButton("A Move");
		this.many_steps = new JButton("Moves");
		this.one_step.addActionListener(autohandler);
		this.many_steps.addActionListener(autohandler);
		autoRunBlock.add(this.one_step);
		autoRunBlock.add(Box.createVerticalStrut(5));		// 加上5像素的間隔
		autoRunBlock.add(this.many_steps);
		this.setAreaBox.add(autoRunBlock);

		this.hint_info = new JLabel("Hint Information");
		this.hint_info.setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));
		this.setAreaBox.add(this.hint_info);

//		JPanel hintBlock = new JPanel();
		Box hintBlock = Box.createVerticalBox();
		this.hint = new JButton("Show Hint");
		this.hint.addMouseListener(new ShowHint());
		hintBlock.add(this.hint);
		hintBlock.add(Box.createVerticalStrut(5));
		this.dig = new JButton("Show Dig Value");
		this.dig.addMouseListener(new ShowDigValue());
		hintBlock.add(this.dig);

		this.setAreaBox.add(hintBlock);
		//		--------------------set setup area
	}

	private class Dig_Handler extends MouseAdapter{		//按地雷格子的事件偵測程式
		public void mouseClicked (MouseEvent event){
			if (ui.isGameOver() == false){		//只有遊戲還沒有結束時才需要偵測
				for (int y = mapY - 1  ; y >= 0 ; y--){
					for (int x = 0 ; x < mapX ; x++){
						if (event.getSource() == map[y][x]){
							if (event.isMetaDown()){		// right mouse button
								outputMessage.append("\n" + ui.communication(UI.order.flag + " " + x + " " + y));
							}else if (event.isAltDown()){	// middle mouse button
								outputMessage.append("\n" + ui.communication(UI.order.alldig + " " + x + " " + y));
							}else {							// left mouse button
								outputMessage.append("\n" + ui.communication(UI.order.dig + " " + x + " " + y));
							}
						}
					}
				}

				gameOverJudge();
				container.validate();		//使其有效，使用重畫具有立即性
				container.repaint();		//重畫
			}
		}
	}

	private void gameOverJudge(){
		if (ui.isGameOver() == false){
			reShowMap();
		}else {
			showMap("show answer");
			if (ui.isWin() == true){
				JOptionPane.showMessageDialog(null, "You win!!");
			}else {
				JOptionPane.showMessageDialog(null, "You lose!!");
			}
		}
	}

	private String communicateWithUI(String order){	//與AI通訊
		return ui.communication(order);
	}

	private void setAI(){	// 設定所使用的AI
		if (singlePoint_Method.isSelected() == true){
			order = UI.order.setAI + " " + UI.usedAI.SinglePoint.toString() + " true";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}else if (singlePoint_Method.isSelected() == false){
			order = UI.order.setAI + " " + UI.usedAI.SinglePoint.toString() + " false";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}

		if (CSP_Method.isSelected() == true){
			order = UI.order.setAI + " " + UI.usedAI.CSP.toString() + " true";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}else if (CSP_Method.isSelected() == false){
			order = UI.order.setAI + " " + UI.usedAI.CSP.toString() + " false";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}

		if (CSP_Method_Plus.isSelected() == true){
			order = UI.order.setAI + " " + UI.usedAI.CSP_Guess.toString() + " true";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}else if (CSP_Method_Plus.isSelected() == false){
			order = UI.order.setAI + " " + UI.usedAI.CSP_Guess.toString() + " false";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}

		if (Teddy_Method.isSelected() == true){
			order = UI.order.setAI + " " + UI.usedAI.TeddyMethod.toString() + " true";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}else if (Teddy_Method.isSelected() == false){
			order = UI.order.setAI + " " + UI.usedAI.TeddyMethod.toString() + " false";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}

		if (Teddy_Method_Plus.isSelected() == true){
			order = UI.order.setAI + " " + UI.usedAI.TeddyMethod_Guess.toString() + " true";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}else if (Teddy_Method_Plus.isSelected() == false){
			order = UI.order.setAI + " " + UI.usedAI.TeddyMethod_Guess.toString() + " false";
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));
		}

		//		if (FOPJ_Method.isSelected() == true){
		//			order = UI.order.setAI + " " + "FOPJ " + "true";
		//			outputMessage.append("\n" + order);
		//			outputMessage.append("\n" + communicateWithUI(order));
		//		}else if (FOPJ_Method.isSelected() == false){
		//			order = UI.order.setAI + " " + "FOPJ " + "false";
		//			outputMessage.append("\n" + order);
		//			outputMessage.append("\n" + communicateWithUI(order));
		//		}

		//		if (FOPJ_Method_Plus.isSelected() == true){
		//			order = UI.order.setAI + " " + "FOPJ_Guess " + "true";
		//			outputMessage.append("\n" + order);
		//			outputMessage.append("\n" + communicateWithUI(order));
		//		}else {
		//			order = UI.order.setAI + " " + "FOPJ_Guess " + "false";
		//			outputMessage.append("\n" + order);
		//			outputMessage.append("\n" + communicateWithUI(order));
		//		}
	}

	private class AutoRun implements ActionListener {
		public void actionPerformed (ActionEvent event){
			setAI();

			if (event.getSource() == one_step){
				order = UI.order.AI_Run_one_Step.toString();
				outputMessage.append("\n" + order);
				outputMessage.append("\n" + communicateWithUI(order));
			}else if (event.getSource() == many_steps){		//一直執行為沒有答案為止
				String echo;
				do {
					order = UI.order.AI_Run_one_Step.toString();
					outputMessage.append("\n" + order);
					echo = communicateWithUI(order);
					outputMessage.append("\n" + echo);
				}while (echo.equals("unknown order: no_answer") == false 
						&& ui.isGameOver() == false);
			}

			gameOverJudge();
			container.validate();		//使其有效，使用重畫具有立即性
			container.repaint();		//重畫
		}
	}

	private class ShowHint extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent event){	// 滑鼠按下時給予提示
			setAI();

			order = UI.order.AI_Think.toString();
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));	// 讓AI解題看看

			double[][] hintMap = ui.getHintMap();
			//			System.out.println("this is hintMap");
			//			for (int y = mapY - 1 ; y >= 0 ; y--){
			//				for (int x = 0 ; x < mapX ; x++){
			//					System.out.print(hintMap[y][x] + "\t");
			//				}
			//				System.out.println();
			//			}

			for (int y = mapY - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < mapX ; x++){
					if (ui.getState(x, y) == Board.initial){
						if (hintMap[y][x] != Board.initial){
							map[y][x].setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.GRID_WORLD_SIZE));	//設定字體的大小。
							map[y][x].setBackground(Color.YELLOW);

							if (hintMap[y][x] == 1){
								map[y][x].setText("╳");
							}else if (hintMap[y][x] == 0){
								map[y][x].setText("〇");
							}else {
								map[y][x].setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));	//設定字體的大小。
								map[y][x].setText(BigDecimal.valueOf(hintMap[y][x]).multiply(BigDecimal.valueOf(100))
										.divide(BigDecimal.valueOf(1), 2, BigDecimal.ROUND_HALF_UP).toString() + "%");
							}
						}					
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent event){	// 滑鼠放開始恢復原狀
			reShowMap();
		}
	}

	private class ShowDigValue extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent event){	// 滑鼠按下時給予提示
			setAI();

			order = UI.order.AI_Think.toString();
			outputMessage.append("\n" + order);
			outputMessage.append("\n" + communicateWithUI(order));	// 讓AI解題看看

			double[][] digMap = ui.getDigValueMap();
			//			System.out.println("this is digMap");
			//			for (int y = mapY - 1 ; y >= 0 ; y--){
			//				for (int x = 0 ; x < mapX ; x++){
			//					System.out.print(digMap[y][x] + "\t");
			//				}
			//				System.out.println();
			//			}

			for (int y = mapY - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < mapX ; x++){
					if (ui.getState(x, y) == Board.initial){
						//						if (digMap[y][x] != 0){
						map[y][x].setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.GRID_WORLD_SIZE));	//設定字體的大小。
						map[y][x].setBackground(Color.YELLOW);
						map[y][x].setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.AREA_WORLD_SIZE));	//設定字體的大小。
						map[y][x].setText(BigDecimal.valueOf(digMap[y][x])
								.divide(BigDecimal.valueOf(1), 2, BigDecimal.ROUND_HALF_UP).toString());
						//						}					
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent event){	// 滑鼠放開始恢復原狀
			reShowMap();
		}
	}

	private void reShowMap(){		//重畫地圖
		for (int y = this.mapY - 1 ; y >= 0 ; y--){
			for (int x = 0 ; x < this.mapX ; x++){
				this.map[y][x].setFont(new Font(Font.DIALOG, Font.BOLD ,GUI.GRID_WORLD_SIZE));	//設定字體的大小。
				switch (this.ui.getState(x, y)){
				case Board.mine:
					this.map[y][x].setText("＠");
					this.map[y][x].setBackground(Color.RED);
					break;
				case Board.flag:
					this.map[y][x].setText("Ｐ");
					this.map[y][x].setForeground(Color.WHITE);	// 白字
					this.map[y][x].setBackground(Color.BLACK);	// 黑底
					break;
				case Board.initial:
					this.map[y][x].setText("　");
					this.map[y][x].setBackground(Color.GRAY);
					break;
				case Board.noMine:
					this.map[y][x].setText("　");
					this.map[y][x].setBackground(Color.WHITE);
//					this.map[y][x].setEnabled(false);		// 已經按過的地方不能再按了
					break;
				default:
//					this.map[y][x].setText(String.valueOf(ui.getState(x, y)));
					
					switch (ui.getState(x, y)){
					case 1:
						this.map[y][x].setText("１");
						break;
					case 2:
						this.map[y][x].setText("２");
						break;
					case 3:
						this.map[y][x].setText("３");
						break;
					case 4:
						this.map[y][x].setText("４");
						break;
					case 5:
						this.map[y][x].setText("５");
						break;
					case 6:
						this.map[y][x].setText("６");
						break;
					case 7:
						this.map[y][x].setText("７");
						break;
					case 8:
						this.map[y][x].setText("８");
						break;
					default:
						this.map[y][x].setForeground(Color.BLACK);
				
				}
					
					this.map[y][x].setBackground(Color.WHITE);
					
					//						this.map[y][x].setEnabled(false);		// 已經按過的地方不能再按了

					// 踩地雷的數字配色
//											switch (this.gameUI.getState(x, y)){
//												case 1:
//													this.map[y][x].setForeground(new Color(65,79,188));
//													break;
//												case 2:
//													this.map[y][x].setForeground(new Color(29,100,0));
//													break;
//												case 3:
//													this.map[y][x].setForeground(new Color(170,3,0));
//													break;
//												case 4:
//													this.map[y][x].setForeground(new Color(5,0,133));
//													break;
//												case 5:
//													this.map[y][x].setForeground(new Color(127,0,0));
//													break;
//												case 6:
//													this.map[y][x].setForeground(new Color(14,117,124));
//													break;
//												case 7:
//													this.map[y][x].setForeground(new Color(168,11,2));
//													break;
//												case 8:
//													this.map[y][x].setForeground(new Color(154,15,12));
//													break;
//												default:
//													this.map[y][x].setForeground(Color.BLACK);
//											
//											}
//					this.map[y][x].setForeground(Color.BLACK);
//					this.map[y][x].setBackground(Color.WHITE);
					//					 this.map[y][x].setEnabled(false);		// 已經按過的地方不能再按了
				}
			}
		}
		this.showMineNuberLabel.setText(String.valueOf(this.ui.getremainMineNumber()));		//更新地雷數字
	}

	private void showMap(String type){
		for (int y = this.mapY - 1 ; y >= 0 ; y--){
			for (int x = 0 ; x < this.mapX ; x++){
				if (type.equals("show answer")){
					this.map[y][x].setText(String.valueOf(this.ui.getInState(x, y))); 
					this.map[y][x].setEnabled(false); 
				}
			}
		}
	}

	private void addComponent(Component component, int row, int column, int width, int height){		//將元件加入GridBagConstraints
		this.constraints.gridx = column;
		this.constraints.gridy = row;

		this.constraints.gridwidth = width;
		this.constraints.gridheight = height;

		this.layout.setConstraints(component, this.constraints);
		this.container.add(component);
	}

}
