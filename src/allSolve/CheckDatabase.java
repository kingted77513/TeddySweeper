package allSolve;
import java.util.LinkedList;

class CheckDatabase {
	final private static double max_winRate = 1;	// 最大勝率
	final private static double min_winRate = 0;	// 最低勝率
	final private static int deepest_Level = 5;
	LinkedList<CheckLink> checklink;
	private double regWinRate;
	private double nodeNumber;
	
	CheckDatabase(int size){
		this.checklink = new LinkedList<CheckLink>();
		for (int i = 0 ; i < (size+1) ; i++){
			this.checklink.addLast(new CheckLink());
		}
		this.nodeNumber = 0;
	}
	
	// 計算已經打開的格子數
	private int countOpenGrid(boolean[] showMap){
		int open = 0;
		for (int index = 0 ; index < showMap.length ; index++){
			if (showMap[index] == true) open++;
		}
		return open;
	}
	
	boolean isSomeMap(int[] map, boolean[] showMap){
		int open = this.countOpenGrid(showMap);
		if (this.checklink.get(open).size() == 0){
			return false;
		}else if(this.checklink.get(open).isSomeMap(map, showMap) == true){
			this.regWinRate = this.checklink.get(open).getWinRate();
			return true;
		}else {
			return false;
		}
	}
	
	void add(int[] map, boolean[] showMap, double winRate){
		int level = this.countOpenGrid(showMap);
		if (level < CheckDatabase.deepest_Level 
				&& winRate > CheckDatabase.min_winRate
				&& winRate < CheckDatabase.max_winRate){		// 太深的層數不要存
			if (this.checklink.get(level).add(map, showMap, winRate) == true){
//				this.nodeNumber++;
//				System.out.println("node = " + this.nodeNumber);
//				System.out.println("level " + level + " node link size = " + this.checklink.get(level).size());
			}
		}
		
//		System.out.println("level = " + this.countOpenGrid(showMap) 
//				+ "\tsize = " + this.checklink.get(this.countOpenGrid(showMap)).size());
	}
	
	double getNodeNumber(){
		return this.nodeNumber;
	}
	
	double getWinRate(){
		return this.regWinRate;
	}
	
	private class CheckLink{
		private LinkedList<CheckNode> checknodelink;
		private double regWinRate;
		private CheckLink(){
			this.checknodelink = new LinkedList<CheckNode>(); 
		}
		
		private int size(){
			return this.checknodelink.size();
		}
		
		// 檢查是否是已存在的地圖
		private boolean isSomeMap(int[] map, boolean[] showMap){
			for (int index = 0 ; index < this.checknodelink.size() ; index++){
				if (this.checknodelink.get(index).equal(map, showMap) == true){
					this.regWinRate = this.checknodelink.get(index).getWinRate(); 
					return true;
				}
			}
			return false;
		}
		
		private double getWinRate(){
			return this.regWinRate;
		}
		
		private boolean add(int[] map, boolean[] showMap, double winRate){
			if (this.isSomeMap(map, showMap) == false){
				this.checknodelink.addLast(new CheckNode(map, showMap, winRate));
				return true;
			}else {
				return false;
			}
		}
	}
	
	private class CheckNode{
		private int[] map;
		private boolean[] showMap;
		private double winRate;
		
		private CheckNode(int[] map, boolean[] showMap, double winRate){
			this.map = map;
			this.showMap = showMap;
			this.winRate = winRate;
		}
		
		private boolean equal(int[] map, boolean[] showMap){
			return (this.map == map && this.isSomeShowMap(map, showMap));
		}
		
		private boolean isSomeShowMap(int[] map, boolean[] showMap){
			for (int index = 0 ; index < showMap.length ; index++){
				if (this.showMap[index] == false){	// close
					if (showMap[index] == true) return false;
				}else {								// open
					if (this.map[index] != map[index]) return false;
				}
			}
			return true;
		}
		
		private double getWinRate(){
			return this.winRate;
		}
	}
}
