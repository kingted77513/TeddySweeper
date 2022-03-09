package ai;

import game.Board;
import game.Player_Board;
import java.util.LinkedList;

import ai.AI;

public final class SinglePoint extends AI{
	LinkedList<Player_Board.Player_Grid> allDigPoint = new LinkedList<Player_Board.Player_Grid>();		//確定周圍都不是地雷的點
	LinkedList<Player_Board.Player_Grid> allFlagPoint = new LinkedList<Player_Board.Player_Grid>();		//確定周圍都是地雷的點
	
	public SinglePoint(Player_Board playerMap){
		super(playerMap);
	}
	
	@Override
	public String answer(){
		this.select();
		return this.order;
	}
	
	private void select(){		//選點
		if (this.allFlagPoint.size() > 0){
			while (this.allFlagPoint.size() > 0){
				if (this.isExistInitial(this.allFlagPoint.getLast().getY(), this.allFlagPoint.getLast().getX()) == true){
					this.order = "allflag " + this.allFlagPoint.getLast().getCoordinate();
					break;
				}else {
					this.allFlagPoint.removeLast();
					if (this.allFlagPoint.size() == 0) this.select();
				}
			}
		}else if (this.allDigPoint.size() > 0){
			while (this.allDigPoint.size() > 0){
				if (this.isExistInitial(this.allDigPoint.getLast().getY(), this.allDigPoint.getLast().getX()) == true){
					this.order = "alldig " + this.allDigPoint.getLast().getCoordinate();
					break;
				}else {
					this.allDigPoint.removeLast();
					if (this.allDigPoint.size() == 0) this.select();
				}
			}
		}
		
		if (this.allFlagPoint.size() == 0 && this.allDigPoint.size() == 0){
			this.search();
			if (this.allFlagPoint.size() > 0 || this.allDigPoint.size() > 0){
				this.select();
			}else {
				this.order = "no_answer";
			}
		}
	}
	
	private void search(){				//搜尋全挖或全插旗點
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				if (this.map.isInformationGrid(y, x) == true){
					if (this.map.getState(y, x) == (this.map.getBesireInitialNumber(y, x) + this.map.getBesireFlagNumber(y, x))){	//數字 = 初始 + 已插棋 且 未打開格子 > 0 -> 全插棋
						this.allFlagPoint.addLast(this.map.getGrid(y, x));
					}else if (this.map.getState(y, x) == this.map.getBesireFlagNumber(y, x)) {	//數字 = 已插棋 且 未打開格子 > 0 -> 全挖
						this.allDigPoint.addLast(this.map.getGrid(y, x));
					}
				}
			}
		}
	}

	@Override
	public boolean isAnswer() {
//		this.select();
		return (this.allDigPoint.size() > 0 || this.allFlagPoint.size() > 0) ? true : false;
	}
	
	@Override
	public double[][] getHintMap(){
		double[][] hint = new double[this.map.getMapY()][this.map.getMapX()];
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				hint[y][x] = Board.initial;
			}
		}
		
		if (this.allDigPoint.size() > 0){
			for (Player_Board.Player_Grid g : this.allDigPoint){
				for (int j = g.getY()-1 ; j <= g.getY()+1 ; j++){
					for (int i = g.getX()-1 ; i <= g.getX()+1 ; i++){
						if (i != g.getX() || j != g.getY()){		//中間的點不用理
							if (this.map.isInBoard(j, i) == true && this.map.isInitial(j, i) == true){
								hint[j][i] = 0;
							}
						}
					}
				}
			}
		}
		
		if (this.allFlagPoint.size() > 0){
			for (Player_Board.Player_Grid g : this.allFlagPoint){
				for (int j = g.getY()-1 ; j <= g.getY()+1 ; j++){
					for (int i = g.getX()-1 ; i <= g.getX()+1 ; i++){
						if (i != g.getX() || j != g.getY()){		//中間的點不用理
							if (this.map.isInBoard(j, i) == true && this.map.isInitial(j, i) == true){
								hint[j][i] = 1;
							}
						}
					}
				}
			}
		}
		return hint;
	}
	
	@Override
	public void showMap(Board showmap, String kind) {
		Player_Board pboard = (Player_Board) showmap;
		for (int y = map.getMapY() - 1 ; y >= 0 ; y--){
			for (int x = 0 ; x < map.getMapX() ; x++){
				if (kind.equals("state")){
					if (pboard.getState(y, x) == Board.mine){
						System.out.print("@" + "\t");
					}else if (pboard.getState(y, x) == Board.flag){
						System.out.print("P" + "\t");
					}
					else if (pboard.getState(y, x) == Board.initial){
						System.out.print("#" + "\t");
					}else {
						System.out.print(pboard.getState(y, x)+ "\t");
					}
				}
			}
			System.out.println("");
		}
	}

	@Override
	public String getStrategy(String strategy) {
//		System.out.println("Single Point = " + true);
		strategy += "| Single Point |";
		return strategy;
	}
}
