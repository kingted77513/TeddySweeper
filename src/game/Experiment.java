/*
 * 專為大量實驗專用
 */
package game;
import java.util.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.*;

public class Experiment {
	private static boolean isinputSet = false;		//是否引入參數
	private static boolean isDebug = true;
	public static boolean isStepByStepDebug = false;	// 是否每一步都要儲存
	public static boolean isLogicTest = false;	// 是否邏輯檢查
	private static boolean isRecordNowRun = true;		// 是否儲存現在跑的案例
	private static boolean isRecord;			//是否讀檔
	public static boolean isShowNowSitulation = false;	// 是否要秀出現在的組合次數
	private static boolean isFirstStep;
	private static String fileParentPath;		// 檔案位置
	
	public static void main(String[] args){
		int mineNumber, mapX, mapY;								// 地雷個數、地圖長、地圖寬
		int testLoopTimes, testTimes, showPeriod;	// 測驗實驗次數、每個實驗的測試數、秀出目前結果的周期數
		int guessTimes;								// 有使用猜測的次數
		
		String rule, strategy = null, notes = "20130930_有預測，有分區。加上計算使用分區化功能的次數";
		System.out.println("This is Experiment");
		
		Date timeDate = Calendar.getInstance().getTime();	//取得系統時間
	    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/M/d a hh:mm");
	    
	    Experiment.fileParentPath = System.getProperty("user.dir");		//取得當前路徑
		
		TimeCounter timeCounter = new TimeCounter();	//計時器
		File loaderfile;		//讀檔專用
		
		UI ui = new UI();
		if (Experiment.isinputSet == true){
			mineNumber = Integer.parseInt(args[0]);
			mapX = Integer.parseInt(args[1]);
			mapY = Integer.parseInt(args[2]);
			
			switch (args[3]){
			case "r1":
				rule = Referee.rule_1;
				break;
			case "r2":
				rule = Referee.rule_2;
				break;
			case "r3":
				rule = Referee.rule_3;
				break;
			default:
				rule = Referee.rule_2;
				break;
			}
			
			testLoopTimes = Integer.parseInt(args[4]);
			testTimes = Integer.parseInt(args[5]);  //測試次數
			showPeriod = Integer.parseInt(args[6]);  //周期數
		}else {
			// 測試內定值 
			mineNumber = 4;
			mapX = 4;
			mapY = 4;
			rule = Referee.rule_2;
			testLoopTimes = 1;
			testTimes = 1000;  //測試次數
			Experiment.isRecord = false;
			showPeriod = 10;
		}
		
		loaderfile = new File(Experiment.fileParentPath, "Problem_" 
				+ mineNumber + "_mines" + "_on_" + mapX + "x" + mapY + "_(1)" + Loader.ioFile);
		if (loaderfile.isFile() == true){		//如果第一個檔存在則讀檔，否則寫檔
			Experiment.isRecord = true;
		}else {
			Experiment.isRecord = false;
		}
		
		if (Experiment.isDebug == true){
			System.out.println("Experiment.isRecord = " + Experiment.isRecord);
		}
		
		if (Experiment.isRecord == true){
			rule = Referee.rule_1;
		}
		
		int wintimes, losetimes, errortimes;		// 成功次數、失敗次數、錯誤次數
		boolean isWin = false;		// 是否成功
		double maxTime;			// 解題最久的秒數
		double winRate;
		BigDecimal sum_usecombineTimes;				// 使用組合方法的次數
		BigDecimal sum_combineTimes;				// 組合數的總和
		BigDecimal maxCombineTimes;					// 最大的組合數
		
		for (int tl = 1 ; tl <= testLoopTimes ; tl++){
			wintimes = 0;
			losetimes = 0;
			errortimes = 0;
			maxTime = 0;
			guessTimes = 0;
			sum_usecombineTimes = BigDecimal.valueOf(0);
			sum_combineTimes = BigDecimal.valueOf(0);
			maxCombineTimes = BigDecimal.valueOf(0);
			timeCounter.initial();
			for (int tt = 1 ; tt <= testTimes ; tt++){
//				ui.input(UI.order.setFinalize.toString());		// 進行解構
				timeDate = Calendar.getInstance().getTime();	//取得系統時間
				if (Experiment.isRecord == true){
					loaderfile = new File(Experiment.fileParentPath, "Problem_" 
							+ mineNumber + "_mines" + "_on_" + mapX + "x" + mapY + "_(" + tt + ")" + Loader.ioFile);
					if (loaderfile.isFile() == false) {
						System.out.println("讀檔時找不到檔案");
						break;
					}
					ui.input(UI.order.loadmap + " " + loaderfile.getPath());
				}else {
					ui.input(UI.order.reset + " " + mapX + " " + mapY + " " + mineNumber + " " + rule + " " + tl + " " + tt);
				}
				
				ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod + " true");
				ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod_Guess + " true");
				
				Experiment.isFirstStep = true;
				timeCounter.start();
				try {
					isWin = Experiment.run(ui, tl, tt);
				}catch(final OutOfMemoryError ome){
					System.out.println("出現記憶體不足錯誤，此局放棄！！");
					Loader.safeMap(ui.getBoard(), Experiment.fileParentPath, ("MemoryError_" + mineNumber + "_mines" + "_on_" + mapX + "x" + mapY), true);
					System.out.println("問題盤面已儲存在：" + Experiment.fileParentPath);
					errortimes++;
					System.out.println("error times = " + errortimes);
					tt--;
					System.gc();	//釋放記憶體
					ome.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
					if (Experiment.isStepByStepDebug == true){
						Loader.safeMap(ui.getBoard(), Experiment.fileParentPath, ("Error_" + mineNumber + "_mines" + "_on_" 
								+ mapX + "x" + mapY + "_" + tl + "_" + tt), false);
						System.out.println("問題盤面已儲存在：" + Experiment.fileParentPath);
					}
				}
				timeCounter.end();
				timeCounter.setCountTotalTime();
				
				if (tl == 1 && tt == 1){
					strategy = ui.getStrategy();							//取得使用策略
					System.out.println("測試時間：" + sdf1.format(timeDate));
					System.out.println("測試板本為：" + "3.1");
					System.out.println("讀入參數：" + isinputSet);
					System.out.println("測試次數為：" + testLoopTimes);
					System.out.println("測試盤數為：" + testTimes);
					System.out.println("實驗規則為：" + rule);
					System.out.println("實驗結果資料夾在：" + Experiment.fileParentPath);
					System.out.println("實驗題目為：" + mineNumber + " mines on " + mapX + "x" + mapY);
					System.out.println("是否讀檔：" + Experiment.isRecord);
					System.out.println("實驗使用策略：" + strategy);
					System.out.println("實驗備註：" + notes);
				}
				
				if (Experiment.isDebug == true){
					if (tt % showPeriod == 0){
						System.out.print("起初時間 = " + sdf1.format(timeDate) + "\t" + "第 " + tl + " 回，" + "第 " + tt + " 次遊戲 " 
								+ mineNumber + "m on " + mapX + "x" + mapY + " in " + rule
								+ "\t");
//						if (Experiment.isRecord == true) ui.showMap("show map", ui.getBoard());
					}
				}
				
				if (timeCounter.getSingleCountTime() > maxTime) {		// 計算最大時間
					maxTime = timeCounter.getSingleCountTime();
				}
				
				sum_usecombineTimes = sum_usecombineTimes.add(ui.getUseCombineTimes());
				sum_combineTimes = sum_combineTimes.add(ui.getAllCombineTimes());
				if (ui.getAllCombineTimes().compareTo(maxCombineTimes) > 0){	// 計算最大的組合數
					maxCombineTimes = ui.getAllCombineTimes();
				}
				
				// 記錄成功或失敗次數
				if (isWin == true){
					wintimes++;
				}else {
					losetimes++;
				}
				
				if (ui.isGuess() == true){
					guessTimes++;
				}
				
				if (tt % showPeriod == 0){
					if (isWin == true){
						System.out.print("It's win" + "\t");
					}else {
						System.out.print("It's lose" + "\t");
					}
//					System.out.printf("win rate = %.3f", ((double)wintimes/(tt))*100);
//					System.out.print("%\t");
					System.out.print("win rate = " + BigDecimal.valueOf(wintimes).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(tt), 3, BigDecimal.ROUND_HALF_UP).toString() + "%");
					winRate = (double)wintimes/tt;
					System.out.print(" +- " + BigDecimal.valueOf(100*Math.sqrt((double)winRate*(1-(double)winRate)/((double)testTimes))*2).divide(BigDecimal.valueOf(1), 3, BigDecimal.ROUND_HALF_UP).toString() + "%\t");
					System.out.print("guess rate = " + BigDecimal.valueOf(guessTimes).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(tt), 3, BigDecimal.ROUND_HALF_UP).toString() + "%\t");
					System.out.print("time = " + timeCounter.getSingleCountTime() + "s\t");
//					System.out.printf("avg time =  + %.3f", (timeCounter.getCountTotalTime()/tt));
//					System.out.print("s\t");
					System.out.print("avg time = " + BigDecimal.valueOf(timeCounter.getCountTotalTime()).divide(BigDecimal.valueOf(tt), 3, BigDecimal.ROUND_HALF_UP).toString() + "s\t");
					System.out.print("max time = " + maxTime + "s\t");
					System.out.print("total time = " + timeCounter.getCountTotalTime() + "s\t");
					System.out.print("use combine times = " + ui.getUseCombineTimes().toBigInteger().toString() + "\t");
					System.out.print("total use combine times = " + sum_usecombineTimes.toBigInteger().toString() + "\t");
//					System.out.print("combine times = " + ui.getMaxCombine() + "\t");
					System.out.print("combine times = " + ui.getAllCombineTimes().toBigInteger().toString() + "\t");
					System.out.print("avg combine times = " + sum_combineTimes.divide(BigDecimal.valueOf(tt), 3, BigDecimal.ROUND_HALF_UP).toString() + "\t");
//					System.out.print("max combine times = " + maxCombineTimes + "\t");
					System.out.print("max combine times = " + maxCombineTimes.toBigInteger().toString() + "\t");
					System.out.println("total combine times = " + sum_combineTimes.toBigInteger().toString());
				}
			}
			
			Loader.outputExperimentResult(ui, tl, testTimes, wintimes, losetimes, guessTimes, 
					maxTime, timeCounter.getCountTotalTime(), maxCombineTimes, sum_combineTimes, 
					sum_usecombineTimes, Experiment.fileParentPath, strategy, notes);
			
			System.out.println("\n結果------------------------------------");
			System.out.println("testtimes = " + testTimes);
			System.out.println("使用規則為 " + rule);
			System.out.println("實驗題目為：" + mineNumber + " mines on " + mapX + "x" + mapY);
			System.out.println("實驗結果資料夾在：" + Experiment.fileParentPath);
			System.out.println("wintimes = " + wintimes);
			System.out.println("losetimes = " + losetimes);
			System.out.println("guesstimes = " + guessTimes);
			System.out.println("total time = " + timeCounter.getCountTotalTime() + " sec");
			System.out.println("實驗使用策略 = " + strategy);
			// Oliver的結果----------------
//			ratioMines = ((double)mineNumber)/((double)(mapX*mapY));	// 地雷密度
//			winRatio = ((double)wintimes/(double)(losetimes+wintimes))*100;
//			loseP=1.-winRatio/100.;
//			realWinP=1.-(loseP-ratioMines)/(1.-ratioMines);
//			System.out.println("Win Ratio：" + winRatio + "%");
//			System.out.printf("Corrected Win Ratio  : %f +- %g %% \n",100*realWinP,100.*Math.sqrt(realWinP*(1-realWinP)/((double)testTimes)));
			// ----------------Oliver的結果
			System.out.println("------------------------------------結果");
		}
	}
	
	private static boolean run(UI ui, int testLoop, int testTimes){		//測試的函數
		while (ui.isGameOver() == false){
//			Experiment.playTimes++;
//			ui.set_PlayerTimes(Experiment.playTimes);
//			ui.input(UI.order.showmap + " " + "out");
			
			ui.input(UI.order.AI_Run_one_Step.toString());
			
			// 記錄現在正在跑的盤面
			if (Experiment.isRecordNowRun == true){
				File nowRunFile = new File(Experiment.fileParentPath, 
						"NowRun_" + ui.getMineNumber() + "_mines" + "_on_" + ui.getMapX() + "x" + ui.getMapY() + Loader.ioFile);
				Loader.safeMap(ui.getBoard(), nowRunFile.getPath(), !ui.isWin());
			}
			
			// 是否將每一個步驟的盤面都存下來
			if (Experiment.isStepByStepDebug == true){
				Loader.safeMap(ui.getBoard(), Experiment.fileParentPath, 
						("Experiment_" + ui.getMineNumber() + "_mines" + "_on_" + ui.getMapX() + "x" + ui.getMapY()
								+ "_" + testLoop + "_" + testTimes), false);
			}
			
			
			if (Experiment.isRecord == false && Experiment.isFirstStep == true) {
//				Loader.safeMap(ui.getBoard(), Experiment.fileParentPath, ("Problem_" + Experiment.mineNumber + "_mines" + "_on_" + Experiment.mapX + "x" + Experiment.mapY), false);
				Experiment.isFirstStep = false;
			}
		}
		
//		if (Experiment.isDebug == true){
//			System.out.print("MaxcombineTiems = " + ui.getMaxCombine() + "\t");
//		}
		
		return ui.isWin();
	}
}
