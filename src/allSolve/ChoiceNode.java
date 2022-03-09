package allSolve;

import game.Board;
import game.Referee;
import game.Referee_Board;

import java.util.LinkedList;

final class ChoiceNode extends Node{
	private int choiceSituation;	// 所選擇的位置(必要)
	ChoiceNode(Node parent, LinkedList<int[]> set, int... choice) {
		super(parent, set);
		this.choiceSituation = choice[0];
	}
	
	int getChoice(){
		return this.choiceSituation;
	}
	
	@Override
	void run(){
		this.createChildren();
		this.setWinRate();
	}
	
	@Override
	void createChildren() {
		this.children = new LinkedList<Node>();
		// 分組
		LinkedList<LinkedList<int[]>> sets = new LinkedList<LinkedList<int[]>>();
		for (int[] map : this.set){
			if (sets.isEmpty() == false){
				for (int index = 0 ; index < sets.size() ; index++){
					if (this.isSomeMap(map, sets.get(index).getFirst(), this.choiceSituation) == true){
						sets.get(index).addLast(map);
						break;
					}else if (index == sets.size()-1){
						sets.addLast(new LinkedList<int[]>());
						sets.getLast().addLast(map);
						break;
					}
				}
			}else {
				sets.addLast(new LinkedList<int[]>());
				sets.getLast().addLast(map);
			}
		}

		if (this.getParent().isRoot() == true){
			for (int index = 0 ; index < sets.size() ; index++){
				// 第一層選擇時，依照規則去展開
				switch (AllSolve.rule){
				case Referee.rule_2:
					if (sets.get(index).getFirst()[this.choiceSituation] != Board.mine){
						//								System.out.println("runned");
						this.children.addLast(new RealNode(this, sets.get(index), this.choiceSituation));
						if (this.isRecord(this.level+1) == true){
//							System.out.println("使用存檔，勝率為：" + this.getWinRateFromRecord(this.level+1));
							this.children.getLast().setWinRate(this.getWinRateFromRecord(this.level+1));
							this.setNextIndexInRecord(this.level+1);
						}else {
							((RealNode)this.children.getLast()).run();
						}
					}else {
						sets.remove(index);
						index--;
					}
					break;

				case Referee.rule_3:
					if (sets.get(index).getFirst()[this.choiceSituation] == Board.noMine){
						this.children.addLast(new RealNode(this, sets.get(index), this.choiceSituation));
						
						if (this.isRecord(this.level+1) == true){
//							System.out.println("使用存檔，勝率為：" + this.getWinRateFromRecord(this.level+1));
							this.children.getLast().setWinRate(this.getWinRateFromRecord(this.level+1));
							this.setNextIndexInRecord(this.level+1);
						}else {
							((RealNode)this.children.getLast()).run();
						}
					}else {
						sets.remove(index);
						index--;
					}
					break;

				default:
					this.children.addLast(new RealNode(this, sets.get(index), this.choiceSituation));
					
					if (this.isRecord(this.level+1) == true){
//						System.out.println("使用存檔，勝率為：" + this.getWinRateFromRecord(this.level+1));
						this.children.getLast().setWinRate(this.getWinRateFromRecord(this.level+1));
						this.setNextIndexInRecord(this.level+1);
					}else {
						((RealNode)this.children.getLast()).run();
					}
					break;
				}
			}
		}else {
			for (int index = 0 ; index < sets.size() ; index++){
				this.children.addLast(new RealNode(this, sets.get(index), this.choiceSituation));
				if (this.isRecord(this.level+1) == true){
//					System.out.println("使用存檔，勝率為：" + this.getWinRateFromRecord(this.level+1));
					this.children.getLast().setWinRate(this.getWinRateFromRecord(this.level+1));
					this.setNextIndexInRecord(this.level+1);
				}else {
					((RealNode)this.children.getLast()).run();
				}
			}
		}
	}

	// 依照set的數量進行加權，然後再進行總和
	@Override
	void setWinRate(){
		int sumofSet = 0;
		for (Node n : this.children){
			sumofSet += n.set.size();
		}

		double winRate = 0;
		for (Node n : this.children){
			winRate += ((double)n.set.size() / sumofSet) * n.getWinRate();
		}
		
		if ((winRate > 1.001 || winRate == 0)){
			System.out.println("error in ChoiceNode.setWinRate");
			System.out.println("ChoiceNode-------------------------------");
			System.out.println("choice = " + this.choiceSituation);
			this.showMap(this.set.getFirst(), showMap);
			System.out.println();
			for (int[] map :this.set){
				AllSolve.showMap(map);
				System.out.println();
			}

			System.out.println("this is ChoiceNode, set = " + this.set.size() + " and winRate is " + winRate);
			for (Node n : this.children){
				System.out.println("child set = " + n.set.size() + ", winRate = " + n.getWinRate());
			}
			System.out.println("-------------------------------ChoiceNode");
		}
		this.setWinRate(winRate);
		this.finalize_By_Teddy();
	}

	@Override
	void showMap(int[] map, boolean[] showMap){
		for (int y = 0 ; y < AllSolve.mapY ; y++){
			for (int x = 0 ; x < AllSolve.mapX ; x++){
				if (this.choiceSituation == Referee_Board.getRealSituation(AllSolve.mapX, y, x)){
					System.out.print("C" + "\t");
				}else if (showMap[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] == false){
					System.out.print("#" + "\t");
				}else if (map[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] == Board.mine){
					System.out.print("P" + "\t");
				}else {
					System.out.print(map[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] + "\t");
				}
			}
			System.out.println();
		}
	}

}
