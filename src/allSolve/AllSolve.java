package allSolve;

import game.Board;
import game.BoardCreater;
import game.Referee;
import game.Referee_Board;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import ai.TeddyMethodTool;

public class AllSolve {
	static boolean isUseMethod_saveReg = false;		// 是否儲存暫存檔
	static boolean isUseMethod_workArea = true;		// 是否設定計算最少的位置
	static boolean outputBestWinRate = false;		// 是否輸出最佳勝率位置的檔案
	static boolean isUseMethod_MapDatabase = true;			// 是否儲存地圖的最佳勝率
	
	static int mineNumber;
	static int mapX;
	static int mapY;
	public static int mapSize;
	static String rule;	 
	
	public static void main(String[] args){
		System.out.println("This is AllSolve");
		try {
			AllSolve.mineNumber = Integer.parseInt(args[0]);
			AllSolve.mapX = Integer.parseInt(args[1]);
			AllSolve.mapY = Integer.parseInt(args[2]);
			
			switch (args[3]){
			case "r1":
				AllSolve.rule = Referee.rule_1;
				break;
			case "r2":
				AllSolve.rule = Referee.rule_2;
				break;
			case "r3":
				AllSolve.rule = Referee.rule_3;
				break;
			default:
				AllSolve.rule = Referee.rule_1;
				break;
			}
		}catch (ArrayIndexOutOfBoundsException aio){
			AllSolve.mineNumber = 4;
			AllSolve.mapX = 4;
			AllSolve.mapY = 4;
			AllSolve.rule = Referee.rule_2;
		}
		
		Date timeDate = Calendar.getInstance().getTime();	//取得系統時間
	    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/M/d a hh:mm");
	    System.out.println("測試時間：" + sdf1.format(timeDate));
		
		AllSolve.mapSize = AllSolve.mapX * AllSolve.mapY;
		
		System.out.println("m = " + AllSolve.mineNumber + " , x = " + AllSolve.mapX + " , y = " + AllSolve.mapY);
		System.out.println("rule = " + AllSolve.rule);
		System.out.println("可能所有盤面總數為：" + new TeddyMethodTool().conbineNumber(mapSize, mineNumber));
		
		BoardCreater creater = new BoardCreater(AllSolve.mapY, AllSolve.mapX, AllSolve.mineNumber);
		LinkedList<int[]> maplink = creater.get_allMap();
//		creater.get_allMap_test();
		
		System.out.println("已製造的所有盤面總數為：" + maplink.size());
		RealNode root;
		
		LoaderTree tree = new LoaderTree();
		
		if (AllSolve.isUseMethod_saveReg == true){
			try {
				tree.load();
//				tree.show();
				root = new RealNode(maplink, tree);
			}catch (FileNotFoundException efe){
				root = new RealNode(maplink);
			}
		}else {
			root = new RealNode(maplink);
		}
		
		root.run();
		
//		File file = new File(System.getProperty("user.dir"), "allSolveSave_15.txt");
//		if (file.isFile() == true){
//			System.out.println("file is exist");
//		}else {
//			System.out.println("file don't exist");
//		}
//		tree.load(file.getPath());
//		tree.show();
//		RealNode root = new RealNode(maplink, tree);
		
		System.out.println("AllSove is end and the ideal win rate is " + root.getWinRate());
//		System.out.println("times = " + Node.setWinRateTimes);
//		System.out.println("Board size is " + "(" + AllSolve.mineNumber + "m on " + AllSolve.mapX + "x" + AllSolve.mapY + ")");
//		System.out.println("rule = " + AllSolve.rule);
//		System.out.println("AllSove is end and the win rate of this board size (" +
//				+ AllSolve.mineNumber + "m on " + AllSolve.mapX + "x" + AllSolve.mapY + ") is " + root.getWinRate());
//		
//		int printSize = 0;
//		int[] map;
//		
//		while ((map=maplink.getNextData()) != null){
//			AllSolve.showMap(map);
//			System.out.println();
//			printSize++;
//		}
//		
//		System.out.println("printSize = " + printSize);
		
//		System.out.println("work");
//		ChoiceNode.workArray();
	}
	
	// 印出地圖
	public static void showMap(int[] map){
		for (int y = 0 ; y < AllSolve.mapY ; y++){
			for (int x = 0 ; x < AllSolve.mapX ; x++){
				if (map[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] == Board.mine){
					System.out.print("@" + "\t");
				}else {
					System.out.print(map[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] + "\t");
				}
			}
			System.out.println();
		}
	}

	
}
