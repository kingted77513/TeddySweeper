package ai;

import game.Board;
import game.Player_Board;

public abstract class AI {
	String order;
	Player_Board map;
	boolean isFirstTime;
	
	protected AI (Player_Board playerMap){
		this.map = playerMap;
		this.isFirstTime = true;
	}
	
	abstract protected String answer();		//回答
	
	abstract protected boolean isAnswer();	//是否有答案
	
	abstract protected double[][] getHintMap();
	
	protected boolean isExistInitial(int y, int x){  //周圍八格是否有初始點
		boolean isExist = false;
		for (int j = y-1 ; j <= y+1 ; j++){
			for (int i = x-1 ; i <= x+1 ; i++){
				if (i != x || j != y){		//中間的點不用理
					if (this.map.isInBoard(j, i) == true && this.map.getState(j, i) == Board.initial){
						isExist = true;
						break;
					}
				}
			}
			if (isExist == true) break;
		}
		return isExist;
	}

	protected static void showMap_data(Player_Board pmap, int[][] data_map){
		for (int y = pmap.getMapY() - 1 ; y >= 0 ; y--){
			for (int x = 0 ; x < pmap.getMapX() ; x++){
				System.out.print(data_map[y][x] + "\t");
			}
			System.out.println("");
		}
		System.out.println("");
	}
//	
//	protected static void showMap_posible(int mapY, int mapX, double[][] data_map){
//		for (int y = mapY - 1 ; y >= 0 ; y--){
//			for (int x = 0 ; x < mapX ; x++){
//				System.out.printf("%.2f", data_map[y][x]);
//				System.out.print("\t");
//			}
//		}
//	}
	
	abstract public void showMap(Board showmap, String kind);		//印出地圖
	
	abstract protected String getStrategy(String strategy); //回傳策略字串
}