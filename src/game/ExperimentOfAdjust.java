/*
 * 為了配合基因演算法
 */
package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

public class ExperimentOfAdjust {
	private static boolean isDebug = false;
	private static boolean isinputSet = true;		//是否引入參數
	public static void main(String[] args){
		if (ExperimentOfAdjust.isDebug == true){
			System.out.println("This is ExperimentOfAdjust");
		}
		int mineNumber = 2, mapX = 4, mapY = 1;
		String rule = Referee.rule_2;
		int testTimes = 1000, winTimes;
		LinkedList<String> parameterSet = new LinkedList<String>();
		
//		if (ExperimentOfAdjust.isinputSet == true){
//			mineNumber = Integer.parseInt(args[0]);
//			mapX = Integer.parseInt(args[1]);
//			mapY = Integer.parseInt(args[2]);
//		}
		
		// 讀取當前路徑的input.txt的檔案
		try{
			File input = new File(System.getProperty("user.dir"), "input" + Loader.ioFile);
			BufferedReader br = new BufferedReader (new InputStreamReader(new FileInputStream(input)));
			String str;
			int index = 0;
			//讀檔讀到完為止
			while ((str = br.readLine()) != null){
				if (index == 0 && ExperimentOfAdjust.isinputSet == true){
					String[] testset = new String[3];
					testset = str.split("\t");
					mineNumber = Integer.parseInt(testset[0]);
					mapX = Integer.parseInt(testset[1]);
					mapY = Integer.parseInt(testset[2]);
					testTimes = Integer.parseInt(testset[3]);
					if (ExperimentOfAdjust.isDebug == true){
						System.out.println("mineNumber = " + mineNumber);
						System.out.println("mapX = " + mapX);
						System.out.println("mapY = " + mapY);
						System.out.println("mineNumber = " + mineNumber);
					}
				}else if (index % 2 == 0){
					if (ExperimentOfAdjust.isDebug == true){
						System.out.println(str);
					}
					parameterSet.addLast(str);
				}
				index++;
			}
			br.close();
		}catch (Exception e){
	          e.printStackTrace();
	    }
		
		// 計算勝率
		UI ui = new UI();
		double[] valueSet = new double[parameterSet.size()];
//		double[] parameter;
		String command;
		String[] commands;
		
		for (int index = 0 ; index < parameterSet.size() ; index++){
			if (ExperimentOfAdjust.isDebug == true){
				System.out.println("this is " + (index+1) + " times.");
			}
			command = parameterSet.get(index);
			commands = command.split("\t");
			winTimes = 0;
			
//			for (int p = 0 ; p < 5 ; p++){
//				parameter = new double[5];
//				parameter[p] = Double.parseDouble(commands[p]);
//				if (ExperimentOfAdjust.isDebug == true){
//					System.out.println("parameter = " + parameter[p]);
//				}
//			}
			
			for (int tt = 0 ; tt < testTimes ; tt++){
				ui.input(UI.order.reset + " " + mapX + " " + mapY + " " + mineNumber + " " + rule + " " + "1" + " " + tt);
				ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod + " true " + 
						commands[0] + " " + commands[1] + " " + commands[2] + " " + commands[3] + " " + commands[4]);
				ui.input(UI.order.setAI + " " + UI.usedAI.TeddyMethod_Guess + " true");
				try {
					if(ExperimentOfAdjust.run(ui, 1, tt) == true){
						winTimes++;
					}
				}catch(final OutOfMemoryError ome){
					System.out.println("出現記憶體不足錯誤，此局放棄！！");
					System.gc();	//釋放記憶體
					ome.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			valueSet[index] = (double)winTimes / testTimes;
			if (ExperimentOfAdjust.isDebug == true){
				System.out.println("win rate = " + valueSet[index]);
//				for (int p = 0 ; p < 5 ; p++){
//					valueSet[index] += parameter[p];
//				}
			}
		}
		
		
		// 將勝率寫入output.txt中
		try{
			File output = new File(System.getProperty("user.dir"), "output" + Loader.ioFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, false)));
			for (int index = 0 ; index < valueSet.length ; index++){
				bw.write(String.valueOf(valueSet[index]));
				if (index < valueSet.length - 1){
					bw.newLine();	//換行
				}
			}
			bw.close();
		}catch (Exception e){
	          e.printStackTrace();
	    }
	}
	
	static boolean run(UI ui, int testLoop, int testTimes){		//測試的函數
		while (ui.isGameOver() == false){
			ui.input(UI.order.AI_Run_one_Step.toString());
		}
		return ui.isWin();
	}
}
