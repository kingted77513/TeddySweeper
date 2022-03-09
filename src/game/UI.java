/*
 * 專與GUI互通，方便觀察和測試
 */
package game;

import java.math.BigDecimal;
public final class UI {
	//通訊協定
	public enum order {		// 指令集
		dig,				// 挖某一格
		alldig, 			// 挖某一格周圍的初始點
		flag, 				// 在某一個上插旗子
		allflag,	 		// 在某一格周圍的初始點都插上旗子
		reset, 				// 重新設定遊戲
		showmap, 			// 顯示地圖
		setAI, 				// 設定所使用的AI
		setFinalize,		// 設定自製解構 
		AI_Run_one_Step,	// 使用AI來玩一次踩雷
		AI_Think, 			// 使用AI來想答案
		safemap, 			// 儲存目前的地圖
		loadmap, 			// 讀取已經儲存的地圖
		showinfo			// 顯示詳細資料
	}
	
	public enum usedAI {		// 所有的AI名稱
		SinglePoint, CSP, CSP_Guess, TeddyMethod, TeddyMethod_Adjust, TeddyMethod_Guess
	}

	private String message;
	private Player player;
	private Referee referee;
	private static Referee static_referee;
	private String[] commands;
//	private Referee_Board map;
	
	public UI(){
		this.message = "";
	}
	
	public String communication(String order){
		this.input(order);
		return this.onput();
	}
	
	public void input(String command){
		commands = command.split(" ");
		if (commands[0].equals(order.dig.toString())){
			this.referee.dig(Integer.parseInt(commands[2]), Integer.parseInt(commands[1]));
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.alldig.toString())){
			this.referee.superDig(Integer.parseInt(commands[2]), Integer.parseInt(commands[1]));
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.flag.toString())){
			this.referee.setFlag(Integer.parseInt(commands[2]), Integer.parseInt(commands[1]));
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.allflag.toString())){
			this.referee.superFlag(Integer.parseInt(commands[2]), Integer.parseInt(commands[1]));
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.reset.toString()) == true){
			int mapX = Integer.parseInt(commands[1]);
			int mapY = Integer.parseInt(commands[2]);
			int mineNumber = Integer.parseInt(commands[3]);
			String rule = commands[4];
			try {
				int loopTimes = Integer.parseInt(commands[5]);
				int testTimes = Integer.parseInt(commands[6]);
				this.restart(mapX, mapY, mineNumber, rule, loopTimes, testTimes);
			}catch (ArrayIndexOutOfBoundsException aio){
				this.restart(mapX, mapY, mineNumber, rule);
			}
			
			
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.showmap.toString())){
			try {
				if (commands[1].equals("out")){
					this.showMap("show map", this.referee.getBoard());
				}else if (commands[1].equals("in")){
					this.showMap("show answer", this.referee.getBoard());
				}else this.message = "unknwon map kind" + commands[1];
			}catch (ArrayIndexOutOfBoundsException aio){
				this.showMap("show answer", this.referee.getBoard());
			}
			
			this.message += "This order is complete: " + command;
		}else if (commands[0].equals(order.setAI.toString())){		//設定使用的AI
			try {
				double sp = Double.parseDouble(commands[3]);
				double eq = Double.parseDouble(commands[4]);
				double cm = Double.parseDouble(commands[5]);
				double other = Double.parseDouble(commands[6]);
				double noans = Double.parseDouble(commands[7]);
				
				if (commands[2].equals("true")){
					this.player.setAI(commands[1], true, sp, eq, cm, other, noans);
				}else if (commands[2].equals("false")){
					this.player.setAI(commands[1], false, sp, eq, cm, other, noans);
				}
			}catch (ArrayIndexOutOfBoundsException aio){
				if (commands[2].equals("true")){
					this.player.setAI(commands[1], true);
				}else if (commands[2].equals("false")){
					this.player.setAI(commands[1], false);
				}
			}
			
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.setFinalize.toString())){
			this.setFinalize();
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.AI_Run_one_Step.toString())){
//			System.out.println("show map");
//			this.showMap("show map", UI.referee.getBoard());
			String ans = this.player.answer(); 
			this.message = "This order is complete: " + command;
			this.message = "\n" + "AI's answer is" + ans;
			this.input(ans);
		}else if (commands[0].equals(order.AI_Think.toString())){
			this.player.answer();
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.safemap.toString())){
			if (commands[2].equals("true")){
				Loader.safeMap(this.referee.getBoard(), commands[1], true);
			}else if (commands[2].equals("false")){
				Loader.safeMap(this.referee.getBoard(), commands[1], false);
			}
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.loadmap.toString())){
//			this.map = Loader.loadMap(commands[1]);
			this.restart(Loader.loadMap(commands[1]));
			this.message = "This order is complete: " + command;
		}else if (commands[0].equals(order.showinfo.toString())){
			if (commands[1].equals("grid")){
				this.message = "This order is complete: " + command;
				this.message += "\n" + this.referee.showGridInfo(Integer.parseInt(commands[3]), Integer.parseInt(commands[2]));
			}else if (commands[1].equals("map")){
				
			}
		}
		else this.message = "unknown order: " + command;
	}
	
	private String onput(){
		return this.message;
	}

	private void restart(int mapX, int mapY, int mineNumber, String rule, int loopTimes, int testTimes){		// 重新開始一局，為GUI設計
		this.referee = new Referee(mapY, mapX, mineNumber, rule, loopTimes, testTimes);
		UI.static_referee = this.referee;
		this.player = new Player(mapX, mapY, mineNumber, rule, this.referee);
	}
	
	private void restart(int mapX, int mapY, int mineNumber, String rule){		// 重新開始一局，為GUI設計
		this.referee = new Referee(mapY, mapX, mineNumber, rule);
		UI.static_referee = this.referee;
		this.player = new Player(mapX, mapY, mineNumber, rule, this.referee);
	}
	
	public void set_PlayerTimes(int playTimes) {
		this.referee.set_PlayTimes(playTimes);
	}
	
	public void restart(Referee_Board map){		// 讀取盤面再開始
		this.referee = new Referee(map);
		UI.static_referee = this.referee;
		this.player = new Player(map.getMapX(), map.getMapY(), map.getMineNumber(), Referee.rule_1, this.referee);
//		System.out.println("mapX = " + map.getMapX());
//		System.out.println("MapY = " + map.getMapY());
//		System.out.println("MineNumber = " + map.getMineNumber());
	}
	
	public void setFinalize(){
		if (this.player != null){
			this.player.finalize_By_Teddy();
			this.player = null;
		}
		
		if (this.referee != null){
			this.referee.finalize_By_Teddy();
			this.referee = null;
			UI.static_referee = null;
		}
		
		for (int index = 0 ; index < 100 ; index++){
			System.gc();	//釋放記憶體
		}
	}
	
	public double[][] getHintMap(){
		return this.player.getHintMap();
	}
	
	public double[][] getDigValueMap(){
		return this.player.getDigValueMap();
	}
	
	public short getState (int x, int y){		//得到某格的狀態
		return this.referee.getBoard().getState(y, x);
	}
	
	public boolean isVisit(int x, int y){
		return this.referee.isVisit(y, x);
	}
	
	public int getremainMineNumber(){
		return this.referee.getremainMineNumber();
	}
	
	public boolean isGameOver(){
		return this.referee.isGameOver();
	}
	
	public boolean isWin(){
		return this.referee.isWin();
	}
	
	public boolean isGuess(){
		return this.player.isGuess();
	}
	
	public static void safeMap_In_debug(String fileName){
		Loader.safeMap(UI.static_referee.getBoard(), fileName + "_" + UI.static_referee.loopTimes + "_" + UI.static_referee.testTimes + "_" + UI.static_referee.playTimes);
	}
	
	public void showMap(String kind, Board map){
		this.message += "\n";
		if (map instanceof Referee_Board){
			Referee_Board board = (Referee_Board) map;
			
			// show the sort of map of Board
			for (int y = map.getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < map.getMapX() ; x++){
					if (kind.equals("show answer")){		//印出內部地圖
						if (board.isInStateMine(y, x) == true){
							System.out.print("@" + "\t");
							this.message += "@" + "\t";
						}else {
							System.out.print(board.getInState(y, x)+ "\t");
							this.message += board.getInState(y, x)+ "\t";
						}
					}
					
					if (kind.equals("show map")){			//印出外部地圖
						if (board.getState(y, x) == Board.initial){
							System.out.print("#" + "\t");
							this.message += "#" + "\t";
						}else if (board.getState(y, x) == Board.flag){
							System.out.print("P" + "\t");
							this.message += "P" + "\t";
						}else if (board.getState(y, x) == Board.mine){
							System.out.print("@" + "\t");
							this.message += "@" + "\t";
						}else {
							System.out.print(board.getState(y, x) +"\t");
							this.message += board.getState(y, x) +"\t";
						}
					}
				}
				System.out.println("");
				this.message += "\n";
			}
			
		}else if (map instanceof Player_Board){
			Player_Board pboard = (Player_Board) map;
			
			for (int y = map.getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < map.getMapX() ; x++){
					if (kind.equals("show map")){
						if (pboard.getState(y, x) == Board.mine){
							System.out.print("@" + "\t");
						}else {
							System.out.print(pboard.getState(y, x)+ "\t");
						}
					}
				}
				System.out.println("");
			}
		}
		System.out.println("");
	}
	
	
	
	public short getInState(int x, int y) {
		return this.referee.getInState(y, x);
	}
	
	public int getMapX(){
		return referee.getMapX();
	}
	
	public int getMapY(){
		return referee.getMapY();
	}

	public int getMineNumber(){
		return this.referee.getMineNumber();
	}
	
	public String getRule(){
		return this.referee.getRule();
	}
	
	public Referee_Board getBoard(){
		return this.referee.getBoard();
	}

	public String getStrategy() {
		String strategy = "";
		strategy = this.player.getStrategy(strategy);
		strategy = strategy.replace("||", "|");	//取代字串
		return strategy;
	}
	
	public BigDecimal getUseCombineTimes() {
		return this.player.getUseCombineTimes();
	}

	public BigDecimal getAllCombineTimes() {
		return this.player.getAllCombineTimes();
	}
	
//	public static void main(String[] args){
//	boolean uiTest = false;
//	
//	if (uiTest){
//		System.out.println("This is UI");
//		UI ui = new UI();
////		ui.map = Loader.loadMap("D:\\nowtest.txt");
////		ui.restart(ui.map);
//		
//		int mapX = 30;
//		int mapY = 16;
//		int mineNumber = 99;
//		String rule = "rule_2";
//		final int testLoopTimes = 10;
//		final int testTimes = 100;  //測試次數
//		
//		while (true){
//			for (int tl = 0 ; tl < testLoopTimes ; tl++){
//				for (int tt = 0 ; tt < testTimes ; tt++){
//					ui.restart(mapX, mapY, mineNumber, rule);
//					ui.player.setAI("FOPJ", true);
//					ui.player.setAI("FOPJ_Guess", true);
//				}
//			}
//			
//			
//////			ui.message = scan.nextLine();
////			ui.showMap("show map", ui.referee.getBoard());
////			System.out.println(ui.player.answer());
////			ui.input(ui.player.answer());
//////			ui.input("flag 0 0");
//////			ui.input("allflag 3 3");
////			ui.showMap("show answer", ui.referee.getBoard());
////			System.out.println("");
////			ui.showMap("show map", ui.referee.getBoard());
////			
////			break;
//		}
//		
////		System.out.println("The UI is end");
//	}
//}
}
