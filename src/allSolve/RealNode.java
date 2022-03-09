package allSolve;

import game.Board;
import game.Referee_Board;
import java.util.LinkedList;

final class RealNode extends Node{
	private int[] choice; // 選擇的節點，僅僅是為了印出地圖(非必要)
	
	// root
	RealNode(LinkedList<int[]> set) {
		super(set);
	}
	
	// root and record
	RealNode(LinkedList<int[]> set, LoaderTree rtree){
		super(set, rtree);
	}

	// child node	
	RealNode(Node parent, LinkedList<int[]> set, int... choice) {
		super(parent, set);
		this.choice = choice;
		
		for (int c : this.choice){
			this.showMap[c] = true;
		}
	}

	@Override
	void run(){
		if (this.isRoot() == true){
			if (this.isRecord(this.level) == true){
//				System.out.println("使用存檔，勝率為：" + this.getWinRateFromRecord(this.level));
				this.setWinRate(this.getWinRateFromRecord(this.level));
				this.setNextIndexInRecord(this.level);
			}else if (AllSolve.isUseMethod_MapDatabase == true 
//					&& LoaderTree.load(this.set.getLast(), this.showMap) == true){
////				System.out.println("讀取成功，數值為 " + LoaderTree.getBestWinRate());
//				this.setWinRate(LoaderTree.getBestWinRate());
					&& Node.getRoot().cdatabase.isSomeMap(this.set.getLast(), this.showMap) == true){
				this.setWinRate(Node.getRoot().cdatabase.getWinRate());
			}else {
				this.createChildren();
				this.setWinRate();
			}
		}else {
//				System.out.println("讀取成功，數值為 " + LoaderTree.getBestWinRate());
			if (AllSolve.isUseMethod_MapDatabase == true 
//					&& LoaderTree.load(this.set.getLast(), this.showMap) == true){
//				this.setWinRate(LoaderTree.getBestWinRate());
					&& Node.getRoot().cdatabase.isSomeMap(this.set.getLast(), this.showMap) == true){
				this.setWinRate(Node.getRoot().cdatabase.getWinRate());
			}else {
				if (this.getParent() instanceof RealNode){
					if (this.isGameOver() == true){		// 已經打開所有的點了
						// 勝利
						this.setWinRate(1);
					}else {
						// 繼續產生子點
						this.createChildren();
						this.setWinRate();
					}
				}else if (this.getParent() instanceof ChoiceNode){
					int[] setMap = this.set.getFirst();
					if (this.isMine(setMap, choice[choice.length-1]) == true){
						// 如果最後一個位置是地雷， 失敗
						this.setWinRate(0);
					}else {
						// 如果最後一個位置不是地雷
						if (this.isGameOver() == true){
							// 勝利
							this.setWinRate(1);
						}else {
							// 繼續產生子點
							this.createChildren();
							this.setWinRate();
						}
					}
				}
			}
		}
	}

	// 製造子節點，使用DFS的寫法
	@Override
	void createChildren() {
		this.children = new LinkedList<Node>();
		int ans[] = this.findAnswer();
		if (ans == null){
			// 猜測，產生所有未開點的子點
			boolean[] work = this.getWorkArray();
			for (int index = 0 ; index < AllSolve.mapSize ; index++){
				if (work[index] == true && this.showMap[index] == false){	// 還沒有打開的點
					this.children.addLast(new ChoiceNode(this, this.set, index));
					if (this.isRecord(this.level+1) == true){
//						System.out.println("使用存檔，勝率為：" + this.getWinRateFromRecord(this.level+1));
						this.children.getLast().setWinRate(this.getWinRateFromRecord(this.level+1));
						this.setNextIndexInRecord(this.level+1);
					}else {
						((ChoiceNode)this.children.getLast()).run();
					}
				}
			}
		}else { 
			// 統計解答
			LinkedList<LinkedList<int[]>> sets = new LinkedList<LinkedList<int[]>>();
			for (int[] map : this.set){
				if (sets.isEmpty() == false){
					for (int index = 0 ; index < sets.size() ; index++){
						if (this.isSomeMap(map, sets.get(index).getFirst(), ans) == true){
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

			for (int index = 0 ; index < sets.size() ; index++){
				this.children.addLast(new RealNode(this, sets.get(index), ans));
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

	// 選擇最大勝率為勝率
	@Override
	void setWinRate(){
		//		System.out.println("This is setWinRate");
		double winRate;
		if (this.children.getLast() instanceof ChoiceNode){
			//			System.out.println("This node have ChoiceNode children");
			
			// 選擇最大的勝率
			winRate = Double.MIN_VALUE;
			for (Node n : this.children){
				if (n.getWinRate() > winRate){
					winRate = n.getWinRate();
				}
			}
			
			if (AllSolve.outputBestWinRate == true){
				// 收集最大的機率的位置
				LinkedList<Integer> bestWinRate = new LinkedList<Integer>();
				
				for (Node n : this.children){
					if ((float)n.getWinRate() >= (float)winRate){
						bestWinRate.clear();
						bestWinRate.addLast(((ChoiceNode)n).getChoice());
					}else if ((float)n.getWinRate() == (float)winRate){
						bestWinRate.addLast(((ChoiceNode)n).getChoice());
					}
				}
				
				// 輸出最大勝率的點
				LoaderTree.safe(this.set.getLast(), this.showMap, bestWinRate, winRate);
			}
			
		}else if (this.children.getLast() instanceof RealNode){
			//			System.out.println("This node have RealNodeNode children");
			winRate = 0;
			int sumofSet = 0;
			for (Node n : this.children){
				sumofSet += n.set.size();
			}

			for (Node n : this.children){
				//				System.out.println("show children winrate = " + n.getWinRate());
				winRate += ((double)n.set.size() / sumofSet) * n.getWinRate();
			}
			//			System.out.println("show sum winrate = " + winRate);
		}else {
			System.out.println("error in RealNode.setWinRate");
			winRate = Node.winRateInitial;
			//			if (this.children.isEmpty() == true){
			//				System.out.println("子節點為零");
			//			}
		}
		
		// 儲存地圖的最大勝率。
		if (AllSolve.isUseMethod_MapDatabase == true){
			// 收集最大的機率的位置
//			System.out.println("winRate = " + winRate);
//			LoaderTree.safe(this.set.getLast(), this.showMap, winRate);
//			if (Node.getRoot().cdatabase.getWinRate() == 0 || Node.getRoot().cdatabase.getWinRate() == 1){
//				System.out.println("絕對點2！");
//			}
			
			Node.getRoot().cdatabase.add(this.set.getLast(), this.showMap, winRate);
		}

		this.setWinRate(winRate);
		this.finalize_By_Teddy();
	}

	// 利用交集法找到絕對點
	private int[] findAnswer(){
		int sumOfAns = 0;
		int[] sumofmineMap = new int[AllSolve.mapSize];
		boolean[] ansMap = new boolean[AllSolve.mapSize];

		// 沒有打開的位置才有可能有答案
		for (int index = 0 ; index < AllSolve.mapSize ; index++){
			if (this.showMap[index] == false){
				ansMap[index] = true;
				sumOfAns++; 
			}
		}

		int setNumber = 0;

		for (int[] map : this.set){
			setNumber++;
			for (int index = 0 ; index < AllSolve.mapSize ; index++){
				if (this.showMap[index] == false && ansMap[index] == true){	// 未打開
					if (map[index] == Board.mine) sumofmineMap[index]++;
					if (sumofmineMap[index] > 0 && sumofmineMap[index] < setNumber){
						ansMap[index] = false;		// 此位置並非絕對點
						sumOfAns--;
					}
				}
			}
			if (sumOfAns == 0) break;	// 完全沒有絕對點就跳出。
		}

		if (sumOfAns > 0){
			int ans[] = new int[sumOfAns];
			int ansIndex = 0;
			for (int index = 0 ; index < AllSolve.mapSize ; index++){
				if (ansMap[index] == true){
					ans[ansIndex] = index;
					ansIndex++;
				}
			}
			return ans;
		}else {
			return null;
		}
	}

	private boolean isMine(int[] map, int n){
		return (map[n] == Board.mine);
	}

	// 得到真的需要計算的位置
	private boolean[] getWorkArray(){
		boolean[] work = new boolean[AllSolve.mapSize];
		
		if (AllSolve.isUseMethod_workArea == true){
			if (this.isRoot() == true){
				// 只設定所需最少的位置
				for (int y = 0 ; y < AllSolve.mapY ; y++){
					for (int x = 0 ; x < AllSolve.mapX ; x++){
						if ((x <= (double)(AllSolve.mapX-1)/2) && (y <= (double)(AllSolve.mapY-1)/2)){
							if ((AllSolve.mapX != AllSolve.mapY) || ((AllSolve.mapX == AllSolve.mapY) && (x >= y))){		// 如果是正方形，可以省去更多
								work[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] = true;
							}
						}
					}
				}
				this.showWorkArray(work);
			}else {
				// TODO 處理鏡射與反射的狀況，目前使用全算的方法
				for (int index = 0 ; index < work.length ; index++){
					work[index] = true;
				}
			}
		}else {
			// 所有的位置
			for (int index = 0 ; index < work.length ; index++){
				work[index] = true;
			}
			
			if (this.isRoot() == true){
				this.showWorkArray(work);
			}
		}
		return work;
	}
	
	private void showWorkArray(boolean[] work){
		System.out.println("shwo work area----------------");
		for (int y = 0 ; y < AllSolve.mapY ; y++){
			for (int x = 0 ; x < AllSolve.mapX ; x++){
				if (work[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] == true){
					System.out.print("T" + "\t");
				}else {
					System.out.print("F" + "\t");
				}
			}
			System.out.println();
		}
		System.out.println("----------------shwo work area");
		System.out.println();
	}

	@Override
	void showMap(int[] map, boolean[] showMap){
		for (int y = 0 ; y < AllSolve.mapY ; y++){
			for (int x = 0 ; x < AllSolve.mapX ; x++){
				if ((this.getParent() instanceof ChoiceNode && (this.choice[0] == Referee_Board.getRealSituation(AllSolve.mapX, y, x))) 
						&& map[Referee_Board.getRealSituation(AllSolve.mapX, y, x)] == Board.mine){
					System.out.print("@" + "\t");
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
