package allSolve;
import game.Board;
import game.Referee_Board;

import java.util.LinkedList;

abstract class Node {
	private static Node root;
	private LoaderTree rTree;	
	private boolean isRecord;		// 是否儲存暫存檔
	final static double winRateInitial = Double.MAX_VALUE;	// 勝率的初始值 
	private Node parent;		// 父節點
	LinkedList<Node> children;	// 子節點
	final int level;	// 層級數 (非必要)
	LinkedList<int[]> set;	// 地圖集合，如果是True代表是地雷
	private double winRate;		// 勝率
	boolean[] showMap;		// 已打開的位置，如果是True就代表打開
	protected CheckDatabase cdatabase;		// 儲存地圖的最佳勝率
	
	// 控制儲存
	static int setWinRateTimes = 1;		// 設定機率的次數
	private static int setWinRateLimit = 10000000;	//	設定幾次機率，儲存一次00
	
	// root
	Node (LinkedList<int[]> set){
		Node.root = this;
		this.set = set;
		this.winRate = Node.winRateInitial;
		this.showMap = new boolean[AllSolve.mapSize];
		this.level = 0;
		this.cdatabase = new CheckDatabase(AllSolve.mapSize);
	}
	
	// root and record
	Node (LinkedList<int[]> set, LoaderTree rtree){
		this(set);
		Node.root.isRecord = true;
		Node.root.rTree = rtree;
	}
	
	Node (Node parent, LinkedList<int[]> set){
		this.parent = parent;
		this.set = set;
		this.winRate = Node.winRateInitial;
		this.showMap = new boolean[AllSolve.mapSize];
		this.level = this.parent.level + 1;
		
		for (int index = 0 ; index < AllSolve.mapSize ; index++){
			this.showMap[index] = this.parent.showMap[index];
		}
	}
	
	// 自訂解構子
	protected void finalize_By_Teddy(){
		if (this.children != null){
			for (Node n : this.children){
				n.parent = null;
				n.set = null;
			}
			this.children.clear();
		}
	}
	
	protected static Node getRoot(){
		return Node.root;
	}
	
	protected boolean isRecord(int level){		// 是否可以在第level層使用存檔
		if (AllSolve.isUseMethod_saveReg == true && this.isRecord() == true){
			return Node.root.rTree.isExistWinRate(level);
		}else {
			return false;
		}
	}
	
	private boolean isRecord(){		// 是否可以使用存檔
//		System.out.println("Node.isRecord = " + Node.isRecord);
		if (Node.root.isRecord == true){
			if (Node.root.rTree.isExistWinRate() == false){
//				System.out.println("讀檔完畢 in isRecord()");
				Node.root.isRecord = false;
			}
		}
		return Node.root.isRecord;
	}
	
	protected double getWinRateFromRecord(int level){	// 取得第level層存檔的數值
//		System.out.println("取得勝率 in getWinRateFromRecord");
		if (Node.root.isRecord == false){
			System.out.println("error in Node.getWinRate");
		}
		return Node.root.rTree.getWinRateFromRecord(level);
	}
	
	protected void setNextIndexInRecord(int level){
		Node.root.rTree.setNextIndex(level);
	}
	
	protected boolean isRoot(){
		return (this == Node.root);
	}
	
	// 改變勝率
	protected void setWinRate(double newWinRate){
		double error = 0.01;	// 容許錯差
		if (this.winRate == Node.winRateInitial && (newWinRate <= (1+error) && newWinRate >= 0)){
			this.winRate = newWinRate;
			
			if (AllSolve.isUseMethod_saveReg == true){
				if (this.isRecord() == false){
					if (Node.setWinRateTimes == Node.setWinRateLimit){
						Node.safe();
						//					System.out.println("safe file");
						Node.setWinRateTimes = 1;
					}else {
						//					System.out.println("safe = " + Node.setWinRateTimes);
						Node.setWinRateTimes++;
					}
				}else {
					//				System.out.println("set win rate from file and win rate is " + this.winRate + ", this level is " + this.level);
				}
			}
			
			// 只要印出第一層的選擇點即可
			if (this instanceof ChoiceNode && this.level == 1){
				System.out.println("This's a ChoiceNode of level " + this.level + " !");
				this.showMap(this.set.getFirst(), this.showMap);
				System.out.println("winRate = " + this.winRate);
				System.out.println();
			}
			
			// 只要印出選擇點即可
//			if (this instanceof ChoiceNode){
//				System.out.println("This's a ChoiceNode of level " + this.level + " !");
//				this.showMap(this.set.getFirst(), this.showMap);
//				System.out.println("set = " + this.set.size());
//				System.out.println("winRate = " + this.winRate);
//				System.out.println();
//			}
			
			// 印出每一個點
//			if (this instanceof RealNode){
//				System.out.println("This's a RealNode of level " + this.level + " !");
//			}else if (this instanceof ChoiceNode){
//				System.out.println("This's a ChoiceNode of level " + this.level + " !");
//			}
//			this.showMap(this.set.getFirst(), this.showMap);
//			System.out.println("winRate = " + this.winRate);
//			System.out.println();
		}else {
			System.out.println("error in Node.setWinRate");
			if (this instanceof RealNode){
				System.out.println("This's a RealNode of level " + this.level + " !");
			}else if (this instanceof ChoiceNode){
				System.out.println("This's a ChoiceNode of level " + this.level + " !");
			}
			this.showMap(this.set.getFirst(), this.showMap);
			System.out.println("winRate = " + this.winRate);
		}
	}
	
	// 取得勝率
	double getWinRate(){
		return this.winRate;
	}
	
	protected Node getParent(){
		return this.parent;
	}
	
	// 盤面是否已經全部解完 
	protected boolean isGameOver(){
		boolean isOver = true;
		for (boolean s : this.showMap){
			isOver &= s;
			if (isOver == false) break;
		}
		return isOver;
	}
	
	// 地圖的部分比較
	protected boolean isSomeMap(int[] map1, int[] map2, int... comparePoint){
		boolean isSome = true;
		for (int c : comparePoint){
			isSome &= (map1[c] == map2[c]);
			if (isSome == false) break;
		}
		return isSome;
	}
	
	private static void safe(){
//		System.out.println("This is Node.safe");
//		System.out.println("size = " + Node.root.children.size());
//		LinkedList<LinkedList<Double>> winRateRecord = new LinkedList<LinkedList<Double>>();
		LoaderTree winRateRecord = new LoaderTree();
		winRateRecord = Node.safe(Node.root, winRateRecord);
		winRateRecord.safe();
//		winRateRecord.show();
//		if (Node.root.children.isEmpty() == false){
//			System.out.println("safe start--------------");
//			for (LinkedList<Double> link : winRateRecord){
//				for (double d : link){
//					System.out.print(d + "\t");
//				}
//				System.out.println();
//			}
//			System.out.println("----------------safe end");
//		}
	}
	
	private static LoaderTree safe(Node node, LoaderTree wr){
		if (node.getWinRate() != Node.winRateInitial){
			// 已經算出機率了
			wr.setWinRate(node.level, node.getWinRate());
//			if (node.level < wr.size()){
//				wr.get(node.level).addLast(node.getWinRate());
//			}else {
//				while(node.level >= wr.size()){
//					wr.addLast(new LinkedList<Double>());
//				}
//				wr.get(node.level).addLast(node.getWinRate());
//			}
		}else {
			if (node.children != null){
				for (Node n : node.children){
					wr = Node.safe(n, wr);
				}
			}
		}
		return wr;
	}
	
	void showMap(int[] map, boolean[] showMap){
		for (int y = 0 ; y < AllSolve.mapY ; y++){
			for (int x = 0 ; x < AllSolve.mapX ; x++){
				if (showMap[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] == false){
					System.out.print("#" + "\t");
				}else if (map[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] == Board.mine){
					System.out.print("@" + "\t");
				}else {
					System.out.print(map[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] + "\t");
				}
			}
			System.out.println();
		}
	}
	
	abstract void run();
	// 產生子節點
	abstract void createChildren();
	// 依照不同的狀況設定勝率
	abstract void setWinRate();
	
}
