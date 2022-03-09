package allSolve;

import game.Board;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

class LoaderTree {
	final private static String fileParentPath = System.getProperty("user.dir");		//取得當前路徑
	final private static String fileName = "allSolveSave_" + AllSolve.mineNumber + "_on_" + 
			AllSolve.mapX + "x" + AllSolve.mapY + "_" +
			AllSolve.rule + "_";
	final private static String fileMsg = "This file is Successful!";
	
	private LinkedList<LoaderLink> treelink;
	private int recordNumber;	// 記錄總數
	private boolean loadSuccess;
	private static double bestWinRate;
	
	LoaderTree(){
		this.treelink = new LinkedList<LoaderLink>();
		this.recordNumber = 0;
		this.loadSuccess = false;
	}
	
	// 輸出地圖的最大勝率
	static void safe(int[] map, boolean[] showMap, double bestWinRate){
		String fileName = "MapDatabase_" + AllSolve.mineNumber + "_on_" + 
				AllSolve.mapX + "x" + AllSolve.mapY + "_" +
				AllSolve.rule + ".txt";
		File file = new File(LoaderTree.fileParentPath, fileName);
		boolean isFileExist = file.exists();
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.getPath(), true)));
			
			if (isFileExist == true){
				bw.newLine();	//換行
			}
			
			for (int index = 0 ; index < map.length ; index++){
				if (showMap[index] == false){
					bw.write(Board.initial + "\t");
				}else if (map[index] == Board.mine){
					bw.write(Board.flag + "\t");
				}else {
					bw.write(map[index] + "\t");
				}
			}
			
			bw.write(String.valueOf(bestWinRate));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 輸出最大勝率的點的位置
	static void safe(int[] map, boolean[] showMap, LinkedList<Integer> bestWinRateLink, double bestWinRate){
		/*
		 * 存檔格式為
		 * level	map	winRate
		 */
		String fileName = "bestWinRate_" + AllSolve.mineNumber + "_on_" + 
				AllSolve.mapX + "x" + AllSolve.mapY + "_" +
				AllSolve.rule + ".txt";
		File file = new File(LoaderTree.fileParentPath, fileName);
		boolean isFileExist = file.exists();
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.getPath(), true)));
			
			if (isFileExist == true){
				bw.newLine();	//換行
			}
			
			for (int index = 0 ; index < map.length ; index++){
				if (showMap[index] == false){
//					bw.write("#" + "\t");
					bw.write(Board.initial + "\t");
				}else if (map[index] == Board.mine){
//					bw.write("P" + "\t");
					bw.write(Board.flag + "\t");
				}else {
					bw.write(map[index] + "\t");
				}
			}
			
			for (int i : bestWinRateLink){
				bw.write(i + "\t");
			}
			
			bw.write(String.valueOf(bestWinRate));
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void safe(){
		int fileNumber = 0;
		File file;
		String fileName;
		do {
			fileNumber++;
			fileName = LoaderTree.fileName + fileNumber + ".txt";
			file = new File(LoaderTree.fileParentPath, fileName);
		}while(file.isFile() == true);
		
		this.safe(file.getPath());
	}
	
	private void safe(String filePath){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePath, true)));
			boolean isFirst = true;
			for (LoaderLink nodelink : this.treelink){
				if (isFirst == true){
					isFirst = false;
				}else {
					bw.newLine();	//換行
				}
				for (double d : nodelink.link){
					bw.write(d + "\t");
				}
			}
			bw.newLine();	//換行
			bw.write(LoaderTree.fileMsg);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	boolean isExistSaveFile(){
//		return (new File(LoaderTree.fileParentPath, LoaderTree.fileName + "1.txt").exists());
//	}
	
	void load() throws FileNotFoundException{
		int fileNumber = 0;
		File file;
		String fileName;
		do {
			fileNumber++;
			fileName = LoaderTree.fileName + fileNumber + ".txt";
			file = new File(LoaderTree.fileParentPath, fileName);
		}while(file.exists() == true);
		
		do {
			fileNumber--;	// 索引要先減一
			fileName = LoaderTree.fileName + fileNumber + ".txt";
			file = new File(LoaderTree.fileParentPath, fileName);
			System.out.println("讀取記錄：" + fileName);
			if (file.exists() == false){
				System.out.println("不存在記錄：" + fileName);
				throw new FileNotFoundException();	// 找不到檔案，回傳例外！
			}
			this.load(file.getPath());
			// 讀取失敗，重新宣告變數
			if (this.loadSuccess == false){
				System.out.println("錯誤的記錄：" + fileName);
				this.treelink = new LinkedList<LoaderLink>();
				this.recordNumber = 0;
			}else {
				System.out.println("讀取成功的記錄：" + fileName);
			}
		}while(this.loadSuccess == false);
	}
	
	//TODO
	static boolean load(int[] map, boolean[] showMap){
		if (AllSolve.isUseMethod_MapDatabase == false) return false;
		
		boolean loadSuccess = false;
		LoaderTree.bestWinRate = 0;
		String fileName = "MapDatabase_" + AllSolve.mineNumber + "_on_" + 
				AllSolve.mapX + "x" + AllSolve.mapY + "_" +
				AllSolve.rule + ".txt";
		File file = new File(LoaderTree.fileParentPath, fileName);
		if (file.isFile() == true){
			try{
				//讀檔
		        BufferedReader br = new BufferedReader (new InputStreamReader
		        		(new FileInputStream(file)));
		        String str = "";
		        String[] strs;
		        double[] winRates;
		        //讀檔讀到完為止
		        while ((str = br.readLine()) != null){
		        	strs = str.split("\t");
		        	winRates = new double[strs.length];
		        	try {
		        		for (int index = 0 ; index < strs.length ; index++){
			        		winRates[index] = Double.parseDouble(strs[index]);
			        	}
		        		
		        		if (LoaderTree.mapCheck(winRates, map, showMap)){
		        			loadSuccess = true;
		        			LoaderTree.bestWinRate = winRates[map.length];
		        			break;
		        		}
		        	}catch (NumberFormatException nfe){
		        		// 空字串不用理
		        	}
		        }
		        
		        br.close();
			}catch (Exception e){
		          e.printStackTrace();
		    }
		}
		
		return loadSuccess;
	}
	
	// TODO
	private static boolean mapCheck(double[] readmap, int[] map, boolean[] showMap){
		for (int index = 0 ; index < map.length ; index++){
			if (showMap[index] == false){
				if (readmap[index] != Board.initial) return false;
			}else if (map[index] == Board.mine){
				if (readmap[index] != Board.flag) return false;
			}else {
				if (readmap[index] != map[index]) return false;
			}
		}
		return true;
	}
	
	static double getBestWinRate(){
		return LoaderTree.bestWinRate;
	}
	
	private void load(String filePath){
		try{
			//讀檔
	        BufferedReader br = new BufferedReader (new InputStreamReader
	        		(new FileInputStream(new File(filePath))));
	        String str = "";
	        String[] strs;
	        double[] winRates;
	        int level = 0;
	        //讀檔讀到完為止
	        while ((str = br.readLine()) != null){
	        	if (str.equals(LoaderTree.fileMsg) == true) {		// 讀到終止字串則直接跳出
	        		this.loadSuccess = true;
	        		break;
	        	}
	        	
	        	strs = str.split("\t");
	        	winRates = new double[strs.length];
	        	try {
	        		for (int index = 0 ; index < winRates.length ; index++){
		        		winRates[index] = Double.parseDouble(strs[index]);
		        	}
	        		this.setWinRate(level, winRates);
	        	}catch (NumberFormatException nfe){
	        		// 空字串不用理
	        	}
	        	level++;
	        }
	        
	        br.close();
		}catch (Exception e){
	          e.printStackTrace();
	    }
	}
	
	// 將機率儲存到指定層
	void setWinRate(int level, double... winRates){
		while(level >= this.treelink.size()){
			this.treelink.addLast(new LoaderLink());
		}
		
		for (double w : winRates){
			this.treelink.get(level).addLast(w);
			this.recordNumber++;
		}
	}
	
	// 是否存著勝率
	boolean isExistWinRate(){
		return (this.recordNumber > 0);
	}
	
	// 檢查level層還沒有數值
	boolean isExistWinRate(int level){
		return (level < this.treelink.size() && 
		this.treelink.get(level).isExistWinRate() == true);
	}
	
	// 取得level層的數值
	double getWinRateFromRecord(int level){
		return this.treelink.get(level).getWinRate();
	}
	
	// 設定下一個勝率的索引
	void setNextIndex(int level){
		this.recordNumber--;
		this.treelink.get(level).setNextIndex();
	}
	
	private int size(){
		return this.treelink.size();
	}
	
	private void show(){
		System.out.println("safe start--------------");
		for (LoaderLink nodelink : this.treelink){
			for (double d : nodelink.link){
				System.out.print(d + "\t");
			}
			System.out.println();
		}
		System.out.println("----------------safe end");
	}
	
	private class LoaderLink{
		private int index;
		private LinkedList<Double> link;
		
		private LoaderLink(){
			this.index = 0;
			this.link = new LinkedList<Double>();
		}
		
		private boolean isExistWinRate(){
			return (this.index < this.link.size());
		}
		
		private void addLast(double winRate) {
			this.link.addLast(winRate);
		}

		private double getWinRate(){
			return this.link.get(this.index);	//給值
		}
		
		private void setNextIndex(){
			this.index++;
		}
	}
}
