/*
 * 測試所有可能的盤面
 */
package game;
import java.io.File;
import java.math.BigDecimal;

public class ExperimentOfRunTimes2 {
	private static String fileParentPath;		// 檔案位置
	public static void main(String[] args){
		int mineNumber, mapX, mapY;								// 地雷個數、地圖長、地圖寬
//		String rule;
		System.out.println("This is ExperimentOfRunTimes2");
	    
	    ExperimentOfRunTimes2.fileParentPath = System.getProperty("user.dir");		//取得當前路徑
		
		TimeCounter timeCounter = new TimeCounter();	//計時器
		File loaderfile;		//讀檔專用
		
		UI ui = new UI();
		
		// 測試內定值
		mineNumber = 4;
		mapX = 4;
		mapY = 4;
//		rule = Referee.rule_1;
		
		int wintimes, losetimes;		// 成功次數、失敗次數
		boolean isWin;		// 是否成功
		
		int problemIndex = 148;		// 問題編號
		int testTimes = 1000;		// 每個問題測幾次
		
		while (true){
			loaderfile = new File(ExperimentOfRunTimes2.fileParentPath, "Problem_" 
					+ mineNumber + "_mines" + "_on_" + mapX + "x" + mapY + "_(" + problemIndex + ")" + Loader.ioFile);
			if (loaderfile.isFile() == false) {
				System.out.println("讀檔時找不到檔案");
				break;
			}
			
			wintimes = 0;
			losetimes = 0;
			
			for (int index = 0 ; index < testTimes ; index++){
				ui.input(UI.order.loadmap + " " + loaderfile.getPath());
				ui.input(UI.order.setAI + " " + "CF " + "true");
				ui.input(UI.order.setAI + " " + "CF_Guess " + "true");
				timeCounter.start();
				isWin = ExperimentOfRunTimes2.run(ui, problemIndex);
				timeCounter.end();
				timeCounter.setCountTotalTime();
				
				if (isWin == true){
					wintimes++;
				}else {
					losetimes++;
				}
			}
			
			System.out.print("The problem of " + problemIndex + "\t");
			System.out.println("win rate = " + BigDecimal.valueOf(wintimes).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(wintimes+losetimes), 3, BigDecimal.ROUND_HALF_UP).toString() + "%");
			
			problemIndex++;
			
//			// 記錄成功或失敗次數
//			if (isWin == true){
//				System.out.print("It's win" + "\t");
//			}else {
//				System.out.print("It's lose" + "\t");
//			}
//			
//			System.out.print("this is problem of " + problemIndex + "\t");
//			System.out.print("retryTimes = " + retryTimes + "\t");
//			System.out.print("runTimes = " + runTimes + "\t");
//			System.out.print("time = " + timeCounter.getSingleCountTime() + "s\t");
//			System.out.println("total time = " + timeCounter.getCountTotalTime() + "s\t");
			
			
		}
		
//		System.out.println("\n結果------------------------------------");
//		System.out.println("runTimes = " + runTimes);
//		System.out.println("使用規則為 " + rule);
//		System.out.println("實驗題目為：" + mineNumber + " mines on " + mapX + "x" + mapY);
//		System.out.println("實驗結果資料夾在：" + ExperimentOfRunTimes2.fileParentPath);
//		System.out.println("wintimes = " + wintimes);
//		System.out.println("losetimes = " + losetimes);
//		System.out.println("total time = " + timeCounter.getCountTotalTime() + " sec");
//		System.out.println("實驗使用策略 = " + ui.getStrategy());
//		System.out.println("------------------------------------結果");
	}
	
	private static boolean run(UI ui, int testTimes){		//測試的函數
		while (ui.isGameOver() == false){
			ui.input(UI.order.AI_Run_one_Step.toString());
		}		
		return ui.isWin();
	}
}
