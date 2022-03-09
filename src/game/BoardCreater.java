package game;

import java.math.BigDecimal;
import java.util.LinkedList;
import ai.TeddyMethodTool;
import allSolve.MapLinkedList;

public class BoardCreater {
	private static boolean isinputSet = false;		//是否引入參數
	private static BigDecimal allProblem, problemIndex;
	private int mapX, mapY, mapSize, mineNumber;
	private final static int limitMineNumber = 2;	// 限定角落可以有的地雷數
	private static int retryTimes = 100;	// 最多重試幾次
	
	private static int runTimes = 1;
	private static int showTime = 100000000;
	
	// 測試變數---------------------------------------
	private static UI ui = new UI();
	private static int wintimes, losetimes;		// 成功次數、失敗次數		, retryTimes
	private static boolean isWin;		// 是否成功
	private static TimeCounter timeCounter;	//計時器
	// ---------------------------------------測試變數
	
	public BoardCreater(int mapY, int mapX, int mineNumber){
		this.mapY = mapY;
		this.mapX = mapX;
		this.mapSize = this.mapY * this.mapX;
		this.mineNumber = mineNumber;
	}
	
	public static void main(String args[]){
		int mapY, mapX, mineNumber;
		if (BoardCreater.isinputSet == true){
			mineNumber = Integer.parseInt(args[0]);
			mapX = Integer.parseInt(args[1]);
			mapY = Integer.parseInt(args[2]);
			BoardCreater.retryTimes = Integer.parseInt(args[3]);
			BoardCreater.exam(mineNumber, mapY, mapX);
		}else {
			int testArray[][] = {	// {mine, y, x}
//					{1, 4, 4},
//					{2, 4, 4},
//					{3, 4, 4},
//					{3, 3, 3},
//					{1, 1, 3},
//					{3, 2, 5},
//					{4, 4, 4}, 
//					{10, 5, 5},
//					{5, 1, 10},
//					{10, 3, 7},
//					{15, 5, 5},
					{10, 8, 8},
//					{10, 9, 9},
				};
				
			for (int index = 0 ; index < testArray.length ; index++){
				mineNumber = testArray[index][0];
				mapY = testArray[index][1];
				mapX = testArray[index][2];
//				BoardCreater.exam(mineNumber, mapY, mapX);
				BoardCreater creater = new BoardCreater(mapY, mapX, mineNumber);
				System.out.println("current size = " + new TeddyMethodTool().conbineNumber((mapY * mapX), mineNumber));
				timeCounter = new TimeCounter();
				
				LinkedList<int[]> maplink = creater.get_allMap();
				BoardCreater.timeCounter.start();
				System.out.println("fact size = " + maplink.size());
				BoardCreater.timeCounter.end();
				BoardCreater.timeCounter.setCountTotalTime();
				System.out.println("total time = " + BoardCreater.timeCounter.getCountTotalTime() + " sec");
			}
		}
	}
	
	private static void exam(int mineNumber, int mapY, int mapX){		// 實驗主體
//		TeddyMethodTool tool = new TeddyMethodTool();
		
		BoardCreater.wintimes = 0;
		BoardCreater.losetimes = 0;
		System.out.println("This is BoardCreater");
		System.out.println("isinputSet = " + BoardCreater.isinputSet);
		System.out.println("mapY = " + mapY);
		System.out.println("mapX = " + mapX);
		System.out.println("mineNumber = " + mineNumber);
		System.out.println("retryTimes = " + BoardCreater.retryTimes);
		System.out.println("limitMineNumber = " + BoardCreater.limitMineNumber);

		BoardCreater creater = new BoardCreater(mapY, mapX, mineNumber);
		BoardCreater.allProblem = new TeddyMethodTool().conbineNumber((mapY * mapX), mineNumber);
		System.out.println("總共會有 " + BoardCreater.allProblem.toString() + " 種盤面");
		BoardCreater.problemIndex = BigDecimal.valueOf(0);
		timeCounter = new TimeCounter();
		creater.createMap();		// 創造地圖

		System.out.println("\n結果------------------------------------");
		System.out.println("已執行出 " + BoardCreater.problemIndex + " 種盤面");
		System.out.println("runTimes = " + (BoardCreater.wintimes+BoardCreater.losetimes));
		System.out.println("實驗題目為：" + mineNumber + " mines on " + mapX + "x" + mapY);
		System.out.println("wintimes = " + BoardCreater.wintimes);
		System.out.println("losetimes = " + BoardCreater.losetimes);
		System.out.println("total time = " + BoardCreater.timeCounter.getCountTotalTime() + " sec");
		System.out.println("實驗使用策略 = " + BoardCreater.ui.getStrategy());
		System.out.println("------------------------------------結果");
	}
	
	// TODO 尋找已開地圖的所有可能
	public LinkedList<int[]> get_allMap(int[] map){
		LinkedList<int[]> maplink = new LinkedList<int[]>();
		boolean[] problemMap = new boolean[this.mapSize];	// true is mine.
		for (int index = 0 ; index < this.mapSize ; index++){
			if (map[index] == Board.flag){
				problemMap[index] = true;
				this.mineNumber--;
			}
		}
		
		for (int index = 0 ; index <= (this.mapSize - this.mineNumber) ; index++){
			if (problemMap[index] == false){
				problemMap[index] = true;
				singleCreate(index, this.mineNumber-1, problemMap, maplink, map);
				problemMap[index] = false;
			}else {
				continue;
			}
		}
		
		return maplink;
	}
	
	public LinkedList<int[]> get_allMap(){
		LinkedList<int[]> maplink = new LinkedList<int[]>();
//		MapLinkedList maplink = new MapLinkedList(); 
		
		boolean[] problemMap = new boolean[this.mapSize];	// true is mine.
		for (int index = 0 ; index <= (this.mapSize - this.mineNumber) ; index++){
			problemMap[index] = true;
//			maplink = singleCreate(index, this.mineNumber-1, problemMap, maplink);
			singleCreate(index, this.mineNumber-1, problemMap, maplink);
			problemMap[index] = false;
		}
		
		return maplink;
	}
	
	public void get_allMap_test(){
//		MapLinkedList maplink = new MapLinkedList();
		LinkedList<int[]> maplink = new LinkedList<int[]>();
		while(true){
//			int[] newMap = new int[this.mapSize];
			maplink.addLast(new int[this.mapSize]);
			
			// 印出目前進度
//			if (BoardCreater.runTimes == BoardCreater.showTime){
//				System.out.println("create map, " + maplink.size());
//				BoardCreater.runTimes = 1;
//			}else {
//				BoardCreater.runTimes++;
//			}
		}
	}
	
	// 利用遞迴創造地圖(陣列版)
		private void singleCreate(int lastIndex, int remainingMine, boolean[] problemMap, LinkedList<int[]> maplink, int[] realMap){
			if (remainingMine > 0){
				for (int index = lastIndex + 1 ; index < this.mapSize ; index++){
					problemMap[index] = true;
					singleCreate(index, remainingMine-1, problemMap, maplink);
					problemMap[index] = false;
				}
			}else {
				int showNumber;
				int[] newMap = new int[problemMap.length];
				boolean isNewMap = true;
				
				// 設定數字
				for (int y = 0 ; y < this.mapY ; y++){
					for (int x = 0 ; x < this.mapX ; x++){
						if (problemMap[(y * this.mapX) + x] == false){
							showNumber = 0;
							for (int j = y-1 ; j <= y+1 ; j++){
								for (int i = x-1 ; i <= x+1 ; i++){
									if (j != y || i != x){		//中間的點不用理
										if (j >=0 && j < this.mapY && i >= 0 && i < this.mapX && problemMap[(j * this.mapX) + i] == true){
											showNumber++;
										} 
									}
								}
							}
							if (realMap[Referee_Board.getRealSituation(this.mapX, y, x)] != Board.initial){
								if (realMap[Referee_Board.getRealSituation(this.mapX, y, x)] != showNumber){
									isNewMap = false;		// 所求數字不符合當前盤面
									break;
								}
							}
							newMap[Referee_Board.getRealSituation(this.mapX, y, x)] = showNumber;
						}else {
							newMap[Referee_Board.getRealSituation(this.mapX, y, x)] = Board.mine;
						}
					}
					if (isNewMap == false) break;
				}
				
				if (isNewMap == true){
//					maplink.addLast(newMap);					
				}

				if (BoardCreater.runTimes == BoardCreater.showTime){
					System.out.println("create map, " + maplink.size());
					BoardCreater.runTimes = 1;
				}else {
					BoardCreater.runTimes++;
				}
				
			}
//			return maplink;
		}
	
	// 利用遞迴創造地圖(陣列版)
	private void singleCreate(int lastIndex, int remainingMine, boolean[] problemMap, LinkedList<int[]> maplink){
		if (remainingMine > 0){
			for (int index = lastIndex + 1 ; index < this.mapSize ; index++){
				problemMap[index] = true;
//				maplink = singleCreate(index, remainingMine-1, problemMap, maplink);
				singleCreate(index, remainingMine-1, problemMap, maplink);
				problemMap[index] = false;
			}
		}else {
			int showNumber;
			int[] newMap = new int[problemMap.length];
			// 設定數字
			for (int y = 0 ; y < this.mapY ; y++){
				for (int x = 0 ; x < this.mapX ; x++){
					if (problemMap[(y * this.mapX) + x] == false){
						showNumber = 0;
						for (int j = y-1 ; j <= y+1 ; j++){
							for (int i = x-1 ; i <= x+1 ; i++){
								if (j != y || i != x){		//中間的點不用理
									if (j >=0 && j < this.mapY && i >= 0 && i < this.mapX && problemMap[(j * this.mapX) + i] == true){
										showNumber++;
									} 
								}
							}
						}
						newMap[Referee_Board.getRealSituation(this.mapX, y, x)] = showNumber;
					}else {
						newMap[Referee_Board.getRealSituation(this.mapX, y, x)] = Board.mine;
					}
				}
			}
			maplink.addLast(newMap);
			if (BoardCreater.runTimes == BoardCreater.showTime){
				System.out.println("create map, " + maplink.size());
				BoardCreater.runTimes = 1;
			}else {
				BoardCreater.runTimes++;
			}
			
		}
//		return maplink;
	}
	
	private void createMap(){	// 創造所有的地圖
		boolean[] problemMap = new boolean[this.mapSize];
		for (int index = 0 ; index <= (this.mapSize - this.mineNumber) ; index++){
			problemMap[index] = true;
			singleCreate(index, this.mineNumber-1, problemMap);
			problemMap[index] = false;
		}
	}
	
	// 利用遞迴創造地圖(陣列版)
	private void singleCreate(int lastIndex, int remainingMine, boolean[] problemMap){
		if (remainingMine > 0){
			for (int index = lastIndex + 1 ; index < this.mapSize ; index++){
				problemMap[index] = true;
				singleCreate(index, remainingMine-1, problemMap);
				problemMap[index] = false;
			}
		}else {
			BoardCreater.problemIndex = BoardCreater.problemIndex.add(BigDecimal.valueOf(1));
//			this.safe_map(problemMap);
			this.exam_basic(problemMap, true);
//			this.exam_all_solve_and_exam_basic(problemMap, true);
//			this.exam_all_solve_in_limitTimes_and_exam_basic(problemMap, true);
//			this.exam_testTimes_and_exam_basic(problemMap, true);
//			this.exam_corner_mine_limit_and_exam_all_solve_and_exam_basic(problemMap, true);
//			this.exam_testTimes_and_exam_basic(problemMap, true);
			
		}
	}
	
	private void safe_map(boolean[] problemMap){
		for (int index = 0 ; index < this.mapSize ; index++){
			System.out.print(problemMap[index] + " ");
		}
		System.out.println();
		Loader.safeMap(new Referee_Board(this.mapY, this.mapX, this.mineNumber, problemMap),
				"Problem_" + this.mineNumber + "_mines" + "_on_" + this.mapX + "x" + this.mapY);
	}
	
	// 最基本的測試
	private void exam_basic(boolean[] problemMap, boolean isNeedPrint){
		Referee_Board creatBoard = new Referee_Board(this.mapY, this.mapX, this.mineNumber, problemMap);
		BoardCreater.ui.restart(creatBoard);
		BoardCreater.ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod + " true");
		BoardCreater.ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod_Guess + " true");
		
//		System.out.println("real map");
//		BoardCreater.ui.input(order.showmap.toString() + " in");
		
		BoardCreater.timeCounter.start();
		BoardCreater.isWin = BoardCreater.run(ui);
		BoardCreater.timeCounter.end();
		BoardCreater.timeCounter.setCountTotalTime();
		
		
		
		// 記錄成功或失敗次數
		if (BoardCreater.isWin == true){
			BoardCreater.wintimes++;
		}else {
			BoardCreater.losetimes++;
		}

		if (isNeedPrint == true){
			System.out.print(this.showProblemIndex() + "\t");
			if (BoardCreater.isWin == true){
				System.out.print("It's win" + "\t");
			}else {
				System.out.print("It's lose" + "\t");
			}
			System.out.print("runTimes = " + (wintimes+losetimes) + "\t");
			System.out.print("time = " + timeCounter.getSingleCountTime() + "s\t");
			System.out.println("total time = " + timeCounter.getCountTotalTime() + "s\t");
		}
	}
	
	// 一定要解出來才會解下一題
	private void exam_all_solve_and_exam_basic(boolean[] problemMap, boolean isNeedPrint){
		int retryTimes = 0;
		while (true){
			this.exam_basic(problemMap, false);
			
			if (isNeedPrint == true){
				System.out.print(this.showProblemIndex() + "\t");
				if (BoardCreater.isWin == true){
					System.out.print("It's win" + "\t");
				}else {
					System.out.print("It's lose" + "\t");
				}
				System.out.print("retryTimes = " + retryTimes + "\t");
				System.out.print("runTimes = " + (wintimes+losetimes) + "\t");
				System.out.print("time = " + timeCounter.getSingleCountTime() + "s\t");
				System.out.println("total time = " + timeCounter.getCountTotalTime() + "s\t");
			}
			
			if (BoardCreater.isWin == true){
				break;
			}else {
				retryTimes++;
			}
		}
	}
	
	//	 一定要解出來才會解下一題，但有重試的次數有限制
	private void exam_all_solve_in_limitTimes_and_exam_basic(boolean[] problemMap, boolean isNeedPrint){
		int retryTimes = 0;
		while (true){
			this.exam_basic(problemMap, false);
			
			if (isNeedPrint == true){
				System.out.print(this.showProblemIndex() + "\t");
				if (BoardCreater.isWin == true){
					System.out.print("It's win" + "\t");
				}else {
					System.out.print("It's lose" + "\t");
				}
				System.out.print("retryTimes = " + retryTimes + "\t");
				System.out.print("runTimes = " + (wintimes+losetimes) + "\t");
				System.out.print("time = " + timeCounter.getSingleCountTime() + "s\t");
				System.out.println("total time = " + timeCounter.getCountTotalTime() + "s\t");
			}
			
			if (BoardCreater.isWin == true || (retryTimes == BoardCreater.retryTimes)){
				break;
			}else {
				retryTimes++;
			}
		}
	}
	
	private boolean is_legal_corner_mine_limit(boolean[] problemMap){	// 是否符合角落地雷的限制
		int cornerMine = 0;
		Referee_Board creatBoard = new Referee_Board(this.mapY, this.mapX, this.mineNumber, problemMap);
		cornerMine = creatBoard.getInState(0, 0) == Board.mine ? cornerMine+1 : cornerMine;
		cornerMine = creatBoard.getInState(0, this.mapX-1) == Board.mine ? cornerMine+1 : cornerMine;
		cornerMine = creatBoard.getInState(this.mapY-1, 0) == Board.mine ? cornerMine+1 : cornerMine;
		cornerMine = creatBoard.getInState(this.mapY-1, this.mapX-1) == Board.mine ? cornerMine+1 : cornerMine;
		return (cornerMine <= BoardCreater.limitMineNumber);
	}
	
	// 一定要解出來才會解下一題，限定角落地雷數量的問題的測試，成功解出後才能往下繼續解
	private void exam_corner_mine_limit_and_exam_all_solve_and_exam_basic(boolean[] problemMap, boolean isNeedPrint){
		if (this.is_legal_corner_mine_limit(problemMap) == true){
			this.exam_all_solve_and_exam_basic(problemMap, isNeedPrint);
		}else {
			if (isNeedPrint == true){
				System.out.println(this.showProblemIndex() + " 不勝而勝");
			}
		}
	}
	
	// 執行一定的次數，以查看每一種盤面的勝率
	private void exam_testTimes_and_exam_basic(boolean[] problemMap, boolean isNeedPrint){
		int testTimes = 100;
		int wintimes = 0, losetimes =0;
		for (int index = 0 ; index < testTimes ; index++){
			this.exam_basic(problemMap, false);
			
			// 記錄成功或失敗次數
			if (isWin == true){
				wintimes++;
			}else {
				losetimes++;
			}
		}
		
		if (isNeedPrint == true){
			System.out.print(this.showProblemIndex() + "\t");
			System.out.print("winRate = " + BigDecimal.valueOf(wintimes).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(wintimes+losetimes), 3, BigDecimal.ROUND_HALF_UP).toString() + "%\t");
			System.out.print("runTimes = " + (wintimes+losetimes) + "\t");
			System.out.println("total time = " + timeCounter.getCountTotalTime() + "s\t");
		}
	}
	
	private static boolean run(UI ui){		//測試的函數
		while (ui.isGameOver() == false){
			ui.input(UI.order.AI_Run_one_Step.toString());
			// 是否將每一個步驟的盤面都存下來
			if (Experiment.isStepByStepDebug == true){
				Loader.safeMap(ui.getBoard(), System.getProperty("user.dir"), 
						("BoardCreater_" + ui.getMineNumber() + "_mines" + "_on_" + ui.getMapX() + "x" + ui.getMapY()
								+ "_" + BoardCreater.problemIndex.toString()), false);
			}
		}		
		return ui.isWin();
	}
	
	private String showProblemIndex(){	// 回傳目前的題目編號
		return ("This is problem of " + BoardCreater.problemIndex.toString() + " / " + BoardCreater.allProblem.toString());
	}
}
