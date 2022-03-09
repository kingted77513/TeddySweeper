package ai;
import game.Board;
import game.Player_Board;
import game.Player_Board.Player_Grid;
import ai.AI;
import java.math.BigDecimal;
import java.util.LinkedList;

public final class CSP extends AI{
	boolean isUseGuessMethod_forecast = true;			//是否有使用預知猜測
	private boolean isDebug = true;			//是否顯示訊息
	boolean isUseFirstStrategy = false;					//是否有使用首步指定策略
	boolean isFirstForDemo = false;						//為了demo而使用的指定模擬的第一步
	
	private static final short isMine = 1;
	private static final short notMine = 0;
	private static final int rootIndex = 0;
	
	// 為了避免大量的重覆模擬，尋找三個點代表
	simulationGrid corner;		//代表角落
	simulationGrid edge;			//代表邊
	simulationGrid center;		//代表中央
	
	fopjMap root;
	fopjMapDatabase mapLink;
	LinkedList<fopjMap.fopjGrid> unknow_Grid_list; 	//暫存未知點
	LinkedList<fopjMap.fopjGrid> bfs; 	//使用BFS擴張
	LinkedList<Player_Grid> noMinePoint = new LinkedList<Player_Grid>();		//確定不是地雷的點
	LinkedList<Player_Grid> isMinePoint = new LinkedList<Player_Grid>();		//確定是地雷的點
	LinkedList<Player_Grid> guessPoint = new LinkedList<Player_Grid>();		//猜的點
	mineDestinyRatio mineRatio;
	boolean isChange;	//局勢有變
	boolean isSimulation;
	long overlappingTimes = 0;
	boolean isUseGuessMethod;		//是否有使用猜的方法
	boolean isFirstOverlapping;		//是否是第一次Overlapping
	private int maxIndex;
	
//	SinglePoint spSolutioner;	//模擬所需的解題物件，使用SP
//	TeddyMethod cfSolutioner;	//模擬所需的解題物件，使用CF
//	CSP fopjSolutioner;	//模擬所需的解題物件，使用FOPJ
	
	public CSP(Player_Board playerMap){
		super(playerMap);
		this.root = new fopjMap(playerMap);	//假設地圖
		this.mapLink = new fopjMapDatabase();
		this.isChange = false;
		this.isUseGuessMethod = false;
		this.isSimulation = false;
		this.maxIndex = CSP.rootIndex;
	}
	
	private CSP(Player_Board playerMap, fopjMap sroot, fopjMapDatabase smapLink, int smaxIndex, boolean isSimulation){
		this(playerMap);
		this.root = sroot;
		this.mapLink = smapLink;
		this.isSimulation = isSimulation;
		this.isUseGuessMethod = false;
		this.isUseGuessMethod_forecast = false;
		this.maxIndex = smaxIndex;
	}

	@Override
	public String answer() {
		if (this.isUseFirstStrategy == true && this.map.getRemainInitialGrid() == (this.map.getMapY() * this.map.getMapX())){
			// 如果是棋面完全沒有開啟過，使用首步指定策略
			this.guessPoint.addLast(this.map.getGrid(this.map.getMapY()/2, this.map.getMapX()/2));
		}
		this.select();
		return this.order;
	}
	
	private void select(){
		if (this.noMinePoint.size() > 0){
			while (this.noMinePoint.size() > 0){
				if (this.map.isVisit(this.noMinePoint.getLast().getY(), this.noMinePoint.getLast().getX()) == false){
					this.order = "dig " + this.noMinePoint.getLast().getCoordinate();
					break;
				}else {
					if (this.isDebug == true){
//						System.out.println("this.noMinePoint.size = " + this.noMinePoint.size());
//						System.out.println(this.noMinePoint.getLast().getCoordinate() + " it is deleted from noMinePoint");
					}
					this.noMinePoint.removeLast();
				}
			}
		}
		
		if (this.noMinePoint.size() == 0 && this.isMinePoint.size() > 0){
			while (this.isMinePoint.size() > 0){
				if (this.map.isFlag(this.isMinePoint.getLast().getY(), this.isMinePoint.getLast().getX()) == false){
					this.order = "flag " + this.isMinePoint.getLast().getCoordinate();
					break;
				}else {
					if (this.isDebug == true){
//						System.out.println("this.isMinePoint.size = " + this.isMinePoint.size());
//						System.out.println(this.isMinePoint.getLast().getCoordinate() + " it is deleted from isMinePoint");						
					}
					this.isMinePoint.removeLast();
				}
			}
		}
		
		if (this.isMinePoint.size() == 0 && this.guessPoint.size() > 0){
			while (this.guessPoint.size() > 0){
				if (this.map.isVisit(this.guessPoint.getLast().getY(), this.guessPoint.getLast().getX()) == false){
					this.order = "dig " + this.guessPoint.getLast().getCoordinate();
					break;
				}else {
					if (this.isDebug == true){
//						System.out.println("this.isMinePoint.size = " + this.isMinePoint.size());
//						System.out.println(this.isMinePoint.getLast().getCoordinate() + " it is deleted from isMinePoint");						
					}
					this.guessPoint.removeLast();
				}
			}
		}
		
		if (this.noMinePoint.size() == 0 && this.isMinePoint.size() == 0 && this.guessPoint.size() == 0){
			if (this.isDebug == true && this.isSimulation == true){
//				System.out.println("show the simulation map in select");
//				this.showMap(this.map, "state");
//				System.out.println("show the root state map in select");
//				this.showMap(this.root, "state");
//				System.out.println("show the root fopj map in select");
//				this.showMap(this.root, "fopj");
			}
			
			this.search();
			if (this.noMinePoint.size() > 0 || this.isMinePoint.size() > 0 || this.guessPoint.size() > 0){				
				this.select();
			}else {
				if (this.isUseGuessMethod == true){
					this.guess();	//機率選擇
				}else {
					this.order = "no_answer";
				}
			}
		}
	}
	
	private void guess(){		//機率選擇
		LinkedList<fopjMap.fopjGrid> bestPointLink = null;
		double pos;
		
		if (this.overlappingTimes == 0){		//沒有經過overlapping，即代表沒有未知點，也沒有經過overlapping，所剩的只有陌生點
			int besideInitial, besideM;
			double strangePossible;
			for (int y = 0 ; y < this.map.getMapY() ; y++){
				for (int x = 0 ; x < this.map.getMapX() ; x++){
					if (this.isDebug == true && x == 0 & y == 0){
					}
					
					if (this.map.isVisit(y, x) == false && this.map.isFlag(y, x) == false){
						strangePossible = (double)this.map.getRemainMineNumber() / this.map.getRemainInitialGrid();
						this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] = strangePossible;
						
						if (this.isUseGuessMethod_forecast == true){
							besideInitial = 0;
							besideM = 0;
							
							if (strangePossible < 1){
								for (int j = y-1 ; j <= y+1 ; j++){
									for (int i = x-1 ; i <= x+1 ; i++){
										if (i != x || j != y){		//中間的點不用理
											try {
												if (this.map.isVisit(j, i) == false && this.map.isFlag(j, i) == false){
													besideInitial++;	//未假設的未打開空格數
												}else if (this.map.isFlag(j, i) == true){
													besideM++;
												}
											}catch (ArrayIndexOutOfBoundsException aio){}
										}
									}
								}
								
								for (int index = 0 ; index <= 8 ; index++){
									this.root.fopjmap[y][x].possilbeTimes[index] = 
											(this.detail_possible_forecast(index, besideInitial, besideM, this.map.getRemainInitialGrid(), this.map.getRemainMineNumber(), this.root.isFOPJ(y, x)))
											* (1 - strangePossible); 
								}
							}
						}
						
						if (this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] == 1){	//如果陌生機率為1，代表所剩的都是地雷
							this.isMinePoint.addLast(this.map.getGrid(y, x));
						}else if (this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] == 0){
							this.noMinePoint.addLast(this.map.getGrid(y, x));
						}
					}
				}
			}
		}
		
		if (this.isDebug == true){
			for (int index = 0 ; index <= 9 ; index++){
				if (index == 9){
					System.out.println("是地雷的機率為：");
					this.showPossible(map, root, index);
				}else {
					if (this.isUseGuessMethod_forecast == true){
						System.out.println("數字是 " + index + " 的機率是：");
						this.showPossible(map, root, index);
					}
				}
			}
		}
		
		for (int y = 0 ; y < this.map.getMapY() ; y++){		//尋找最小地雷機率的格子們
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				if (this.map.isVisit(y, x) == false && this.map.isFlag(y, x) == false){
					if (bestPointLink == null){
						bestPointLink = new LinkedList<fopjMap.fopjGrid>();
						bestPointLink.addLast(this.root.fopjmap[y][x]);
					}else if (this.root.getMinePossible(y, x, this.map, this.overlappingTimes) < bestPointLink.getFirst().getMinePossible(this.map, this.overlappingTimes)){
						bestPointLink.clear();		//刪除所有的元素
						bestPointLink.addLast(this.root.fopjmap[y][x]);
					}else if (this.root.getMinePossible(y, x, this.map, this.overlappingTimes) == bestPointLink.getFirst().getMinePossible(this.map, this.overlappingTimes)){
						bestPointLink.addLast(this.root.fopjmap[y][x]);
					}
					
					if (this.isDebug == true && this.isUseGuessMethod_forecast == true){
						pos = 0;
						for (int index = 0 ; index <= 9 ; index++){
							pos += this.root.fopjmap[y][x].possilbeTimes[index];
						}
						
						if (pos < 0.99 || pos > 1.01){
							System.out.println("this is possible error in guess");
							System.out.println(this.root.fopjmap[y][x].getCoordinate() + " in guess -> " + pos);
							System.out.println("this.overlappingTimes = " + this.overlappingTimes);
							System.out.println("isMine = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()]);
							System.out.println("haveZeroMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveZeroMines.getCode()]);
							System.out.println("haveOneMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveOneMines.getCode()]);
							System.out.println("haveTwoMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveTwoMines.getCode()]);
							System.out.println("haveThreeMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveThreeMines.getCode()]);
							System.out.println("haveFourMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveFourMines.getCode()]);
							System.out.println("haveFiveMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveFiveMines.getCode()]);
							System.out.println("haveSixMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveSixMines.getCode()]);
							System.out.println("haveSevenMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveSevenMines.getCode()]);
							System.out.println("haveEightMines = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.haveEightMines.getCode()]);
						}
					}
				}
			}
		}
		
		if (this.noMinePoint.size() == 0 && this.isMinePoint.size() == 0){
			if (bestPointLink.size() == 1){		//如果只有一點就沒有什麼好挑的。
				this.noMinePoint.addLast(bestPointLink.getFirst());
			}else {
//				if (this.isUseGuessMethod_forecast == true){
//						this.guess_forecast(bestPointLink);
//				}else {
//					//隨機從最小機率選擇一點
					this.guessPoint.addLast(bestPointLink.get((int)(Math.random()*bestPointLink.size())));
//				}
			}
		}
		
		this.select();
	}
	
//	private void guess_forecast(LinkedList<fopjMap.fopjGrid> bestPointLink){	//預測猜測
//		//TODO guess_forecast
//		this.corner = new simulationGrid();
//		this.edge = new simulationGrid();
//		this.center = new simulationGrid();
//		boolean isCorner, isEdge;
//		boolean isFirst;
//		boolean isStrange;
////		double digValue;
//		
//		for (int index = 0 ; index < bestPointLink.size() ; index++){
//			if (this.isDebug == true){
////				if (bestPointLink.get(index).getX() == 8 && bestPointLink.get(index).getY() == 0){
////				}
//			}
//			
//			isFirst = (this.map.getRemainInitialGrid() == (this.map.getMapY() * this.map.getMapX()));
//			isStrange = this.isStrange(bestPointLink.get(index));
//			isCorner = false;
//			isEdge = false;
//			
//			if (isStrange == true){
//				if ((bestPointLink.get(index).getY() == 0 || bestPointLink.get(index).getY() == this.map.getMapY() - 1)
//						&& (bestPointLink.get(index).getX() == 0 || bestPointLink.get(index).getX() == this.map.getMapX() - 1)){		//如果是角落
//					isCorner = true;
//				}else if ((bestPointLink.get(index).getY() == 0 || bestPointLink.get(index).getY() == this.map.getMapY() - 1)
//						|| (bestPointLink.get(index).getX() == 0 || bestPointLink.get(index).getX() == this.map.getMapX() - 1)){	//如果是邊
//					isEdge = true;
//				}
//			}
//			
//			if (this.corner.isSimulation() == true && isStrange == true && isCorner == true){		//如果是角落
//				bestPointLink.get(index).possilbeTimes[possibleJudge.digValue.getCode()] = this.corner.getValue();
//			}else if (this.edge.isSimulation() == true && isStrange == true	&& isEdge == true){	//如果是邊
//				bestPointLink.get(index).possilbeTimes[possibleJudge.digValue.getCode()] = this.edge.getValue();
//			}else if (this.center.isSimulation() == true && isStrange == true){	//如果是中央
//				bestPointLink.get(index).possilbeTimes[possibleJudge.digValue.getCode()] = this.center.getValue();
//			}else {
////				評價摸擬加權分數
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveZeroMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveOneMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveTwoMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveThreeMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveFourMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveFiveMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveSixMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveSevenMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				this.guess_forecast_evaluate(bestPointLink.get(index), possibleJudge.haveEightMines.getCode(), isFirst, isStrange, isCorner, isEdge);
//				
//				if (isStrange == true){
//					if (this.corner.isSimulationed == false && isCorner == true){		//如果是角落
//						this.corner.setSimulationed();
//						if (this.isDebug == true){
////							System.out.println("corner's digValue = " + this.corner.getValue());
//						}
//					}else if (this.edge.isSimulationed == false && isEdge == true){	//如果是邊
//						this.edge.setSimulationed();
//						if (this.isDebug == true){
////							System.out.println("edge's digValue = " + this.edge.getValue());
//						}
//					}else if (this.center.isSimulationed == false){	//如果是中央
//						this.center.setSimulationed();
//						if (this.isDebug == true){
////							System.out.println("center's digValue = " + this.center.getValue());
//						}
//					}
//				}
//			}
//		}
//		
//		if (this.isDebug == true){
//			for (int j = 0 ; j < bestPointLink.size() ; j++){
////				System.out.println(bestPointLink.get(j).getCoordinate());
////				System.out.println("digValue = " 
////						+ bestPointLink.get(j).possilbeTimes[possibleJudge.digValue.getCode()]);
//			}
//		}
//		
//		//選擇踩分數最高的點
//		LinkedList<fopjMap.fopjGrid> bestPoint = null;
//		fopjMap.fopjGrid bestp = null;
//		for (int i = 0 ; i < bestPointLink.size() ; i++){
//			if (bestPoint == null){
//				bestPoint = new LinkedList<fopjMap.fopjGrid>();
//				bestPoint.addLast(bestPointLink.get(i));
//			}else if (bestPointLink.get(i).possilbeTimes[possibleJudge.digValue.getCode()] 
//					> bestPoint.getFirst().possilbeTimes[possibleJudge.digValue.getCode()]){
//				bestPoint.clear();		//刪除所有的元素
//				bestPoint.addLast(bestPointLink.get(i));
//			}else if (bestPointLink.get(i).possilbeTimes[possibleJudge.digValue.getCode()] 
//					== bestPoint.getFirst().possilbeTimes[possibleJudge.digValue.getCode()]){
//				bestPoint.addLast(bestPointLink.get(i));
//			}
//		}
//		bestp = bestPoint.get((int)(Math.random()*bestPoint.size()));
//		this.noMinePoint.addLast(this.map.getGrid(bestp.getY(), bestp.getX()));
//	}
	
//	private void guess_forecast_evaluate(fopjMap.fopjGrid bestgrid, int evaluateNumber, boolean isFirst, boolean isStrange, boolean isCorner, boolean isEdge){
//		double digValue = 0;
//		
//		if (evaluateNumber == possibleJudge.haveZeroMines.getCode()){
//			//是 0 時的分數
//			if (bestgrid.possilbeTimes[evaluateNumber] > 0){
//				digValue = (bestgrid.possilbeTimes[evaluateNumber] * guess_Weight.isZero.getWeight());
//				bestgrid.possilbeTimes[evaluateNumber] += digValue;
//			}
//		}else {
//			if (isFirst == false){
//				if (bestgrid.possilbeTimes[evaluateNumber] > 0){
//					Player_Board sMap = this.getSimulationMap(bestgrid, (short)evaluateNumber, this.map);
//					if (this.isAnswerIn_SP_Solution(sMap) == true){			//如果使用single_point有解時的加權
//						if (this.isDebug == true){
////							System.out.println("This is SP simulation");
//						}
//						digValue = (bestgrid.possilbeTimes[evaluateNumber] * guess_Weight.have_sol_in_SinglePoint.getWeight());
//						bestgrid.possilbeTimes[possibleJudge.digValue.getCode()] += digValue;				
//					}else if (this.isAnswerIn_CF_Solution(sMap) == true){	//如果使用common_field有解時的加權
//						if (this.isDebug == true){
////							System.out.println("This is CF simulation");
//						}
//						digValue = (bestgrid.possilbeTimes[evaluateNumber] * guess_Weight.have_sol_in_CF.getWeight());
//						bestgrid.possilbeTimes[possibleJudge.digValue.getCode()] += digValue;
//					}else if (this.isAnswerIn_FOPJ_Solution(sMap) ==  true){	//如果使用FOPJ有解時的加權
//						if (this.isDebug == true){
////							System.out.println("This is FOPJ simulation");
//						}
//						digValue = (bestgrid.possilbeTimes[evaluateNumber] * guess_Weight.have_sol_in_FOPJ.getWeight());
//						bestgrid.possilbeTimes[possibleJudge.digValue.getCode()] += digValue;
//					}
//				}
//			}
//		}
//		
//		if (isStrange == true){
//			if (this.corner.isSimulation() == false && isCorner == true){		//如果是角落
//				this.corner.setWeight(digValue);
//			}else if (this.edge.isSimulation() == false && isEdge == true){	//如果是邊
//				this.edge.setWeight(digValue);
//			}else if (this.center.isSimulation() == false){	//如果是中央
//				this.center.setWeight(digValue);
//			}
//		}
//	}
	
	private boolean isStrange(fopjMap.fopjGrid fgrid){
		boolean isstrange = true;
		
		for (int j = fgrid.getY() - 1 ; j <= fgrid.getY() + 1 ; j++){
			for (int i = fgrid.getX() - 1 ; i <= fgrid.getX() + 1 ; i++){
				if (i != fgrid.getX() || j != fgrid.getY()){		//中間的點不用理
					try {
						if (this.map.isVisit(j, i) == true || this.map.isFlag(j, i) == true || this.root.isFOPJ(j, i) == true){
							isstrange = false;
							break;
						}
					}catch (ArrayIndexOutOfBoundsException aio){}
				}
			}
			if (isstrange == false) break;
		}
		
		return isstrange;
	}
	
	private Player_Board getSimulationMap(fopjMap.fopjGrid solutionGrid, short digValue, Player_Board map){	//回傳假設地圖
		return new Player_Board(map, solutionGrid, digValue);
	}
	
//	private boolean isAnswerIn_SP_Solution(Player_Board sMap){			//調查在假設地圖下是否有解
//		this.spSolutioner = new SinglePoint(sMap);
//		return (spSolutioner.isAnswer());
//	}
	
//	private boolean isAnswerIn_FOPJ_Solution(Player_Board sMap){		//調查在假設地圖下是否有解
//		fopjMapDatabase sfmaplink = null;
//		fopjMap sfroot = null;
//		try {
//			sfmaplink = (fopjMapDatabase)this.mapLink.clone();
//			sfroot = (fopjMap)this.root.clone();
//		}catch (CloneNotSupportedException cns){
//			cns.printStackTrace();
//		}
//		
//		this.fopjSolutioner = new CSP(sMap, sfroot, sfmaplink, this.maxIndex, true);
//		return (fopjSolutioner.isAnswer());
//	}
	
//	private boolean isAnswerIn_CF_Solution(Player_Board sMap){		//調查在假設地圖下是否有解
//		this.cfSolutioner = new TeddyMethod(sMap);
//		return (cfSolutioner.isAnswer());
//	}
	
	@Override
	public boolean isAnswer(){			//是否有答案
//		this.select();
		return (this.isMinePoint.size() > 0 || this.noMinePoint.size() > 0) ? true : false;
	}
	
	@Override
	public double[][] getHintMap(){
		double[][] hint = new double[this.map.getMapY()][this.map.getMapX()];
//		System.out.println("show information in getHintMap");
//		System.out.println("noMinePoint size = " + this.noMinePoint.size());
//		System.out.println("isMinePoint size = " + this.isMinePoint.size());
//		System.out.println("guessPoint size = " + this.guessPoint.size());
		
		if (this.isMinePoint.size() > 0 || this.noMinePoint.size() > 0){
			for (int y = 0 ; y < this.map.getMapY() ; y++){
				for (int x = 0 ; x < this.map.getMapX() ; x++){
					hint[y][x] = Board.initial;
				}
			}
			
			for (Player_Board.Player_Grid g : this.noMinePoint){
				hint[g.getY()][g.getX()] = 0;
			}
			
			for (Player_Board.Player_Grid g : this.isMinePoint){
				hint[g.getY()][g.getX()] = 1;
			}
		}else {
			for (int y = 0 ; y < this.map.getMapY() ; y++){
				for (int x = 0 ; x < this.map.getMapX() ; x++){
					hint[y][x] = this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()];
				}
			}
		}
		
		return hint;
	}

	private enum guess_Weight{			//審局的權重
		isZero(5), have_sol_in_FOPJ(10), have_sol_in_CF(15), have_sol_in_SinglePoint(20);
		
		private final int weight;
		
		guess_Weight(int w) {weight = w;}
		
		private int getWeight() {return weight;}
	}
	
	private void search(){
		/*
		 * It is total 4 step
		 * 1. Assumption
		 * 2. Extending ratiocination
		 * 3. Mistake testing
		 * 4. Overlapping
		 */
		
		if (this.isDebug == true && this.isSimulation == true){
//			System.out.println("show the simulation map in search");
//			this.showMap(this.map, "state");
//			System.out.println("show the root state map in search");
//			this.showMap(this.root, "state");
//			System.out.println("show the root fopj map in search");
//			this.showMap(this.root, "fopj");
		}
		
		this.isChange = this.root.reRead(this.map, this.mapLink);
		
		if (this.isDebug == true){
//			if (this.isSimulation == true){
//				System.out.println("show the simulation root in search");
//				this.showMap(this.root, "state");
//			}
		}
			
		this.get_unknownGrid(); 	//暫存未知點
		this.bfs = new LinkedList<fopjMap.fopjGrid>(); 	//使用BFS擴張
		
		fopjMap.fopjGrid assumptionGrid = null;		//要假設時的參考位置
		int randomSelect;
		
		if (this.unknow_Grid_list.isEmpty() == false || this.isChange == true){		//有未知點才執行
			while (this.unknow_Grid_list.isEmpty() == false || this.isChange == true){
				if (this.isMinePoint.size() > 0 || this.noMinePoint.size() > 0) break;	//有答案就先退出
				if (this.unknow_Grid_list.isEmpty() == false) {
					randomSelect = (int)(Math.random()*this.unknow_Grid_list.size());
					
//					//為了demo而用
//					if (this.isFirstForDemo == true){
//						assumptionGrid = this.root.fopjmap[2][3];
//						this.isFirstForDemo = false;
//					}else {
//						assumptionGrid = this.unknow_Grid_list.get(randomSelect);
//					}
					
					assumptionGrid = this.unknow_Grid_list.get(randomSelect);		//隨機從未知點中挑一個。
					if (this.root.isFOPJ(assumptionGrid) == true){	//代表有假設過，則刪掉重選點
						this.unknow_Grid_list.remove(randomSelect);	
						continue;
					}else {
						this.maxIndex++;
					}
				}
				
				if (assumptionGrid != null) {
					this.extending(assumptionGrid);		//擴展
				}
				
				if (this.isDebug == true && this.isSimulation == true){
//					System.out.println("Show root fopj in search()");
//					this.showMap(root, "fopj");
				}
				this.overlapping();			//疊合
				this.isChange = false;		//局勢已定
				if (this.isDebug == true) {
//					System.out.println("isChange in search = " + this.isChange);
				}
			}
		}
	}
	
	private void assumption(fopjMap.fopjGrid grid){		//假設
		int size = this.mapLink.getLast().size();
		
		for (int number = 0 ; number < size ; number++){
			if (this.isDebug == true){
//				System.out.println("Show this.mapLink.getLast().getFirst() state in assumption()");
//				this.showMap(this.mapLink.getLast().getFirst(), "stae");
			}
			
			fopjMap[] fopjMapArray = {		//新增地圖，順便假設grid位置是否為地圖
				new fopjMap(this.mapLink.getLast().getFirst(), grid, CSP.notMine, this.maxIndex, this.map), 
				new fopjMap(this.mapLink.getLast().getFirst(), grid, CSP.isMine, this.maxIndex, this.map)
			};
			
			this.root.setAssumptionInRoot(grid, this.maxIndex);	//在root作記錄
			
			if (this.isDebug == true){
//				System.out.println("Show fopj state in assumption()");
//				this.showMap(root, "fopj");
			}
			
			for (fopjMap fmap : fopjMapArray){
				if (this.isDebug == true){
//					System.out.println("Show fmap state in assumption()，" + fmap.isLegit());
//					this.showMap(fmap, "stae");
				}
				this.deMistake(grid, fmap);
				if (fmap.isLegit() == true){
					if (this.isDebug == true){
//						this.showMap(fmap, "state");						
					}
					this.mapLink.getLast().addLast(fmap);
				}
			}
			this.mapLink.getLast().removeFirst();
		}
	}
	
	private void extending(fopjMap.fopjGrid grid){		//延伸，和grid相連的未知點一口氣展開
		this.mapLink.addLast(new fopjMapLink(this.maxIndex, this.map));	//新增鍊結
		this.isFirstOverlapping = true;
		this.bfs = this.get_unknownGrid(grid, this.bfs);	//找到grid旁的未知點，並存入bfs內
		while (this.bfs.isEmpty() == false){
			
			for (fopjMap.fopjGrid fgrid : this.bfs){
				if (this.root.isFOPJ(fgrid) == false) {
					this.assumption(fgrid);
				}
			}
			
			if (this.isDebug == true){
//				System.out.println("Show simulation root fopj in extending()");
//				this.showMap(root, "fopj");
			}
			
			if (this.bfs.isEmpty() == false) this.bfs = this.get_unknownGrid(this.bfs.getFirst(), this.bfs);	//使用BFS擴張
		}
	}

	private void deMistake(fopjMap.fopjGrid fgrid, fopjMap fmap){		//除錯
		LinkedList<fopjMap.fopjGrid> openGrid = this.get_openGrid(fgrid);	//fgrid是位置，fgrid是root的實際位子
		int remainMineNumber, initialNumber, flagNumber, fopjMineNumber;
		for (fopjMap.fopjGrid grid : openGrid){
			initialNumber = 0;
			flagNumber = 0;
			fopjMineNumber = 0;

			for (int j = grid.getY()-1 ; j <= grid.getY()+1 ; j++){
				for (int i = grid.getX()-1 ; i <= grid.getX()+1 ; i++){
					if (i != grid.getX() || j != grid.getY()){		//中間的點不用理
						try{
							if (this.map.isVisit(j, i) == false){	//未打開的點
								if (this.root.fopjmap[j][i].getIndex() == this.maxIndex){	//
									if (fmap.getState(j, i) == CSP.isMine){
										fopjMineNumber++;
									}
								}else {
									if (this.map.getState(j, i) == Board.flag){
										flagNumber++;
									}else if (this.map.getState(j, i) == Board.initial){
										initialNumber++;
									}
								}
							}
						}catch(ArrayIndexOutOfBoundsException aio){}
					}
				}
			}
			remainMineNumber = (grid.getState() - flagNumber - fopjMineNumber);

			if (this.isDebug == true){
//				System.out.println("show inpormation in deMistake");
//				this.showMap(fmap, "state");
//				System.out.println("initialNumber = " + initialNumber);
//				System.out.println("flagNumber = " + flagNumber);
//				System.out.println("fopjMineNumber = " + fopjMineNumber);
//				System.out.println("remainMineNumber = " + remainMineNumber);
			}
			
			//				前者是檢查地雷數是否太多，後者是檢查地雷數是否太少，前者為(剩餘地雷>0)，後者為當剩餘地雷大於零時，未ES的初始格數要大於剩餘地雷數。
			if (remainMineNumber < 0 || remainMineNumber > initialNumber){
				fmap.setIllegal();
				if (this.isDebug == true){
//					System.out.println("this fmap is illegal");
				}
			}
			
			if (fmap.isLegit == false) break;	//既然已經不合法了，就沒有必要繼續檢查了。
		}
	}
	
	private LinkedList<fopjMap.fopjGrid> get_openGrid(fopjMap.fopjGrid grid){	//得到grid旁邊的已開點位置
		LinkedList<fopjMap.fopjGrid> openGrid = new LinkedList<fopjMap.fopjGrid>();
		for (int j = grid.getY() - 1 ; j <= grid.getY()+1 ; j++){
			for (int i = grid.getX()-1 ; i <= grid.getX()+1 ; i++){
				if (i != grid.getX() || j != grid.getY()){		//中間的點不用理
					try{
						if (this.map.isVisit(j, i) == true){
							openGrid.addLast(this.root.fopjmap[j][i]);
						}
					}catch(ArrayIndexOutOfBoundsException aio){}
				}
			}
		}
		return openGrid;
	}
	
	private void overlapping(){		//重疊
		if (this.isDebug == true) {
//			System.out.println("show root's state in overlapping");
//			this.showMap(root, "state");
//			
//			System.out.println("show root's fopj in overlapping");
//			this.showMap(root, "fopj");	
		}
		
		fopjMap fmap;
		this.overlappingTimes = 0;
		this.setInitialPossibleTimes();		//機率初始化
		
		this.get_unknownGrid(); 	//暫存未知點
		boolean isNoExistUnknowGrid = (this.unknow_Grid_list.isEmpty());		//是否已無未知點
		boolean isFirst = this.isFirstOverlapping;								//是否是第一次overlapping
		boolean isGlobal = (isNoExistUnknowGrid == true	 	//是否全局考量 
				&& ((this.mapLink.mapDatabase.size() > 1 && isFirst == false)
				|| this.mapLink.mapDatabase.size() == 1)) ? true : false; 
		
		if (this.mapLink.isEmpty() == false && isGlobal == true){		//全域疊合才計算地雷所影響的機率
			if (this.isDebug == true){
				if (isGlobal == true){
					if (this.isSimulation == true){
						System.out.println("this is global simulation overlapping and run " + this.mapLink.getOverlappingTimes(isGlobal) + " times");
					}else {
						System.out.println("this is global overlapping and run " + this.mapLink.getOverlappingTimes(isGlobal) + " times");
					}
					if (isFirst == true){
//						this.showMap(this.map, "state");
					}
//					this.showMap(this.root, "fopj");
				}
			}
			
			this.mapLink.setInitialOverlapping();
			this.mineRatio = new mineDestinyRatio();
			while (this.mapLink.isExistNext(isGlobal) == true){
				fmap = this.mapLink.getNext(isGlobal, this.map, false);
				if (fmap.isLegit == true){	//合法才重疊
					this.mineRatio.add(this.map, fmap);
				}
				System.gc();	//釋放記憶體
			}
			
			this.mineRatio.countPossible(this.map);
//			this.mineRatio.print();
		}
		
		if (this.mapLink.isEmpty() == false){
			if (this.isDebug == true){
				if (isGlobal == false){
					if (this.isSimulation == true){
						System.out.println("this is simulation overlapping and run " + this.mapLink.getOverlappingTimes(isGlobal) + " times");
					}else {
						System.out.println("this is overlapping and run " + this.mapLink.getOverlappingTimes(isGlobal) + " times");
					}
					
					if (isFirst == true){
//						this.showMap(this.map, "state");
					}
				}
			}
			this.mapLink.setInitialOverlapping();
			while (this.mapLink.isExistNext(isGlobal) == true){
				fmap = this.mapLink.getNext(isGlobal, this.map, true);
				if (fmap.isLegit == true){	//合法才重疊
					if (this.isDebug == true){
//						if (isGlobal == true){
//							System.out.println("show the legit global fmap state in overlapping");
//							this.showMap(fmap, "state");
//						}
					}
					this.overlappingTimes++;
					this.overlapping(fmap, this.mineRatio, isGlobal);
				}
				System.gc();	//釋放記憶體
			}
		}
		
		if (this.overlappingTimes > 0){
			for (int y = 0 ; y < this.map.getMapY() ; y++){		//使用交集法來尋找解答
				for (int x = 0 ; x < this.map.getMapX() ; x++){
					if (this.root.isFOPJ(y, x) == true){
						if ((isNoExistUnknowGrid == false && this.root.fopjmap[y][x].getIndex() == this.maxIndex) || isNoExistUnknowGrid == true){
							if (((isGlobal == false) 
									&& ((this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] <= this.overlappingTimes + 0.01)			//考慮誤差
									&& (this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] >= this.overlappingTimes - 0.01)))
									|| ((isGlobal == true) 
									&& ((this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] <= 1.01)			//考慮誤差
									&& (this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] >= 0.99)))){
								this.isMinePoint.addLast(this.map.getGrid(y, x));
								if (this.isDebug == true){
//									System.out.println("this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] = " + this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()]);
//									System.out.println(x + " " +  y + " add to isMinePoint");								
								}

							}else if(this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] == 0){
								this.noMinePoint.addLast(this.map.getGrid(y, x));
								if (this.isDebug == true){
//									System.out.println(x + " " +   y + " add to noMinePoint");								
								}
							}
						}
					}
				}
			}
		}
		
		this.isFirstOverlapping = false;
		
		//視情況再一次overlapping
		if (this.isMinePoint.isEmpty() == true && this.noMinePoint.isEmpty() == true 
				&& isNoExistUnknowGrid == true && (isFirst == true && this.mapLink.mapDatabase.size() > 1)){
			if (this.isDebug == true) {
				if (this.isSimulation == true){
					System.out.println("simulation overlapping again");
				}else {
					System.out.println("overlapping again");
				}
//				System.out.println();
			}
			this.overlapping();
		}
		
		if (this.isDebug == true) {
//			System.out.println("isNoExistUnknowGrid = " + isNoExistUnknowGrid);
//			System.out.println("this.isChange = " + this.isChange);

//			System.out.println("show root's fopj in overlapping");
//			this.showMap(root, "fopj");	

//			System.out.println("show root's state in overlapping");
//			this.showMap(root, "state");

//			System.out.println("overlappingTimes = " + overlappingTimes);
//
//			System.out.println("show root overlapping in overlapping");
//			this.showMap(root, "overlapping");
//			
			if (isGlobal == true){
//				System.out.println("Mine possible in root in overlapping");
//				this.showMap(root, "possible");
			}
		}
	}

	private void overlapping(fopjMap fmap, mineDestinyRatio mineRatio, boolean isGlobal){	//重疊並收集有用的資料	
		int mapRemainM = this.map.getRemainMineNumber() - fmap.getConsumeMine();		//總地圖所剩下的地雷數
		int mapRemainL = this.map.getRemainInitialGrid() - fmap.getConsumeLattice();		//總地圖所剩下的空格數
		double strangePossible = (double)mapRemainM / mapRemainL;
		double minepos, pos; //測試機率是否為1
		int besideInitial, besideM;
		double[] detail_possible_forecast = new double[9];
		
		
		if (this.isDebug == true){
//			System.out.println("show fmap state in overlapping");
//			this.showMap(fmap, "state");
		}
		
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				minepos = 0;
				if (fmap.fopjmap[y][x] != null){						//有假設到的未知點
					if (fmap.fopjmap[y][x].getState() == CSP.isMine) {
						if (isGlobal == true){		//全域時，考慮地雷密度的影響
							minepos = mineRatio.getPossible(fmap.getConsumeMine());
						}else {						//部份時，只考慮有沒有
							minepos = 1;
						}						
						this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] += minepos;
					}
				}else if (this.map.isVisit(y, x) == false && this.map.isFlag(y, x) == false){				//沒假設到的陌生點
					if (this.isDebug == true){
//						System.out.println("strange possible in overlapping = " + (double) (map.getRemainMineNumber() - fmap.getConsumeMine()) 
//								 / ((map.getRemainInitialGrid() - (map.getMineNumber() - map.getRemainMineNumber()) - fmap.getConsumeLattice())));
//						System.out.println("map.getMineNumber() = " + map.getRemainMineNumber());
//						System.out.println("fmap.getConsumeMine() = " + fmap.getConsumeMine());
//						System.out.println("map.getRemainInitialGrid() = " + (map.getRemainInitialGrid() - (map.getMineNumber() - map.getRemainMineNumber())));
//						System.out.println("fmap.getConsumeLattice() = " + fmap.getConsumeLattice());
					}
					
					if (isGlobal == true){		//只有全域時才需要計算地雷機率
						minepos = mineRatio.getPossible(fmap.getConsumeMine()) * strangePossible;
					}else {
						minepos = strangePossible;
					}
					this.root.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()] += minepos;
				}

			 	//全局考量，針對未知點和陌生點
				if (this.isUseGuessMethod == true && this.isUseGuessMethod_forecast == true 
						&& (isGlobal == true)
						&& (this.map.isVisit(y, x) == false	&& this.map.isFlag(y, x) == false)
						&& ((fmap.fopjmap[y][x] != null && fmap.fopjmap[y][x].getState() == CSP.notMine) 
								|| ((fmap.fopjmap[y][x] == null) && (strangePossible < 1)))){	//陌生點機率要在小於100之間
					
					//數值初值化
					besideInitial = 0;
					besideM = 0;
					double ratio;
					
					for (int j = y - 1 ; j <= y+1 ; j++){
						for (int i = x-1 ; i <= x+1 ; i++){
							if (i != x || j != y){		//中間的點不用理
								try {
									if (this.map.isVisit(j, i) == false && this.map.isFlag(j, i) == false && this.root.isFOPJ(j, i) == false){	//未假到的未知點
										besideInitial++;	//未假設的未打開空格數
									}else if (this.map.isFlag(j, i) == true || (fmap.fopjmap[j][i] != null && fmap.getState(j, i) == CSP.isMine)){
										besideM++;
									}
								}catch (ArrayIndexOutOfBoundsException aio){}
							}
						}
					}
					
//					參考筆記22_20121010
					if (fmap.fopjmap[y][x] != null && fmap.fopjmap[y][x].getState() == CSP.notMine){	//未知點
						ratio = mineRatio.getPossible(fmap.getConsumeMine());
					}else {		//陌生點
						ratio = (1 - strangePossible) * mineRatio.getPossible(fmap.getConsumeMine());
					}
					
					pos = 0;
					for (int index = 0 ; index <= 8 ; index++){
						this.root.fopjmap[y][x].possilbeTimes[index] += ((this.detail_possible_forecast(index, besideInitial, besideM, mapRemainL, mapRemainM, this.root.isFOPJ(y, x))) * ratio);
						if (isDebug == true){
							detail_possible_forecast[index] = (this.detail_possible_forecast(index, besideInitial, besideM, mapRemainL, mapRemainM, this.root.isFOPJ(y, x)));
							pos += detail_possible_forecast[index];
//							System.out.println(x + " " + y + " 的數字 " + index + " 的可能性為：" + this.root.fopjmap[y][x].possilbeTimes[index]);
						}
					}
					if (isDebug == true){
						if (pos < 0.99 || pos > 1.01){
							System.out.println("show fmap state in overlapping");
							this.showMap(fmap, "state");
							
							System.out.println("error in overlapping, because pos = " + pos);
							for (int index = 0 ; index < detail_possible_forecast.length ; index++){
								System.out.println(x + " " + y + " 的數字 " + index + " 的可能性為：" + detail_possible_forecast[index]);
							}
							System.out.println(x + " " + y + "的數字總可能性為：" + pos);
						}
					}
				}
			}
		}
	}

	private double detail_possible_forecast(int number, int besideL, int besideM, int mapRemainL, int mapRemainM, boolean isAssume){
		//參考 筆記21-2012/9/15
		int mapL;							//所剩下的空格數
		int mapM = mapRemainM;				//所剩下的地雷數
		int needM = number - besideM;		//還需要假設的地雷數目
		double possible;
		
		if (isAssume == true){
			mapL = mapRemainL; //如果是已經假設中就不用扣掉本身那一格
		}else {
			mapL = mapRemainL - 1; //必須扣掉本身的那一格
		}
		
//		//參考 筆記21-2012/9/18
		if ((besideL == 0 && number != besideM) || (mapM == mapL && number != besideL + besideM) || (mapM == 0 && number != besideM)
				|| (number < besideM) || (number - besideM > mapM) || (number > (besideL + besideM))
				|| (mapL - besideL) < mapM - needM){
			/*
			 * besideL == 0 && number != besideM 旁邊沒有空格的況態下
			 * mapRemainM == mapRemainL 旁邊的空格都是地雷的狀態下
			 * mapRemainM == 0 旁邊的空格都不是地雷的狀態下
			 * mapRemainL - (besideL + 1)) < mapRemainM + needMineNumber 數字太小，假設完使得剩下的空格不夠擺地雷。
			 * number < besideM 數字太小
			 * number - besideM > mapRemainM 數字太大，就算所有的地雷都在旁邊也不夠
			 * number > (besideL + besideM) 數字太大
			 */
			possible = 0;
		}else if ((besideL == 0 && number == besideM) || (mapM == mapL && number == besideL + besideM) || (mapM == 0 && number == besideM)){
			/*
			 * besideInitial == 0 && number == besideM 旁邊已經沒有空格且數字剛好為旁邊的地雷數
			 * (besideInitial == dmapRemainL) && (number == mapRemainM + besideM)  旁邊的空格正好為所有的空格且數字剛好為旁邊的地雷數加上剩下的地雷數
			 * (mapRemainM == 0) && 
			 * 
			 */
			possible = 1;
		}else {
			//參考 筆記21-2012/9/15
//			double divisor = 1, dividend = 1;	//divisor：除數，dividend：被除數
//			
//			for (int a = 1 ; a <= besideL ; a++){
//				if (needM > 0){
//					divisor *= mapM;
//					mapM--;
//					needM--;
//				}else {
//					divisor *= (mapL - mapM);
//				}
//				dividend *= mapL;
//				
//				mapL--;
//			}
//			if (divisor == 0 || dividend == 0){
//				System.out.println("detail_possible_forecast error, it's possible is wrong");
//				System.out.println(this.isUseGuessMethod);
//			}
//			possible = divisor/dividend;
//			System.out.println("possible = " + possible);
			
//			long test1 = FOPJ.conbineNumber(besideL, needM).longValue();
//			System.out.println("test1 = " + test1);
//			long test2 = FOPJ.permutation(mapM, needM).longValue();
//			System.out.println("test2 = " + test2);
//			long test3 = FOPJ.permutation(mapL-mapM, besideL-needM).longValue();
//			System.out.println("test3 = " + test3);
//			long test4 = FOPJ.permutation(mapL, besideL).longValue();
//			System.out.println("test4 = " + test4);
//			double test5 = (test1 * test2 * test3) / (double)test4;
//			System.out.println("test5 = " + test5);
			possible = ((CSP.conbineNumber(besideL, needM).multiply(CSP.permutation(mapM, needM)).multiply(CSP.permutation(mapL-mapM, besideL-needM)))).divide(CSP.permutation(mapL, besideL), 16, BigDecimal.ROUND_HALF_UP).doubleValue();
//			System.out.println("possible = " + possible);
		}
		return possible;
	}
	
	private static BigDecimal conbineNumber (int m, int n){	//組合數
		// 參考網頁：http://www.start.helishi.net/archives/computing-combinations-with-java-and-python/
		
		if ((float)m/2 < n){
			n = m - n;
		}
		
		if (m < n || n < 0){
//			System.out.println("error! m < n in conbineNumber");
			return BigDecimal.valueOf(0);
		}else if (m == n || n == 0){
			return BigDecimal.valueOf(1);
		}else {		// m > n && n > 0
			BigDecimal fenzi = BigDecimal.valueOf(1);
			BigDecimal fenmu = BigDecimal.valueOf(1);
			for (int a = m ; a >= (m-n+1) ; a--){
				fenzi = fenzi.multiply(BigDecimal.valueOf(a));
//				System.out.println("a = " + a);
//				System.out.println("fenzi = " + fenzi.toString());
			}
			
			for (int b = 1 ; b <= n ; b++){
//				fenmu *= b;
				fenmu = fenmu.multiply(BigDecimal.valueOf(b));
//				System.out.println("b = " + b);
//				System.out.println("fenmu = " + fenmu.toString());
			}
//			System.out.println("m = " + m);
//			System.out.println("n = " + n);
//			System.out.println("fenzi = " + fenzi);
//			System.out.println("fenmu = " + fenmu);
//			System.out.println("fenzi/fenmu = " + (fenzi/fenmu));
			
			return fenzi.divide(fenmu);
		}
	}
	
	private static BigDecimal permutation(int m, int n){		//排列數
		BigDecimal per;
		if (m >= n){
			per = BigDecimal.valueOf(1);
			for (int a = m ; a >= (m-n+1) ; a--){
				per = per.multiply(BigDecimal.valueOf(a));
			}
			return per;
		}else {
			System.out.println("error in permutation, becouse m = " + m + " , n = " + n);
			return BigDecimal.valueOf(0);
		}
	}
	
	
	private void get_unknownGrid(){		//得到map所有的未知點，不包括已插旗的地方
		this.unknow_Grid_list = new LinkedList<fopjMap.fopjGrid>();
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				if (this.root.isFOPJ(y, x) == false && this.map.isUnknownGrid(y, x) == true 
						&& this.map.isFlag(y, x) == false){
					this.unknow_Grid_list.addLast(this.root.fopjmap[y][x]);
				}
			}
		}
	}
	
	private LinkedList<fopjMap.fopjGrid> get_unknownGrid(fopjMap.fopjGrid grid, LinkedList<fopjMap.fopjGrid> unkownGrid){	//得到某點旁邊的未知點，不包括已插旗的地方
		if (this.root.isFOPJ(grid) == false) {		//本身若還沒有處理過則加入
			unkownGrid.addLast(grid);
		}else {
			for (int index = 0 ; index < unkownGrid.size() ; index++){		//刪除掉已經處理過的點
				if (unkownGrid.get(index).getY() == grid.getY() && unkownGrid.get(index).getX() == grid.getX()){
					unkownGrid.remove(index);
					break;
				}
			}
		}
		
		for (int j = grid.getY() - 1 ; j <= grid.getY() + 1 ; j++){
			for (int i = grid.getX() - 1 ; i <= grid.getX() + 1 ; i++){
				if (i != grid.getX() || j != grid.getY()){		//中間的點不用理
					try{
						if (this.root.isFOPJ(j, i) == false && this.map.isUnknownGrid(j, i) == true && this.map.isFlag(j, i) == false){
							unkownGrid.addLast(this.root.fopjmap[j][i]);
						}
						
						//參考筆記21_20120917
						if (this.map.isVisit(j, i) == true){
							if (j == grid.getY()){
								if (i == grid.getX() - 1){
									if (this.root.isFOPJ(j, i-1) == false 
											&& this.map.isUnknownGrid(j, i-1) == true && this.map.isFlag(j, i-1) == false){
										unkownGrid.addLast(this.root.fopjmap[j][i-1]);
									}
								}else if (i == grid.getX() + 1){
									if (this.root.isFOPJ(j, i+1) == false 
											&& this.map.isUnknownGrid(j, i+1) == true && this.map.isFlag(j, i+1) == false){
										unkownGrid.addLast(this.root.fopjmap[j][i+1]);
									}
								}
							}else if (i == grid.getX()){
								if (j == grid.getY() - 1){
									if (this.root.isFOPJ(j-1, i) == false 
											&& this.map.isUnknownGrid(j-1, i) == true && this.map.isFlag(j-1, i) == false){
										unkownGrid.addLast(this.root.fopjmap[j-1][i]);
									}
								}else if (j == grid.getY() + 1){
									if (this.root.isFOPJ(j+1, i) == false 
											&& this.map.isUnknownGrid(j+1, i) == true && this.map.isFlag(j+1, i) == false){
										unkownGrid.addLast(this.root.fopjmap[j+1][i]);
									}
								}
							}
						}
					}catch(ArrayIndexOutOfBoundsException aio){}
				}
			}
		}
		
		return unkownGrid;
	}

	private void setInitialPossibleTimes(){		//機率參考值初始化
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				for (int i = 0 ; i < this.root.fopjmap[y][x].possilbeTimes.length ; i++){
					this.root.fopjmap[y][x].possilbeTimes[i] = 0;
				}
			}
		}
	}
	
	public boolean isGuess(){
		return this.isUseGuessMethod;
	}
	
	public void setGuess(boolean isSet) {	//設定機率
		this.isUseGuessMethod = isSet;
	}
	
	public void showMap(Board showmap, String kind){
		if (showmap instanceof fopjMap){
			fopjMap fmap = (fopjMap)showmap;
			
			for (int y = fmap.getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < fmap.getMapX() ; x++){
					if (fmap == this.root){
						if (kind.equals("fopj")) {
							System.out.print(fmap.fopjmap[y][x].getIndex() + "\t");
						}else if (kind.equals("state"))	{
							if (fmap.getState(y, x) == Board.initial){
								System.out.print("#" + "\t");
							}else if (fmap.getState(y, x) == Board.flag){
								System.out.print("P" + "\t");
							}else if (fmap.getState(y, x) == Board.mine){
								System.out.print("@" + "\t");
							}else {
								System.out.print(fmap.getState(y, x) +"\t");
							}
						}else if (kind.equals("overlapping")){
							if (this.map.isVisit(y, x) == true || this.map.isFlag(y, x) == true){
								System.out.print("#" + "\t");
							}else {
								System.out.printf("%.2f", fmap.fopjmap[y][x].possilbeTimes[possibleJudge.isMine.getCode()]);
								System.out.print("\t");
							}
						}else if (kind.equals("possible")){
							double possible;
							if (this.map.isVisit(y, x) == true){
								possible = 0;
							}else if (this.map.isFlag(y, x) == true){
								possible = 100;
							}else {
								possible = 100 * fmap.getMinePossible(y, x, this.map, this.overlappingTimes);
							}
							
							System.out.printf("%.2f", possible);
							System.out.print("%" + "\t");
						}
					}else {
						if (fmap.fopjmap[y][x] == null){
							System.out.print("#" + "\t");
						}else if (fmap.fopjmap[y][x].getState() == CSP.isMine){
							System.out.print("O" + "\t");
						}else if (fmap.fopjmap[y][x].getState() == CSP.notMine){
							System.out.print("X" + "\t");
						}
					}
				}
				System.out.println("");
			}
		}else if(showmap instanceof Player_Board){
			SinglePoint sp = new SinglePoint(this.map);
			sp.showMap(showmap, "state");
		}
		
		System.out.println("");
	}
	
	private void showPossible(Player_Board pmap, fopjMap fmap, int number){
		double possible;
		for (int y = fmap.getMapY() - 1 ; y >= 0 ; y--){
			for (int x = 0 ; x < fmap.getMapX() ; x++){
				if (this.map.isVisit(y, x) == true){
					possible = 0;
				}else if (this.map.isFlag(y, x) == true){
					possible = 100;
				}else {
					possible = 100 * fmap.fopjmap[y][x].possilbeTimes[number];
				}
				
				System.out.printf("%.2f", possible);
				System.out.print("%" + "\t");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	private enum possibleJudge {		//opfjGrid的權重參考
		haveZeroMines(0), haveOneMines(1), haveTwoMines(2), 
		haveThreeMines(3), haveFourMines(4), haveFiveMines(5), 
		haveSixMines(6), haveSevenMines(7), haveEightMines(8), 
		isMine(9), digValue(10);
		
		private final int code;
		
		possibleJudge(int c) {code = c;}
		
		private int getCode() {return code;}
	}
	@Override
	public String getStrategy(String strategy) {		//回傳所使用的策略子串
//		System.out.println("FOPJ = " + true);
		strategy += "| FOPJ ";
		if (this.isUseGuessMethod == true){
			strategy += "FOPJ_GuessMethod ";
			
			if (this.isUseGuessMethod_forecast == true){
				strategy += "Guess_forecast_Method ";
			}
		}
		
//		System.out.println("isUseFirstStrategy = " + this.isUseFirstStrategy); //是否有使用首步指定策略
		if (this.isUseFirstStrategy == true){
			strategy += "First_Strategy ";
		}
		strategy += "|";
		return strategy;
	}
	
	// inner calss area---------------------------------------
	public class fopjMap extends Player_Board implements Cloneable{
		int index;
		boolean isLegit;		//確認是否合法
//		boolean isRoot;
		fopjGrid[][] fopjmap;
//		boolean[][] fopjmapInOverlapping;
		private int consumeMine;
		private int consumeLattice;
		
		private fopjMap(int index, Player_Board pmap){
			super(pmap.getMapY(), pmap.getMapX(), pmap.getMineNumber());
			this.index = index;
			this.isLegit = true;
			this.fopjmap = new fopjGrid[pmap.getMapY()][pmap.getMapX()];
			this.consumeMine = 0;
			this.consumeLattice = 0;
		}

		private fopjMap(fopjMap fmap, fopjGrid grid, short assumptionValue, int index, Player_Board pmap){			//不是root
			this(index, pmap);
			for (int y = 0 ; y < pmap.getMapY() ; y++){		//複製地圖
				for (int x = 0 ; x < pmap.getMapX() ; x++){
					try {
						if (fmap.fopjmap[y][x] != null) this.fopjmap[y][x] = (fopjGrid)fmap.fopjmap[y][x].clone();
					}catch (CloneNotSupportedException cns){
						cns.printStackTrace();
					}
				}
			}
			this.fopjmap[grid.getY()][grid.getX()] = new fopjGrid(grid.getY(), grid.getX(), assumptionValue);
			this.consumeLattice = fmap.consumeLattice + 1;
			if (assumptionValue == CSP.isMine){
				this.consumeMine = fmap.consumeMine + 1;
			}else {
				this.consumeMine = fmap.consumeMine;
			}
			this.index = index;
		}
		
		private fopjMap(Player_Board pmap){	//froot，基本上等於複製當前盤面
			this(CSP.rootIndex, pmap);
			for (int y = 0 ; y < pmap.getMapY() ; y++){
				for (int x = 0 ; x < pmap.getMapX() ; x++){
					this.fopjmap[y][x] = new fopjGrid(y, x, pmap.getState(y, x), true);
				}
			}
		}

		private fopjMap(fopjMapLink link, Player_Board pmap, boolean isWantMap){		//overlapping專用
			this(CSP.rootIndex, pmap);
			for (fopjMap fmap : link.fmapLink){
				if (isWantMap == true){
					for (int y = 0 ; y < pmap.getMapY() ; y++){
						for (int x = 0 ; x < pmap.getMapX() ; x++){
							try {
								if (fmap.fopjmap[y][x] != null) this.fopjmap[y][x] = (fopjGrid)fmap.fopjmap[y][x].clone();
							}catch (CloneNotSupportedException cns){
								cns.printStackTrace();
							}
						}
					}
				}
				this.consumeLattice += fmap.getConsumeLattice();
				this.consumeMine += fmap.getConsumeMine();
			}
			
			if (isDebug == true){
//				System.out.println("this.consumeLattice = " + this.consumeLattice);
//				System.out.println("this.consumeMine = " + this.consumeMine);
//				System.out.println("pmap.getRemainInitialGrid() = " + pmap.getRemainInitialGrid());
//				System.out.println("pmap.getRemainMineNumber() = " + pmap.getRemainMineNumber());
			}
			
			if (this.consumeMine > pmap.getRemainMineNumber()
					|| ((pmap.getRemainInitialGrid() - this.consumeLattice) < (pmap.getRemainMineNumber() - this.consumeMine))
					|| (this.consumeLattice == pmap.getRemainInitialGrid() && this.consumeMine != pmap.getRemainMineNumber())){
				/*
				 * 假設的地圖所使用的地雷數目不能超過剩下的地雷數
				 * 假設後的空格數不能小於假設後的地雷數
				 * 當所有空格都被假設時，還有地雷還沒有被假設過
				 */
				
				if (this.consumeLattice > pmap.getRemainInitialGrid()){
//					假設的地圖所使用的空格數目不能超過剩下的空格數
					System.out.println("error in overlpping getNext");
				}
				
				this.isLegit = false;	//設定不合法
			}
			
			if (this.consumeLattice == pmap.getRemainInitialGrid()){
				if (this.consumeMine != pmap.getRemainMineNumber()){
					this.isLegit = false;	//設定不合法
				}
			}
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			fopjMap fmap = null;
			try {
				fmap = (fopjMap)super.clone();
				//因為fopjMap中的成員並非只有基本資料型別，所以需要深度clone
				fmap.fopjmap = new fopjGrid[this.getMapY()][this.getMapX()];
				
				for (int j = 0 ; j < this.getMapY() ; j++){
					for (int i = 0 ; i < this.getMapX() ; i++){
						if (this.fopjmap[j][i] != null){
							fmap.fopjmap[j][i] = (fopjGrid)this.fopjmap[j][i].clone();  
						}
					}
				}

			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return fmap;
		}
		
		public boolean isLegit() {
			return this.isLegit;
		}
		
		//專門是root在呼叫
		private boolean reRead(Player_Board pmap, fopjMapDatabase fmapLink){			//閱讀並比較地圖
			boolean fisChange = false;
			for (int y = 0 ; y < pmap.getMapY() ; y++){
				for (int x = 0 ; x < pmap.getMapX() ; x++){
					if (pmap.getState(y, x) != this.fopjmap[y][x].getState()){
						fisChange = true;
						this.fopjmap[y][x].setState(pmap.getState(y, x));

						for (int j = y - 2 ; j <= y + 2 ; j++){
							for (int i = x - 2 ; i <= x + 2 ; i++){
								try{
									if (this.isFOPJ(j, i) == true){
										fmapLink.removeIndex(this.fopjmap[j][i].getIndex(), pmap, this);	//地圖已改變，之前的假設需要刪除
									}
								}catch(ArrayIndexOutOfBoundsException aio){}
							}
						}
					}
				}
			}
			if (isDebug == true) {
//				System.out.println("isChange in reRead = " + fisChange);
			}
			return fisChange;
		}

		private void setAssumptionInRoot(fopjGrid grid, int index){		//新增了假設，記錄在root上
			this.fopjmap[grid.getY()][grid.getX()].setIndex(index);
		}
	
		private void setIllegal(){
			this.isLegit = false;
		}
	
		private int getConsumeLattice(){
			return this.consumeLattice;
		}
		
		private int getConsumeMine(){
			return this.consumeMine;
		}
		
		@Override
		public short getState(int y, int x){
			return this.fopjmap[y][x].getState();
		}
	
		private double getMinePossible(int y, int x, Player_Board pmap, long foverlappingTimes){
			return this.fopjmap[y][x].getMinePossible(pmap, foverlappingTimes);
		}
		
		private boolean isFOPJ(int y, int x){
			return (this.fopjmap[y][x].getIndex() > CSP.rootIndex) ? true : false;
		}
		
		private boolean isFOPJ(fopjGrid grid){
			return this.isFOPJ(grid.getY(), grid.getX());
		}

		private int getIndex() {
			return this.index;
		}
		
		private class fopjGrid extends Player_Grid implements Cloneable{
			private int index;
			private double possilbeTimes[];
			
			private fopjGrid(int mapY, int mapX, short state){
				this(mapY, mapX, state, false);
			}
			
			private fopjGrid(int mapY, int mapX, short state, boolean isRoot){		//root的grid
				super(mapY, mapX, state);
				if (isRoot == true){
					this.possilbeTimes = new double[11];
					this.index = CSP.rootIndex;
				}
			}
			
			private double getMinePossible(Player_Board pmap, long foverlappingTimes) {
				if (foverlappingTimes > 0){
					return (double)this.possilbeTimes[possibleJudge.isMine.getCode()];
				}else {
					return (double)pmap.getRemainMineNumber() / pmap.getRemainInitialGrid();
				}
				
			}

			public int getIndex() {
				return this.index;
			}
			
			@Override
			public Object clone() throws CloneNotSupportedException{
				fopjGrid grid = null;
				try {
					grid = (fopjGrid)super.clone();
					if (this.possilbeTimes != null)	{
						grid.possilbeTimes = (double[]) this.possilbeTimes.clone();
					}
				}catch (CloneNotSupportedException cse) {
					cse.printStackTrace();
				}
				return grid;
			}
			
			private void setIndex(int index){
				this.index = index;
			}
		}
	}
	
	private class simulationGrid {
		private double weight;
		private boolean isSimulationed;
		private simulationGrid(){
			this.weight = 0;
			this.isSimulationed = false;
		}
		
		public double getValue() {
			return this.weight;
		}

		private void setWeight(double point){
			this.weight += point;
		}
		
		private void setSimulationed(){
			this.isSimulationed = true;
		}
		
		private boolean isSimulation(){
			return this.isSimulationed;
		}
	}
	
	private class fopjMapLink implements Cloneable{
		private LinkedList<fopjMap> fmapLink;
		int index;
		boolean isCarried;	//已經進位過了嗎？
		int overlappingNumber;
		private fopjMapLink() {	//overlapping專用
			this.fmapLink = new LinkedList<fopjMap>();
		}		
		
		private fopjMapLink(int index, Player_Board pmap){
			this();
			this.index = index;
			this.fmapLink.addLast(new fopjMap(index, pmap));
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			fopjMapLink link = null;
			try {
				link = (fopjMapLink)super.clone();
				link.fmapLink = new LinkedList<fopjMap>();
				for (int number = 0 ; number < this.fmapLink.size() ; number++){
					link.fmapLink.addLast((fopjMap)this.fmapLink.get(number).clone());
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return link;
		}
		
		public void removeFirst() {
			this.fmapLink.removeFirst();
		}
		
		private void addLast(fopjMap pmap){
			this.fmapLink.addLast(pmap);
		}
		
		private fopjMap get(){
			return this.fmapLink.get(this.overlappingNumber);
		}
		
		private fopjMap getFirst(){
			return this.fmapLink.getFirst();
		}
		
		private int size(){
			return this.fmapLink.size();
		}
		
		private boolean isCarryied(){
			return this.isCarried;
		}
	
		private void setInitialOverlapping(){
			this.overlappingNumber = 0;
			this.isCarried = false;
		}
		
		private boolean isCarry(){		//是否要進位了
			return (this.overlappingNumber == this.fmapLink.size()) ? true : false;
		}
	}
		
	private class fopjMapDatabase implements Cloneable{
		private LinkedList<fopjMapLink> mapDatabase = new LinkedList<fopjMapLink>();

		private fopjMapLink getLast() {
			return this.mapDatabase.getLast();
		}

		private void addLast(fopjMapLink fopjMapLink) {
			this.mapDatabase.addLast(fopjMapLink);
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			fopjMapDatabase database = null;
			try {
				database = (fopjMapDatabase)super.clone();
				database.mapDatabase = new LinkedList<fopjMapLink>();
				for (int number = 0 ; number < this.mapDatabase.size() ; number++){
					database.mapDatabase.addLast((fopjMapLink)this.mapDatabase.get(number).clone());
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return database;
		}
		
		private void setOverlappingNumberPlusOne(){		//設定overlapping的數字
			for (int index = this.mapDatabase.size() - 1 ; index >= 0 ; index--){
				this.mapDatabase.get(index).overlappingNumber++;
				
				if (this.mapDatabase.get(index).isCarry() == true){
					this.mapDatabase.get(index).overlappingNumber = 0;
					this.mapDatabase.get(index).isCarried = true;
				}else {
					break;
				}
			}
		}
		
		private boolean isExistNext(boolean isGlobal){		//是否存在下一個組合
			if (isGlobal == true){
				/*
				 * case1. 已找完所有未知點，有複數的區塊且不是第一次重疊
				 * case2. 已找完所有未知點，只有一個區塊
				 */
				return (this.mapDatabase.getFirst().isCarryied() == false) ? true : false;
			}else {								//部份考量
				return (this.mapDatabase.getLast().isCarryied() == false) ? true : false;
			}
		}
		
		private long getOverlappingTimes(boolean isGlobal){
			long times = 1;
			if (isGlobal == true){
				for (int index = this.mapDatabase.size() - 1 ; index >= 0 ; index--){
					times *= this.mapDatabase.get(index).size();
				}
			}else {
				times = this.mapDatabase.getLast().size();
			}
			return times;
		}
		
		private long getNowOverlappingTimes(boolean isGlobal, boolean isWantMap){
			long times = 1;
			if (isGlobal == true && isWantMap == true){
				for (int index = this.mapDatabase.size() - 1 ; index >= 0 ; index--){
					if (index != this.mapDatabase.size()){
						times *= this.mapDatabase.get(index).overlappingNumber;
					}else {
						times += this.mapDatabase.get(index).overlappingNumber;
					}
				}
			}else if (isWantMap == true){
				times = this.mapDatabase.getLast().overlappingNumber;
			}
			return times+1;
		}
		
		private fopjMap getNext(boolean isGlobal, Player_Board pmap, boolean isWantMap){		//取得第一個組合
			fopjMapLink link = new fopjMapLink();
			int showTime = 10000;		//設定幾次才印出一次
			long nowTimes = this.getNowOverlappingTimes(isGlobal, isWantMap);
			String debugMes = "";
			
			if (isDebug == true && isWantMap == true){
//				System.out.println("times = " + nowTimes);
			}
			
			if (isGlobal == true){
				/*
				 * case1. 已找完所有未知點，有複數的區塊且不是第一次重疊
				 * case2. 已找完所有未知點，只有一個區塊
				 */
				
				for (int index = this.mapDatabase.size() - 1 ; index >= 0 ; index--){
					if (this.mapDatabase.getFirst().isCarryied() == false){
						if (isDebug == true && isSimulation == false) {
							if (nowTimes % showTime == 0){
								debugMes = (this.mapDatabase.get(index).overlappingNumber+1) + "/" + this.mapDatabase.get(index).size() 
										+ " in " + "\"" + this.mapDatabase.get(index).index + "\"" + " " + debugMes;
							}
//							System.out.print((this.mapDatabase.get(index).overlappingNumber+1) + "/" + this.mapDatabase.get(index).size() 
//									+ " in " + this.mapDatabase.get(index).index + " ");
						}
						link.addLast(this.mapDatabase.get(index).get());
					}
				}
				this.setOverlappingNumberPlusOne();
				
				if (isDebug == true && isSimulation == false){
					if (nowTimes % showTime == 0){
//						System.out.print("global ");
						debugMes = "global " + debugMes;
					}
				}
			}else {		//若並非將所有的組合都算出，就只要算最後的組合即可
				if (this.mapDatabase.getLast().isCarryied() == false){
					if (isDebug == true && isSimulation == false){
						if (nowTimes % showTime == 0){
							debugMes += "local " + (this.mapDatabase.getLast().overlappingNumber+1)
									 + "/" + this.mapDatabase.getLast().size() + " in " + "\"" + + this.mapDatabase.getLast().index + "\"";
						}
//						System.out.println("local " + (this.mapDatabase.getLast().overlappingNumber+1)
//								 + "/" + this.mapDatabase.getLast().size() + " in " + this.mapDatabase.getLast().index);
					}
					link.addLast(this.mapDatabase.getLast().get());
					this.setOverlappingNumberPlusOne();
				}
			}
			
			if (isDebug == true && isSimulation == false){
				if (nowTimes % showTime == 0){		//除非數目很大，否則不用output
					System.out.println(debugMes + " in the " + nowTimes + " times.");
				}
			}
			return (new fopjMap(link, pmap, isWantMap));
		}
		
		private boolean isEmpty(){
			return this.mapDatabase.isEmpty();
		}
		
		private void removeIndex(int index, Player_Board fmap, fopjMap froot){
			for (int number = this.mapDatabase.size() - 1 ; number >= 0 ; number--){
				if (this.mapDatabase.get(number).getFirst().getIndex() == index){
					for (int y = 0 ; y < fmap.getMapY() ; y++){
						for (int x = 0 ; x < fmap.getMapX() ; x++){
							if (this.mapDatabase.get(number).getFirst().fopjmap[y][x] != null){
								froot.fopjmap[y][x].setIndex(CSP.rootIndex);
							}
						}
					}
					this.mapDatabase.remove(number);
					break;
				}
			}
		}
		
		private void setInitialOverlapping(){		//重疊時所需要的初始化
			for (fopjMapLink link : this.mapDatabase){
				link.setInitialOverlapping();
			}
		}
	}
	
	private class mineDestinyRatio{  //計算地雷密度所影響的比值
		LinkedList <mineDestinyRatioNode> mLink = new LinkedList <mineDestinyRatioNode>();
		BigDecimal allPossibleN = BigDecimal.valueOf(0);
		
		private void add (Player_Board pmap, fopjMap fmap){
			if (this.mLink.size() > 0){
				for (int index = 0 ; index < this.mLink.size() ; index++){
					if (fmap.consumeMine == this.mLink.get(index).consumeMine){
						this.mLink.get(index).add();		//增加次數
						break;
					}else if (fmap.consumeMine < this.mLink.get(index).consumeMine){
						this.mLink.add(index, new mineDestinyRatioNode(pmap, fmap));
						break;
					}else if (index == this.mLink.size() - 1){
						this.mLink.addLast(new mineDestinyRatioNode(pmap, fmap));
						break;
					}
				}
			}else {
				this.mLink.addLast(new mineDestinyRatioNode(pmap, fmap));
			}
		}
		
		private void countPossible(Player_Board pmap){
			for (int index = 0 ; index < this.mLink.size() ; index++){
				this.mLink.get(index).count(pmap);
				this.allPossibleN = this.allPossibleN.add(this.mLink.get(index).allPossibleCases);
			}
			
			for (int index = 0 ; index < this.mLink.size() ; index++){
				this.mLink.get(index).countPossible(this.allPossibleN);
			}
		}
		
		private double getPossible(int consumeMine){
			double possible = 0;
			for (int index = 0 ; index < this.mLink.size() ; index++){
				if (this.mLink.get(index).consumeMine == consumeMine){
					possible = this.mLink.get(index).possible;
				}
			}
			return possible;
		}
		
		private void print(){
			System.out.println("size = " + this.mLink.size());
			for (int index = 0 ; index < this.mLink.size() ; index++){
				System.out.print("remainL = " + this.mLink.get(index).remainL + "\t");
				System.out.print("remainM = " + this.mLink.get(index).consumeMine + "\t");
				System.out.print("size = " + this.mLink.get(index).size + "\t");
				System.out.println("possible = " + this.mLink.get(index).possible + "\t");
			}
		}
		
		private class mineDestinyRatioNode{
			int remainL;
			int consumeMine;
			int size;
			BigDecimal allPossibleCase;		//有使用fmap.consumeMine的其中一種可能性
			BigDecimal allPossibleCases;		//有使用fmap.consumeMine的所有可能性
			double possible;			//有使用fmap.consumeMine的機率
			private mineDestinyRatioNode(Player_Board pmap, fopjMap fmap){
				this.remainL = pmap.getRemainInitialGrid() - fmap.consumeLattice;
				this.consumeMine = fmap.consumeMine;
				this.size = 1;
			}
			
			private void add(){
				this.size++;
			}
			
			private void count(Player_Board pmap){
				this.allPossibleCase = CSP.conbineNumber(this.remainL, pmap.getRemainMineNumber() - this.consumeMine);
				this.allPossibleCases = this.allPossibleCase.multiply(BigDecimal.valueOf(this.size)); 
			}
			
			private void countPossible(BigDecimal allPossibleN){
				// 參考網站：http://blog.sina.com.cn/s/blog_6ac6965a0100nod5.html
				this.possible = this.allPossibleCase.divide(allPossibleN, 16, BigDecimal.ROUND_HALF_UP).doubleValue();	//保留小數點後十六位(與double相同)，再四捨五入，再轉成double
			}
		}
	}
	
	
	// ---------------------------------------inner calss area
	
	
}
