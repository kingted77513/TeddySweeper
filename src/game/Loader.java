package game;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class Loader {
	public static String ioFile = ".txt";
	static int mapX;
	static int mapY;
	static int mineNumber;
	static String rule;
	static int remainMineNumber;
	static int remainInitialGrid;
	
	public static Referee_Board loadMap(String filePath){		//讀取地圖
		Referee_Board inputMap = null;
		try{
			//讀檔
	        InputStreamReader isr=new InputStreamReader(new FileInputStream(new File(filePath)));
	        BufferedReader  br = new BufferedReader (isr);
	        String str = "";
	        
	        //讀檔讀到完為止
	        while ((str = br.readLine()) != null){
	        	inputMap = Loader.mapInputHandle(inputMap, str, br);
	        }
	        
	        br.close();
		}catch (Exception e){
	          e.printStackTrace();
	    }
		
		return inputMap;
	}
	
	private static Referee_Board mapInputHandle(Referee_Board inputMap2, String mes, BufferedReader br){
		Referee_Board inputMap = null;
		String[] commands;
		commands = mes.split(" ");	//以" "分離字串
		try {
			if (commands[0].equals("start")){
//				System.out.println("Upload beginning");
			}else if(commands[0].equals("MapX=")){
				Loader.mapX = Integer.parseInt(commands[1]);
			}else if(commands[0].equals("MapY=")){
				Loader.mapY = Integer.parseInt(commands[1]);
			}else if(commands[0].equals("mineNumber=")){
				Loader.mineNumber = Short.parseShort(commands[1]);
			}
//			else if(commands[0].equals("rule=")){
//				Loader.rule = commands[1];
//			}
//			else if(commands[0].equals("remainMineNumber=")){
//				Loader.remainMineNumber = Integer.parseInt(commands[1]);
//			}else if(commands[0].equals("remainInitialGrid=")){
//				Loader.remainInitialGrid = Integer.parseInt(commands[1]);
//			}
			
			else if(commands[0].equals("inState_Map")){
				String instateMes = "";
				if(inputMap == null){
					inputMap = new Referee_Board(Loader.mapY, Loader.mapX, Loader.mineNumber);
				}
				
				for (int y = inputMap.getMapY() - 1 ; y >=  0 ; y--){
					try{
						instateMes = br.readLine();
						commands = instateMes.split(" ");	//以" "分離字串
						for (int x = inputMap.getMapX() - 1; x >= 0 ; x--){
							inputMap.setInState(y, x, Short.parseShort(commands[x]));
						}
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}else if(commands[0].equals("outState_Map")){
				String outstateMes = "";
				short state;
				
				if (inputMap2 != null){
					inputMap = (Referee_Board)inputMap2.clone();
				}else {
					System.out.println("inputMap2 is null");
				}
				
				Loader.remainInitialGrid = Loader.mapX * Loader.mapY;
				Loader.remainMineNumber = Loader.mineNumber;
				
				for (int y = inputMap.getMapY() - 1 ; y >=  0 ; y--){
					try{
						outstateMes = br.readLine();
						commands = outstateMes.split(" ");	//以" "分離字串
						for (int x = inputMap.getMapX() - 1; x >= 0 ; x--){	
							state = Short.parseShort(commands[x]);
							inputMap.setState(y, x, state);
							if (inputMap.isVisit(y, x) == true){
								Loader.remainInitialGrid--;
							}
							if (state == Board.flag){
								Loader.remainMineNumber--;
							}
						}
					}catch (Exception e){
						e.printStackTrace();
					}
					inputMap.setRemainNumber(Loader.remainInitialGrid, Loader.remainMineNumber);
				}
			}else {
				System.out.println("unknown command: " + '\"' + mes + '\"');
			}
			
			return inputMap;
		}catch(Exception e){
			e.printStackTrace(System.out); //顯示詳細錯誤原因
			return inputMap;
		}
	}
	
	public static void safeMap(Referee_Board outputMap, String fileName){	//程式運行中儲存地圖專用
		Loader.safeMap(outputMap, System.getProperty("user.dir"), fileName, false);
	}
	
	public static void safeMap(Referee_Board outputMap, String filePath, String fileName, boolean isLose){		//儲存地圖，決定名稱時用，以免覆蓋檔案
		File fileTest = new File(filePath, fileName + "_(1)" + Loader.ioFile);
		int childPathNumber = 0;
		String sfileName;
		while(fileTest.isFile() == true){
			childPathNumber++;
			sfileName = fileName;
			sfileName += "_(" + childPathNumber + ")" + Loader.ioFile;
			fileTest = new File(filePath, sfileName);
		}
		Loader.safeMap(outputMap, fileTest.getPath(), isLose);
		System.out.println("檔案已儲存在：" + fileTest.getPath());
	}
	
	public static void safeMap(Referee_Board outputMap, String filePath, boolean isLose){		//實際儲存地圖用
		try{
	        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, false)));
	        
	        bw.write("start record");
	        bw.newLine();	//換行
	        //輸出MapX和MapY
	        bw.write("MapX=" + ' ' + outputMap.getMapX());
	        bw.newLine();	//換行
	        bw.write("MapY=" + ' ' + outputMap.getMapY());
	        bw.newLine();	//換行
	        bw.write("mineNumber=" + ' ' + outputMap.getMineNumber());
	        bw.newLine();	//換行
//	        bw.write("rule=" + ' ' + outputMap.getRule());
//	        bw.newLine();	//換行
//	        bw.write("remainMineNumber=" + ' ' + outputMap.getremainMineNumber());
//	        bw.newLine();	//換行
//	        if (isLose == false){
//	        	bw.write("remainInitialGrid=" + ' ' + outputMap.getremainInitialGrid());
//	        }else {
//	        	bw.write("remainInitialGrid=" + ' ' + (outputMap.getremainInitialGrid() + 1));
//	        }
//	        bw.newLine();	//換行
	        
	      //輸出inState的地圖資訊
	        bw.write("inState_Map");
	        bw.newLine();	//換行
	        for (int y = outputMap.getMapY() - 1 ; y >= 0  ; y--){
	        	for (int x = 0 ; x < outputMap.getMapX() ; x++){
	        		bw.write(outputMap.getInState(y, x) + " ");
	        	}
	        	bw.newLine();	//換行
	        }
	      
	    //輸出outState的地圖資訊
	        bw.write("outState_Map");
	        bw.newLine();	//換行
	        for (int y = outputMap.getMapY() - 1 ; y >= 0  ; y--){
	        	for (int x = 0 ; x < outputMap.getMapX() ; x++){
	        		if (isLose == true && outputMap.getState(y, x) == Board.mine){
	        			bw.write(Board.initial + " ");
	        		}else {
	        			bw.write(outputMap.getState(y, x) + " ");
	        		}
	        	}
	        	if (y > 0) bw.newLine();	//換行
	        }
	      bw.close();
	      }
	      catch (Exception e){
	          e.printStackTrace();
	      }
	}

	// 儲存實驗結果
	public static void outputExperimentResult(UI ui, int experimentTimes, int testTimes, int wintimes, int losetimes, int guesstimes, 
			double maxTime, double countTotalTime, BigDecimal maxCombineTimes, BigDecimal sum_combineTimes, 
			BigDecimal sum_usecombineTimes, 
			String fileParentPath, String strategy, String notes) {
		
		String childPath = ui.getMineNumber() + "_mines_on_" + ui.getMapX() + "x" + ui.getMapY() 
				+ "_in_" + ui.getRule() + "_run_" + testTimes + "_times" + Loader.ioFile;
		
		File fileTest = new File(fileParentPath, childPath);
		
		try{
			// FileOutputStream可以設定資料是否要疊加上去
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileTest, true)));
			if (experimentTimes == 1){
				Date date = Calendar.getInstance().getTime();	//取得系統時間
			    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/M/d a hh:mm");
				bw.write("exam time=" + sdf1.format(date) + "\t");
//				bw.newLine();	//換行
			}
			bw.write("size=\t" + ui.getMineNumber() + " mines" + " on " + ui.getMapX() + "x" + ui.getMapY() + "\t");
			bw.write("run_loop=\t" + experimentTimes + "\t");
			bw.write("run_times=\t" + testTimes + "\t");
			
			bw.write("win_rate=\t" 
					+ BigDecimal.valueOf(wintimes).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(testTimes), 3, BigDecimal.ROUND_HALF_UP).toString() +
					" +- " + BigDecimal.valueOf(100*Math.sqrt(((double)wintimes/testTimes)*(1-((double)wintimes/testTimes))/((double)testTimes))*2).divide(BigDecimal.valueOf(1), 3, BigDecimal.ROUND_HALF_UP).toString() + 
					"%\t");
			bw.write("win_times=\t" + wintimes + "\t");
	        bw.write("lose_times=\t" + losetimes + "\t");
	        bw.write("guess_times=\t" + guesstimes + "\t");
	        bw.write("avg_time=\t" 
	        		+ BigDecimal.valueOf(countTotalTime).divide(BigDecimal.valueOf(testTimes), 3, BigDecimal.ROUND_HALF_UP).toString() + 
	        		"s\t");
	        bw.write("max_time=\t" + maxTime + "s\t");
	        bw.write("total_time=\t" + countTotalTime + "s\t");
	        
	        bw.write("avg_combine_times=\t" 
	        		+ sum_combineTimes.divide(BigDecimal.valueOf(testTimes), 3, BigDecimal.ROUND_HALF_UP).toString() 
	        		+ "\t");
	        bw.write("max_combine_times=\t" 
	        		+ maxCombineTimes.toBigInteger().toString() + "\t");
	        bw.write("total_combine_times=\t" + sum_combineTimes.toBigInteger().toString() + "\t");
	        bw.write("total_use_combine_times=\t" + sum_usecombineTimes.toBigInteger().toString() + "\t");
	        bw.write("rule=\t" + ui.getRule() + "\t");
	        bw.write("strategy=\t" + strategy + "\t");
	        bw.write("notes=\t" + notes);
	        if (testTimes != experimentTimes) bw.newLine();	//換行
	        bw.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
}
