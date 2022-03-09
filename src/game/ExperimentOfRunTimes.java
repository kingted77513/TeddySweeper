/*
 * 測試所有可能的盤面
 */
package game;
import java.io.File;

public class ExperimentOfRunTimes {
	private static String fileParentPath;		// 檔案位置
	public static void main(String[] args){
		int mineNumber, mapX, mapY;								// 地雷個數、地圖長、地圖寬
		String rule;
		System.out.println("This is ExperimentOfRunTimes");
	    
	    ExperimentOfRunTimes.fileParentPath = System.getProperty("user.dir");		//取得當前路徑
		
		TimeCounter timeCounter = new TimeCounter();	//計時器
		File loaderfile;		//讀檔專用
		
		UI ui = new UI();
		
		// 測試內定值
		mineNumber = 4;
		mapX = 4;
		mapY = 4;
		rule = Referee.rule_1;
		
		int wintimes = 0, losetimes = 0;		// 成功次數、失敗次數
		boolean isWin;		// 是否成功
		
		int tt = 1;
		int runTimes = 0;
		int retryTimes = 0;
		
		int cornerMine;
		while (true){
			loaderfile = new File(ExperimentOfRunTimes.fileParentPath, "Problem_" 
					+ mineNumber + "_mines" + "_on_" + mapX + "x" + mapY + "_(" + tt + ")" + Loader.ioFile);
			if (loaderfile.isFile() == false) {
				System.out.println("讀檔時找不到檔案");
				break;
			}
			
			Referee_Board cheatBoard = Loader.loadMap(loaderfile.getPath());
			cornerMine = 0;
			cornerMine = cheatBoard.getInState(0, 0) == Board.mine ? cornerMine+1 : cornerMine;
			cornerMine = cheatBoard.getInState(0, mapX-1) == Board.mine ? cornerMine+1 : cornerMine;
			cornerMine = cheatBoard.getInState(mapY-1, 0) == Board.mine ? cornerMine+1 : cornerMine;
			cornerMine = cheatBoard.getInState(mapY-1, mapX-1) == Board.mine ? cornerMine+1 : cornerMine;
			
			if (cornerMine >= 1){
				System.out.println("this is problem of " + tt + " 不勝而勝");
				tt++;
				continue;
			}
			
//			if (cheatBoard.getInState(0, 0) == Map.mine || cheatBoard.getInState(0, mapX-1) == Map.mine 
//					|| cheatBoard.getInState(mapY-1, 0) == Map.mine || cheatBoard.getInState(mapY-1, mapX-1) == Map.mine){
//				System.out.println("this is problem of " + tt + " 不勝而勝");
//				tt++;
//				continue;
//			}
			
			ui.input(UI.order.loadmap + " " + loaderfile.getPath());
			ui.input(UI.order.setAI + " " + "CF " + "true");
			ui.input(UI.order.setAI + " " + "CF_Guess " + "true");
			runTimes++;
			timeCounter.start();
			isWin = ExperimentOfRunTimes.run(ui, tt);
			timeCounter.end();
			timeCounter.setCountTotalTime();
			
			// 記錄成功或失敗次數
			if (isWin == true){
				System.out.print("It's win" + "\t");
			}else {
				System.out.print("It's lose" + "\t");
			}
			
			System.out.print("this is problem of " + tt + "\t");
			System.out.print("retryTimes = " + retryTimes + "\t");
			System.out.print("runTimes = " + runTimes + "\t");
			System.out.print("time = " + timeCounter.getSingleCountTime() + "s\t");
			System.out.println("total time = " + timeCounter.getCountTotalTime() + "s\t");
			
			if (isWin == true){
				retryTimes = 0;
				wintimes++;
				tt++;
			}else {
				retryTimes++;
				losetimes++;
			}
		}
		
		System.out.println("\n結果------------------------------------");
		System.out.println("runTimes = " + runTimes);
		System.out.println("使用規則為 " + rule);
		System.out.println("實驗題目為：" + mineNumber + " mines on " + mapX + "x" + mapY);
		System.out.println("實驗結果資料夾在：" + ExperimentOfRunTimes.fileParentPath);
		System.out.println("wintimes = " + wintimes);
		System.out.println("losetimes = " + losetimes);
		System.out.println("total time = " + timeCounter.getCountTotalTime() + " sec");
		System.out.println("實驗使用策略 = " + ui.getStrategy());
		System.out.println("------------------------------------結果");
	}
	
	private static boolean run(UI ui, int testTimes){		//測試的函數
		while (ui.isGameOver() == false){
			ui.input(UI.order.AI_Run_one_Step.toString());
		}		
		return ui.isWin();
	}
}
