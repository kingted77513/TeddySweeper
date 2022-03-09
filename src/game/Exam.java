package game;

import java.io.File;

class Exam {
	String className;
	private int mineNumber, mapX, mapY;
	private String rule;
	final UI ui;
	
	Exam (int mineNumber, int mapX, int mapY, String rule){
		this.className = "Exam";
		this.ui = new UI();
		this.mineNumber = mineNumber;
		this.mapX = mapX;
		this.mapY = mapY;
		this.rule = rule;
//		System.out.println("This is " + this.className);
	}
	
//	public static void main(String[] args){
//		Exam exam = new Exam(4, 4, 4, Referee.rule_2);
//		exam.setExam();
////		File loaderfile = new File(System.getProperty("user.dir"), "NowRun_40_mines_on_16x16.txt");
////		if (loaderfile.isFile() == true){
////			System.out.println("this is exist");
////		}
////		exam.setExam(loaderfile.getPath());
//		exam.setUseTeddyMethod();
//		exam.run();
////		System.out.println("mineNumber = " + exam.mineNumber);
////		System.out.println("mapX = " + exam.mapX);
////		System.out.println("mapY = " + exam.mapY);
//		System.out.println("the game end and isWin = " + exam.ui.isWin());
//	}
	
	// 一般開局
	void setExam(){
		this.ui.input(UI.order.reset + " " + this.mapX + " " + this.mapY + " " + this.mineNumber + " " + this.rule);
	}
	
	// 讀檔開局
	void setExam(String filePath){
		File boardFile = new File(filePath);
		if (boardFile.isFile() == true){
			this.ui.input(UI.order.loadmap + " " + filePath);
		}
	}
	
	// 使用TeddyMethod
	void setUseTeddyMethod(){
		this.ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod + " true");
		this.ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod_Guess + " true");
	}
	
	// 跑一次實驗
	void run(){
		while (this.ui.isGameOver() == false){
			this.ui.input(UI.order.AI_Run_one_Step.toString());
		}
	}
	
	boolean isWin(){
		return this.ui.isWin();
	}
}
