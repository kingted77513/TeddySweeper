package ai;

import java.math.BigDecimal;
import java.util.LinkedList;
import game.Board;
import game.Experiment;
import game.Player_Board;
import game.Player_Board.Player_Grid;
import game.UI;

public final class TeddyMethod extends AI{
	final private static boolean isMine = true;					// 是地雷
	final private static boolean notMine = false;				// 非地雷
	final private boolean isDebug = false;								//是否要輸出除錯訊息
	
	// 輔助瞭解目前狀況的變數
	public static BigDecimal total_useCombineTimes;					// 查看總共使用幾次分區化
	public static BigDecimal total_combineTimes;					// 查看總共的組合次數是多少
	private static BigDecimal total_copyMap;						// 總共總共的儲存了多少地圖
	private static int single_combineTimes;							// 單次組合次數
	private static long single_combineLimit = Long.MAX_VALUE;		// 單次組合次數的限制
	private static boolean isCombineLimit;							// 組合次數是否有大於限制
	private static long startTime;									// 此局開始的時間
	private static long lastShowTime;								// 上一次印出資訊的時間
	final private static long showTimePeriod = 1000;						// 印出資訊的周期
	
	// 參數化調整，參考筆記38 20130802
	private double value_of_SP = 20;
	private double value_of_EQ = 15;
	private double value_of_CM = 10;
	private double value_of_Other = 5;
	private double value_of_NoAns = 0; 
	
//	private static boolean condition2;
//	private static boolean condition3;
//	private static boolean condition145;
	
	// 是否為全解的題目
	private boolean isGuess;							// 是否有猜側過

	// 預知猜測，參考筆記27 20121230
	private boolean isUseGuessMethod_forecast = true;			// 是否有使用預知猜測
	final private boolean isUseGuessMethod_forecast_allPossible = true;			// 是否算所有機率
	final private boolean isUseGuessMethod_forecast_maxPossible = false;			// 是否算最大機率
	final private boolean isUseGuessMethod_forecast_stepValue = true;			// 是否評估階段性分數
	final private boolean isUseGuessMethod_forecast_answerValue = false;			// 是否評估答案數
	final private boolean isUseGuessMethod_forecast_step_and_answer_Value = false;	// 是否評估階段性分數乘於答案數
	final private boolean isUseCombineInEvaluateJudge = true;						// 是否有判斷是否應該在評估時進行combine的動作
	private boolean isNeedCombineInSimulation;								// 是否在模擬時進行combine的動作

	// 首步指定
	final private boolean isUseFirstStrategy = false;					// 是否有使用首步指定策略
	final private static String firstMoveIsCorner = "corner";		// 第一步踩角落
	final private static String firstMoveIsEdge = "edge";			// 第一步踩邊
	final private static String firstMoveIsCenter = "center";		// 第一步踩中央
	final private static String firstMoveIsRandom = "random";		// 第一步隨機 
	final private static String firstMoveIsUser = "user";
	final private static String firstStep = TeddyMethod.firstMoveIsUser;
	
	// 使用者可以自訂第一步要踩那裡
	private int firstX;
	private int firstY;

	// 區域化
	private boolean isUseBlockCombine = true;					// 是否有使用區域化策略
	private boolean isUseBlockCombine_unique = false;			// 是否有使用獨立區域化策略
	private boolean isUseBlockCombine_complete = true;			// 是否有使用完全分區化策略
	private final boolean isUseBlockCombine_choice = false;			// 是否有使用分區化選擇策略

	// 其他輔助功能
	final private boolean isUseRereadMap = true;						// 是否有使用重讀地圖功能
	final private boolean isUseRereadCombineMap = false;					// 是否有使用重讀組合地圖的功能
	private boolean isUseFindCrapsShoot = true;				// 是否有使用找到craps shoot的功能
	
	private Teddy_Map root;
	private Answer answer;
	private CombineMap last_com_Map;		// 上一次的combineMap
	private CombineMap com_Map;				
	
	private boolean isUseGuessMethod;		// 是否有用猜的方法
	private boolean isSimulation;			// 是否是在模擬中
	private double digValue;
	
	public TeddyMethod(Player_Board playerMap, double value_SP, double value_EQ, double value_CM, double value_Other, double value_NoAns){
		this(playerMap);
		this.value_of_SP = value_SP;
		this.value_of_EQ = value_EQ;
		this.value_of_CM = value_CM;
		this.value_of_Other = value_Other;
		this.value_of_NoAns = value_NoAns;
//		System.out.println("this is TeddyMethod.adjust " + this.value_of_SP + " " + this.value_of_EQ + " " + this.value_of_CM + " " + this.value_of_Other + " " + this.value_of_NoAns);
	}

	public TeddyMethod(Player_Board playerMap) {		// 一般用的建構子
		super(playerMap);
//		TeddyMethod.mapOutputer = caller;
		this.root = new Teddy_Map(this.map);
		this.answer = new Answer(this.map);
		this.isUseGuessMethod = false;
		this.isSimulation = false;
		this.digValue = Integer.MIN_VALUE;
		this.isGuess = false;
		TeddyMethod.total_useCombineTimes = BigDecimal.valueOf(0);
		TeddyMethod.total_combineTimes = BigDecimal.valueOf(0);
		if (Experiment.isShowNowSitulation == true){
			TeddyMethod.startTime = System.currentTimeMillis();		//取得系統時間
			TeddyMethod.lastShowTime = TeddyMethod.startTime;
			if (this.isUseBlockCombine == true && this.isUseBlockCombine_complete == true){
				TeddyMethod.total_copyMap = BigDecimal.valueOf(0);
			}
		}
	}

	protected TeddyMethod(Player_Board sMap, Teddy_Map lastRoot, CombineMap lastCombineMap, boolean isNeedCombine){	//模擬時用的建構子
		super(sMap);
//		TeddyMethod.mapOutputer = caller;
		this.root = lastRoot;
		this.last_com_Map = lastCombineMap;
		this.answer = new Answer(this.map);
		this.isUseGuessMethod = false;		//模擬不用猜
		this.isUseGuessMethod_forecast = false;	// 也不用預測
		this.isUseFindCrapsShoot = false;		// 也不用找craps shoot
		this.isSimulation = true;
		this.digValue = 0;
		this.isNeedCombineInSimulation = isNeedCombine;
		if (this.isDebug == true){
			//			System.out.println("show simulation map in CommonField");
			//			this.map.showMap();
		}
	}

//	public TeddyMethod(Player_Board sMap) {		//fopj專用
//		super(sMap);
////		TeddyMethod.mapOutputer = null;
//		this.root = new CF_Map(this.map);
//		this.answer = new Answer(this.map);
//		this.isUseGuessMethod = false;
//		this.isSimulation = false;
//		this.digValue = Integer.MIN_VALUE;
//	}

	// 結合Oliver的程式專用的程式-----------------------------------
	public static double[][][] getPossibleMapForOliverProgram(int mapY, int mapX, int mineNumber, int[][] showMap, int combineLimit){
		Player_Board showBoard = new Player_Board(mapY, mapX, mineNumber, showMap);
//		System.out.println("show map in getPossibleMapForOliverProgram");
//		showBoard.showMap();
		TeddyMethod test = new TeddyMethod(showBoard, combineLimit);
		test.isUseGuessMethod = true;
		test.select();
		if (test.answer.isCheckAnswer() == true){
//			System.out.println("警告！因為此題找得到絕對點，所以傳回的數字機率地圖是無效的！");
			double[][][] possibleMap = new double[11][mapY][mapX];
			for (int recordType = 0 ; recordType < 11 ; recordType ++){
				for (int y = 0 ; y < mapY ; y++){
					for (int x = 0 ; x < mapX ; x++){
						possibleMap[recordType][y][x] = -1;
					}
				}
			}
			
			if (test.answer.noMinePoint != null){
				for (Player_Grid g : test.answer.noMinePoint){
					//	System.out.println("警告1！");
					possibleMap[9][g.getY()][g.getX()] = 0;
				}
			}

			if (test.answer.isMinePoint != null){
				for (Player_Grid g : test.answer.isMinePoint){
//					System.out.println("警告2！");
					possibleMap[9][g.getY()][g.getX()] = 1;
				}
			}
			return possibleMap;
		}else {
			return test.com_Map.rMap.possibleMap;
		}
	}
	
	public TeddyMethod(Player_Board playerMap, int combineLimit) {
		this(playerMap);
		TeddyMethod.single_combineLimit = combineLimit;
		TeddyMethod.isCombineLimit = false;
	}
	
	// 回傳組合次數是否大於限制
	public static boolean isCombineAboveLimit(){
		return TeddyMethod.isCombineLimit;
	}
	// -----------------------------------結合Oliver的程式專用的程式
	
	// 自製的解構式
    public void finalize_By_Teddy(){
    	if (this.root != null){
    		((Teddy_Map)this.root).finalize_By_Teddy();
    		this.root = null;
    	}
    	
    	if (this.answer != null){
    		this.answer.finalize_By_Teddy();
    		this.answer = null;
    	}
		
    	if (this.com_Map != null){
    		this.com_Map.finalize_By_Teddy();
    		this.com_Map = null;
    	}
    	
		this.last_com_Map = null;
//		TeddyMethod.mapOutputer = null;
    } 
    
	@Override
	public String answer() {
//		TeddyMethod.condition145 = false;
//		TeddyMethod.condition2 = false;
//		TeddyMethod.condition3 = false;
		
		// 如果是棋面完全沒有開啟過，使用首步指定策略
		if (this.isUseFirstStrategy == true && this.map.getRemainInitialGrid() == (this.map.getMapY() * this.map.getMapX())){
			if (TeddyMethod.firstStep.equals(TeddyMethod.firstMoveIsCorner) == true){		// 使用第一步等於角落
				this.firstX = 0;
				this.firstY = 0;
			}else if (TeddyMethod.firstStep.equals(TeddyMethod.firstMoveIsEdge) == true){		// 使用第一步等於邊
				this.firstX = this.map.getMapX()/2;
				this.firstY = 0;
			}else if (TeddyMethod.firstStep.equals(TeddyMethod.firstMoveIsCenter) == true){		// 使用第一步等於中央
				this.firstX = this.map.getMapX()/2;
				this.firstY = this.map.getMapY()/2;
			}else if (TeddyMethod.firstStep.equals(TeddyMethod.firstMoveIsRandom) == true){		// 使用第一步隨機
				this.firstX = (int)(Math.random()*this.map.getMapX());
				this.firstY = (int)(Math.random()*this.map.getMapY());
			}else if (TeddyMethod.firstStep.equals(TeddyMethod.firstMoveIsUser) == true){
				this.firstX = 1;
				this.firstY = 0;
			}
			
			this.answer.add(this.map.getGrid(this.firstY, this.firstX), TeddyMethod.notMine);
		}

		this.select();

		if (Experiment.isLogicTest == true){
			if (this.isUseGuessMethod == true && this.order.equals("no_answer")){	// 檢查是否在猜測模式下，卻沒有找出適合的地方。
				System.out.println("error_in_answer, no answer in guess model!");
				if (Experiment.isStepByStepDebug == true){
					UI.safeMap_In_debug("error_in_answer_因為在猜測模式下卻沒有答案");
				}
			}
		}

		return this.order;
	}
	
	public boolean isGuess(){						// 回傳是否有猜過
		return this.isGuess; 
	}
	
	public boolean isUseGuess(){					// 回傳是否有使用猜測方法
		return this.isUseGuessMethod;
	}

	public void setGuess(boolean isSet){		//設定是否使用猜測方法
		this.isUseGuessMethod = isSet;
	}

	private double get_simulation_digValue(){		// 傳回階段性分數
		this.select();
		return this.digValue;
	}

	private int get_simulation_answerNumber(){	// 傳回答案數目
		this.select();
		return this.answer.getAnswerNumber();
	}
	
	private double get_simulation_step_and_answerValue(){
		this.select();
		return this.digValue * this.answer.getAnswerNumber();
	}

	@Override
	public boolean isAnswer() {
//		this.select();
		return this.answer.isCheckAnswer();
	}
	
	@Override
	public double[][] getHintMap(){
		double[][] hint;
		
		if (this.answer.isCheckAnswer() == true){
			hint = new double[this.map.getMapY()][this.map.getMapX()];
			for (int y = 0 ; y < this.map.getMapY() ; y++){
				for (int x = 0 ; x < this.map.getMapX() ; x++){
					hint[y][x] = Board.initial;
				}
			}
			
			for (Player_Board.Player_Grid g : this.answer.noMinePoint){
				hint[g.getY()][g.getX()] = 0;
			}
			
			for (Player_Board.Player_Grid g : this.answer.isMinePoint){
				hint[g.getY()][g.getX()] = 1;
			}
		}else {
			hint = this.com_Map.rMap.possibleMap[9];
		}
		
		return hint;
	}
	
	public double[][] getDigValueMap(){
		double[][] digValue;
		
		if (this.answer.isCheckAnswer() == false){
			digValue = this.com_Map.rMap.possibleMap[10];
		}else {
			digValue = new double[this.map.getMapY()][this.map.getMapX()];
		}
		
		return digValue;
	}

	private void select(){
		this.answer.checkExistAnswer();
		if ((this.isUseGuessMethod == false && this.answer.isCheckAnswer() == false) 
				|| (this.isUseGuessMethod == true && this.answer.isExistAnswer() == false)){
			this.search();
		}

		if (this.isSimulation == false){
			this.order = this.answer.getAnswer(this.isUseGuessMethod);
			if (this.isGuess == false && this.answer.isCheckAnswer() == false && this.answer.isExistAnswer() == true 
					&& this.map.getRemainInitialGrid() < (this.map.getMapY() * this.map.getMapX())){
				this.isGuess = true;
//				System.out.println("此題有使用猜測");
			}
		}
	}

	private void search(){
		if (this.isSimulation == true){
			this.digValue = this.value_of_NoAns;		// 初始化
		}

		// ***Read board step***
		if (this.isUseRereadMap == true){
			this.root.reRead(this.map, this.answer);
		}else {
			this.root = new Teddy_Map(this.map);
			//			this.root.reRead(this.map, this.answer);
		}


		if (this.answer.isCheckAnswer() == false){
			// ***Extending and create set step***
			this.cf_Extending();
		}

		if (this.isSimulation == true && this.digValue == this.value_of_NoAns && this.answer.isCheckAnswer() == true){	// 如果找到答案的話，給予權重
			this.digValue = this.value_of_SP;
		}

		if (this.answer.isCheckAnswer() == false){
			// ***CF_Judge***
			this.cf_Judge();
		}

		if (this.isSimulation == true && this.digValue == this.value_of_NoAns && this.answer.isCheckAnswer() == true){	// 如果找到答案的話，給予權重
			this.digValue = this.value_of_EQ;
		}

		if (this.answer.isCheckAnswer() == false){
			// ***CM_Judge***
			this.cm_Judge();
		}
		
		if (this.isSimulation == true && this.digValue == this.value_of_NoAns && this.answer.isCheckAnswer() == true){	// 如果找到答案的話，給予權重
			this.digValue = this.value_of_CM;
		}

		if (this.answer.isCheckAnswer() == false){	// 如果找到答案的話，給予權重
			if (this.isDebug == true && this.isSimulation == false){
				//				System.out.println("show player_board map before CM_Guess");
				//				this.map.showMap();
			}
			
			// 當使用isUseCombineInEvaluateJudge功能時，只有當isNeedCombineInSimulation是true，即判斷需要組合地圖才進行組合
			if (this.isSimulation == false || (this.isSimulation == true 
					&& ((this.isUseCombineInEvaluateJudge == false)	
							|| (this.isUseCombineInEvaluateJudge == true && this.isNeedCombineInSimulation == true)))){
				this.cm_Guess();
			}
		}

		if (this.isSimulation == true && this.digValue == this.value_of_NoAns && this.answer.isCheckAnswer() == true){
			this.digValue = this.value_of_Other;
		}

		if (this.isDebug == true){
			//			System.out.println("before judge---");
			//			this.showMap(root, "cf");
			//			this.showMap(root, "state");
			//			this.root.showSet();
			//			System.out.println("---before judge");
		}
	}

	private void cm_Guess(){		//計算所有點的機率並選擇最好的一點
		if (this.isDebug == true && this.isSimulation == false){
			System.out.println("cm_Guess start");
		}

		LinkedList<Teddy_Map.CF_Grid> clink = new LinkedList<Teddy_Map.CF_Grid>();
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				if (this.map.isInformationGrid(y, x) == true && this.root.is_isAssumption(y, x) == true){
					clink.addLast(this.root.get_cfGrid(y, x));
				}
			}
		}

		if (this.isDebug == true && this.isSimulation == false){
			//			System.out.println("show clink of cm_Guess");
			//			for (cfMap.cfGrid cgrid : clink){
			//				System.out.print("(" + cgrid.getCoordinate() + "), ");
			//			}
			//			System.out.println("");

			if (clink.isEmpty() == false){
				//				UI.safeMap_In_debug(this.caller, "combine_map");
			}
		}
		
		// 分區化選擇策略
		if (this.isSimulation == false && this.isUseBlockCombine == true && this.isUseBlockCombine_choice == true){
			if (this.isUseGuessMethod == true && this.isUseGuessMethod_forecast == true){
				if (this.isSimulation == false && this.isUseRereadCombineMap == true){
					if (this.isUseBlockCombine == true && this.isUseBlockCombine_complete == true){
						this.last_com_Map = this.com_Map;
					}else {
						this.last_com_Map = null;
					}
				}
				
				// 先改成非評價時的完全分區化策略，以方便作業
				this.isUseBlockCombine = true;
				this.isUseBlockCombine_unique = false;
				this.isUseBlockCombine_complete = true;
				boolean reg[] = {this.isUseGuessMethod, this.isUseGuessMethod_forecast};
				this.isUseGuessMethod = false;
				this.isUseGuessMethod_forecast = false;
				CombineMap choiceMap = new CombineMap(clink, this);
				choiceMap.combine_for_blockStategy_choice();
//				System.out.println("所有區域有" + choiceMap.getSumOfArea());
//				System.out.println("獨立區域有" + choiceMap.getSumOfIndependentArea());
//				System.out.println("相依區域有" + choiceMap.getSumOfDependentArea());

				/*
				 * 根據盤面使用不同的分區方法。
				 * 如果是只有一個區域 -> 非分區策略
				 * 如果是只有一個非獨立區域 -> 獨立分區策略
				 * 如果有兩個或以上的非獨立區域 -> 完全分區策略
				 */

				if (choiceMap.getSumOfArea() == 1){
//					System.out.println("此盤面適合使用非分區化策略");
					this.isUseBlockCombine = false;
					this.isUseBlockCombine_unique = false;
					this.isUseBlockCombine_complete = false;
					if (this.isSimulation == false && this.isUseRereadCombineMap == true){
						this.last_com_Map = null;
					}
				}else if (choiceMap.getSumOfDependentArea() == 1){
//					System.out.println("此盤面適合使用獨立分區化策略");
					this.isUseBlockCombine = true;
					this.isUseBlockCombine_unique = true;
					this.isUseBlockCombine_complete = false;
					if (this.isSimulation == false && this.isUseRereadCombineMap == true){
						this.last_com_Map = null;
					}
				}else {
//					System.out.println("此盤面適合使用完全分區化策略");
					this.isUseBlockCombine = true;
					this.isUseBlockCombine_unique = false;
					this.isUseBlockCombine_complete = true;
//					if (this.isSimulation == false && this.isUseRereadCombineMap == true){
//						this.last_com_Map = this.com_Map;
//					}
				}
				
				// 設定還原
				this.isUseGuessMethod = reg[0];
				this.isUseGuessMethod_forecast = reg[1];
				choiceMap.finalize_By_Teddy();	// 解構子
			}else {
				// 沒有評價時一律使用完全分區化比較有利，可以省去判斷何種分區化策略的時間
				this.isUseBlockCombine = true;
				this.isUseBlockCombine_unique = false;
				this.isUseBlockCombine_complete = true;
			}
		}else if (this.isSimulation == false && this.isUseRereadCombineMap == true){
			if (this.isUseBlockCombine == true && this.isUseBlockCombine_complete == true){
				this.last_com_Map = this.com_Map;
			}else {
				this.last_com_Map = null;
			}
		}
		
		this.com_Map = new CombineMap(clink, this);
		this.com_Map.combine();		// 組合地圖並計算機率
		
		// 當使用craps shoot功能時，沒有找到答案才去求最小地雷機率中最有價值的點
		if (this.isUseFindCrapsShoot == false || (this.isUseFindCrapsShoot == true && this.answer.isExistAnswer() == false)){
			if (this.isDebug == true){
				System.out.println("show mine possible map in cm_Guess");
				this.com_Map.showMap("possible", 9);
			}
			this.com_Map.getBests(this.answer);
			if (this.isDebug == true && this.isSimulation == false){
				System.out.println("show record map in cm_Guess");
				for (int index = 0 ; index <= 9 ; index++){
					if (this.isUseGuessMethod_forecast == true || (this.isUseGuessMethod_forecast == false && index == 9)){
						this.com_Map.showMap("record", index);
					}
				}

				System.out.println("show possible map in cm_Guess");
				for (int index = 0 ; index <= 10 ; index++){
					if (this.isUseGuessMethod_forecast == true || (this.isUseGuessMethod_forecast == false && index == 9)){
						this.com_Map.showMap("possible", index);
					}
				}
				System.out.println("cm_Guess end");
			}
		}
	}
	
	private void cf_Extending(){
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				if (this.map.isInformationGrid(y, x) == true && this.root.is_isSetCF(y, x) == false){		//只找未設定過的資訊點
					this.root.get_cfGrid(y, x).set_remainMine((this.map.getState(y, x) - this.map.getBesireFlagNumber(y, x)));	//設定地雷殘量
					this.root.get_cfGrid(y, x).setCF(this.map, this.root, this.answer);
				}
			}
		}
	}

	private void cf_Judge(){
		boolean isAllJudge;
		do {
			isAllJudge = true;
			if (this.isDebug == true){
				//				System.out.println("judge start");				
			}

			for (int y = 0 ; y < this.map.getMapY() ; y++){
				for (int x = 0 ; x < this.map.getMapX() ; x++){

					if (this.map.isUnknownGrid(y, x) == true && this.root.is_isJudge(y, x) == false){		//只找有集合且未經過judge的點的點
						if (this.isDebug == true){
							//							((cfMap.cfGrid)this.root.playerGridMap[y][x]).showSet();
						}

						this.cf_Judge(this.root.get_cfGrid(y, x));

						if (this.isDebug == true){
							//							this.root.get_cfGrid(y, x).showSet("cf");
							//							System.out.println(x + " " + y + " boolean in judge = " + ((cfMap.cfGrid)this.root.playerGridMap[y][x]).isJudge());
						}

						isAllJudge &= this.root.get_cfGrid(y, x).is_isJudge();		//確認是否每一個都確實經過Judge了
					}
				}
			}

			if (this.isDebug == true){
				//				this.showMap(root, "cf");
				//				System.out.println("judge end");
			}
		}while (isAllJudge == false);
	}

	private void cf_Judge(Teddy_Map.CF_Grid cgrid){
		CF_GridSet differenceSet = null;
		boolean isAddSuc;

		//由個數大的集合與個數少的集合去找出新的資訊集合，若個數少的集合為個數大的集合的子集合，其差集就是為新的資訊集合。
		if (cgrid.cfLink.size() > 1){		// 只檢查階層大於一的點
			for (int highLevel = 0 ; highLevel < cgrid.cfLink.size() - 1 ; highLevel++){
				for (int lowLevel = highLevel + 1 ; lowLevel < cgrid.cfLink.size() ; lowLevel++){
					for (int bigSet = 0 ; bigSet < cgrid.cfLink.get(highLevel).size() ; bigSet++){
						for (int smallSet = 0 ; smallSet < cgrid.cfLink.get(lowLevel).size() ; smallSet++){
							// 小的集合必須是大集合的子集合
							if (this.isSubset(cgrid.cfLink.get(highLevel).get(bigSet), cgrid.cfLink.get(lowLevel).get(smallSet)) == true){
								// 注意差集的地雷數可能為負數
								differenceSet 
								= this.getDifferenceSet(cgrid.cfLink.get(highLevel).get(bigSet), cgrid.cfLink.get(lowLevel).get(smallSet));

								if (Experiment.isLogicTest == true){
									if (differenceSet.size() == 0){
										System.out.println("error_in_cf_Judge，因為差集的集合的數目為零");
										if (Experiment.isStepByStepDebug == true){
											UI.safeMap_In_debug("error_in_cf_Judge_因為差集的集合的數目為零");
										}
									}
								}

								if (differenceSet.get_RemainMine() >= 0){
									isAddSuc = cgrid.addGridSet(differenceSet);	//加入其集合
									if (isAddSuc == true){		//如果成功的產生新差集，必須加到所影響的範圍。
										differenceSet.simple_Judge(this.answer);		//判斷尋找是否有新解答
										cgrid.set_isJudge(false);
										for (Player_Grid ggrid : differenceSet.gridLink){
											isAddSuc = this.root.get_cfGrid(ggrid).addGridSet(differenceSet);
											if (isAddSuc == true) {		// 加入成功的話，就需要再進行判斷
												this.root.get_cfGrid(ggrid).set_isJudge(false);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}else {
			if (Experiment.isLogicTest == true){
				if (cgrid.cfLink.size() == 0){
					System.out.println("error_in_cf_Judge，因為cgrid.cfLink個數為零");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_cf_Judge_因為cgrid.cfLink個數為零");
					}
				}
			}
		}

		cgrid.set_isJudge(true);
	}

	private void assumption(Teddy_Map.CF_Grid grid){		//假設
		boolean isLastGrid;
		LinkedList<Teddy_Map.CF_Grid> blink = this.get_unknownGrid(grid);

		if (this.isDebug == true){
//			System.out.println(grid.getCoordinate() + " in assumption at first---");
//			System.out.print("[");
//			for (int index = 0 ; index < blink.size() ; index++){
//				System.out.print("(" + blink.get(index).getCoordinate() + "), ");
//			}
//			System.out.println(grid.get_remainMine() + "]");
		}

		for (int index = 0 ; index < blink.size() ; index++){
			isLastGrid = (index == (blink.size() - 1)) ? true : false; 	//是否是最後一個 
			grid.addCM(this.map, blink.get(index), isLastGrid);
		}

		grid.set_isAssumption(true);

		if (this.isDebug == true){
//			System.out.println(grid.getCoordinate() + " in assumption before deMistake");
//			System.out.println(grid.cmLink.showSet());
		}

		grid.deMistake(this.map, this.root);		//刪掉不合法的組合

		if (this.isDebug == true){
//			System.out.println(grid.getCoordinate() + " in assumption after deMistake");
//			System.out.println(grid.cmLink.showSet());
		}

		grid.little_overlapping(this.map, this.answer);
	}

	private LinkedList<Teddy_Map.CF_Grid> get_unknownGrid(Teddy_Map.CF_Grid grid){	//得到某點旁邊的未知點，不包括已插旗的地方
		LinkedList<Teddy_Map.CF_Grid> blink = new LinkedList<Teddy_Map.CF_Grid>();

		for (int j = grid.getY() - 1 ; j <= grid.getY() + 1 ; j++){
			for (int i = grid.getX() - 1 ; i <= grid.getX() + 1 ; i++){
				if (i != grid.getX() || j != grid.getY()){		//中間的點不用理
					if (this.map.isInBoard(j, i) == true && this.map.isInitial(j, i) == true){
						blink.addLast(this.root.get_cfGrid(j, i));
					}
				}
			}
		}
		return blink;
	}

	private void cm_Judge(){		//擴展
		for (int y = 0 ; y < this.map.getMapY() ; y++){
			for (int x = 0 ; x < this.map.getMapX() ; x++){
				if (this.map.isInformationGrid(y, x) == true && this.root.is_isAssumption(y, x) == false){	// 只找沒有假設過的資訊點
					this.root.get_cfGrid(y, x).set_remainMine((this.map.getState(y, x) - this.map.getBesireFlagNumber(y, x)));
					this.assumption(this.root.get_cfGrid(y, x));
				}
			}
		}
	}

	private CF_GridSet getDifferenceSet(CF_GridSet bigSet, CF_GridSet smallSet){		//求差集
		CF_GridSet differenceSet = new CF_GridSet(bigSet.get_RemainMine() - smallSet.get_RemainMine());
		boolean isExist;

		for (int bigIndex = 0 ; bigIndex < bigSet.size() ; bigIndex++){
			isExist = false;
			for (int smallIndex = 0 ; smallIndex < smallSet.size() ; smallIndex++){
				if (smallSet.get(smallIndex).equals(bigSet.get(bigIndex)) == true){
					isExist = true;
					break;
				}
			}
			if (isExist == false){					//bigSet若有集合不是smallSet中，則新增新的集合
				differenceSet.add(bigSet.get(bigIndex));
			}
		}
		if (Experiment.isLogicTest == true){
			if (differenceSet.gridLink.size() == 0){
				System.out.println("error_in_getDifferenceSet，因為差集的gridLink數目為零");
				if (Experiment.isStepByStepDebug == true){
					UI.safeMap_In_debug("error_in_getDifferenceSet_因為差集的gridLink數目為零");
				}
			}
		}

		return differenceSet;
	}

	private boolean isSubset(CF_GridSet bigSet, CF_GridSet smallSet){		//smallSet是否是bigSet的子集合
		boolean isSub = true;
		boolean isExist;
		for (int smallIndex = 0 ; smallIndex < smallSet.size() ; smallIndex++){
			isExist = false;
			for (int bigIndex = 0 ; bigIndex < bigSet.size() ; bigIndex++){
				if (smallSet.get(smallIndex).equals(bigSet.get(bigIndex)) == true){
					isExist = true;
					break;
				}
			}
			isSub &= isExist;
			if (isSub == false) break;
		}
		return isSub;
	}

	@Override
	public void showMap(Board showmap, String kind){
		if (showmap instanceof Teddy_Map){
			Teddy_Map cfboard = (Teddy_Map) showmap;
			for (int y = cfboard.getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < cfboard.getMapX() ; x++){
					if (kind.equals("cf")) {
						if (cfboard.is_isSetCF(y, x) == false){
							System.out.print("#" + "\t");
						}else if (cfboard.is_isJudge(y, x) == true){
							System.out.print("O" + "\t");
						}else if (cfboard.is_isJudge(y, x) == false){
							System.out.print("X" + "\t");
						}
					}else if (kind.equals("state"))	{
						if (cfboard.getGrid(y, x) == null || cfboard.getState(y, x) == Board.initial){
							System.out.print("#" + "\t");
						}else if (cfboard.getState(y, x) == Board.flag){
							System.out.print("P" + "\t");
						}else if (cfboard.getState(y, x) == Board.mine){
							System.out.print("@" + "\t");
						}else {
							System.out.print(cfboard.getState(y, x) +"\t");
						}
					}
				}
				System.out.println("");
			}
			System.out.println("");
		}
	}

	@Override
	public String getStrategy(String strategy) {
		strategy += "| Common Field |";

		if (this.isUseGuessMethod == true){		// 有使用猜測
			strategy += "CF_GuessMethod ";
			if (this.isUseGuessMethod_forecast == true){	// 有使用預測猜測
				strategy += "Guess_forecast_Method ";
				if (this.isUseGuessMethod_forecast_allPossible == true){	// 預測所有的數字機率
					strategy += "isUseGuessMethod_forecast_allPossible ";
				}else if (this.isUseGuessMethod_forecast_maxPossible == true){	// 預測最高的數字機率
					strategy += "isUseGuessMethod_forecast_maxPossible ";
				}

				if (this.isUseGuessMethod_forecast_stepValue == true){			// 評估階段性分數
					strategy += "isUseGuessMethod_forecast_stepValue ";
				}else if (this.isUseGuessMethod_forecast_answerValue == true){	// 評估總答案數
					strategy += "isUseGuessMethod_forecast_answerValue ";
				}else if (this.isUseGuessMethod_forecast_step_and_answer_Value == true){
					strategy += "isUseGuessMethod_forecast_step_and_answer_Value ";
				}
			}
		}
		
		if (this.isUseBlockCombine_choice == true){		// 是否有使用分區化選擇策略
			strategy += "Block_Combine_choice";
		}
		
		if (this.isUseBlockCombine == true){			// 有使用分區方法
			strategy += "Block_Combine ";
			if (this.isUseBlockCombine_unique == true){		// 使用獨立分區化
				strategy += "isUseBlockCombine_unique ";
			}else if (this.isUseBlockCombine_complete == true){		// 使用完全分區化
				strategy += "isUseBlockCombine_complete ";
			}
		}

		if (this.isUseFirstStrategy == true){			// 有使用首步指定策略
			strategy += "First_Strategy " + "x = " + this.firstX + ", y = " + this.firstY + " ";
		}

		if (this.isUseRereadMap == true){				// 有使用重讀地圖方法
			strategy += "Reread_Map ";
		}
		
		if (this.isUseRereadCombineMap == true) {		// 有使用重讀組合地圖的方法
			strategy += "Reread_CombineMap ";
		}

		if (this.isUseFindCrapsShoot == true){		// 使用尋找craps shoot的方法
			strategy += "Find_CrapsShoot";
		}

		strategy += "|";
		return strategy;
	}

//	public BigDecimal getAllCombineTimes() {
//		return this.total_combineTimes;
//	}

	// inner calss area---------------------------------------
	class Teddy_Map extends Player_Board implements Cloneable{
		Teddy_Map(Player_Board pmap){
			super(pmap);
			this.board = new CF_Grid[this.mapSize];
			for (int y = 0 ; y < this.mapY ; y++){
				for (int x = 0 ; x < this.mapX ; x++){
					this.board[this.getRealSituation(y, x)] = new CF_Grid(pmap.getGrid(y, x));
				}
			}
		}

		@Override
		public Object clone() throws CloneNotSupportedException{
			Teddy_Map cmap = null;
			try {
				cmap = (Teddy_Map)super.clone();
				cmap.board = new CF_Grid[this.mapSize];
				for (int y = 0 ; y < this.mapY ; y++){
					for (int x = 0 ; x < this.mapX ; x++){
						cmap.board[this.getRealSituation(y, x)] = (CF_Grid)this.get_cfGrid(y, x).clone();
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return cmap;
		}
		
	    protected void finalize_By_Teddy() {
			for (int y = 0 ; y < this.mapY ; y++){
				for (int x = 0 ; x < this.mapX ; x++){
					((CF_Grid)this.board[this.getRealSituation(y, x)]).finalize_By_Teddy();
				}
			}
			this.board = null;
		}
		
		private Teddy_Map.CF_Grid get_cfGrid(int y, int x){
			return (Teddy_Map.CF_Grid)this.board[this.getRealSituation(y, x)];
		}

		private Teddy_Map.CF_Grid get_cfGrid(Player_Grid pgrid){
			return this.get_cfGrid(pgrid.getY(), pgrid.getX());
		}

		private boolean is_isSetCF(int y, int x){
			return this.get_cfGrid(y, x).is_isSetCF();
		}

		private boolean is_isAssumption(int y, int x){
			return get_cfGrid(y, x).isAssumption;
		}

		private boolean is_isJudge(int y, int x){
			return this.get_cfGrid(y, x).is_isJudge();
		}

		//專門是root在呼叫
		private void reRead(Player_Board pmap, Answer canswer){			//閱讀並比較地圖
			for (int y = 0 ; y < this.getMapY() ; y++){
				for (int x = 0 ; x < this.getMapX() ; x++){
					if (pmap.getState(y, x) != this.board[this.getRealSituation(y, x)].getState()){	//只處理和原來的地圖不同的地方
						this.get_cfGrid(y, x).setState(pmap.getState(y, x));	//設定新狀態
//						this.board[y][x].setState(pmap.getState(y, x));

						for (int j = y - 2 ; j <= y + 2 ; j++){
							for (int i = x - 2 ; i <= x + 2 ; i++){
								if (this.isInBoard(j, i) == true){	// 地圖已改變，修改會影響到的二層範圍，包括自已
									this.get_cfGrid(j, i).setChange(pmap.getGrid(y, x), pmap, canswer);
								}
							}
						}
					}
				}
			}

			for (int y = 0 ; y < this.getMapY() ; y++){
				for (int x = 0 ; x < this.getMapX() ; x++){
					if (this.is_isAssumption(y, x) == true && pmap.isInformationGrid(y, x) == true){
						this.get_cfGrid(y, x).deMistake(pmap, this);		//刪掉不合法的組合
						this.get_cfGrid(y, x).little_overlapping(pmap, canswer);
					}
				}
			}
		}

		private class CF_Grid extends Player_Grid implements Cloneable{
			private int remainMine;			//地雷殘量
			private boolean isSetCF;		//是否設過組合
			private boolean isJudge;		//是否經過判斷
			private boolean isAssumption;	//是否經過假設
			private CF_Database cfLink;	//共領域的所有集合
			private CM_Database cmLink;	//cm的所有集合

			private CF_Grid(Player_Grid ggrid){			//未知點
				super(ggrid);
				this.isSetCF = false;
				this.isJudge = false;
				this.isAssumption = false;
			}
			
		    private void finalize_By_Teddy() {
		    	if (this.cfLink != null){
		    		this.cfLink.finalize_By_Teddy();
		    		this.cfLink = null;
		    	}
				if (this.cmLink != null){
					this.cmLink.finalize_By_Teddy();
					this.cmLink = null;
				}
			}

			private void little_overlapping(Player_Board pmap, Answer canswer){
				int[] overlappingArray = new int[this.cmLink.cmlink.getFirst().cmgridLink.size()];
				int gridY, gridX;

				for (CM_GridSet clink : this.cmLink.cmlink){
					for (int index = 0 ; index < clink.cmgridLink.size() ; index++){
						if (clink.cmgridLink.get(index).is_isMine() == true) overlappingArray[index]++;
					}
				}

				// little overlapping
				for (int index = 0 ; index < overlappingArray.length ; index++){
					if (overlappingArray[index] == 0){
						gridY = this.cmLink.cmlink.getFirst().cmgridLink.get(index).get_coordinateY();
						gridX = this.cmLink.cmlink.getFirst().cmgridLink.get(index).get_coordinateX();
						canswer.add(pmap.getGrid(gridY, gridX), TeddyMethod.notMine);
					}else if (overlappingArray[index] == this.cmLink.cmlink.size()){
						gridY = this.cmLink.cmlink.getFirst().cmgridLink.get(index).get_coordinateY();
						gridX = this.cmLink.cmlink.getFirst().cmgridLink.get(index).get_coordinateX();
						canswer.add(pmap.getGrid(gridY, gridX), TeddyMethod.isMine);
						
//						if (overlappingArray[index] > 1){
//							map.showMap();
//							System.out.println("this grid = " + this.getCoordinate());
//							System.out.println("mingrid =  " + gridX + " " + gridY);
//							int x, y;
//							for (int index2 = 0 ; index2 < overlappingArray.length ; index2++){
//								x = this.cmLink.cmlink.getFirst().cmgridLink.get(index2).get_coordinateX();
//								y = this.cmLink.cmlink.getFirst().cmgridLink.get(index2).get_coordinateY();
//								System.out.println("[(" + x + ", " + y + "), " + overlappingArray[index2] + "]");
//							}
//						}
					}
					
					
					//					System.out.print(overlappingArray[index] + ", ");
				}
			}

			private void deMistake(Player_Board pmap, Teddy_Map cmap){	//除錯
				//cgrid是位置，cgrid是root的實際位子，因為所影響的範圍是二層，所以回傳的是兩層的已開點
				LinkedList<Teddy_Map.CF_Grid> openGrid = this.get_openGrid(pmap, cmap);	
				int remainMineNumber, initialNumber, flagNumber, cmMineNumber;

				for (CM_GridSet cset : this.cmLink.cmlink){		//檢查每一個cm組合
					
					
					for (Teddy_Map.CF_Grid cfgrid : openGrid){
						if (isDebug == true){
							//							System.out.println("show cfgrid in deMistake " + cfgrid.getCoordinate() + ", " + cfgrid.getState());
						}
						initialNumber = 0;
						flagNumber = 0;
						cmMineNumber = 0;

						for (int j = cfgrid.getY() - 1 ; j <= cfgrid.getY() + 1 ; j++){
							for (int i = cfgrid.getX() - 1 ; i <= cfgrid.getX() + 1 ; i++){
								if (i != cfgrid.getX() || j != cfgrid.getY()){		//中間的點不用理
									if (pmap.isInBoard(j, i) == true && pmap.isVisit(j, i) == false){		//未打開的點
										if (cset.isExistCM(j, i) == true){
											if (cset.isMineInCM(j, i) == true){
												cmMineNumber++;
											}
										}else {
											if (pmap.isFlag(j, i) == true){
												flagNumber++;
											}else if (pmap.isInitial(j, i) == true){
												initialNumber++;
											}
										}
									}
								}
							}
						}
						remainMineNumber = (cfgrid.getState() - flagNumber - cmMineNumber);

						if (isDebug == true){
							//							System.out.println("show inpormation in deMistake");
							//							this.showMap(fmap, "state");
							//							System.out.println("initialNumber = " + initialNumber);
							//							System.out.println("flagNumber = " + flagNumber);
							//							System.out.println("cmMineNumber = " + cmMineNumber);
							//							System.out.println("remainMineNumber = " + remainMineNumber);
						}

						//						前者是檢查地雷數是否太多，後者是檢查地雷數是否太少，前者為(剩餘地雷>0)，後者為當剩餘地雷大於零時，初始格數要大於剩餘地雷數。
						if (remainMineNumber > initialNumber || remainMineNumber < 0){
							cset.set_isLegit(false);
							if (isDebug == true){
								//								System.out.println("this fmap is illegal");
							}
						}

						if (cset.is_isLegit() == false) break;	//既然已經不合法了，就沒有必要繼續檢查了。
					}
				}

				for (int index = 0 ; index < this.cmLink.cmlink.size() ; index++){		//刪除不合法
					if (this.cmLink.cmlink.get(index).is_isLegit() == false){
						this.cmLink.cmlink.remove(index);
						index--;
					}
				}

				if (Experiment.isLogicTest == true){
					if (this.cmLink.cmlink.size() == 0){
						System.out.println("error_in_deMistake，cmLink.cmlink被刪到一個都不剩！");
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_deMistake_cmLink.cmlink被刪到一個都不剩");
//							System.out.println("now map");
//							pmap.showMap();
						}
					}
				}
			}

			private LinkedList<Teddy_Map.CF_Grid> get_openGrid(Player_Board pmap, Teddy_Map cmap){	//得到grid旁邊的已開點位置
				
				LinkedList<Teddy_Map.CF_Grid> openGrid = new LinkedList<Teddy_Map.CF_Grid>();

				for (int j = this.getY() - 1 ; j <= this.getY() + 1 ; j++){
					for (int i = this.getX() - 1 ; i <= this.getX() + 1 ; i++){
						if (i != this.getX() || j != this.getY()){			//中間的點不用理
							if ((pmap.isInBoard(j, i) == true) && (pmap.isVisit(j, i) == true)){
								openGrid.addLast(cmap.get_cfGrid(j, i));
							}
						}
					}
				}
				
				/*
				 * a a a a a
				 * a * * * a
				 * a * n * a
				 * a * * * a
				 * a a a a a
				 * 收集 a 位置的已開點，且a的周圍至少有一個cm假設
				 * 
				 * (i-2 j+2)	(i-1 j+2)	(i j+2)	(i+1 j+2)	(i+2 j+2)
				 * (i-2 j+1)	(i-1 j+1)	(i j+1)	(i+1 j+1)	(i+2 j+1)
				 * (i-2 j)		(i-1 j)		(i j)	(i+1 j)		(i+2 j)
				 * (i-2 j-1)	(i-1 j-1)	(i j-1)	(i+1 j-1)	(i+2 j-1)
				 * (i-2 j-2)	(i-1 j-2)	(i j-2)	(i+1 j-2)	(i+2 j-2)
				 */
				
//				for (int j = this.getY() - 2 ; j <= this.getY() + 2 ; j++){
//					for (int i = this.getX() - 2 ; i <= this.getX() + 2 ; i++){
//						//中間的區塊不用理
//						if (Math.abs(j - this.getY()) == 2 || Math.abs(i - this.getX()) == 2){
//							if (pmap.isInBoard(j, i) == true && (pmap.isVisit(j, i) == true && this.isBesideCM(j, i) == true)){
//								openGrid.addLast(cmap.get_cfGrid(j, i));
//							}
//						}
//					}
//				}

				return openGrid;
			}

//			private boolean isBesideCM(int y, int x) {		//是否在CM點的旁邊
//				boolean isbeside = false;
//				for (CM_Grid cgrid : this.cmLink.cmlink.getFirst().cmgridLink){
//					if (Math.abs(cgrid.get_coordinateX() - x) <=1 && Math.abs(cgrid.get_coordinateY() - y) <=1){
//						isbeside = true;
//						break;
//					}
//				}
//				return isbeside;
//			}

			@Override
			public Object clone() throws CloneNotSupportedException{
				CF_Grid cgrid = null;
				try {
					cgrid = (CF_Grid)super.clone();
					if (this.cfLink != null){
						cgrid.cfLink = (CF_Database)this.cfLink.clone();
					}

					if (this.cmLink != null){
						cgrid.cmLink = (CM_Database)this.cmLink.clone();
					}
				}catch (CloneNotSupportedException cse) {
					cse.printStackTrace();
				}
				return cgrid;
			}

			private void setCF(Player_Board cmap, Teddy_Map croot, Answer canswer){	//已知點
				// ***create cf set step***
				this.createGridSet(cmap, croot, canswer);
				this.isSetCF = true;
			}

			private boolean addGridSet(CF_GridSet gridSet){		//加到集合之中
				boolean isAddSuc = false;

				if (this.cfLink == null){
					this.cfLink = new CF_Database();
					this.cfLink.addLast(new CF_GridSetLink(gridSet.size()));
					isAddSuc = this.cfLink.getLast().add(gridSet);	
				}else {
					for (int index = 0 ; index < this.cfLink.size() ; index++){		//依照格子總數加到正確的階層中。
						if (gridSet.size() > this.cfLink.get(index).get_SumOfGrids()){
							this.cfLink.add(index, new CF_GridSetLink(gridSet.size()));
							isAddSuc = this.cfLink.get(index).add(gridSet);
							break;
						}else if (gridSet.size() == this.cfLink.get(index).get_SumOfGrids()){
							isAddSuc = this.cfLink.get(index).add(gridSet);
							break;
						}else if (index == this.cfLink.size() - 1){		// 最後一個
							this.cfLink.addLast(new CF_GridSetLink(gridSet.size()));
							isAddSuc = this.cfLink.getLast().add(gridSet);
							break;
						}
					}
				}				
				if (isAddSuc == true && this.isJudge == true){		//如果加入了新集合，就必須要重新判斷了。
					this.isJudge = false;
				}

				return isAddSuc;	// 回傳是否加入成功了
			}

			private void addCM(Player_Board pmap, Player_Grid pgrid, boolean isLastGrid){
				if (this.cmLink == null){
					this.cmLink = new CM_Database(this.remainMine);
				}
				this.cmLink.add(pgrid, isLastGrid);
			}

			private void createGridSet(Player_Board cmap, Teddy_Map croot, Answer canswer){					//創造集合
				CF_GridSet besideGridSet = new CF_GridSet(this.remainMine);
				for (int j = this.getY() - 1 ; j <= this.getY() + 1 ; j++){		//製作集合
					for (int i = this.getX() - 1 ; i <= this.getX() + 1 ; i++){
						if (j != this.getY() || i != this.getX()){		//中間的點不用理
							if (cmap.isInBoard(j, i) == true && cmap.isInitial(j, i) == true){
								besideGridSet.add(cmap.getGrid(j, i));
							}
						}
					}
				}

				if (Experiment.isLogicTest == true){
					if (besideGridSet.size() == 0){
						System.out.println("error_in_createGridSet，因為資訊點所創造出來的cfGridSet的格子數目為零");
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_createGridSet_因為資訊點所創造出來的cfGridSet的格子數目為零");
						}
					}
				}

				// Simple_Judge step
				besideGridSet.simple_Judge(canswer);		//制作時就先判斷了
				for (Player_Grid bgrid : besideGridSet.gridLink){		//加入集合到周圍的點
					croot.get_cfGrid(bgrid).addGridSet(besideGridSet);
				}
			}		

			private boolean is_isJudge(){
				return this.isJudge;
			}

			private void set_isJudge(boolean isJudge){
				this.isJudge = isJudge;
			}

			private boolean is_isSetCF(){
				return this.isSetCF;
			}

			private void set_remainMine(int remainMine){
				this.remainMine = remainMine;
			}

			private void set_isAssumption(boolean isAssumption){
				this.isAssumption = isAssumption;
			}

			private void setChange(Player_Grid grid, Player_Board pmap, Answer canswer) {	//處理因為有新資訊而改變的集合
				if ((this.getY() == grid.getY()) && (this.getX() == grid.getX())){	//改變的是本身的那一點
					this.cfLink = null;
					if (Experiment.isLogicTest == true){
						if (this.cmLink != null){
							System.out.println("error_in_setChange, this.cmlink isn't null in first open");
							if (Experiment.isStepByStepDebug == true){
								System.out.println("error_in_setChange_第一次打開時cmlink已存在");
							}
						}else if (this.isAssumption == true){
							System.out.println("error_in_setChange, this.isAssumption isn't false in first open");
							if (Experiment.isStepByStepDebug == true){
								System.out.println("error_in_setChange_第一次打開時已經假設過了");
							}
						}
					}
				}else {
					if (this.cfLink != null){	//原本是已設定的未知點
						for (int index = 0 ; index < this.cfLink.size() ; index++){	//縮簡組合
							this.cfLink.get(index).setChange(grid, canswer);

							if (this.cfLink.get(index).size() == 0){
								this.cfLink.remove(index);
								index--;
							}
						}

						if (this.cfLink.size() == 0){	//縮簡到沒有集合就刪掉
							this.cfLink = null;
						}else {
							for (int griddataIndex = 0 ; griddataIndex < this.cfLink.size() ; griddataIndex++){		//處理已改變的集合，重新加入刪除
								for (int gridlinkIndex = 0 ; gridlinkIndex < this.cfLink.get(griddataIndex).size() ; gridlinkIndex++){
									if (this.cfLink.get(griddataIndex).get(gridlinkIndex).gridLink.size() 
											!= this.cfLink.get(griddataIndex).sumOfGrids){
										this.addGridSet(this.cfLink.get(griddataIndex).get(gridlinkIndex));	//重新加入
										this.cfLink.get(griddataIndex).remove(gridlinkIndex);					//刪除
										gridlinkIndex--;
									}
								}
								if (this.cfLink.get(griddataIndex).size() == 0){
									this.cfLink.remove(griddataIndex);
									griddataIndex--;
								}
							}

							if (this.cfLink.size() == 0) this.cfLink = null;
						}
					}else if (this.isAssumption == true && this.cmLink != null){	//原本是資訊點
						if (pmap.isAllKnowGrid(this.getY(), this.getX()) == true){	//後來變成已知點
							this.remainMine = 0;
							this.isSetCF = false;
							this.isJudge = false;
							this.isAssumption = false;
							this.cfLink = null;
							this.cmLink = null;
						}else {
							this.cmLink.setChange(grid, pmap);

							if (Experiment.isLogicTest == true){
								if (this.cmLink.cmlink == null){
									this.cmLink = null;
									System.out.println("error_in_setChange，因為資訊點的的cmLink在不為全知點的狀況下縮減為 0");
									if (Experiment.isStepByStepDebug == true){
										UI.safeMap_In_debug("error_in_setChange_因為資訊點的的cmLink在不為全知點的狀況下縮減為 0");
									}
								}
							}
						}
					}
				}

				if (isDebug == true){
					//					System.out.println("show the information of setChange of cfGrid " + this.getCoordinate());
				}
			}

			//			private void showSet(String order){
			//				if (order.equals("cf")){
			//					System.out.println(this.getCoordinate() + " show cfGrid's cfset in showSet");
			//					if (this.cfLink != null){
			//						for (int index = 0 ; index < this.cfLink.size() ; index++){
			//							System.out.println(this.cfLink.get(index).showSet());
			//						}
			//					}else {
			//						System.out.println("this.gridsetdatabase == null");
			//					}
			//				}else if (order.equals("cm")){
			//					System.out.println(this.getCoordinate() + " show cfGrid's cmset in showSet");
			//					if (this.cmLink != null){
			//						System.out.println(this.cmLink.showSet());
			//					}else {
			//						System.out.println("this.cmgridsetdatabase == null");
			//					}
			//				}
			//				
			//			}
		}
	
		// TODO 可以考慮將CM_Grid移到這裡
//		private class CM_Grid extends Player_Grid implements Cloneable{
//			private CM_Grid(Player_Grid ggrid, boolean isMine){
//				super(ggrid);
//				if (isMine == true){
//					this.setState(Board.mine);
//				}else {
//					this.setState(Board.noMine);
//				}
//			}
//			
//			@Override
//			public Object clone() throws CloneNotSupportedException{
//				CM_Grid cmgrid = null;
//				try {
//					cmgrid = (CM_Grid)super.clone();
//				}catch (CloneNotSupportedException cse) {
//					cse.printStackTrace();
//				}
//				return cmgrid;
//			}
//			
//			private boolean equals(Player_Grid grid){
//				return (this.getX() == grid.getX() && this.getY() == grid.getY()) ? true : false;
//			}
//
//			private boolean is_isMine(){
//				return (this.getState() == Board.mine) ? true : false;
//			}
//		}
	}

	private class CF_GridSetLink implements Cloneable{
		private LinkedList<CF_GridSet> gridsetLink;
		int sumOfGrids;
		
		private CF_GridSetLink(int sumOfGrids){
			this.sumOfGrids = sumOfGrids;
			this.gridsetLink = new LinkedList<CF_GridSet>();
		}

		@Override
		public Object clone() throws CloneNotSupportedException{
			CF_GridSetLink cgridlink = null;
			try {
				cgridlink = (CF_GridSetLink)super.clone();
				if (this.gridsetLink != null){
					cgridlink.gridsetLink = new LinkedList<CF_GridSet>();
					for (CF_GridSet cset : this.gridsetLink){
						cgridlink.gridsetLink.addLast((CF_GridSet)cset.clone());
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return cgridlink;
		}

//		private String showSet() {
//			String mes = this.sumOfGrids + " -> ";
//			for (int index = 0 ; index < this.gridsetLink.size() ; index++){
//				mes += this.gridsetLink.get(index).showSet() + ", ";
//			}
//			return mes;
//		}

	    private void finalize_By_Teddy() { 
	    	if (this.gridsetLink != null){
	    		for (CF_GridSet cfset : this.gridsetLink){
					cfset.finalize_By_Teddy();
				}
				this.gridsetLink.clear();
				this.gridsetLink = null;
	    	}
		}
		
		private void setChange(Player_Grid grid, Answer canswer) {
			//			boolean isFlag = false;
			for (int index = 0 ; index < this.gridsetLink.size() ; index++){
				this.gridsetLink.get(index).setChange(grid, canswer);
				if (this.gridsetLink.get(index).size() == 0){
					this.gridsetLink.remove(index);
					index--;
				}
			}
			//			return isFlag;
		}

		private int size() {
			return this.gridsetLink.size();
		}

		private boolean add(CF_GridSet gridSet) {
			boolean isSomeSet = false;
			for (int index = 0 ; index < this.gridsetLink.size() ; index++){	//只能加入非重覆的集合才行
				isSomeSet = this.gridsetLink.get(index).equals(gridSet);		//需要確定是否可行
				if (isSomeSet == true) break;
			}

			if (isSomeSet == false){	//如果不存在其集合才加入
				this.gridsetLink.addLast(gridSet);
			}
			return (isSomeSet == false) ? true : false;			//回傳結果，等於是否有加入。
		}

		private int get_SumOfGrids() {
			return this.sumOfGrids;
		}

		private CF_GridSet get(int index){
			return this.gridsetLink.get(index);
		}

		private void remove(int index){
			this.gridsetLink.remove(index);
		}
	}

	private class CF_Database implements Cloneable{
		private LinkedList<CF_GridSetLink> cflink;
		private CF_Database(){
			this.cflink = new LinkedList<CF_GridSetLink>();
		}

		@Override
		public Object clone() throws CloneNotSupportedException{
			CF_Database ccf = null;
			try {
				ccf = (CF_Database)super.clone();
				if (this.cflink != null){
					ccf.cflink = new LinkedList<CF_GridSetLink>();
					for (CF_GridSetLink clink : this.cflink){
						ccf.cflink.addLast((CF_GridSetLink)clink.clone());
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return ccf;
		}

		//		public cfGridSetLink getFirst() {
		//			return this.cflink.getFirst();
		//	
		
	    private void finalize_By_Teddy() {
	    	if (this.cflink != null){
	    		for (CF_GridSetLink cfsetlink : this.cflink){
					cfsetlink.finalize_By_Teddy();
				}
				this.cflink.clear();
				this.cflink = null;
	    	}
		}
		
		public void add(int index, CF_GridSetLink cfGridSetLink) {
			this.cflink.add(index, cfGridSetLink);
		}

		public void addLast(CF_GridSetLink cfGridSetLink) {
			this.cflink.addLast(cfGridSetLink);
		}

		public CF_GridSetLink get(int index) {
			return this.cflink.get(index);
		}

		public CF_GridSetLink getLast() {
			return this.cflink.getLast();
		}

		public void remove(int index) {
			this.cflink.remove(index);
		}

		public int size() {
			return this.cflink.size();
		}
	}

	private class CF_GridSet implements Cloneable{
		private int remainMine;
		private LinkedList<Player_Grid> gridLink;

		private CF_GridSet(int remainMine){
			this.remainMine = remainMine;
			this.gridLink = new LinkedList<Player_Grid>(); 
		}
		
	    private void finalize_By_Teddy() {
	    	if (this.gridLink != null){
	    		this.gridLink.clear();
				this.gridLink = null;
	    	}
		}

		@Override
		public Object clone() throws CloneNotSupportedException{
			CF_GridSet cgridset = null;
			try {
				cgridset = (CF_GridSet)super.clone();
				if (this.gridLink != null)	{
					cgridset.gridLink = new LinkedList<Player_Grid>();
					for (Player_Grid pgrid : this.gridLink){
						cgridset.gridLink.addLast((Player_Grid)pgrid.clone());
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return cgridset;
		}

		private void setChange(Player_Grid grid, Answer canswer) {
			//			boolean isFlag = false;
			for (int index = 0 ; index < this.gridLink.size() ; index++){
				if (this.gridLink.get(index).equals(grid) == true){
					this.gridLink.remove(index);
					if (grid.getState() == Board.flag){
						this.remainMine--;
						//						isFlag = true;
					}
					break;
				}
			}
			if (this.gridLink.size() > 0){
				this.simple_Judge(canswer);
			}
			//			return isFlag;
		}

		public int get_RemainMine() {
			return this.remainMine;
		}

		private Player_Grid get(int index) {
			return this.gridLink.get(index);
		}

		private void add(Player_Grid grid){
			this.gridLink.addLast(grid);
		}

		private int size(){
			return this.gridLink.size();
		}

		private boolean equals(CF_GridSet gridset){		//集合是否相同
			boolean isSome = true;
			if (this.remainMine != gridset.get_RemainMine()) isSome = false;		//所剩地雷數不相同則必不相同

			if (isSome == true){
				if (this.gridLink.size() != gridset.gridLink.size())  isSome = false;		//集合的格子數不相同則必不相同
			}

			if (isSome == true){		// 檢查每一個格子
				boolean isExist;
				for (int gridsetIndex = 0 ; gridsetIndex < gridset.gridLink.size() ; gridsetIndex++){
					isExist = false;
					for (int thisIndex = 0 ; thisIndex < this.gridLink.size() ; thisIndex++){
						if (this.gridLink.get(thisIndex).equals(gridset.gridLink.get(gridsetIndex)) == true){
							isExist = true;
						}
					}
					isSome &= isExist;
					if (isSome == false) break;
				}
			}
			return isSome;
		}

		private void simple_Judge(Answer canswer){	//簡單的集合判斷
			if (this.remainMine == 0){						//如果集合中所包含的地雷數為0，則代表集合中的所有格子皆非地雷。
				for (Player_Grid pgrid : this.gridLink){
					canswer.add(pgrid, TeddyMethod.notMine);
				}
			}else if (this.remainMine == this.gridLink.size()){	//如果集合中所包含的地雷數等於集合的格子總數，則代表集合中的所有格子皆是地雷。
				for (Player_Grid pgrid : this.gridLink){
					canswer.add(pgrid, TeddyMethod.isMine);
				}
			}

			if (Experiment.isLogicTest == true){
				if (this.remainMine < 0){
					System.out.println("error_in_simple_Judge，因為判斷的集合的地雷數小於零");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_simple_Judge_因為判斷的集合的地雷數小於零");
					}
				}else if (this.gridLink.size() == 0){
					System.out.println("eroor in simple_Judge，因為判斷的集合的格子數為零");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_simple_Judge_因為判斷的集合的格子數為零");
					}
				}
			}
		}

//		private String showSet(){
//			String mes = "[";
//			for (int index = 0 ; index < this.gridLink.size() ; index++){
//				mes += "(" + this.gridLink.get(index).getCoordinate() + "), ";
//			}
//
//			mes += this.remainMine + "]";
//			return mes; 
//		}
	}

	
	// TODO
	private class CM_Grid implements Cloneable{
		//記錄座標所用
		final private int coordinateX;
		final private int coordinateY;
		final private boolean isMine;
		CM_Grid(Player_Grid ggrid, boolean isMine){
			this.coordinateX = ggrid.getX();
			this.coordinateY = ggrid.getY();
			this.isMine = isMine;
		}
		public int get_coordinateX() {
			return this.coordinateX;
		}
		public int get_coordinateY() {
			return this.coordinateY;
		}

		@Override
		public Object clone() throws CloneNotSupportedException{
			CM_Grid cmgrid = null;
			try {
				cmgrid = (CM_Grid)super.clone();
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return cmgrid;
		}

		public String getCoordinate() {
			return this.coordinateX + " " + this.coordinateY;
		}

		private boolean equals(Player_Grid grid){
			return (this.coordinateX == grid.getX() && this.coordinateY == grid.getY()) ? true : false;
		}

		private boolean is_isMine(){
			return this.isMine;
		}
	}

	private class CM_GridSet implements Cloneable{
		private LinkedList<CM_Grid> cmgridLink = new LinkedList<CM_Grid>();
		private boolean isLegit;		// 是否合法
		private int usedM;				// 使用地雷數

		private CM_GridSet(Player_Grid pgrid, int mineNumber, boolean isMine){
			this.cmgridLink.addLast(new CM_Grid(pgrid, isMine));
			this.isLegit = true;
			this.usedM = mineNumber;
		}
		
	    private void finalize_By_Teddy() {
	    	if (this.cmgridLink != null){
	    		this.cmgridLink.clear();
				this.cmgridLink = null;
	    	}
		}

		public void setAssumpt(Player_Grid grid) {
			for (int index = 0 ; index < this.cmgridLink.size() ; index++){
				if (this.cmgridLink.get(index).equals(grid) == true){
					this.cmgridLink.remove(index);
					if (grid.getState() == Board.flag){
						this.usedM--;
					}
					break;
				}
			}
		}

		private void set_isLegit(boolean isLegit){
			this.isLegit = isLegit;
		}

		private boolean is_isLegit(){
			return this.isLegit;
		}

		public boolean isMineInCM(int y, int x) {	//cfgrid是否在集合中是地雷
			boolean ismine = false;
			for (CM_Grid cgrid : this.cmgridLink){
				if ((cgrid.get_coordinateY() == y && cgrid.get_coordinateX() == x) && cgrid.is_isMine() == TeddyMethod.isMine){
					ismine = true;
					break;
				}
			}
			return ismine;
		}

		public boolean isExistCM(int y, int x) {		//cfgrid是否在集合中
			boolean isexist = false;
			for (CM_Grid cgrid : this.cmgridLink){
				if ((cgrid.get_coordinateY() == y) && (cgrid.get_coordinateX() == x)){
					isexist = true;
					break;
				}
			}
			return isexist;
		}

		@Override
		public Object clone() throws CloneNotSupportedException{
			CM_GridSet cmgridset = null;
			try {
				cmgridset = (CM_GridSet)super.clone();
				if (this.cmgridLink != null)	{
					cmgridset.cmgridLink = new LinkedList<CM_Grid>();
					for (CM_Grid cgrid : this.cmgridLink){
						cmgridset.cmgridLink.addLast((CM_Grid)cgrid.clone());
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return cmgridset;
		}

		private void add(Player_Grid pgrid, boolean isMine, boolean isLastGrid){	// 加入並檢查
			this.cmgridLink.addLast(new CM_Grid(pgrid, isMine));
			this.check(isMine, isLastGrid);
		}

		private void check(boolean isMine, boolean isLastGrid){		//確認集合是否正確
			int mineNum = 0;

			// 計算使用的地雷數
			for (int index = 0 ; index < this.cmgridLink.size() ; index++){
				if (this.cmgridLink.get(index).isMine == TeddyMethod.isMine){
					mineNum++;
				}
			}

			if (isMine == TeddyMethod.isMine && isLastGrid == false){			//只有加入地雷才需要檢查，避免地雷數過多
				this.isLegit = (mineNum > this.usedM) ? false : true;
			}

			if (this.isLegit == true && isLastGrid == true){					//加點完畢後，地雷數必需要符合數字減去周圍旗子數
				this.isLegit = (mineNum != this.usedM) ? false : true;
			}
		}

		private String showSet(){
			String mes = "[";
			for (int index = 0 ; index < this.cmgridLink.size() ; index++){
				mes += "(" + this.cmgridLink.get(index).getCoordinate() + ", " + this.cmgridLink.get(index).isMine + "), ";
			}

			mes += this.usedM + "]";
			return mes; 
		}
	}

	private class CM_Database implements Cloneable{
		private int mineNumber;
		private LinkedList<CM_GridSet> cmlink;
		private CM_Database(int mineNumber){
			this.mineNumber = mineNumber;
		}

		@Override
		public Object clone() throws CloneNotSupportedException{
			CM_Database ccs = null;
			try {
				ccs = (CM_Database)super.clone();
				if (this.cmlink != null){
					ccs.cmlink = new LinkedList<CM_GridSet>();
					for (CM_GridSet cset : this.cmlink){
						ccs.cmlink.addLast((CM_GridSet)cset.clone());
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return ccs;
		}

	    private void finalize_By_Teddy() {
	    	if (this.cmlink != null){
	    		for (CM_GridSet cmset : this.cmlink){
					cmset.finalize_By_Teddy();
				}
				this.cmlink.clear();
				this.cmlink = null;
	    	}
		}
		
		public int size() {
			return this.cmlink.size();
		}

		public void setChange(Player_Grid grid, Player_Board pmap){
			for (int linkIndex = 0 ; linkIndex < this.cmlink.size() ; linkIndex++){
				for (int index = 0 ; index < this.cmlink.get(linkIndex).cmgridLink.size() ; index++){
					if (grid.getY() == this.cmlink.get(linkIndex).cmgridLink.get(index).get_coordinateY() 
							&& grid.getX() == this.cmlink.get(linkIndex).cmgridLink.get(index).get_coordinateX()){
						// 如果發現「之前假設不是地雷，而後來得知是地雷」或是「之前假設是地雷，而後來得知不是地雷」，則把假設的組合刪除
						if ((pmap.isVisit(grid.getY(), grid.getX()) == true 
								&& this.cmlink.get(linkIndex).cmgridLink.get(index).is_isMine() == TeddyMethod.isMine)
								|| 
								(pmap.isFlag(grid.getY(), grid.getX()) == true 
								&& this.cmlink.get(linkIndex).cmgridLink.get(index).is_isMine() == TeddyMethod.notMine)){
							this.cmlink.remove(linkIndex);
							linkIndex--;
							break;
						}else {		//合法則需要扣掉已經假設的格子
							this.cmlink.get(linkIndex).setAssumpt(grid);
							if (this.cmlink.get(linkIndex).cmgridLink.size() == 0){
								this.cmlink.remove(linkIndex);
								linkIndex--;
								break;
							}
						}
					}
				}
			}

			if (this.cmlink.size() == 0){
				this.cmlink = null;
			}
		}

		private void add(Player_Grid pgrid, boolean isLastGrid){		//加入節點
			if (this.cmlink == null){
				this.cmlink = new LinkedList<CM_GridSet>();
				this.cmlink.addLast(new CM_GridSet(pgrid, this.mineNumber, TeddyMethod.isMine));
				this.cmlink.getLast().check(TeddyMethod.isMine, isLastGrid);
				this.cmlink.addLast(new CM_GridSet(pgrid, this.mineNumber, TeddyMethod.notMine));
				this.cmlink.getLast().check(TeddyMethod.notMine, isLastGrid);
			}else {
				int setSize = this.cmlink.size();
				for (int index = 0 ; index < setSize ; index++){
					try {
						this.cmlink.addLast((CM_GridSet)this.cmlink.getFirst().clone());
						this.cmlink.getLast().add(pgrid, TeddyMethod.isMine, isLastGrid);
						this.cmlink.addLast((CM_GridSet)this.cmlink.getFirst().clone());
						this.cmlink.getLast().add(pgrid, TeddyMethod.notMine, isLastGrid);
						this.cmlink.removeFirst();	// 刪除第一個
					}catch (CloneNotSupportedException cse) {
						cse.printStackTrace();
					}
				}
			}

			for (int index = 0 ; index < this.cmlink.size() ; index++){		//刪掉非法的集合
				if (this.cmlink.get(index).isLegit == false){
					this.cmlink.remove(index);
					index--;
				}
			}
		}

		private String showSet(){
			String mes = this.cmlink.size() + " -> ";
			for (int index = 0 ; index < this.cmlink.size() ; index++){
				mes += this.cmlink.get(index).showSet() + ", ";
			}
			return mes;
		}
	}

	private class Answer{
		LinkedList<Player_Board.Player_Grid> noMinePoint;		//確定不是地雷的點
		LinkedList<Player_Board.Player_Grid> isMinePoint;		//確定是地雷的點
		LinkedList<Player_Board.Player_Grid> guessPoint;		//craps shoot，和猜測的點
		Player_Board amap;
		private Answer(Player_Board map){
			this.amap = map;
			this.noMinePoint = new LinkedList<Player_Board.Player_Grid>();
			this.isMinePoint = new LinkedList<Player_Board.Player_Grid>();
			this.guessPoint = new LinkedList<Player_Board.Player_Grid>();
		}
		
	    private void finalize_By_Teddy() {
	    	if (this.noMinePoint != null){
	    		this.noMinePoint.clear();
				this.noMinePoint = null;
	    	}
			
	    	if (this.isMinePoint != null){
	    		this.isMinePoint.clear();
				this.isMinePoint = null;
	    	}
			
	    	if (this.isMinePoint != null){
	    		this.guessPoint.clear();
				this.guessPoint = null;
	    	}
		}

		private int getAnswerNumber() {
			return (this.noMinePoint.size() + this.isMinePoint.size());
		}

		private void checkExistAnswer() {		//扣掉已經按掉或是插旗子的點
			for (int index = 0 ; index < this.noMinePoint.size() ; index++){
				if (this.can_be_set(this.noMinePoint.get(index)) == false){
					this.noMinePoint.remove(index);
					index--;
				}
			}

			for (int index = 0 ; index < this.isMinePoint.size() ; index++){
				if (this.can_be_set(this.isMinePoint.get(index)) == false){
					this.isMinePoint.remove(index);
					index--;
				}
			}

			this.guessPoint.clear();	// 清除猜的點
		}

		private String getAnswer(boolean isUseGuessMethod){		//輸出答案
			if (this.noMinePoint.size() > 0){
				return "dig " + this.noMinePoint.getFirst().getCoordinate();
			}else if (this.isMinePoint.size() > 0){
				return "flag " + this.isMinePoint.getFirst().getCoordinate();
			}else {
				if (isUseGuessMethod == false || this.guessPoint.size() == 0){
					return "no_answer";
				}else {
					return "dig " + this.guessPoint.get((int)(Math.random()*this.guessPoint.size())).getCoordinate();
				}
			}
		}

		private boolean can_be_set(Player_Board.Player_Grid grid){		// 確認是否仍為初始點
			return (this.amap.isInitial(grid) == true) ? true : false;
		}

		private void add (Player_Board.Player_Grid grid){
			if (this.can_be_set(grid) == true) {
				if (Experiment.isLogicTest == true){
					if (this.isLegalPoint(grid, true) == false && this.isLegalPoint(grid, false) == false){
						System.out.println("error_in_add，因為加入了不合理的點");
						System.out.println("加點 grid = " + grid.getCoordinate() + " is guess");
						this.amap.showMap();
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_add_因為加入了不合理的點");
						}
					}
				}

				if (this.guessPoint.size() == 0){
					this.guessPoint.addLast(grid);
				}else {
					for (int index = 0 ; index < this.guessPoint.size() ; index++){
						if (this.guessPoint.get(index).getY() > grid.getY()){
							this.guessPoint.add(index, grid);
							break;
						}else if (this.guessPoint.get(index).getY() == grid.getY()){
							if (this.guessPoint.get(index).getX() > grid.getX()){
								this.guessPoint.add(index, grid);
								break;
							}else if (this.guessPoint.get(index).getX() == grid.getX()){
								break;	//此點已經加入過，所以不用再加入一次。
							}else if (index == this.guessPoint.size()-1){		//已經到最後一點了
								this.guessPoint.addLast(grid);
								break;
							}
						}else if (index == this.guessPoint.size()-1){	//已經到最後一點了
							this.guessPoint.addLast(grid);
							break;
						}
					}
				}
			}else if (isUseRereadMap == false){
				if (Experiment.isLogicTest == true){
					System.out.println("error_in_add，因為加入了非初始的點");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_add，因為加入了非初始的點");
					}
				}
			}
		}

		private void add (Player_Board.Player_Grid grid, boolean isSetMine){
			if (this.can_be_set(grid) == true){
				if (Experiment.isLogicTest == true){
					if (this.isLegalPoint(grid, isSetMine) == false){
						System.out.println("error_in_add，因為加入了不合理的點");
						System.out.println("加點 grid = " + grid.getCoordinate() + " isMine = " + isSetMine);
						this.amap.showMap();
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_add_因為加入了不合理的點");
						}
					}
				}

				if (isSetMine == TeddyMethod.isMine){	
					if (this.isMinePoint.size() == 0){
						this.isMinePoint.addLast(grid);
					}else {
						for (int index = 0 ; index < this.isMinePoint.size() ; index++){	// 依照座標的順序儲存
							if (grid.getY() < this.isMinePoint.get(index).getY()){
								this.isMinePoint.add(index, grid);
								break;
							}else if (grid.getY() == this.isMinePoint.get(index).getY()){
								if (grid.getX() < this.isMinePoint.get(index).getX()){
									this.isMinePoint.add(index, grid);
									break;
								}else if (grid.getX() == this.isMinePoint.get(index).getX()){
									break;	//此點已經加入過，所以不用再加入一次。
								}else if (index == this.isMinePoint.size()-1){		//已經到最後一點了
									this.isMinePoint.addLast(grid);
									break;
								}
							}else if (index == this.isMinePoint.size()-1){	//已經到最後一點了
								this.isMinePoint.addLast(grid);
								break;
							}
						}
					}
				}else {
					if (this.noMinePoint.size() == 0){
						this.noMinePoint.addLast(grid);
					}else {
						for (int index = 0 ; index < this.noMinePoint.size() ; index++){
							if (grid.getY() < this.noMinePoint.get(index).getY()){
								this.noMinePoint.add(index, grid);
								break;
							}else if (grid.getY() == this.noMinePoint.get(index).getY()){
								if (grid.getX() < this.noMinePoint.get(index).getX()){
									this.noMinePoint.add(index, grid);
									break;
								}else if (grid.getX() == this.noMinePoint.get(index).getX()){
									break;	//此點已經加入過，所以不用再加入一次。
								}else if (index == this.noMinePoint.size()-1){		//已經到最後一點了
									this.noMinePoint.addLast(grid);
									break;
								}
							}else if (index == this.noMinePoint.size()-1){	//已經到最後一點了
								this.noMinePoint.addLast(grid);
								break;
							}
						}
					}
				}
			}else if (isUseRereadMap == false){
				if (Experiment.isLogicTest == true){
					System.out.println("error_in_add，因為加入了非初始的點");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_add，因為加入了非初始的點");
					}
				}
			}
		}

		private boolean isExistAnswer(){		// 是否有答案
			return (this.isMinePoint.size() > 0 || this.noMinePoint.size() > 0 || this.guessPoint.size() > 0) ? true : false;
		}

		private boolean isCheckAnswer(){		// 是否有確定的答案
			return (this.isMinePoint.size() > 0 || this.noMinePoint.size() > 0) ? true : false;
		}

		private boolean isLegalPoint(Player_Grid grid, boolean isMine){
			boolean isLegal = true;
			LinkedList<Player_Grid> openGrid = new LinkedList<Player_Grid>();
			int remainMineNumber, initialNumber, flagNumber, cmMineNumber;
			for (int j = grid.getY() - 1 ; j <= grid.getY() + 1 ; j++){
				for (int i = grid.getX() - 1 ; i <= grid.getX() + 1 ; i++){
					if (this.amap.isInBoard(j, i) == true && ((i != grid.getX() || j != grid.getY()) && this.amap.isVisit(j, i) == true)){
						openGrid.addLast(this.amap.getGrid(j, i));
					}
				}
			}

			for (Player_Grid ggrid : openGrid){
				initialNumber = 0;
				flagNumber = 0;
				cmMineNumber = 0;
				for (int j = ggrid.getY() - 1 ; j <= ggrid.getY() + 1 ; j++){
					for (int i = ggrid.getX() - 1 ; i <= ggrid.getX() + 1 ; i++){
						if (this.amap.isInBoard(j, i) == true && this.amap.isVisit(j, i) == false){	//未打開的點
							if (j == grid.getY() && i == grid.getX()){
								if (isMine == true){
									cmMineNumber++;
								}
							}else {
								if (this.amap.isFlag(j, i) == true){
									flagNumber++;
								}else {
									initialNumber++;
								}
							}
						}
					}
				}

				remainMineNumber = (ggrid.getState() - flagNumber - cmMineNumber);

				if (remainMineNumber > initialNumber || remainMineNumber < 0){
					isLegal = false;
					break;
				}
			}
			return isLegal;
		}
	}

	private class CombineMap implements Cloneable{
		TeddyMethod caller;
		private LinkedList<CombineGrid> gridlink;	//combine的主要格子
		private LinkedList<CombineGrid> missCombineLink;	//在combine中被忽略的格子，必須用於檢查
		private RecordMapDatabase rMap;	//記錄統計結果。
		private int[][] iMap;			//記錄未知的點的索引位置
		private boolean[][] mMap;		//記錄地雷位置
		private int usedM;				//地圖所使用的地雷數
		private int usedL;				//地圖所使用的空格數
		private boolean isLittleCombine;	// 是否是區域組合
		Block_combineMap bcMaplink;			// 區域地圖
		private int index;					// 如果非子區域則為0，否則為0以上的數值
		
		// 為了isNeedCombineInSimulation所需的資料--------
		private int[][] besideL_Map;
		private int[][] besideM_min_Map;
		private int[][] besideM_max_Map;
		// --------為了isNeedCombineInSimulation所需的資料

		// 最佳化處理，為了避免大量的重覆模擬，尋找三個點代表
		RegGrid corner;	// 角落
		RegGrid center;	// 中央
		RegGrid edge;		// 邊

		private CombineMap(LinkedList<Teddy_Map.CF_Grid> glink, TeddyMethod caller){
			this.isLittleCombine = false;
			this.caller = caller;
			this.gridlink = new LinkedList<CombineGrid>();
			this.missCombineLink = new LinkedList<CombineGrid>();
			this.mMap = new boolean[this.get_map().getMapY()][this.get_map().getMapX()];	//預設全是false
			this.iMap = new int[this.get_map().getMapY()][this.get_map().getMapX()];		//預設全是0
			this.rMap = new RecordMapDatabase(this);
			this.bcMaplink = new Block_combineMap(this);
			this.index = 0;

			for (Teddy_Map.CF_Grid cgrid : glink){	//不重覆的加入新點
				if (isDebug == true){
					//					System.out.println("加入點 (" + cgrid.getCoordinate() + ")到");
					//					for (int index = 0 ; index < gridlink.size() ; index++){
					//						System.out.print("(" + this.gridlink.get(index).getCoordinate() + ")");
					//					}
					//					System.out.println();
				}
				if (this.gridlink.size() == 0){
					this.gridlink.addLast(new CombineGrid(cgrid));
				}else {	// 不重覆並依順序加入點
					for (int index = 0 ; index < this.gridlink.size() ; index++){
						if (this.gridlink.get(index).getY() > cgrid.getY()){
							this.gridlink.add(index, (new CombineGrid(cgrid)));
							break;
						}else if (this.gridlink.get(index).getY() == cgrid.getY()){
							if (this.gridlink.get(index).getX() > cgrid.getX()){
								this.gridlink.add(index, (new CombineGrid(cgrid)));
								break;
							}else if (this.gridlink.get(index).getX() == cgrid.getX()){
								break;	//此點已經加入過，所以不用再加入一次。
							}else if (index == this.gridlink.size()-1){		//已經到最後一點了
								this.gridlink.addLast(new CombineGrid(cgrid));
								break;
							}
						}else if (index == this.gridlink.size()-1){	//已經到最後一點了
							this.gridlink.addLast(new CombineGrid(cgrid));
							break;
						}
					}
				}
			}
			
			// 為了isNeedCombineInSimulation而搜集需要的資料
			if (this.isUseCombineInEvaluateJudge() == true){
				this.besideL_Map = new int[this.get_map().getMapY()][this.get_map().getMapX()];
				this.besideM_max_Map = new int[this.get_map().getMapY()][this.get_map().getMapX()];
				this.besideM_min_Map = new int[this.get_map().getMapY()][this.get_map().getMapX()];

				for (int y = this.get_map().getMapY() - 1 ; y >= 0 ; y--){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						this.besideL_Map[y][x] = Integer.MIN_VALUE;
						this.besideM_max_Map[y][x] = Integer.MIN_VALUE;
						this.besideM_min_Map[y][x] = Integer.MAX_VALUE;
					}
				}
			}

			if (isDebug == true){
				//				System.out.println("show cgrid in new combineMap");
				//				for (combineGrid cgrid : this.gridlink){
				//					System.out.print("(" + cgrid.getCoordinate() + ", " + cgrid.bIndex + "), ");
				//				}
				//				System.out.println("");
			}
		}

		private CombineMap(int index, LinkedList<CombineGrid> gridlink, CombineMap caller){	// little combine專用
			this.caller = caller.caller;
			this.isLittleCombine = true;
			this.gridlink = gridlink;
			this.missCombineLink = caller.missCombineLink;
			this.mMap = new boolean[this.get_map().getMapY()][this.get_map().getMapX()];	//預設全是false
			this.iMap = new int[this.get_map().getMapY()][this.get_map().getMapX()];		//預設全是0
			this.rMap = new RecordMapDatabase(this);
			this.index = index;
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			CombineMap cmap = null;
			try {
				cmap = (CombineMap)super.clone();
				cmap.rMap = (RecordMapDatabase)this.rMap.clone();	// 區域地圖只需複製地雷資料即可
				
//				if (this.isLittleCombine == false){
//					cmap.bcMaplink = new block_combineMap(this);		// 如果是全域地圖只需複製區域地圖的link即可
//					cmap.bcMaplink = (block_combineMap)this.bcMaplink.clone();
//				}else {
//					cmap.rMap = (recordMapDatatbase)this.rMap.clone();	// 如果是區域地圖只需複製地雷資料即可
//				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return cmap;
		}
		
		private void finalize_By_Teddy() { 
			this.caller = null;
			
			if (this.gridlink != null){
				for (CombineGrid cgrid : this.gridlink){
					cgrid.finalize_By_Teddy();
				}
		    	this.gridlink.clear();
		    	this.gridlink = null;
			}
			
	    	if (this.missCombineLink != null){
		    	for (CombineGrid cgrid : this.missCombineLink){
		    		cgrid.finalize_By_Teddy();
		    	}
		    	this.missCombineLink.clear();
		    	this.missCombineLink = null;
	    	}
	    	
	    	if (this.bcMaplink != null){
	    		this.bcMaplink.finalize_By_Teddy();
	    		this.bcMaplink = null;
	    	}
	    	
	    	if (this.rMap != null){
	    		this.rMap.finalize_By_Teddy();
		    	this.rMap = null;
	    	}
	    	
	    	this.iMap = null;
	    	this.mMap = null;
		}
		
		private Player_Board get_map(){
			return this.caller.map;
		}

		private Teddy_Map get_root(){
			return this.caller.root;
		}
		
		private int get_index(){
			return this.index;
		}
		
		private void set_index(int index){
			if (this.isLittleCombine == true){
				this.index = index;
			}else {
				if (Experiment.isLogicTest == true){
					System.out.println("error_in_set_index，在不合理的條件設定索引");
					System.out.println("isLittleComebine = " + this.isLittleCombine);
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_set_index，在不合理的條件設定索引");
					}
				}
			}
		}
		
		private Answer get_answer(){
			return this.caller.answer;
		}
		
		private boolean isUseGuessMethod() {
			return this.caller.isUseGuessMethod;
		}

		private boolean isUseGuessMethod_forecast() {
			return this.caller.isUseGuessMethod_forecast;
		}

		private boolean isUseBlockCombine(){
			return this.caller.isUseBlockCombine;
		}

		private boolean isUseBlockCombine_unique(){
			return this.caller.isUseBlockCombine_unique;
		}

		private boolean isUseBlockCombine_complete(){
			return this.caller.isUseBlockCombine_complete;
		}

		private boolean isUseFindCrapsShoot(){
			return this.caller.isUseFindCrapsShoot;
		}
		
		private boolean isUseCombineInEvaluateJudge(){
			return this.caller.isUseCombineInEvaluateJudge;
		}

		private boolean isUseRereadCombineMap(){
			return this.caller.isUseRereadCombineMap;
		}
		
		private boolean isSimulation(){
			return this.caller.isSimulation;
		}

		public void getBests(Answer answer) {
			if (isDebug == true && this.isSimulation() == false){
				System.out.println("getBests start");
			}
			
			this.rMap.Find_least_mine_possible(answer);		// 尋找地雷機率最小的點，並存入answer中
			
			// 沒有找到確定點才做價值辨斷
			if (this.isUseGuessMethod() == true && this.isUseGuessMethod_forecast() == true && this.isSimulation() == false 
					&& this.get_answer().isCheckAnswer() == false){
				// old method--------------------------------------------------------------------------------------
				if (answer.guessPoint.size() > 1){		// 要猜的點的數量大於一時才須要預測
					LinkedList<Player_Grid> plink = new LinkedList<Player_Grid>();
//					for (Player_Grid pgrid : answer.guessPoint){
//						plink.addLast(pgrid);	// 轉移地雷地雷最小的點
//					}
					
					plink = this.rMap.find_least_mine_possible_above_leastp(plink, 0);
					
//					plink.addLast(this.get_map().getGrid(1, 1));
//					plink.addLast(this.get_map().getGrid(2, 4));
//					plink.addLast(this.get_map().getGrid(1, 8));
//					plink.addLast(this.get_map().getGrid(7, 8));
//					plink.addLast(this.get_map().getGrid(8, 5));
//					plink.addLast(this.get_map().getGrid(0, 4));
//					plink.addLast(this.get_map().getGrid(0, 6));
					
					// 當使用完全分區時，在這計算數字機率
					if (this.isUseBlockCombine() == true && this.isUseBlockCombine_complete() == true){
						this.transferToGlobalRecord_number_probability_on_CPM(plink);
					}
					
					plink = this.rMap.guess_forecast(plink);		// 預測猜測，評估最有價值的點
					
					if (plink.size() > 0){		// 如果都沒出現有價值的點，就只好照原來的猜了！
						answer.guessPoint.clear();
						for (Player_Grid pgrid : plink){
							answer.add(pgrid);		// 加回guess中
						}
					}
				}
				// --------------------------------------------------------------------------------------old method
				
				// TODO new method
				// new method--------------------------------------------------------------------------------------
//				LinkedList<Player_Grid> plink = new LinkedList<Player_Grid>();
//				LinkedList<Player_Grid> bestLink = new LinkedList<Player_Grid>();		// 儲存目前分數最高的點
//				int min_eValue = 0;					// 最低分數
//				boolean isPass = false;				// 是否通過標準
//				double min_minePossible = 0;
//				do {
//					plink = this.rMap.find_least_mine_possible_above_leastp(plink, min_minePossible);
//					
//					if (plink.isEmpty() == false){
//						min_minePossible = this.rMap.getMinePossible(plink.getFirst().getCoordinateY(), plink.getFirst().getCoordinateX());	// 更新最小機率
//						// 當使用完全分區時，在這計算數字機率
//						if (this.isUseBlockCombine() == true && this.isUseBlockCombine_complete() == true){
//							this.transferToGlobalRecord_number_probability(plink);
//						}
//						
//						plink = this.rMap.guess_forecast(plink);
//						
//						if (plink.isEmpty() == false){
//							for (Player_Grid grid : plink){
//								if (this.rMap.getEvaluateValue(grid.getCoordinateY(), grid.getCoordinateX()) > min_eValue){
//									isPass = true;
//									break;
//								}else {		// 儲存分數最高的點
//									if (bestLink.isEmpty() == true){
//										bestLink.addLast(grid);
//									}else {
//										if (this.rMap.getEvaluateValue(grid.getCoordinateY(), grid.getCoordinateX()) 
//												> this.rMap.getEvaluateValue(bestLink.getFirst().getCoordinateY(), bestLink.getFirst().getCoordinateX())){
//											bestLink.clear();
//											bestLink.addLast(grid);	// 分數高優先加入
//										}else if ((this.rMap.getEvaluateValue(grid.getCoordinateY(), grid.getCoordinateX()) 
//												== this.rMap.getEvaluateValue(bestLink.getFirst().getCoordinateY(), bestLink.getFirst().getCoordinateX()))
//												&&(this.rMap.getMinePossible(grid.getCoordinateY(), grid.getCoordinateX()) 
//														== this.rMap.getMinePossible(bestLink.getFirst().getCoordinateY(), bestLink.getFirst().getCoordinateX()))){
//											bestLink.addLast(grid);	// 分數一樣高時，地雷機率也要一樣低才會加入
//										}
//									}
//								}
//							}
//							
//							if (isPass == false) {
////								System.out.println("有分數點，但因為分數太低而被淘汰！");
//								plink.clear();
//							}
//						}
//					}else {
//						// 代表已經沒有地雷機率小於1的候選點了，此時選擇從地雷機率最小的點中隨便選一點。
////						System.out.println("從地雷機率最小的點中選點");
//						if (bestLink.isEmpty() == true){
//							plink = this.rMap.find_least_mine_possible_above_leastp(plink, 0);
//						}else {
//							plink = bestLink;
//						}
//					}
//				}while (plink.isEmpty() == true);
//				
//				answer.guessPoint.clear();
//				for (Player_Grid pgrid : plink){
//					answer.add(pgrid);		// 加回guess中
//				 }
				// --------------------------------------------------------------------------------------new method
			}
			if (isDebug == true && this.isSimulation() == false){
				System.out.println("getBests end");
			}
		}

		private boolean isLittleCombine() {
			return this.isLittleCombine;
		}
		
		// 分區化選擇策略專用--------------------------------------------------------
		// 專門為了分區化選擇策略，是combine的簡略版
		private void combine_for_blockStategy_choice(){
//			// 變數設定----------
//			this.usedL = 0;			// 使用空格數歸零
//			this.usedM = 0;			// 使用地雷數歸零

			LinkedList<CombineGrid> combineLink = null;

			if (this.gridlink.isEmpty() == false){		// 有資訊點
				// 組合 iMap----------
				if (this.isLittleCombine == false){		// 全局組合
					for (int index = 0 ; index < this.gridlink.size() ; index++){		// 設定格子的影響範圍
						this.gridlink.get(index).set_bIndex(index+1);		// 設定索引
						if (this.iMap_Unite(this.gridlink.get(index)) == false){	//更新集合到地圖之中。
							// 沒有聯合到的點的處理
							this.gridlink.get(index).set_bIndex(Integer.MAX_VALUE);		//設定忽略點的索引為無限大
							this.missCombineLink.addLast(this.gridlink.get(index));		//忽略的格子轉移
							this.gridlink.remove(index);	//刪除沒有用的點
							index--;
						}
					}
				}else {		// 區域組合
					for (int index = 0 ; index < this.gridlink.size() ; index++){
						this.iMap_Unite(this.gridlink.get(index));
					}
				}
//				// 計算此地圖所使用的空格數----------
//				this.countUsedL();

				// 劃分區域，只有在有使用分區且非分區中才使用----------
				if (this.isLittleCombine == false){		//分區域
					combineLink = new LinkedList<CombineGrid>();
					for (CombineGrid cgrid : this.gridlink){	//複製
						combineLink.addLast(cgrid);
					}

					this.bcMaplink.divideSet();		//分區
					this.bcMaplink.little_combine();	// 區域逐一組合
				}else {
					combineLink = this.gridlink;
				}
			}
		}
		
		private int getSumOfArea(){
			return this.bcMaplink.cMapLink.size();
		}
		
		// 回傳獨立區域的總數
		private int getSumOfIndependentArea(){
			int sum = 0;
			for (CombineMap cmap : this.bcMaplink.cMapLink){
				if (cmap.isCheckIndependent() == true){
					sum++;
				}	
			}
			return sum;
		}
		
		// 回傳相依區域的總數
		private int getSumOfDependentArea(){
			int sum = 0;
			for (CombineMap cmap : this.bcMaplink.cMapLink){
				if (cmap.isCheckDependent() == true){
					sum++;
				}
			}
			return sum;
		}
		// --------------------------------------------------------分區化選擇策略專用

		private void combine(){
			TeddyMethod.total_useCombineTimes = TeddyMethod.total_useCombineTimes.add(BigDecimal.valueOf(1)); 
			// 變數設定----------
			this.usedL = 0;			// 使用空格數歸零
			this.usedM = 0;			// 使用地雷數歸零
			int remainL, remainM;

			LinkedList<CombineGrid> combineLink = null;

			if (this.gridlink.isEmpty() == false){		// 有資訊點
				// 組合 iMap----------
				if (this.isLittleCombine == false){		// 全局組合
					for (int index = 0 ; index < this.gridlink.size() ; index++){		// 設定格子的影響範圍
						this.gridlink.get(index).set_bIndex(index+1);		// 設定索引
						if (this.iMap_Unite(this.gridlink.get(index)) == false){	//更新集合到地圖之中。
							// 沒有聯合到的點的處理
							this.gridlink.get(index).set_bIndex(Integer.MAX_VALUE);		//設定忽略點的索引為無限大
							this.missCombineLink.addLast(this.gridlink.get(index));		//忽略的格子轉移
							this.gridlink.remove(index);	//刪除沒有用的點
							index--;
						}
					}
				}else {		// 區域組合
					for (int index = 0 ; index < this.gridlink.size() ; index++){
						if (this.iMap_Unite(this.gridlink.get(index)) == false){
							if (Experiment.isLogicTest == true){
								System.out.println("error in unite in little combine，因為在區域組合時發現有忽略點");
								if (Experiment.isStepByStepDebug == true){
									UI.safeMap_In_debug("error_in_combine_因為在區域組合時發現有忽略點");
								}
							}
						}
					}
				}
				// 計算此地圖所使用的空格數----------
				this.countUsedL();

				// 劃分區域，只有在有使用分區且非分區中才使用----------
				if (this.isUseBlockCombine() == true && this.isLittleCombine == false){		//分區域
					combineLink = new LinkedList<CombineGrid>();
					for (CombineGrid cgrid : this.gridlink){	//複製
						combineLink.addLast(cgrid);
					}

					this.bcMaplink.divideSet();		//分區
					
					if (this.isUseBlockCombine() == true && this.isUseBlockCombine_complete() == true
							&& this.isUseRereadCombineMap() == true){
						this.bcMaplink.little_combine(this.caller.last_com_Map);	// 重覆的地圖直接複製使用
					}else {
						this.bcMaplink.little_combine();	// 區域逐一組合
					}
					
					if (this.isUseBlockCombine_unique() == true){
						for (CombineMap cmap : this.bcMaplink.cMapLink){
							if (cmap.isCheckIndependent() == true){
								if (Experiment.isLogicTest == true){
									if (cmap.rMap.mapLink.size() == 0){
										System.out.println("error_in_combine，因為在分區域時發現部份地雷的地雷組合為零");
										if (Experiment.isStepByStepDebug == true){
											UI.safeMap_In_debug("error_in_combine，因為在分區域時發現部份地雷的地雷組合為零");
										}
									}
								}

								this.usedM += cmap.rMap.mapLink.getFirst().get_usedM();	//使用地雷數相加

								for (int l_index = 0 ; l_index < cmap.gridlink.size() ; l_index++){			//刪除未影響範圍的格子
									for (int c_index = 0 ; c_index < combineLink.size() ; c_index++){
										if (combineLink.get(c_index).getIndex() == cmap.gridlink.get(l_index).getIndex()){
											this.iMap_Recover(combineLink.get(c_index));
											combineLink.remove(c_index);		// 從原本的link中除去
											break;
										}
									}
								}
							}
						}
					}else if (this.isUseBlockCombine_complete() == true){
						// 尋找是否有craps shoot
						if (isUseFindCrapsShoot == true){
							for (CombineMap cmap : this.bcMaplink.cMapLink){
								if (cmap.isCrapsShoot() == true){
									if (isDebug == true){
										System.out.println("show the map in countPossible_block");
										this.get_map().showMap();
										System.out.println("show the combine map in countPossible_block");
										cmap.showMap("index");
//										UI.safeMap_In_debug("CrapsShootExample");
									}
										
									for (int y = 0 ; y < this.get_map().getMapY() ; y++){
										for (int x = 0 ; x < this.get_map().getMapX() ; x++){
											if (cmap.isInformationGrid(y, x) == true){
												this.caller.answer.add(this.get_map().getGrid(y, x));
											}
										}
									}
								}
							}
						}
						combineLink.clear();	// 完成分區化已經將所有分區的地圖都存起來了，所以不需要做全域組合
					}
				}else {
					combineLink = this.gridlink;
				}
				
				if (combineLink.isEmpty() == false){
					// 組合並紀錄所有的地圖
					this.combineAllMap(combineLink);
				}
				
				if (isDebug == true && this.isUseBlockCombine() == true && this.isLittleCombine == true){  
					// 印出record 次數
					System.out.println("show block record map in combine, isLittleCombine = " + this.isLittleCombine + ", isSimulation = " + this.isSimulation());
					for (int indexr = 0 ; indexr <= 9 ; indexr++){
						if (indexr == 9 || (indexr < 9 && this.isUseGuessMethod_forecast() == true))
							this.showMap("record", indexr);
					}
					
					if (this.isCheckIndependent() == true){
						System.out.println("this map is independent");
					}else {
						System.out.println("this map isn't independent");
					}
				}

				if (this.isLittleCombine == false){
					// 當使用尋找craps shoot的功能時，如果沒有找到答案才進行轉換記錄和計算機率
					if (this.isUseFindCrapsShoot() == false || (this.isUseFindCrapsShoot() == true && this.get_answer().isExistAnswer() == false)){
						// 轉換紀綠，將分區的紀錄轉換並合併成非分區的紀錄
						if (this.isUseBlockCombine() == true){
							if (this.isUseBlockCombine_unique() == true){
								LinkedList<CombineMap> uniqueArea = new LinkedList<CombineMap>(); 
								CombineMap thisMap= (combineLink.isEmpty() == false) ? this : null;
								
								// 集合獨立區域
								for (int index = 0 ; index < this.bcMaplink.cMapLink.size() ; index++){
									if (this.bcMaplink.cMapLink.get(index).isCheckIndependent() == true){
										uniqueArea.addLast(this.bcMaplink.cMapLink.get(index));
									}
								}
								
								if (isDebug == true){
									if (uniqueArea.isEmpty() == false){
										System.out.println("show block record map before transfer in combine");
										
										for (CombineMap cmap : uniqueArea){
											cmap.showMap("record", 9);
										}
									}else {
										System.out.println("no independent block in this map");
									}
									
									
									if (thisMap != null){
										System.out.println("show global record map before transfer in combine");
										for (int index = 0 ; index <= 9 ; index++){
											if (index == 9 || (index < 9 && this.isUseGuessMethod_forecast() == true))
												thisMap.showMap("record", index);
										}
									}else {
										System.out.println("no dependent block in this map");
									}
								}
								
								if (this.isUseGuessMethod() == true && this.isUseGuessMethod_forecast() == true){
									this.transferToGlobalRecord_number_probability_on_IPM(uniqueArea, thisMap);
									
									if (isDebug == true){
										if (uniqueArea.isEmpty() == false){
											System.out.println("show block record map after num possible transfer in combine");
											
											for (CombineMap cmap : uniqueArea){
												for (int index = 0 ; index <= 9 ; index++){
													if (index == 9 || (index < 9 && this.isUseGuessMethod_forecast() == true))
														cmap.showMap("record", index);
												}
											}
										}
										
										
										if (thisMap != null){
											System.out.println("show global record map after num possible transfer in combine");
											for (int index = 0 ; index <= 9 ; index++){
												if (index == 9 || (index < 9 && this.isUseGuessMethod_forecast() == true))
													thisMap.showMap("record", index);
											}
										}
									}
								}
								this.transferToGlobalRecord_on_IPM(uniqueArea, thisMap);
							}else if(this.isUseBlockCombine_complete() == true){
								this.transferToGlobalRecord_mine_probability_on_CPM(this.bcMaplink.cMapLink);
							}
						}
						
						// 計算機率
						this.countCaseNumber();		//計算case值
						this.countPossible_global();		//算完組合後立刻算機率
					}
				}
			}else {										// 無資訊點
				remainL = this.get_map().getRemainInitialGrid();
				remainM = this.get_map().getRemainMineNumber();
				this.countPossible_global(remainL, remainM);
			}
			
			if (isDebug == true && this.isLittleCombine == false && this.isSimulation() == false){  
				// 印出record 次數
				System.out.println("show record map in combine");
				for (int index = 0 ; index <= 9 ; index++){
					if (index == 9 || (index < 9 && this.isUseGuessMethod_forecast() == true))
						this.showMap("record", index);
				}
				
				System.out.println("show possible map in combine");
				for (int index = 0 ; index <= 10 ; index++){
					if (this.isUseGuessMethod_forecast() == true || (this.isUseGuessMethod_forecast() == false && index == 9)){
						this.showMap("possible", index);
					}
				}
			}
		}

		private void combineAllMap(LinkedList<CombineGrid> combineLink){	// 組合並記錄所有地圖
			int nowIndex = 0;	//目前的組合格子的位置
			CM_GridSet cset;

			for (int index = 0 ; index < combineLink.size() ; index++){	//將索引調回零
				combineLink.get(index).combineInitail();
			}

			TeddyMethod.single_combineTimes = 0;
			while (combineLink.isEmpty() == false && combineLink.getFirst().isCarryied() == false){	//第一個資訊點的集合是否已經跑完了
				// 如果組合次數大於限制則跳出
				if (TeddyMethod.single_combineTimes >= TeddyMethod.single_combineLimit) {
//					System.out.println("因組合次數大於" + TeddyMethod.single_combineLimit + "，所以跳出");
					TeddyMethod.isCombineLimit = true;
					break;
				}
				for (int index = nowIndex ; index < combineLink.size() ; index++){
					cset = combineLink.get(index).getNextCase(this.iMap, this.mMap);

					// 每個資訊點的cm_link不可能只有一個集合，所以可以不用考慮只有一個集合的狀況
					if (cset != null){	//找到合適的組合
						this.mapUpdate(combineLink.get(index), cset);	//更新集合到地圖之中。
						if (isDebug == true){	 //&& this.isLittleCombine == false && this.isSimulation() == false
//							for (int arrayIndex = 0 ; arrayIndex < combineLink.size() ; arrayIndex++){
//								System.out.print("(" + combineLink.get(arrayIndex).usedIndex + " / " 
//										+ combineLink.get(arrayIndex).cgrid.cmLink.size() + ")");
//								if (arrayIndex < combineLink.size() - 1 || index == combineLink.size() - 1){
//									System.out.print(",\t");
//								}
//							}
//							if (index < combineLink.size() - 1){
//								System.out.println("");
//							}
							
//							System.out.println("mapRecord的地圖-------");
//							System.out.println("this.usedL = " + this.usedL);
//							System.out.println("this.usedM = " + this.usedM);
//							this.showMap("mine");
//							System.out.println("-------mapRecord的地圖");
						}
					}else {	//找不到合適的組合，應該進位了
						while (combineLink.get(index).isCarry() == true){
							combineLink.get(index).setCarryed();
							index--;	//倒退一位

							if (index < 0) break;	// 代表已經進位到第一個了！
						}

						if (combineLink.getFirst().isCarryied() == false){	//第一個index進位時則代表全部都組合完了。
							index--;	//有進位的再減一，因為for迴圈結束會加一。
							continue;
						}else {
							break;
						}
					}

					if (cset != null && index == combineLink.size() - 1){	//紀錄的條件
						if (isDebug == true){
//							CommonField.combineTimes++;
//							if (CommonField.combineTimes > CommonField.max_combineTimes) CommonField.max_combineTimes = CommonField.combineTimes;
//							System.out.print("combineTimes = " + combineTimes + " / Max combine = " + CommonField.max_combineTimes);
							
//							System.out.println("mapRecord的地圖before check-------");
//							System.out.println("this.usedL = " + this.usedL);
//							System.out.println("this.usedM = " + this.usedM);
//							this.showMap("mine");
//							System.out.println("-------mapRecord的地圖");
						}

						if (this.cmCheck() == true){	//檢查是否合法的地圖
							TeddyMethod.total_combineTimes = TeddyMethod.total_combineTimes.add(BigDecimal.valueOf(1));
							TeddyMethod.single_combineTimes++;
							
							if (isDebug == true){
								System.out.println("mapRecord的地圖after check-------");
								System.out.println("this.usedL = " + this.usedL);
								System.out.println("this.usedM = " + this.usedM);
								this.showMap("mine");
								System.out.println("-------mapRecord的地圖");
//								System.out.print("all combineTimes = " + total_combineTimes.toString());
//								System.out.println(" usedL = " + this.usedL + ", usedM = " + this.usedM);
							}
							this.mapRecord();		//地圖紀錄
							
							if (isDebug == true && this.isLittleCombine == false && this.isSimulation() == false){
								System.out.println("show record map in combineAllMap");
								for (int Iindex = 0 ; Iindex <= 9 ; Iindex++){
									if (Iindex == 9 || (Iindex < 9 && this.isUseGuessMethod_forecast() == true))
										this.showMap("record", Iindex);
								}
							}
						}else {							
							if (isDebug == true){
								System.out.println();
							}
						}
					}
					
					nowIndex = index;
					
					// 印出現在的combine次數
					if (Experiment.isShowNowSitulation == true){
						if (System.currentTimeMillis() - TeddyMethod.lastShowTime > TeddyMethod.showTimePeriod){	// 每隔一秒印出一次Combine 次數
							System.out.println("\nThis is CombineAllMap");
							System.out.println("Simulation = " + this.isSimulation());
							System.out.println("Time = " + (double)(System.currentTimeMillis()-startTime)/1000 + "s");
							System.out.println("Now Combine Times = " + total_combineTimes.toString());
							System.out.println("Now Save map number = " + TeddyMethod.total_copyMap.toString());
							TeddyMethod.lastShowTime = System.currentTimeMillis();		// 更新時間
						}
					}
				}
				
				if (this.isUseBlockCombine() == true && this.isLittleCombine == true){
					// 當使用獨立分區時，若有超過一種使用地雷種類則跳出
					if (this.isUseBlockCombine_unique() == true && this.isCheckDependent() == true){
						// 如果不是獨立區域，暫存地圖就沒有必要留著了。
						this.rMap.mapLink.clear();
						break;
//						for (int index = 0 ; index < this.rMap.mapLink.size() ; index++){
//							this.rMap.mapLink.get(index).copyMap = null;			
//						}
						
					}
//					else if (this.isUseBlockCombine_complete() == true){
//					// do nothing
//					}
				}
			}
		}

		private void countCaseNumber(){		// 計算所有的可能數目，當有資訊點時使用
			if (Experiment.isLogicTest == true){
				if (this.rMap.totalPossibleN.intValue() != 0){
					System.out.println("error_in_countCaseNumber，因為總case數的初始值不為零");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_countCaseNumber_因為總case數的初始值不為零");
					}
				}

				if (this.rMap.mapLink.isEmpty() == true){
					System.out.println("error_in_countCaseNumber，因為在沒有資訊點使用");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_countCaseNumber，因為在沒有資訊點使用");
					}
				}
			}

			for (int index = 0 ; index < this.rMap.mapLink.size() ; index++){
				this.rMap.mapLink.get(index).countCaseNumber();
				this.rMap.totalPossibleN = this.rMap.totalPossibleN
						.add(this.rMap.mapLink.get(index).get_caseNumber()
								.multiply(BigDecimal.valueOf(this.rMap.mapLink.get(index).get_recordTimes())));
			}

			// 算出case的比例
			for (int index = 0 ; index < this.rMap.mapLink.size() ; index++){
				this.rMap.mapLink.get(index).countCaseRatio(this.rMap.totalPossibleN);
			}
		}

		// 轉換記錄專用-----------------------------------------
		
		// 將分區的記錄轉換成非分區的記錄，獨立分區化(Independent Partition Method)專用
		private void transferToGlobalRecord_on_IPM(LinkedList<CombineMap> maplink, CombineMap thisMap){		
			/*
			 * 全部有可能有四種狀況
			 * case 1. 有非獨立區域	有獨立區域 -> 將原有的記錄加上獨立的區域
			 * case 2. 有非獨立區域	g無獨立區域 -> 什麼都不做，因為就等於非分區的狀況
			 * case 3. 無非獨立區域	有獨立區域 -> 新增記錄，等同於完成分區化
			 * case 4. 無非獨立區域	無獨立區域 -> 不可能在這裡發生
			 */
			
			if (thisMap != null && maplink.isEmpty() == false){		// case 1
				for (RecordMap rmap : thisMap.rMap.mapLink){
					for (CombineMap cmap : maplink){
						rmap.record_multiple(cmap.rMap.mapLink.getFirst());
					}
				}
			}else if (thisMap != null && maplink.isEmpty() == true){	// case 2
				// do nothing
			}else if (thisMap == null && maplink.isEmpty() == false){	// case 3
				this.transferToGlobalRecord_on_IPM(maplink);
			}
			
			if (Experiment.isLogicTest == true){
				if (thisMap == null && maplink.isEmpty() == true){
					System.out.println("error_in_transferToGlobalRecord，出現case 4");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_transferToGlobalRecord，出現case 4");
					}
				}
			}
		}
		
		// 計算獨立分區化(Independent Partition Method)的數字機率。
		private void transferToGlobalRecord_number_probability_on_IPM(LinkedList<CombineMap> maplink, CombineMap thisMap){
			/*
			 * 全部有可能有四種狀況
			 * case 1. 有非獨立區域	有獨立區域 -> 依照非獨立區域的記錄所佔比例，進行獨立分區的數字機率計算
			 * case 2. 有非獨立區域	無獨立區域 -> 什麼都不做，因為就等於非分區的狀況
			 * case 3. 無非獨立區域	有獨立區域 -> 計算所有獨立區域的使用空格數和地雷數，再分別計算獨立區域的數字機率
			 * case 4. 無非獨立區域	無獨立區域 -> 不可能在這裡發生
			 */

			double[][] usedInfo;
			TeddyMethodTool tool = new TeddyMethodTool(); 
			if (thisMap != null && maplink.isEmpty() == false){		// case 1				
				// 求得總案例數
				BigDecimal totalCase = BigDecimal.valueOf(0);
				BigDecimal[] caseNumber = new BigDecimal[thisMap.rMap.mapLink.size()];

				for (int index = 0 ; index < thisMap.rMap.mapLink.size() ; index++){
					RecordMap rmap = thisMap.rMap.mapLink.get(index);
					caseNumber[index] = tool.conbineNumber(this.get_map().getRemainInitialGrid() - rmap.get_usedL(), 
							this.get_map().getRemainMineNumber() - rmap.get_usedM()).multiply(BigDecimal.valueOf(rmap.get_recordTimes()));
					totalCase = totalCase.add(caseNumber[index]);
				}

				usedInfo = new double[thisMap.rMap.mapLink.size()][3];

				for (int index = 0 ; index < thisMap.rMap.mapLink.size() ; index++){
					RecordMap rmap = thisMap.rMap.mapLink.get(index);
					usedInfo[index][0] = rmap.get_usedL();
					usedInfo[index][1] = rmap.get_usedM();
					usedInfo[index][2] = caseNumber[index].divide(totalCase, 16, BigDecimal.ROUND_HALF_UP).doubleValue();
				}

				for (CombineMap cmap : maplink){
					this.transferToGlobalRecord_number_probability_on_IPM(cmap, usedInfo);
				}
			}else if (thisMap != null && maplink.isEmpty() == true){	// case 2
				// do nothing
			}else if (thisMap == null && maplink.isEmpty() == false){	// case 3
				int usedL = 0, usedM = 0;
				double caseRatio = 0;

				for (CombineMap cmap : maplink){
					usedL += cmap.rMap.mapLink.getFirst().get_usedL();	// 計算總和的使用空格數
					usedM += cmap.rMap.mapLink.getFirst().get_usedM();	// 計算總和的使用地雷數
				}
				caseRatio = 1;

				usedInfo = new double[1][3];
				usedInfo[0][0] = usedL;
				usedInfo[0][1] = usedM;
				usedInfo[0][2] = caseRatio;

				for (CombineMap cmap : maplink){
					this.transferToGlobalRecord_number_probability_on_IPM(cmap, usedInfo);
				}
			}

			if (Experiment.isLogicTest == true){
				if (thisMap == null && maplink.isEmpty() == true){
					System.out.println("error_in_transferToGlobalRecord，出現case 4");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_transferToGlobalRecord，出現case 4");
					}
				}
			}
		}

		private void transferToGlobalRecord_number_probability_on_IPM(CombineMap blockMap, double[][] countInfo){		// 計算獨立分區化的數字機率。
			/*
			 * 參考筆記34_20130603
			 */

			if (isDebug == true){
				System.out.println("trans start獨立區域的數字機率");
			}

			if (Experiment.isLogicTest == true){
				if (blockMap.rMap.mapLink.size() > 1){ 
					System.out.println("error_in_transferToGlobalRecord_number_probability_on_IPM，出現非獨立區域");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("terror_in_ransferToGlobalRecord_number_probability_on_IPM，出現非獨立區域");
					}
				}
				double totalRatio = 0;
				for (int index = 0 ; index < countInfo.length ; index++){
					totalRatio += countInfo[index][2];
				}

				if (totalRatio > 1.001 || totalRatio < 0.999){
					//						if (totalRatio != 1){
					System.out.println("totalRatio = " + totalRatio);
					System.out.println("error_in_transferToGlobalRecord_number_probability_on_IPM，總和比例不為1");
					for (int index = 0 ; index < countInfo.length ; index++){
						System.out.println("ratio" + index + " = " + countInfo[index][2]);
					}
				}
			}

			int mapL = this.get_map().getRemainInitialGrid();
			int mapM = this.get_map().getRemainMineNumber();
			int usedL, usedM, remainL, remainM, besideL, besideM;
			double caseRatio, strangeMinePossible, numP;
			RecordMap rmap = blockMap.rMap.mapLink.getFirst();

			for (boolean[][] copymap : rmap.copyMap){
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (this.get_map().isInitial(y, x) == true &&
								(blockMap.isInformationGrid(y, x) == true || blockMap.isSubinformationGrid(y, x) == true)){
							for (int index = 0 ; index < countInfo.length ; index++){
								usedL = (int)countInfo[index][0];
								usedM = (int)countInfo[index][1];
								caseRatio = countInfo[index][2];
								remainL = mapL - usedL;
								remainM = mapM - usedM;
								strangeMinePossible = (double)remainM / remainL;
								besideL = 0;
								besideM = 0;

								if (blockMap.isInformationGrid(y, x) == true){		// 資訊點
									if (copymap[y][x] == TeddyMethod.notMine){
										for (int j = y-1 ; j <= y+1 ; j++){
											for (int i = x-1 ; i <= x+1 ; i++){
												if (i != x || j != y){		//中間的點不用理
													if (this.get_map().isInBoard(j, i) == true){
														if (blockMap.isInformationGrid(j, i) == false && this.get_map().isInitial(j, i) == true){
															besideL++;	//未假設的未打開空格數
														}else if ((blockMap.isInformationGrid(j, i) == true && copymap[j][i] == TeddyMethod.isMine) 
																|| this.get_map().isFlag(j, i) == true){
															besideM++;
														}
													}
												}
											}
										}
										for (int num = 0 ; num < 9 ; num++){
											numP = this.detail_possible_forecast(num, besideL, besideM, remainL, remainM, true);
											numP *= caseRatio;
											rmap.recordMap[num][y][x] += numP;
										}
									}else {
										//												rmap.recordMap[9][y][x] += caseRatio;
									}
								}else if (blockMap.isSubinformationGrid(y, x) == true){		// 次資訊點
									for (int j = y-1 ; j <= y+1 ; j++){
										for (int i = x-1 ; i <= x+1 ; i++){
											if (i != x || j != y){		//中間的點不用理
												if (this.get_map().isInBoard(j, i) == true){
													if (blockMap.isInformationGrid(j, i) == false && this.get_map().isInitial(j, i) == true){
														besideL++;	//未假設的未打開空格數
													}else if ((blockMap.isInformationGrid(j, i) == true && copymap[j][i] == TeddyMethod.isMine) 
															|| this.get_map().isFlag(j, i) == true){
														besideM++;
													}
												}
											}
										}
									}
									for (int num = 0 ; num < 9 ; num++){
										numP = this.detail_possible_forecast(num, besideL, besideM, remainL, remainM, false);
										numP *= (1-strangeMinePossible);
										numP *= caseRatio;
										rmap.recordMap[num][y][x] += numP;
									}
								}

								// 為了isNeedCombineInSimulation而搜集需要的資料
								if (this.isUseCombineInEvaluateJudge() == true){
									if (besideL > this.besideL_Map[y][x]){
										this.besideL_Map[y][x] = besideL;
									}

									if (besideM > this.besideM_max_Map[y][x]){
										this.besideM_max_Map[y][x] = besideM;
									}

									if (besideM < this.besideM_min_Map[y][x]){
										this.besideM_min_Map[y][x] = besideM;
									}	
								}
							}
						}
					}
				}
			}
		}
		
		// 將分區的記錄轉換成非分區的記錄，獨立分區化(Independent Partition Method)專用，在只有獨立分區時使用
		private void transferToGlobalRecord_on_IPM(LinkedList<CombineMap> maplink){
			// 參考方法private void transferToGlobalRecord_mine_probability_on_CPM(LinkedList<CombineMap> maplink)
			double combineCaseTimes, multipleCombine;
			int usedL, usedM;
			int[][] all_iMap = new int[this.get_map().getMapY()][this.get_map().getMapX()];
			
			usedL = 0;
			for (int index = 0 ; index < maplink.size() ; index++){
				usedL += maplink.get(index).rMap.get_recordMap().get_usedL();	// 計算所使用的總空格數
				maplink.get(index).rMap.initail_record_index();		// 索引初始化
				// 組合合局地圖
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						all_iMap[y][x] += maplink.get(index).iMap[y][x];
					}
				}
			}
			
			// 新建所有的使用地雷數和記錄次數
			while (maplink.getFirst().rMap.isCarryied() == false){	//第一個資訊點的區域是否已經跑完了
				combineCaseTimes = 1;
				usedM = 0;
				
				// 計算總記錄次數
				for (int index = 0 ; index < maplink.size() ; index++){
					combineCaseTimes *= maplink.get(index).rMap.get_recordMap().get_recordTimes();
					usedM += maplink.get(index).rMap.get_recordMap().get_usedM();
				}
				
				if (this.isLegalCombineMap(usedL, usedM, this.get_map()) == true){
					// 在使用地雷數為usedM的記錄的記錄次數上加上newCombineNumber
					this.rMap.record_multiple(usedL, usedM, combineCaseTimes);
				}
				
				maplink.getLast().rMap.indexAddOne();

				if (maplink.getLast().rMap.isCarry() == true){	// 確實的跑完每一個組合
					int index = maplink.size() - 1;
					do {
						maplink.get(index).rMap.setCarryed();
						index--;	//倒退一位
						if (index < 0) {
							break;	// 代表已經進位到第一個了！
						}else {
							maplink.get(index).rMap.indexAddOne();
						}
					}while (maplink.get(index).rMap.isCarry() == true);
				}
			}
			
			for (int index = 0 ; index < maplink.size() ; index++){
				maplink.get(index).rMap.initail_record_index();		// 索引初始化
			}
			
			// 計算記錄次數並加總到原本記錄中
			while (maplink.getFirst().rMap.isCarryied() == false){	//第一個資訊點的區域是否已經跑完了
				combineCaseTimes = 1;
				usedM = 0;

				// 計算總記錄次數
				for (int index = 0 ; index < maplink.size() ; index++){
					combineCaseTimes *= maplink.get(index).rMap.get_recordMap().get_recordTimes();
					usedM += maplink.get(index).rMap.get_recordMap().get_usedM();
				}

				if (this.isLegalCombineMap(usedL, usedM, this.get_map()) == true){
					for (int index = 0 ; index < maplink.size() ; index++){
						multipleCombine = combineCaseTimes / maplink.get(index).rMap.get_recordMap().get_recordTimes();
						this.rMap.record_multiple_on_IPM(maplink.get(index), all_iMap, usedL, usedM, multipleCombine);
					}
					this.rMap.record_multiple_on_IPM(null, all_iMap, usedL, usedM, combineCaseTimes);	// 處理陌生點的數值
				}

				maplink.getLast().rMap.indexAddOne();

				if (maplink.getLast().rMap.isCarry() == true){	// 確實的跑完每一個組合
					int index = maplink.size() - 1;
					do {
						maplink.get(index).rMap.setCarryed();
						index--;	//倒退一位
						if (index < 0) {
							break;	// 代表已經進位到第一個了！
						}else {
							maplink.get(index).rMap.indexAddOne();
						}
					}while (maplink.get(index).rMap.isCarry() == true);
				}
			}
			
			// 印出record 次數
			if (isDebug == true){
				System.out.println("show record map in transferToGlobalRecord");
				rMap.showRecordMap(9);
			}
		}
		
		// 將分區的記錄轉換成非分區的記錄，完全分區化(Complete Partition Method)專用
		private void transferToGlobalRecord_mine_probability_on_CPM(LinkedList<CombineMap> maplink){		
			double combineCaseTimes, multipleCombine;
			int usedL, usedM;
			int[][] all_iMap = new int[this.get_map().getMapY()][this.get_map().getMapX()];
			
			usedL = 0;
			for (int index = 0 ; index < maplink.size() ; index++){
				usedL += maplink.get(index).rMap.get_recordMap().get_usedL();	// 計算所使用的總空格數
				maplink.get(index).rMap.initail_record_index();		// 索引初始化
				// 組合合局地圖
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						all_iMap[y][x] += maplink.get(index).iMap[y][x];
					}
				}
			}
			
			// 新建所有的使用地雷數和記錄次數
			while (maplink.getFirst().rMap.isCarryied() == false){	//第一個資訊點的區域是否已經跑完了
				combineCaseTimes = 1;
				usedM = 0;
				
				// 計算總記錄次數
				for (int index = 0 ; index < maplink.size() ; index++){
					combineCaseTimes *= maplink.get(index).rMap.get_recordMap().get_recordTimes();
					usedM += maplink.get(index).rMap.get_recordMap().get_usedM();
				}
				
				if (this.isLegalCombineMap(usedL, usedM, this.get_map()) == true){
					// 在使用地雷數為usedM的記錄的記錄次數上加上newCombineNumber
					this.rMap.record_multiple(usedL, usedM, combineCaseTimes);
				}
				
				maplink.getLast().rMap.indexAddOne();

				if (maplink.getLast().rMap.isCarry() == true){	// 確實的跑完每一個組合
					int index = maplink.size() - 1;
					do {
						maplink.get(index).rMap.setCarryed();
						index--;	//倒退一位
						if (index < 0) {
							break;	// 代表已經進位到第一個了！
						}else {
							maplink.get(index).rMap.indexAddOne();
						}
					}while (maplink.get(index).rMap.isCarry() == true);
				}
			}
			
			for (int index = 0 ; index < maplink.size() ; index++){
				maplink.get(index).rMap.initail_record_index();		// 索引初始化
			}
			
			// 計算記錄次數並加總到原本記錄中
			while (maplink.getFirst().rMap.isCarryied() == false){	//第一個資訊點的區域是否已經跑完了
				combineCaseTimes = 1;
				usedM = 0;

				// 計算總記錄次數
				for (int index = 0 ; index < maplink.size() ; index++){
					combineCaseTimes *= maplink.get(index).rMap.get_recordMap().get_recordTimes();
					usedM += maplink.get(index).rMap.get_recordMap().get_usedM();
				}

				if (this.isLegalCombineMap(usedL, usedM, this.get_map()) == true){
					for (int index = 0 ; index < maplink.size() ; index++){
						multipleCombine = combineCaseTimes / maplink.get(index).rMap.get_recordMap().get_recordTimes();
						this.rMap.record_multiple_on_CPM(maplink.get(index), all_iMap, usedL, usedM, multipleCombine);
					}
					this.rMap.record_multiple_on_CPM(null, all_iMap, usedL, usedM, combineCaseTimes);	// 處理陌生點的數值
				}

				maplink.getLast().rMap.indexAddOne();

				if (maplink.getLast().rMap.isCarry() == true){	// 確實的跑完每一個組合
					int index = maplink.size() - 1;
					do {
						maplink.get(index).rMap.setCarryed();
						index--;	//倒退一位
						if (index < 0) {
							break;	// 代表已經進位到第一個了！
						}else {
							maplink.get(index).rMap.indexAddOne();
						}
					}while (maplink.get(index).rMap.isCarry() == true);
				}
			}
			
			// 印出record 次數
			if (isDebug == true){
				System.out.println("show record map in transferToGlobalRecord");
				rMap.showRecordMap(9);
			}
		}
		
		// 將完全分區(Complete Partition Method)的記錄經處理後，計算出數字機率
		private void transferToGlobalRecord_number_probability_on_CPM(LinkedList<Player_Grid> plink){		
			/*
			 * 參考筆記29_20130220
			 * step 1.
			 * 	依照候選點plink，分成區域資訊點blockGridLink、完全陌生點completeStrangeGridLink和非完全陌生點strangeGridLink(有部分資訊，例如有旗子)
			 * step 2.
			 * 	依照blockGridLink的每一點，可能將某些小區域結合成大區域
			 * step 3.
			 * 	以大區域為單位，用小區域的地圖組合成大地圖，並計算在此大區域內的候選點的地雷機率，並加權其他區域的記錄次數。記錄在相符的地雷數記錄(recordMap)。
			 * step 4. 
			 * 	在組合完畢後，再將在記錄(recordMap)中的數字除以總記錄次數，即為機率，再乘於比例(caseRatio)。即為數字機率
			 * step 5.
			 * 	完全陌生點和非完全陌生點是在最後時處理，依照舊有方法即可。
			 */
			
			if (isDebug == true){
				System.out.println("trans start");
			}
			
			int besideL, besideM, remainM, remainL;
			remainL = this.get_map().getRemainInitialGrid() - this.usedL;	// 剩餘空格數
			int y, x;
			
			// step 1.
			LinkedList<Player_Grid> blockGridLink = null, completeStrangeGridLink = null, strangeGridLink = null;
			for (Player_Grid p : plink){
				if (this.isStrangeGrid(p) == true){		// 是否是陌生點
					if (this.isCompleteStrangeGrid(p) == true){ // 是否是完全陌生點
						//  加入完全陌生點
						if (completeStrangeGridLink == null){
							completeStrangeGridLink = new LinkedList<Player_Grid>(); 
						}
						completeStrangeGridLink.addLast(p);
					}else {
						// 加入非完全陌生點
						if (strangeGridLink == null){
							strangeGridLink = new LinkedList<Player_Grid>();
						}
						strangeGridLink.addLast(p);
					}
				}else {
					// 加入區域資訊點
					if (blockGridLink == null){
						blockGridLink = new LinkedList<Player_Grid>();
					}
					blockGridLink.addLast(p);
				}
				
				if (Experiment.isLogicTest == true){
					if (this.get_map().isInitial(p) == false){ 
						System.out.println("error_in_transferToGlobalRecord_number_probability，候選點已按過");
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_transferToGlobalRecord_number_probability，候選點已按過");
						}
					}
				}
			}
			
			// step 2.
			if (blockGridLink != null){		// 如果有資訊點
				boolean isSet;		// 是否已經將候選點分區了。
				LinkedList<Big_block_combineMap> blockLink = new LinkedList<Big_block_combineMap>(); 
				int [][]blockMap = new int[this.get_map().getMapY()][this.get_map().getMapX()];		// 區域地圖
				LinkedList<CombineMap> otherBlock = new LinkedList<CombineMap>();		// 其他區域
				
				for (CombineMap cmap : this.bcMaplink.cMapLink){	// 複製區域
					otherBlock.addLast(cmap);
				}
				
				int thisBlockNumber, lowBlockIndex, highBlockIndex;
				for (Player_Grid p : blockGridLink){
					x = p.getX();
					y = p.getY();
					isSet = false;
					thisBlockNumber = 0;
					for (int j = y - 1 ; j <= y + 1 ; j++){
						for (int i = x - 1 ; i <= x + 1 ; i++){
							if (this.get_map().isInBoard(j, i) == true){
								if (blockMap[j][i] == 0 && this.bcMaplink.bMap[j][i] > 0){	// 找到新的區域
									if (isSet == false){	// 沒有設定區域過
										thisBlockNumber = blockLink.size() + 1;		// 指定所屬區域
										blockLink.addLast(new Big_block_combineMap(thisBlockNumber, this));
									}
									
									blockLink.get(thisBlockNumber-1).add_block(this.bcMaplink.cMapLink.get(this.bcMaplink.bMap[j][i]-1));	// 加入區域
									this.copyMap(blockMap, this.bcMaplink.cMapLink.get(this.bcMaplink.bMap[j][i]-1).iMap, thisBlockNumber);
									
									// 刪除相關區域
									for (int index = 0 ; index < otherBlock.size() ; index++){
										if (otherBlock.get(index).get_index() == this.bcMaplink.bMap[j][i]){
											otherBlock.remove(index);	
											break;
										}
									}
								}else if (blockMap[j][i] == 0 && this.bcMaplink.bMap[j][i] == 0){		// 完全沒有資訊
									continue;
								}else if (blockMap[j][i] > 0){			// 找到已設定的區域
									if (thisBlockNumber == 0){
										thisBlockNumber = blockMap[j][i];		// 指定所屬區域
									}else if (thisBlockNumber != blockMap[j][i]){	// 找到其他的區域，進行合併
										lowBlockIndex = Math.min(thisBlockNumber, blockMap[j][i]);
										highBlockIndex = Math.max(thisBlockNumber, blockMap[j][i]);
										blockLink.get(lowBlockIndex-1).merge(blockLink.get(highBlockIndex-1));	// 合併成大區域
										thisBlockNumber = lowBlockIndex;		// 指定所屬區域
										this.replaceMap(blockMap, highBlockIndex, lowBlockIndex);

										if (highBlockIndex < blockLink.size()){
											this.replaceMap(blockMap, highBlockIndex);
											for (int index = highBlockIndex ; index < blockLink.size() ; index++){
												blockLink.get(index).index_Subtract_one();		// 索引減一
											}
										}
										blockLink.remove(highBlockIndex-1);	// 刪除區域
									}
								}
								
								if (Experiment.isLogicTest == true){
									if (thisBlockNumber == 0){
										System.out.println("error_in_transferToGlobalRecord_number_probability，未找到所屬區域");
										if (Experiment.isStepByStepDebug == true){
											UI.safeMap_In_debug("error_in_transferToGlobalRecord_number_probability，未找到所屬區域");
										}
									}
								}

								if (isSet == false){
									blockLink.get(thisBlockNumber-1).add_gridLink(p);		// 加入候選點
									isSet = true;		// 只能一次加入候選點
								}
							}
						}
					}
					
					if (Experiment.isLogicTest == true){
						if (isSet == false){ 
							System.out.println("error_in_transferToGlobalRecord_number_probability，未加入候選點");
							if (Experiment.isStepByStepDebug == true){
								UI.safeMap_In_debug("error_in_transferToGlobalRecord_number_probability，未加入候選點");
							}
						}
					}
				}
				
				if (isDebug == true){
					// show block map-------------
					System.out.println("show block map in transferToGlobalRecord_number_probability");
					AI.showMap_data(this.get_map(), blockMap);
					// -------------show block map
				}
				
				// step 3.
				LinkedList<CombineMap> weightMapLink = new LinkedList<CombineMap>();	// 非處理中的加權區域
				boolean[][] combineMap;
				double[][][] possible;		// 數字機率暫存區
				int[][] besideL_Map = new int[this.get_map().getMapY()][this.get_map().getMapX()];
				int[][] besideM_Map = new int[this.get_map().getMapY()][this.get_map().getMapX()];
				int blockUsedM, usedM;
				double weight;
				
				for (Big_block_combineMap bcmap : blockLink){
					weightMapLink.clear();
					for (Big_block_combineMap cmap : blockLink){
						if (cmap.get_index() != bcmap.get_index()){
							for (CombineMap ccmap : cmap.get_blockLink()){
								weightMapLink.addLast(ccmap);				// 其他大區域的區域
							}
						}
					}
					
					for (CombineMap cmap : otherBlock){
						weightMapLink.addLast(cmap);						// 沒有關係的區域
					}
					
					// 參考方法：transferToGlobalRecord_mine_probability(LinkedList<combineMap> maplink)
					bcmap.initial();
					while(bcmap.isCombineOver() == false){
						combineMap = bcmap.getNextCombineMap();
						blockUsedM = bcmap.getUsedMine();
						
						for (Player_Grid g : bcmap.gridLink){
							x = g.getX();
							y = g.getY();
							
							if ((blockMap[y][x] > 0 && combineMap[y][x] == TeddyMethod.notMine) || blockMap[y][x] == 0){
								besideL = 0;
								besideM = 0;
								
								for (int j = y-1 ; j <= y+1 ; j++){
									for (int i = x-1 ; i <= x+1 ; i++){
										if (i != x || j != y){		//中間的點不用理
											if (this.get_map().isInBoard(j, i) == true){
												if (blockMap[j][i] == 0 && this.get_map().isInitial(j, i) == true){
													besideL++;	//未假設的未打開空格數
												}else if ((blockMap[j][i] > 0 && combineMap[j][i] == TeddyMethod.isMine) || this.get_map().isFlag(j, i) == true){
													besideM++;
												}
											}
										}
									}
								}
								
								besideL_Map[y][x] = besideL;
								besideM_Map[y][x] = besideM;
								
								// 為了isNeedCombineInSimulation而搜集需要的資料
								if (this.isUseCombineInEvaluateJudge() == true){
									if (besideL > this.besideL_Map[y][x]){
										this.besideL_Map[y][x] = besideL;
									}
									
									if (besideM > this.besideM_max_Map[y][x]){
										this.besideM_max_Map[y][x] = besideM;
									}
									
									if (besideM < this.besideM_min_Map[y][x]){
										this.besideM_min_Map[y][x] = besideM;
									}	
								}
							}
						}
						
						if (isDebug == true){
							// show combine map----------
							System.out.println("show information in transferToGlobalRecord_number_probability");
							System.out.println("usedM = " + blockUsedM);
							System.out.println("combineMap");
							this.showMap_boolean(bcmap, blockMap, combineMap);
							System.out.println("besideL Map");
							AI.showMap_data(this.get_map(), besideL_Map);
							System.out.println("besideM Map");
							AI.showMap_data(this.get_map(), besideM_Map);
							// ----------show combine map
						}
						
						if (weightMapLink.isEmpty() == false){		// 如果有其他區域的話
							for (int index = 0 ; index < weightMapLink.size() ; index++){
								weightMapLink.get(index).rMap.initail_record_index();		// 索引初始化
							}
							
							while (weightMapLink.getFirst().rMap.isCarryied() == false){	//第一個資訊點的區域是否已經跑完了
								weight = 1;		// 加權數預設為1
								usedM = blockUsedM;
								// 計算總記錄次數
								for (int index = 0 ; index < weightMapLink.size() ; index++){
									weight *= weightMapLink.get(index).rMap.get_recordMap().get_recordTimes();
									usedM += weightMapLink.get(index).rMap.get_recordMap().get_usedM();
								}
								
								if (this.isLegalCombineMap(this.usedL, usedM, this.get_map()) == true){	// 是否是合法的地雷使用數量
									remainM = this.get_map().getRemainMineNumber() - usedM;
									possible = this.getNumberPosible(bcmap, remainL, remainM, combineMap, blockMap, besideL_Map, besideM_Map);
									
									// 加權後加到原本的記錄中
									this.rMap.record_multiple(possible, usedM, weight);
								}
								
								weightMapLink.getLast().rMap.indexAddOne();

								if (weightMapLink.getLast().rMap.isCarry() == true){	// 確實的跑完每一個組合
									int index = weightMapLink.size() - 1;
									do {
										weightMapLink.get(index).rMap.setCarryed();
										index--;	//倒退一位
										if (index < 0) {
											break;	// 代表已經進位到第一個了！
										}else {
											weightMapLink.get(index).rMap.indexAddOne();
										}
									}while (weightMapLink.get(index).rMap.isCarry() == true);
								}
							}
						}else {
							weight = 1;		// 加權數預設為1
							usedM = blockUsedM;
							if (this.isLegalCombineMap(this.usedL, usedM, this.get_map()) == true){	// 是否是合法的地雷使用數量
								remainM = this.get_map().getRemainMineNumber() - usedM;
								possible = this.getNumberPosible(bcmap, remainL, remainM, combineMap, blockMap, besideL_Map, besideM_Map);
								// 加權後加到原本的記錄中
								this.rMap.record_multiple(possible, usedM, weight);
							}
						}
						bcmap.indexAddOne();
						
						if (Experiment.isShowNowSitulation == true){
							if (System.currentTimeMillis() - TeddyMethod.lastShowTime > TeddyMethod.showTimePeriod){	// 每隔一秒印出一次Combine 次數
								System.out.println("\nThis is transferToGlobalRecord_number_probability");
								System.out.println("Time = " + (double)(System.currentTimeMillis()-startTime)/1000 + "s");
								System.out.println("Now Combine Times = " + total_combineTimes.toString());
								System.out.println("Now Save map number = " + TeddyMethod.total_copyMap.toString());
								TeddyMethod.lastShowTime = System.currentTimeMillis();		// 更新時間
							}
						}
					}
				}
				
				// step 4.
				for (Player_Grid p : blockGridLink){
					x = p.getX();
					y = p.getY();
					
					// 機率轉換並輸入
					for (RecordMap rmap : this.rMap.mapLink){
						for (int index = 0 ; index < 9 ; index++){
							this.rMap.possibleMap[index][y][x] += 
									(rmap.recordMap[index][y][x] / rmap.get_recordTimes()) * rmap.get_caseRatio();
						}
					}
				}
			}
			
			// step 5.
			double noMinePossible, possible;
			
			if (strangeGridLink != null){				// 如果有非完全陌生點
				for (Player_Grid p : strangeGridLink){
					y = p.getY();
					x = p.getX();
					
					besideL = 0;
					besideM = 0;

					for (int j = y-1 ; j <= y+1 ; j++){
						for (int i = x-1 ; i <= x+1 ; i++){
							if (i != x || j != y){		//中間的點不用理
								if (this.get_map().isInBoard(j, i) == true){
									if (this.get_map().isInitial(j, i) == true){
										besideL++;	//未假設的未打開空格數
									}else if (this.get_map().isFlag(j, i) == true){
										besideM++;
									}
								}
							}
						}
					}
					
					// 為了isNeedCombineInSimulation而搜集需要的資料
					if (this.isUseCombineInEvaluateJudge() == true){
						if (besideL > this.besideL_Map[y][x]){
							this.besideL_Map[y][x] = besideL;
						}
						
						if (besideM > this.besideM_max_Map[y][x]){
							this.besideM_max_Map[y][x] = besideM;
						}
						
						if (besideM < this.besideM_min_Map[y][x]){
							this.besideM_min_Map[y][x] = besideM;
						}	
					}
					
					if (this.rMap.mapLink.isEmpty() == false){		// rMap有使用地雷的紀錄
						for (RecordMap rmap : this.rMap.mapLink){
							remainM = this.get_map().getRemainMineNumber() - rmap.get_usedM();
							noMinePossible = 1 - (double)remainM / remainL;
							for (int index = 0 ; index < 9 ; index++){
								possible = this.detail_possible_forecast(index, besideL, besideM, remainL, remainM, false);
								this.rMap.possibleMap[index][y][x] += (noMinePossible * possible) * rmap.get_caseRatio();
							}
						}
					}else {
						remainM = this.get_map().getRemainMineNumber();
						noMinePossible = 1 - (double)remainM / remainL;
						
						for (int index = 0 ; index < 9 ; index++){
							possible = this.detail_possible_forecast(index, besideL, besideM, remainL, remainM, false);
							this.rMap.possibleMap[index][y][x] = noMinePossible * possible;
						}
					}
				}
			}
			
			if (completeStrangeGridLink != null){		// 如果有完全陌生點
				boolean isCorner, isCenter;
				// 最佳化處理
				RegGrid reg = null;				// 暫存
				this.corner = new RegGrid();	// 角落
				this.center = new RegGrid();	// 中央
				this.edge = new RegGrid();		// 邊
				
				
				for (Player_Grid p : completeStrangeGridLink){
					y = p.getY();
					x = p.getX();
					// 是否是角落
					isCorner = ((y == 0 || y == (this.get_map().getMapY() - 1))
							&& (x == 0 || x == (this.get_map().getMapX() - 1))) ? true : false;
					// 是否是中央
					isCenter = ((y > 0 && y < this.get_map().getMapY() - 1)
							&& (x > 0 && x < this.get_map().getMapX() - 1)) ? true : false;
					
					if ((isCorner == true && corner.isSimulation() == true) 								// 在角落是否有記錄
							|| (isCenter == true && center.isSimulation() == true) 							// 在中央是否有記錄
							|| (isCorner == false && isCenter == false && edge.isSimulation() == true)){	// 在邊是否有記錄
						if (isCorner == true){
							reg = this.corner;
						}else if (isCenter == true){
							reg = this.center;
						}else {
							reg = this.edge;
						}
						
						// 為了isNeedCombineInSimulation而搜集需要的資料
						if (this.isUseCombineInEvaluateJudge() == true){
							this.besideL_Map[y][x] = reg.get_besideL();
							this.besideM_max_Map[y][x] = reg.get_besideM_max();
							this.besideM_min_Map[y][x] = reg.get_besideM_min();
						}
						
						// 複製數值
						for (int index = 9 ; index >= 0 ; index--){
							this.rMap.possibleMap[index][y][x] = reg.get_possible(index);
						}
					}else {
						besideL = 0;
						besideM = 0;

						for (int j = y-1 ; j <= y+1 ; j++){
							for (int i = x-1 ; i <= x+1 ; i++){
								if (i != x || j != y){		//中間的點不用理
									if (this.get_map().isInBoard(j, i) == true){
										if (this.get_map().isInitial(j, i) == true){
											besideL++;	//未假設的未打開空格數
										}else if (this.get_map().isFlag(j, i) == true){
											besideM++;
										}
									}
								}
							}
						}
						
						// 為了isNeedCombineInSimulation而搜集需要的資料
						if (this.isUseCombineInEvaluateJudge() == true){
							if (besideL > this.besideL_Map[y][x]){
								this.besideL_Map[y][x] = besideL;
							}
							
							if (besideM > this.besideM_max_Map[y][x]){
								this.besideM_max_Map[y][x] = besideM;
							}
							
							if (besideM < this.besideM_min_Map[y][x]){
								this.besideM_min_Map[y][x] = besideM;
							}	
						}
						
						if (this.rMap.mapLink.isEmpty() == false){		// rMap有使用地雷的紀錄
							for (RecordMap rmap : this.rMap.mapLink){
								remainM = this.get_map().getRemainMineNumber() - rmap.get_usedM();
								noMinePossible = 1 - (double)remainM / remainL;
								for (int index = 0 ; index < 9 ; index++){
									possible = this.detail_possible_forecast(index, besideL, besideM, remainL, remainM, false);
									this.rMap.possibleMap[index][y][x] += (noMinePossible * possible) * rmap.get_caseRatio();
								}
							}
						}else {
							remainM = this.get_map().getRemainMineNumber();
							noMinePossible = 1 - (double)remainM / remainL;
							
							for (int index = 0 ; index < 9 ; index++){
								possible = this.detail_possible_forecast(index, besideL, besideM, remainL, remainM, false);
								this.rMap.possibleMap[index][y][x] = noMinePossible * possible;
							}
						}
					}
					
					// 登記暫存資料
					if (((isCorner == true && corner.isSimulation() == false) 
							|| (isCenter == true && center.isSimulation() == false) 
							|| (isCorner == false && isCenter == false && edge.isSimulation() == false))){
						if (isCorner == true){
							reg = corner;
						}else if (isCenter == true){
							reg = center;
						}else {
							reg = edge;
						}
						
						// 為了isNeedCombineInSimulation而搜集需要的資料
						if (this.isUseCombineInEvaluateJudge() == true){
							reg.set_besideInfomation(this.besideL_Map[y][x], this.besideM_max_Map[y][x], this.besideM_min_Map[y][x]);
						}
						
						for (int index = 9 ; index >= 0 ; index--){
							reg.set_possible(index, this.rMap.possibleMap[index][y][x]);
						}
						
						reg.setSimulationed();
					}
				}
			}
			
			if (isDebug == true){
				System.out.println("show record map in transferToGlobalRecord_number_probability");
				for (int index = 0 ; index <= 9 ; index++){
					this.showMap("record", index);
				}

				System.out.println("show possible map in transferToGlobalRecord_number_probability");
				for (int index = 0 ; index <= 10 ; index++){
					this.showMap("possible", index);
				}
			}
			
			if (Experiment.isLogicTest == true){
				for (Player_Grid p : plink){		// 檢查每一個點的機率和
					if (this.isIllegalTotalPosible(p, this.rMap.possibleMap) == true){
						x = p.getX();
						y = p.getY();
						System.out.println("error_in_transferToGlobalRecord_number_probability，異常機率總和");
						System.out.println("isSimulation = " + this.isSimulation());
						System.out.println("run map");
						this.get_map().showMap();
						System.out.println("run grid = " + p.getCoordinate());
						System.out.println("is Strange Grid = " + this.isStrangeGrid(p));
						System.out.println("is Complete Strange Grid = " + this.isCompleteStrangeGrid(p));
						possible = 0;
						for (int index = 0 ; index <= 9 ; index++){
							System.out.println("posible of "+ index + " = " + this.rMap.possibleMap[index][y][x]);
							possible += this.rMap.possibleMap[index][y][x];
						}
						System.out.println("grid total posible = " + possible);
						UI.safeMap_In_debug("error_in_transferToGlobalRecord_number_probability，異常機率總和");
					}
				}
			}
		}
		
		private boolean isIllegalTotalPosible(Player_Grid p, double[][][] possibleMap){		// 總和機率是否違法
			double possible = 0;
			for (int index = 0 ; index <= 9 ; index++){
				possible += possibleMap[index][p.getY()][p.getX()];
			}
			return (possible > 1.01 || possible < 0.99) ? true : false;
		}
		
		// 計算數字機率
		private double[][][] getNumberPosible(Big_block_combineMap bcmap, int remainL, int remainM, boolean[][] combineMap, int[][] blockMap, int[][] besideL_Map, int[][] besideM_Map){
			int x, y;
			boolean isAssume;
			double numberP, strangePossible, noMinePossible;
			strangePossible = (double)remainM/remainL;
			double possible;
			double[][][] possibleMap = new double[9][this.get_map().getMapY()][this.get_map().getMapX()];
			for (Player_Grid g : bcmap.gridLink){
				x = g.getX();
				y = g.getY();
				if (combineMap[y][x] == TeddyMethod.notMine){
					isAssume = (blockMap[y][x] > 0) ? true : false;		//是否是資訊點
					noMinePossible = (isAssume == true) ? 1 : (1 - strangePossible);
					possible = (isAssume == true) ? 0 : strangePossible;
					for (int index = 0 ; index < 9 ; index++){
						numberP = this.detail_possible_forecast(index, besideL_Map[y][x], besideM_Map[y][x], 
								remainL, remainM, isAssume);
						possibleMap[index][y][x] = (noMinePossible * numberP);
						possible += possibleMap[index][y][x];
					}
					
					if (Experiment.isLogicTest == true){
						if (possible > 1.01 || possible < 0.99){ 
							System.out.println("error_in_getNumberPosible，機率總和異常");
							System.out.println("run map");
							this.get_map().showMap();
							System.out.println("remainM = " + remainM);
							System.out.println("run grid = " + g.getCoordinate());
							for (int index = 0 ; index <= 9 ; index++){
								if (index < 9){
									System.out.println("posible of "+ index + " = " + possibleMap[index][y][x]);
								}else {
									if (isAssume == true){
										System.out.println("mine possible = " + 0);
									}else {
										System.out.println("mine possible = " + strangePossible);
									}
								}
								
							}
							System.out.println("grid total posible = " + possible);
							if (Experiment.isStepByStepDebug == true){
								UI.safeMap_In_debug("error_in_getNumberPosible，機率總和異常");
							}
						}
					}
				}
			}
			return possibleMap;
		}
		
		private void showMap_boolean(Big_block_combineMap bcmap, int[][] blockMap, boolean[][] combineMap){
			for (int y = this.get_map().getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (blockMap[y][x] == bcmap.get_index()){
						if (combineMap[y][x] == true){
							System.out.print("T" + "\t");
						}else {
							System.out.print("F" + "\t");
						}
					}else {
						System.out.print("0" + "\t");
					}
				}
				System.out.println("");
			}
			System.out.println("");
		}
		
		private void copyMap(int[][] map, int[][] copyMap, int copyNumber){		// 在copyMap有值的位置，在map中寫入copyNumber的值
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (copyMap[y][x] > 0) map[y][x] = copyNumber;
				}
			}
		}
		
		private void replaceMap(int[][] map, int m, int n){		// 在map中，將m換成n
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (map[y][x] == m) map[y][x] = n;
				}
			}
		}
		
		private void replaceMap(int[][] map, int m){		// 在map中，將比m大的值都減一
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (map[y][x] > m) map[y][x]--;
				}
			}
		}
		
		// 是否是資訊點
		private boolean isInformationGrid(int y, int x){
			return (this.iMap[y][x] > 0) ? true : false;
		}
		
		// 是否是次資訊點
		private boolean isSubinformationGrid(int y, int x){
			// 次資訊點的定義為本身非資訊點，但是3*3的範圍內有資訊點
			boolean subI = false;
			if (this.isInformationGrid(y, x) == false){
				for (int j = y - 1 ; j <= y + 1 ; j++){
					for (int i = x - 1 ; i <= x + 1 ; i++){
						if (this.get_map().isInBoard(j, i) == true && this.isInformationGrid(j, i) == true){
							subI = true;
							break;
						}
					}
					if (subI == true) break;
				}
			}
			return subI;
		}
		
		private boolean isCompleteStrangeGrid(Player_Grid g){
			return this.isCompleteStrangeGrid(g.getY(), g.getX());
		}
		
		private boolean isCompleteStrangeGrid(int y, int x){	// 是否為完全陌生點
			/*
			 * 在5*5的範圍內什麼也沒有，包含資訊點和旗子
			 */
			boolean isCompleteStrange = true;
			for (int j = y - 1 ; j <= y + 1 ; j++){
				for (int i = x - 1 ; i <= x + 1 ; i++){
					if (this.get_map().isInBoard(j, i) == true && (this.iMap[j][i] > 0 || this.get_map().isFlag(j, i) == true)){
						isCompleteStrange = false;
						break;
					}
				}
				if (isCompleteStrange == false) break;
			}
			return isCompleteStrange;
		}
		
		private boolean isStrangeGrid(Player_Grid g){
			return this.isStrangeGrid(g.getY(), g.getX());
		}
		
		private boolean isStrangeGrid(int y, int x){	// 是否為陌生點
			boolean isStrange = true;
			for (int j = y - 1 ; j <= y + 1 ; j++){
				for (int i = x - 1 ; i <= x + 1 ; i++){
					if (this.get_map().isInBoard(j, i) == true && this.iMap[j][i] > 0){
						isStrange = false;
						break;
					}
				}
				if (isStrange == false) break;
			}
			return isStrange;
		}
		
		// -----------------------------------------轉換記錄專用
		
		// isUseCombineInEvaluateJudge專用--------------------
		private boolean isNeedCombineInSimulation(Player_Grid grid, int evaluateNumber){
			if (this.isUseCombineInEvaluateJudge() == true){
				/*
				 * 參考筆記28 20130120
				 * 此為判斷在評價時，需不需要進行combine map，因為此步驟所花費的時間甚多
				 * 在之前的方法中之所以會選擇combine的原因是期待以下四種可能性，所以藉由一些參數來判斷可不可能發生以下狀況。
				 * case 1: 所有的陌生點皆不是地雷，即代表陌生點的地雷機率為 0
				 * case 2: 所有的陌生點皆是地雷，即代表陌生點的地雷機率為 1
				 * case 3: 沒有陌生點、因地雷數過低而導致某些可能性是不可能的、或是因地雷數過高而導致某些可以性是不可能的
				 * 
				 * grid 為所預測的候選點
				 * gl = position lattice 為若p不是地雷的狀況下所減少的陌生點空格數(包括本格)
				 * gm_max = position mines 為若p不是地雷的狀況下所減少的最大地雷數
				 * gm_min = position mines 為若p不是地雷的狀況下所減少的最小地雷數
				 * remainL 為所有的空格數減去所假設的空格數所得到的剩餘空格數
				 * remainM_min 為所有的地雷數減去所假設的最大地雷數所得到的最小剩餘地雷數
				 * remainM_max 為所有的地雷數減去所假設的最小地雷數所得到的最大剩餘地雷數 
				 */
				
				boolean isNeed = false;			// 預設不需要組合
				
				if (Experiment.isLogicTest == true){
					if (this.besideM_max_Map == null || this.besideM_min_Map == null || this.besideL_Map == null){
						System.out.println("error_in_isNeedCombineInSimulation，所需地圖未經過設定");
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_isNeedCombineInSimulation，所需地圖未經過設定");
						}
					}
				}
				
				// 候選點座標
				int x = grid.getX();
				int y = grid.getY();
				int gl, gm_max, gm_min, remainL, remainM_min, remainM_max;
				
				
				remainL = this.get_map().getRemainInitialGrid() - this.usedL;
				remainM_min = (this.rMap.mapLink.isEmpty() == false) ? this.get_map().getRemainMineNumber() - this.rMap.mapLink.getLast().get_usedM()
						: this.get_map().getRemainMineNumber();
				remainM_max = (this.rMap.mapLink.isEmpty() == false) ? this.get_map().getRemainMineNumber() - this.rMap.mapLink.getFirst().get_usedM()
						: this.get_map().getRemainMineNumber();
				
				if (Experiment.isLogicTest == true){
					if (remainL < 0 || remainM_min < 0 || remainM_max < 0){
						System.out.println("error_in_isNeedCombineInSimulation，設定參數異常1");
						System.out.println("remainL = " + remainL);
						System.out.println("mapL = " + this.get_map().getRemainInitialGrid());
						System.out.println("usedL = " + this.usedL);
						System.out.println("remainM_min = " + remainM_min);
						System.out.println("remainM_max = " + remainM_max);
						System.out.println("mapM = " + this.get_map().getRemainMineNumber());
						System.out.println("usedM_min = " + this.rMap.mapLink.getFirst().get_usedM());
						System.out.println("usedM_max = " + this.rMap.mapLink.getLast().get_usedM());
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_isNeedCombineInSimulation，設定參數異常1");
						}
					}
				}
				
				if (this.isStrangeGrid(grid) == true){		// 是否是陌生點
					int besideL = 0;
					int besideM = 0;

					for (int j = y-1 ; j <= y+1 ; j++){
						for (int i = x-1 ; i <= x+1 ; i++){
							if (i != x || j != y){		//中間的點不用理
								if (this.get_map().isInBoard(j, i) == true){
									if (this.get_map().isInitial(j, i) == true){
										besideL++;	//未假設的未打開空格數
									}else if (this.get_map().isFlag(j, i) == true){
										besideM++;
									}
								}
							}
						}
					}
					
					gl = besideL + 1;		// 需加入本身的那一點
					gm_min = evaluateNumber - besideM;
					gm_max = gm_min;
				}else {
					gl = (this.isInformationGrid(y, x) == true) ? this.besideL_Map[y][x] : this.besideL_Map[y][x]+1;		// 如果不是資訊點的話需要加入本身的那一點
					gm_min = evaluateNumber - this.besideM_max_Map[y][x];
					gm_min = (gm_min < 0) ? 0 : gm_min;						// 減少地雷數不得少於零
					gm_max = evaluateNumber - this.besideM_min_Map[y][x];
				}
				
				if (Experiment.isLogicTest == true){
					if (gl < 0 || gm_min < 0 || gm_max < 0){
						System.out.println("error_in_isNeedCombineInSimulation，設定參數異常2");
						System.out.println("grid = " + grid.getCoordinate());
						System.out.println("gl = " + gl);
						System.out.println("gm_min = " + gm_min);
						System.out.println("gm_max = " + gm_max);
						System.out.println("evaluateNumber = " + evaluateNumber);
						System.out.println("besideM_min = " + this.besideM_min_Map[y][x]);
						System.out.println("besideM_max = " + this.besideM_max_Map[y][x]);
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_isNeedCombineInSimulation，設定參數異常2");
						}
					}
				}
				
				if (remainL > gl){		// 仍有陌生點
					if (remainM_min <= gm_max){									// case 1	所剩最少地雷數小於p所減的最多地雷數(假設後的最少地雷數)，意指地雷數有可能歸零
						isNeed = true;
					}else if (remainL - gl <= remainM_max - gm_min){			// case 2	所剩空格數減去p所減的空格數(假設後的空格數)小於所剩最多地雷數減去p所減最少地雷數(假設後的最多地雷數)，意指空格數可能等於地雷數
						isNeed = true;
					}
				}else {					// 沒有陌生點
					isNeed = true;
				}
				
				if (isDebug == true){
					if (isNeed == false){
						System.out.println("show map in isNeedCombineInSimulation");
						this.get_map().showMap();
						System.out.println("grid = " + grid.getCoordinate() + ", evaluateNumber = " + evaluateNumber);
					}
				}
				
				return isNeed;
			}else {
				return true;		// 如果沒有使用isUseCombineInEvaluateJudge功能的話，一律進行combine
			}
			
			
		}
		// --------------------isUseCombineInEvaluateJudge專用
		
//		private void countPossible_block() {		// 算出獨立區域的機率
//			for (int bindex= 0 ; bindex < this.bcMaplink.cMapLink.size() ; bindex++){
//				if (isDebug == true && this.bcMaplink.cMapLink.get(bindex).isCheckIndependent() == true){
//					System.out.println("show index map in countPossible_block");
//					this.bcMaplink.cMapLink.get(bindex).showMap("index");
//				}
//				if (this.rMap.mapLink.isEmpty() == false){
//					for (int index = 0 ; index < this.rMap.mapLink.size() ; index++){
//						if (this.bcMaplink.cMapLink.get(bindex).isCheckIndependent() == true){
//							this.bcMaplink.cMapLink.get(bindex).isLittleCombine = false;
//							this.bcMaplink.cMapLink.get(bindex).rMap.record_block(this.usedL, this.rMap.mapLink.get(index).get_usedM());
//
//							for (int y = 0 ; y < this.get_map().getMapY() ; y++){
//								for (int x = 0 ; x < this.get_map().getMapX() ; x++){
//									if (this.get_map().isInitial(y, x) == true 
//											&& this.bcMaplink.isIndependentArea(y, x, this.bcMaplink.cMapLink.get(bindex).iMap) == true){
//										for (int typeindex = 9 ; typeindex >= 0 ; typeindex--){
//											if (typeindex == 9 || (typeindex <= 8 && this.isUseGuessMethod_forecast() == true)){
//												this.rMap.possibleMap[typeindex][y][x] += this.rMap.mapLink.get(index).get_caseRatio() 
//														* ((double)this.bcMaplink.cMapLink.get(bindex).rMap.mapLink.getFirst().recordMap[typeindex][y][x]
//																/ this.bcMaplink.cMapLink.get(bindex).rMap.mapLink.getFirst().get_recordTimes());
//											}
//										}
//									}
//								}
//							}
//
//
//							if (isDebug == true){
//								//										System.out.println("show record map in countPossible_block");
//								//										for (int indexr = 0 ; indexr <= 9 ; indexr++){
//								//											if (indexr == 9 || (indexr < 9 && this.isUseGuessMethod_forecast() == true))
//								//												this.bcMaplink.cMapLink.get(bindex).showMap("record", indexr);
//								//										}
//
//								//										System.out.println("show possible map in countPossible_block");
//								//										for (int indexr = 0 ; indexr <= 9 ; indexr++){
//								//											if (indexr == 9 || (indexr < 9 && this.isUseGuessMethod_forecast() == true))
//								//												this.showMap("possible", indexr);
//								//										}
//							}
//						}
//					}
//				}else {
//					if (Experiment.isLogicTest == true){
//						if (this.bcMaplink.cMapLink.get(bindex).isCheckIndependent() == false){
//							System.out.println("error_in_countPossible_block，因為在只有獨立區域的情況下，存在非獨立區域");
//							if (Experiment.isStepByStepDebug == true){
//								UI.safeMap_In_debug("error_in_countPossible_block_因為在只有獨立區域的情況下_存在非獨立區域");
//							}
//						}
//					}
//					this.bcMaplink.cMapLink.get(bindex).isLittleCombine = false;
//					this.bcMaplink.cMapLink.get(bindex).rMap.record_block(this.usedL, this.usedM);
//					for (int y = 0 ; y < this.get_map().getMapY() ; y++){
//						for (int x = 0 ; x < this.get_map().getMapX() ; x++){
//							if (this.get_map().isInitial(y, x) == true 
//									&& this.bcMaplink.isIndependentArea(y, x, this.bcMaplink.cMapLink.get(bindex).iMap) == true){
//								for (int typeindex = 9 ; typeindex >= 0 ; typeindex--){
//									if (typeindex == 9 || (typeindex <= 8 && this.isUseGuessMethod_forecast() == true)){
//										this.rMap.possibleMap[typeindex][y][x] += (double)this.bcMaplink.cMapLink.get(bindex).rMap.mapLink.getFirst().recordMap[typeindex][y][x]
//												/ this.bcMaplink.cMapLink.get(bindex).rMap.mapLink.getFirst().get_recordTimes();
//									}
//								}
//							}
//						}
//					}
//
//					if (isUseFindCrapsShoot == true && this.bcMaplink.cMapLink.get(bindex).isCrapsShoot() == true){
//						if (isDebug == true){
//							System.out.println("show the map in countPossible_block");
//							this.get_map().showMap();
//							System.out.println("show the combine map in countPossible_block");
//							this.bcMaplink.cMapLink.get(bindex).showMap("index");
//						}
//						for (int y = 0 ; y < this.get_map().getMapY() ; y++){
//							for (int x = 0 ; x < this.get_map().getMapX() ; x++){
//								if (this.bcMaplink.cMapLink.get(bindex).iMap[y][x] > 0){
//									this.caller.answer.add(this.get_map().getGrid(y, x));
//								}
//							}
//						}
//						break;
//					}
//
//					if (isDebug == true){
//						System.out.println("show record map in countPossible_block");
//						for (int indexr = 0 ; indexr <= 9 ; indexr++){
//							if (indexr == 9 || (indexr < 9 && this.isUseGuessMethod_forecast() == true))
//								this.bcMaplink.cMapLink.get(bindex).showMap("record", indexr);
//						}
//						System.out.println("show possible map in countPossible_block");
//						for (int indexr = 0 ; indexr <= 9 ; indexr++){
//							if (indexr == 9 || (indexr < 9 && this.isUseGuessMethod_forecast() == true))
//								this.showMap("possible", indexr);
//						}
//					}
//				}
//			}
//			//					if (isDebug == true){
//			//						System.out.println("show possible map in countPossible_block");
//			//						for (int indexr = 0 ; indexr <= 9 ; indexr++){
//			//							if (indexr == 9 || (indexr < 9 && this.isUseGuessMethod_forecast() == true))
//			//								this.showMap("possible", indexr);
//			//						}
//			//					}
//		}

		private void countPossible_global(){		// 計算全局的機率
			if (Experiment.isLogicTest == true){
				if (this.isLittleCombine == true){
					System.out.println("error_in_countPossible_global，在此使用分區");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_countPossible_在此使用分區");
					}
				}
				
				if (this.gridlink.isEmpty() == true){
					System.out.println("error_in_countPossible_global，在此沒有分區");
				}
			}
			
			RecordMap nowR = null;
			for (int index = 0 ; index < this.rMap.mapLink.size() ; index++){
				nowR = this.rMap.mapLink.get(index);
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (this.get_map().isInitial(y, x) == true){
							for (int typeindex = 9 ; typeindex >= 0 ; typeindex--){
								if (typeindex == 9 || (typeindex <= 8 && this.isUseGuessMethod_forecast() == true)){
									this.rMap.possibleMap[typeindex][y][x] +=
											(nowR.recordMap[typeindex][y][x] / nowR.get_recordTimes()) 
											* nowR.get_caseRatio(); 
								}
							}
						}
					}
				}
			}
		}

		private void countPossible_global(int remainL, int remainM) {	// 計算所有格子的機率，當沒有資訊點時使用
			double strangePossible = (double) remainM / remainL;

			// 最佳化處理
			this.corner = new RegGrid();	// 角落
			this.center = new RegGrid();	// 中央
			this.edge = new RegGrid();		// 邊

			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (this.get_map().isInitial(y, x) == true){
						this.rMap.possibleMap[9][y][x] = strangePossible;
						if (this.isUseGuessMethod_forecast() == true){
							this.countStrangePossible(y, x, remainL, remainM, strangePossible);
						}
					}
				}
			}
		}
		
		private void countStrangePossible(int y, int x, int remainL, int remainM, double strangePossible){
			if (Experiment.isLogicTest == true){
				if (this.isUseGuessMethod_forecast() == false){
					System.out.println("error_in_countStrangePossible，因為並沒有進行預測");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_countStrangePossible_因為並沒有進行預測");
					}
				}
			}

			int besideL, besideM;
			RegGrid reg = null;				// 暫存

			boolean isStrange = this.rMap.isStrangeGridInForecast(y, x);		// 是否為完全陌生點？
			// 是否是角落
			boolean isCorner = ((y == 0 || y == (this.get_map().getMapY() - 1))
					&& (x == 0 || x == (this.get_map().getMapX() - 1))) ? true : false;
			// 是否是中央
			boolean isCenter = ((y > 0 && y < this.get_map().getMapY() - 1)
					&& (x > 0 && x < this.get_map().getMapX() - 1)) ? true : false;
			if (Experiment.isLogicTest == true){
				if (isCorner == true && isCenter == true){
					System.out.println("error_in_countPossible，因為位置同時為角落和中央");
				}
			}
			if (isStrange == true && ((isCorner == true && corner.isSimulation() == true) 
					|| (isCenter == true && center.isSimulation() == true) 
					|| (isCorner == false && isCenter == false && edge.isSimulation() == true))){
				if (isCorner == true){
					reg = this.corner;
				}else if (isCenter == true){
					reg = this.center;
				}else {
					reg = this.edge;
				}
				
				// 為了isNeedCombineInSimulation而搜集需要的資料
				if (this.isUseCombineInEvaluateJudge() == true){
					this.besideL_Map[y][x] = reg.get_besideL();
					this.besideM_max_Map[y][x] = reg.get_besideM_max();
					this.besideM_min_Map[y][x] = reg.get_besideM_min();
				}

				// 複製數值
				for (int typeindex = 9 ; typeindex >= 0 ; typeindex--){
					if ((typeindex <= 8 && this.isUseGuessMethod_forecast() == true) || typeindex == 9){
						this.rMap.possibleMap[typeindex][y][x] = reg.get_possible(typeindex);
					}
				}
			}else {
				besideL = 0;
				besideM = 0;

				for (int j = y-1 ; j <= y+1 ; j++){
					for (int i = x-1 ; i <= x+1 ; i++){
						if (i != x || j != y){		//中間的點不用理
							if (this.get_map().isInBoard(j, i) == true){
								if (this.get_map().isInitial(j, i) == true){
									besideL++;	//未假設的未打開空格數
								}else if (this.get_map().isFlag(j, i) == true){
									besideM++;
								}
							}
						}
					}
				}
				
				// 為了isNeedCombineInSimulation而搜集需要的資料
				if (this.isUseCombineInEvaluateJudge() == true){
					if (besideL > this.besideL_Map[y][x]){
						this.besideL_Map[y][x] = besideL;
					}
					
					if (besideM > this.besideM_max_Map[y][x]){
						this.besideM_max_Map[y][x] = besideM;
					}
					
					if (besideM < this.besideM_min_Map[y][x]){
						this.besideM_min_Map[y][x] = besideM;
					}	
				}

				for (int typeindex = 0 ; typeindex < 9 ; typeindex++){
					this.rMap.possibleMap[typeindex][y][x] = 
							((1 - strangePossible) * 
									this.detail_possible_forecast(typeindex, besideL, besideM, 
											remainL, remainM, false));
				}
			}

			if (isStrange == true && ((isCorner == true && corner.isSimulation() == false) 
					|| (isCenter == true && center.isSimulation() == false) 
					|| (isCorner == false && isCenter == false && edge.isSimulation() == false))){
				if (isCorner == true){
					reg = corner;
				}else if (isCenter == true){
					reg = center;
				}else {
					reg = edge;
				}
				
				if (this.isUseCombineInEvaluateJudge() == true){
					reg.set_besideInfomation(this.besideL_Map[y][x], this.besideM_max_Map[y][x], this.besideM_min_Map[y][x]);
				}

				for (int typeindex = 9 ; typeindex >= 0 ; typeindex--){
					if ((typeindex <= 8 && this.isUseGuessMethod_forecast() == true) || typeindex == 9){
						reg.set_possible(typeindex, this.rMap.possibleMap[typeindex][y][x]);
					}
				}
				reg.setSimulationed();
			}
		}

		private boolean cmCheck() {		//其地圖也必須符合忽略的點的集合
			boolean isLegit = true;
			for (CombineGrid g : this.missCombineLink){
				if (g.isInRange(this.iMap) == true){
					isLegit &= g.isMatchCase(this.iMap, this.mMap);
					
//					$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
					//						UI.safeMap_In_debug("符合條件3的CSP範例");
//					if (this.isSimulation() == false){
//						if (g.isMatchCase(this.iMap, this.mMap) == false){
//							TeddyMethod.condition3 = true;
//							System.out.println("符合條件3的範例");
//							System.out.println("show map in cmCheck");
//							this.get_map().showMap();
//							System.out.println("show index map in cmCheck");
//							this.showMap("index");
//							System.out.println("show mine map in cmCheck");
//							this.showMap("mine");
//							System.out.println("g = " + g.getX() + ", " + g.getY());
//						}
//					}
//					$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
					
					if (isLegit == false) break;
				}
			}
			
			

			return isLegit;
		}

		private void mapUpdate(CombineGrid combineGrid, CM_GridSet cset){	// 地圖某格更新
			for (CM_Grid cgrid : cset.cmgridLink){

				// 只更換自已索引的地方
				if (combineGrid.getIndex() == this.iMap[cgrid.get_coordinateY()][cgrid.get_coordinateX()]){
					// 原本的位置是地雷
					if (this.mMap[cgrid.get_coordinateY()][cgrid.get_coordinateX()] == TeddyMethod.isMine){
						this.usedM--;
					}

					// 更換之後是地雷
					if (cgrid.is_isMine() == TeddyMethod.isMine) {
						this.usedM++;
					}

					this.mMap[cgrid.get_coordinateY()][cgrid.get_coordinateX()] = cgrid.is_isMine();
				}
			}
		}

		private boolean iMap_Unite(CombineGrid combineGrid){	// 將combineGrid的範圍設定成其點的索引
			boolean isUnite = false;
			CM_GridSet cset = combineGrid.cgrid.cmLink.cmlink.getFirst();
			for (int index = 0 ; index < cset.cmgridLink.size() ; index++){
				if (this.iMap[cset.cmgridLink.get(index).get_coordinateY()]
						[cset.cmgridLink.get(index).get_coordinateX()] == 0){

					this.iMap[cset.cmgridLink.get(index).get_coordinateY()]
							[cset.cmgridLink.get(index).get_coordinateX()] = combineGrid.getIndex();

					if (isUnite == false) isUnite = true;	// 回傳是否有聯合到
				}
			}
			return isUnite;
		}

		private void iMap_Recover(CombineGrid combineGrid){	// 將combineGrid的範圍抹成零
			CM_GridSet cset = combineGrid.cgrid.cmLink.cmlink.getFirst();
			for (int index = 0 ; index < cset.cmgridLink.size() ; index++){
				if (this.iMap[cset.cmgridLink.get(index).get_coordinateY()]
						[cset.cmgridLink.get(index).get_coordinateX()] == combineGrid.getIndex()){

					this.iMap[cset.cmgridLink.get(index).get_coordinateY()]
							[cset.cmgridLink.get(index).get_coordinateX()] = 0;
				}
			}
		}

		private void mapRecord(){		// 新地圖的紀錄
			/*
			 * 組合地圖要合法需要符合以下條件
			 * 1. 使用空格數必需要比全體空格數小
			 * 2. 使用地雷數必需要比全體空格數小
			 * 3. 若空格全部使用，則地雷也必需全部使用
			 * 4. 若空格沒有全部使用，則所剩空格數必需大於所剩地雷數
			 */

			if (Experiment.isLogicTest == true){
				if (this.usedL > this.get_map().getRemainInitialGrid()){
					System.out.println("error_in_record，發現使用空格數過大的地圖");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_record_發現使用空格數過大的地圖");
					}
				}
			}

			if (isDebug == true){
				//				System.out.println("show information in mapRecord");
				//				System.out.println("mapL = " + this.get_map().getRemainInitialGrid());
				//				System.out.println("mapM = " + this.get_map().getRemainMineNumber());
				//				System.out.println("usedL = " + this.usedL);
				//				System.out.println("usedM = " + this.usedM);
				//				System.out.println("remainL = " + (this.get_map().getRemainInitialGrid() - this.usedL));
				//				System.out.println("remainM = " + (this.get_map().getRemainMineNumber() - this.usedM));
			}

			if (this.isLegalCombineMap(this.usedL, this.usedM, this.get_map()) == true){
				this.rMap.record_global(this.iMap, this.mMap, this.usedL, this.usedM);
			}else {
//				$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
				
//				UI.safeMap_In_debug("符合條件2的CSP範例");
//				if (this.isSimulation() == false){
//					TeddyMethod.condition2 = true;
//					System.out.println("符合條件2的範例");
//					
//					System.out.println("show map in mapRecord");
//					this.get_map().showMap();
//					System.out.println("Show index map in mapRecord");
//					this.showMap("index");
//					System.out.println("Show index map in mapRecord");
//					this.showMap("mine");
//					System.out.println("usedL = " + this.usedL);
//					System.out.println("usedM = " + this.usedM);
//				}
//				$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
			}
		}

		private void countUsedL(){		//計算所使用的空格數
			this.usedL = 0;
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (this.isInformationGrid(y, x) == true){
						this.usedL++;
					}
				}
			}
		}

		private void showMap(String kind){
			for (int y = this.get_map().getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (this.iMap[y][x] == 0){
						System.out.print("#" + "\t");
					}else if (kind.equals("mine")){
						if (this.mMap[y][x] == TeddyMethod.isMine){
							System.out.print("T" + "\t");
						}else {
							System.out.print("F" + "\t");
						}
					}else if (kind.equals("index")){
						System.out.print(this.iMap[y][x] + "\t");
					}
				}
				System.out.println("");
			}
			System.out.println("");
		}

		private void showMap(String type, int recordType){
			if (type.equals("record")){
				System.out.println("show the map of record " + recordType);
				this.rMap.showRecordMap(recordType);
			}else if (type.equals("possible")){
				if (recordType <= 9){
					if (recordType < 9){
						System.out.println("show the map of number " + recordType +" possible");
					}else {
						System.out.println("show the map of mine possible");
					}

					double possible;
					for (int y = this.get_map().getMapY() - 1 ; y >= 0 ; y--){
						for (int x = 0 ; x < this.get_map().getMapX() ; x++){
							if (this.get_map().isFlag(y, x) == true && recordType == 9){
								possible = 100;
							}else {
								possible = 100 * this.rMap.possibleMap[recordType][y][x];
							}
							System.out.printf("%.2f", possible);
							System.out.print("%" + "\t");
						}
						System.out.println("");
					}
				}else if (recordType == 10){
					System.out.println("show the map of digValue");
					for (int y = this.get_map().getMapY() - 1 ; y >= 0 ; y--){
						for (int x = 0 ; x < this.get_map().getMapX() ; x++){
							System.out.printf("%.2f", this.rMap.possibleMap[recordType][y][x]);
							System.out.print("\t");
						}
						System.out.println("");
					}
				}
				System.out.println("");
			}
		}

		private boolean isCheckDependent(){		// 確定相依，專門為little_combine使用，是否所使用的地雷數只有一種
			return (this.rMap.mapLink.size() > 1) ? true : false;
		}

		private boolean isCheckIndependent(){	// 確定獨立
			return (this.rMap.mapLink.size() == 1) ? true : false;
		}

		private boolean isCrapsShoot(){		// 尋找craps shoot，專為完全分區化設計
			if (Experiment.isLogicTest == true){
				if (this.usedL == 0 || this.usedM == 0){
					System.out.println("error_in_isCrapsShoot，空格數或地雷數有誤");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_isCrapsShoot_空格數或地雷數有誤");
					}
				}
			}

			if (isDebug == true){
//				System.out.println("show index map in isCrapsShoot");
//				this.showMap("index");
			}

			/*
			 * 是craps shoot必需符合以下條件
			 * condition 1. 只有一種地雷數記錄，且所存地圖總數為2
			 * condition 2. 每個未知點旁邊都沒有陌生點
			 * 
			 * 其他參考的條件
			 * condition 1. 只有一種地雷數記錄，且空格數是地雷數的2倍
			 * condition 2. 每一個未知點的記錄次數皆是總記錄次數的1/2，即為機率都是50%
			 * condition 3. 每個未知點旁邊都沒有陌生點
			 */

			boolean isCraps = false;
			boolean isStrange = false;
			if (this.isCheckIndependent() == true && this.rMap.mapLink.getFirst().copyMap.size() == 2){
				isCraps = true;		// 先假設是craps shoot
				isStrange = false;	// 先假設都沒有陌生點
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (this.isInformationGrid(y, x) == true){
							for (int j = y - 1 ; j <= y + 1 ; j++){
								for (int i = x - 1 ; i <= x + 1 ; i++){
									if (j != y || i != x){
										if (this.get_map().isInBoard(j, i) == true && this.get_map().isInitial(j, i) == true){
											isStrange |= this.get_map().isStrangeGrid(j, i);
											if (isStrange == true) break;		// 已發現陌生點，不須繼續作下去
										}
									}
								}
								if (isStrange == true) break;		// 已發現陌生點，不須繼續作下去
							}
						}
						if (isStrange == true) break;		// 已發現陌生點，不須繼續作下去
					}
					if (isStrange == true) break;		// 已發現陌生點，不須繼續作下去
				}
			}
			return (isCraps == true && isStrange == false);
		}

		private double detail_possible_forecast(int number, int besideL, int besideM, int mapRemainL, int mapRemainM, boolean isAssume){
			//參考 筆記21-2012/9/15
			int mapL;							//所剩下的空格數
			int mapM = mapRemainM;				//所剩下的地雷數
			int needM = number - besideM;		//還需要假設的地雷數目
			double possible;
			TeddyMethodTool tool = new TeddyMethodTool();
		
			if (isAssume == true){
				mapL = mapRemainL; //如果是已經假設中就不用扣掉本身那一格
			}else {
				mapL = mapRemainL - 1; //必須扣掉本身的那一格
			}
		
			//		//參考 筆記21-2012/9/18
			/*
			 * besideL == 0 -> 周圍沒有空格的情況下
			 * 		number != besideM -> 數字不等於周圍地雷總數，不可能
			 *  	number == besideM -> 數字等於周圍地雷總數，一定是
			 * besideL == mapL -> 周圍的空格是所剩的空格的情況下
			 * 		mapM != needM -> 總地雷數不等於需求地雷數，不可能
			 * 		mapM == needM -> 總地雷數等於需求地雷數，一定是
			 * mapL == mapM -> 周圍都一定是地雷的情況下
			 * 		number != besideL + besideM -> 數字不等於空格數加地雷數，不可能
			 * 		number == besideL + besideM -> 數字等於空格數加地雷數，一定是
			 * mapM == 0 -> 周圍都一定不是地雷的情況下
			 * 		number != besideM -> 數字不等於周圍地雷數，不可能
			 * 		number == besideM -> 數字等於周圍地雷數，一定是
			 * 
			 * number < besideM -> 數字太小，不可能
			 * number > (besideL + besideM) -> 數字太大，不可能
			 * needM > mapM -> 需要地雷數過多，不可能
			 * (mapL - besideL) < (mapM - needM) -> 需要地雷數過少，不可能
			 */
			if ((besideL == 0 && number != besideM) || (besideL == mapL && mapM != needM) || (mapL == mapM && number != besideL + besideM) || (mapM == 0 && number != besideM)
					|| (number < besideM) || (number > (besideL + besideM)) || (needM > mapM) || (mapL - besideL) < (mapM - needM)){
				possible = 0;
			}else if ((besideL == 0 && number == besideM) || (besideL == mapL && mapM == needM) || (mapL == mapM && number == besideL + besideM) || (mapM == 0 && number == besideM)){
				possible = 1;
			}else {
				//參考 筆記21-2012/9/15
				possible = ((tool.conbineNumber(besideL, needM)
						.multiply(tool.permutation(mapM, needM))
						.multiply(tool.permutation(mapL-mapM, besideL-needM))))
						.divide(tool.permutation(mapL, besideL), 16, BigDecimal.ROUND_HALF_UP).doubleValue();
				if (possible == 0 || possible == 1){
					System.out.println("error_in_detail_possible_forecast，因為通過前置條件，但是機率卻絕對！");
					System.out.println("possible = " + possible);
					System.out.println("number = " + number);
					System.out.println("besideL = " + besideL);
					System.out.println("besideM = " + besideM);
					System.out.println("mapRemainL = " + mapRemainL);
					System.out.println("mapRemainM = " + mapRemainM);
					System.out.println("isAssume = " + isAssume);
				}
			}
		
			if (possible < 0 || possible > 1){
				System.out.println("error_in_detail_possible_forecast，因為機率超出範圍");
				System.out.println("possible = " + possible);
				System.out.println("number = " + number);
				System.out.println("besideL = " + besideL);
				System.out.println("besideM = " + besideM);
				System.out.println("mapRemainL = " + mapRemainL);
				System.out.println("mapRemainM = " + mapRemainM);
				System.out.println("isAssume = " + isAssume);
			}
		
			return possible;
		}

		private boolean isLegalCombineMap(int usedL, int usedM, Player_Board map){	// 檢查是否是合法的使用空格數和地雷數
			/*
			 * 組合地圖要合法需要符合以下條件
			 * 1. 使用空格數必需要小於等於全體空格數
			 * 2. 使用地雷數必需要小於等於全體地雷數
			 * 3. 若空格全部使用，則地雷也必需全部使用；若空格沒有全部使用，則所剩空格數必需大於等於所剩地雷數
			 */
			
			return (usedL <= map.getRemainInitialGrid()) 
					&& (usedM <= map.getRemainMineNumber())
					&& ((usedL == map.getRemainInitialGrid() && usedM == map.getRemainMineNumber())
					|| (usedL < map.getRemainInitialGrid() && ((map.getRemainInitialGrid() - usedL) >= (map.getRemainMineNumber() - usedM))));
		}

	}

	private class CombineGrid{
		private int bIndex;		//索引編號
		private Teddy_Map.CF_Grid cgrid;
		private int usedIndex;		//目前案例所使用的編號
		boolean isCarried;	//已經進位過了嗎？

		private CombineGrid(Teddy_Map.CF_Grid cgrid){
			this.bIndex = 0;
			this.cgrid = cgrid;
			this.isCarried = false;
		}
		
	    private void finalize_By_Teddy() { 
			this.cgrid = null;
		}

		private boolean isMatchCase(int[][] iMap, boolean[][] mMap) {		//檢查是否符合其點的其中一個case
			boolean isMatch = false;
			for (int caseIndex = 0 ; caseIndex < this.cgrid.cmLink.size() ; caseIndex++){
				isMatch |= this.isMatchCase(caseIndex, iMap, mMap);
				if (isMatch == true) break;
			}
			return isMatch;
		}

		private boolean isMatchCase(int caseIndex, int[][] iMap, boolean[][] mMap) {		//檢查特定某點的其中一個case
			boolean isMatch = true;
			CM_Grid cmgrid;
			for (int index = 0 ; index < this.cgrid.cmLink.cmlink.get(caseIndex).cmgridLink.size() ; index++){
				cmgrid = this.cgrid.cmLink.cmlink.get(caseIndex).cmgridLink.get(index);
				if (iMap[cmgrid.get_coordinateY()][cmgrid.get_coordinateX()] < this.bIndex
						&& mMap[cmgrid.get_coordinateY()][cmgrid.get_coordinateX()] != cmgrid.is_isMine()){
					isMatch = false;
					break;
				}
			}
			return isMatch;
		}

		private boolean isInRange(int[][] iMap){
			boolean isinrange = false;
			CM_Grid m;
			for (int index = 0 ; index < this.cgrid.cmLink.cmlink.getFirst().cmgridLink.size() ; index++){
				m = this.cgrid.cmLink.cmlink.getFirst().cmgridLink.get(index);
				if (iMap[m.get_coordinateY()][m.get_coordinateX()] > 0){
					isinrange = true;
					break;
				}
			}
			return isinrange;
		}

		private void set_bIndex(int bIndex){
			this.bIndex = bIndex;
		}

		public void combineInitail() {
			this.usedIndex = 0;
			this.isCarried = false;
		}

		public void setCarryed() {
			if (this.isCarried == false) this.isCarried = true;
			this.usedIndex = 0;
		}

		private CM_GridSet getNextCase(int[][] iMap, boolean[][] mMap){		//取得下一個合法集合
			int index;
			boolean isFind = false;
			for (index = this.usedIndex ; index < this.cgrid.cmLink.size() ; index++){
				isFind |= this.isMatchCase(index, iMap, mMap);
				if (isFind == true) break;
			}

			// 尤於for迴圈的關係，如果是break出來的話index不會加一，若是正常跑完則會加一
			this.usedIndex = (isFind == true) ? (index + 1) : index;

			if (isFind == true){
				return this.cgrid.cmLink.cmlink.get(index);
			}else {
				return null;
			}
		}

		private boolean isCarryied(){	//是否已經進位過了
			return this.isCarried;
		}

		private boolean isCarry(){		//是否要進位了。
			if (Experiment.isLogicTest == true){
				if (this.usedIndex > this.cgrid.cmLink.size()){
					System.out.println("error_in_isCarry，因為索引大於集合總數");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_isCarry_因為索引大於集合總數");
					}
				}
			}
			return (this.usedIndex == this.cgrid.cmLink.size()) ? true : false;
		}

		//		private String getCoordinate() {
		//			return this.getCoordinateX() + " " + this.getCoordinateY();
		//		}

		private int getX(){
			return this.cgrid.getX();
		}

		private int getY(){
			return this.cgrid.getY();
		}
		
		private boolean equals(CombineGrid g){
			return (this.cgrid.getX() == g.cgrid.getX() && this.cgrid.getY() == g.cgrid.getY());
		}

		private int getIndex(){
			return this.bIndex;
		}
	}

	private class Block_combineMap implements Cloneable{
		private CombineMap caller;
		private int[][] bMap;		// block map
		private LinkedList<CombineGrid> copylink;
		private LinkedList<CombineGrid> bfsLink;
		private LinkedList<CombineMap> cMapLink;
		private int recordIndex;
		private Block_combineGrid bcGrid;
		private boolean isDeepBFS;

		private Block_combineMap(CombineMap caller){
			this.caller = caller;
			this.bMap = new int[this.get_map().getMapY()][this.get_map().getMapX()];
			this.cMapLink = new LinkedList<CombineMap>();
			this.recordIndex = 0;
			this.isDeepBFS = (this.caller.isUseBlockCombine() == true && this.caller.isUseBlockCombine_unique() == true) && 
					(this.caller.isUseGuessMethod() == true && this.caller.isUseGuessMethod_forecast() == true); 
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			Block_combineMap bcMap = null;
			try {
				bcMap = (Block_combineMap)super.clone();
				bcMap.cMapLink = new LinkedList<CombineMap>();
				for (CombineMap cmap : this.cMapLink){
					bcMap.cMapLink.addLast((CombineMap)cmap.clone());
				}
				bcMap.bMap = new int[this.get_map().getMapY()][this.get_map().getMapX()];
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						bcMap.bMap[y][x] = this.bMap[y][x];
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return bcMap;
		}

	    private void finalize_By_Teddy() { 
			this.caller = null;
			
			if (this.copylink != null){
				for (CombineGrid cgrid : this.copylink){
					cgrid.finalize_By_Teddy();
				}
				this.copylink.clear();
				this.copylink = null;
			}
			
			if (this.bfsLink != null){
				for (CombineGrid cgrid : this.bfsLink){
					cgrid.finalize_By_Teddy();
				}
				this.bfsLink.clear();
				this.bfsLink = null;
			}
			
			if (this.cMapLink != null){
				for (CombineMap cmap : this.cMapLink){
					cmap.finalize_By_Teddy();
				}
				this.cMapLink.clear();
				this.cMapLink = null;
			}
			
			if (this.bcGrid != null){
				this.bcGrid.finalize_By_Teddy();
				this.bcGrid = null;
			}
			
			this.bMap = null;
		}
		
		private Player_Board get_map(){
			return this.caller.get_map();
		}

		private boolean isSimulation(){
			return this.caller.isSimulation();
		}
		
		private boolean isUseRereadCombineMap(){
			return this.caller.isUseRereadCombineMap();
		}
		
		private void divideSet(){		//分組
			this.bcGrid = new Block_combineGrid();
			this.copylink = new LinkedList<CombineGrid>();
			this.bfsLink = new LinkedList<CombineGrid>();

			for (CombineGrid cgrid : this.caller.gridlink){	//複製
				this.copylink.addFirst(cgrid);
			}

			if (isDebug == true){
				System.out.println("show index map before divide in divideSet");
				this.get_map().showMap();
				this.showMap(this.caller.iMap);				
			}

			while (this.copylink.isEmpty() == false){
				this.recordIndex++;
				this.bfsLink.addLast(this.copylink.getFirst());
				this.copylink.removeFirst();		//刪除點
				this.bcGrid.add(this.recordIndex, this.bfsLink.getFirst());	//加入點
				while (this.bfsLink.isEmpty() == false){
					this.bfs(this.bfsLink.getFirst());
					this.bfsLink.removeFirst();
				}
			}

			if (isDebug == true){
				System.out.println("show beside map after divide in divideSet");
				this.showMap(this.bMap);				
			}
		}

		private void bfs(CombineGrid cgrid){				//擴展
			for (CM_Grid g: cgrid.cgrid.cmLink.cmlink.getFirst().cmgridLink){		// g是未知點
				// Check 1
				if (Experiment.isLogicTest == true){
					if (this.bMap[g.get_coordinateY()][g.get_coordinateX()] > 0 
							&& this.bMap[g.get_coordinateY()][g.get_coordinateX()] != this.recordIndex){
						System.out.println("error_in_bfs in out BFS，因為連結到不同區塊的點");
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_out_bfs_因為連結到不同區塊的點");
						}
					}
				}
				
				if (this.bMap[g.get_coordinateY()][g.get_coordinateX()] == 0 
						&& this.caller.iMap[g.get_coordinateY()][g.get_coordinateX()] > 0){
					if (cgrid.getIndex() == this.caller.iMap[g.get_coordinateY()][g.get_coordinateX()]){
						this.bMap[g.get_coordinateY()][g.get_coordinateX()] = this.recordIndex;		// 標記
						if (isDebug == true){
//							System.out.println("show beside map in bfs");
//							System.out.println("g = " + g.getCoordinate());
//							this.showMap(this.bMap);
						}
					}else {
						this.add_BFS(this.caller.iMap[g.get_coordinateY()][g.get_coordinateX()]);	// 加入bfs link
					}
				}
				
				if (this.isDeepBFS == false){
					for (int j = g.get_coordinateY() - 1 ; j <= g.get_coordinateY() + 1 ; j++){
						for (int i = g.get_coordinateX() - 1 ; i <= g.get_coordinateX() + 1 ; i++){
							if (i != g.get_coordinateX() || j != g.get_coordinateY()){		//中間的點不用理
								if (this.get_map().isInBoard(j, i) == true && this.get_map().isVisit(j, i) == true){
									for (int lj = j - 1 ; lj <= j + 1 ; lj++){
										for (int li = i - 1 ; li <= i + 1 ; li++){
//											if (li != i || lj != j){		//中間的點不用理
												if (this.get_map().isInBoard(lj, li) == true){
													// Check 2
													if (Experiment.isLogicTest == true){
														if (this.bMap[lj][li] > 0 && this.bMap[lj][li] != this.recordIndex){
															System.out.println("error_in_bfs in inner shallow BFS，因為連結到不同區塊的點");
															if (Experiment.isStepByStepDebug == true){
																UI.safeMap_In_debug("error_in_inner_shallow_bfs_因為連結到不同區塊的點");
															}
														}
													}

													if (this.bMap[lj][li] == 0 && this.caller.iMap[lj][li] > 0){
														this.add_BFS(this.caller.iMap[lj][li]);	// 加入bfs link
													}
												}
//											}
										}
									}
								}
							}
						}
					}
				}else {
					for (int j = g.get_coordinateY() - 2 ; j <= g.get_coordinateY() + 2 ; j++){
						for (int i = g.get_coordinateX() - 2 ; i <= g.get_coordinateX() + 2 ; i++){
							if (i != g.get_coordinateX() || j != g.get_coordinateY()){		//中間的點不用理
								if (this.get_map().isInBoard(j, i) == true){
//									Check 2
									if (Experiment.isLogicTest == true){
										if (this.bMap[j][i] > 0 && this.bMap[j][i] != this.recordIndex){
											System.out.println("error in bfs in inner deep BFS，因為連結到不同區塊的點");
											if (Experiment.isStepByStepDebug == true){
												UI.safeMap_In_debug("error_in_inner_deep_bfs_因為連結到不同區塊的點");
											}
										}
									}
									if (this.bMap[j][i] == 0 && this.caller.iMap[j][i] > 0){
										this.add_BFS(this.caller.iMap[j][i]);	// 加入bfs link
									}
								}
							}
						}
					}
				}
			}
		}

		private void add_BFS(int bIndex){
			for (int index = 0 ; index < this.copylink.size() ; index++){
				if (this.copylink.get(index).getIndex() == bIndex){
					if (isDebug == true){
//						System.out.println("This is add_BFS");
//						System.out.println("add grid " 
//						+ this.copylink.get(index).getX() + " " + this.copylink.get(index).getX() 
//						+ ", index is " + this.copylink.get(index).getIndex());
					}
					this.bcGrid.add(this.copylink.get(index));	//加入點
					this.bfsLink.addLast(this.copylink.get(index));
					this.copylink.remove(index);		//刪除點
					break;
				}
			}
		}

		private void little_combine(){
			for (CombineGridSet cset : this.bcGrid.gsetlink){
				this.cMapLink.addLast(new CombineMap(cset.get_index(), cset.gsetlink, this.caller));
				this.cMapLink.getLast().combine();
			}
		}
		
//		private boolean isSomeBlock(Block_combineMap lastMap, CombineGridSet cset){
//			boolean isSomeRange = true;
//			LinkedList<Integer> blockIndex = new LinkedList<Integer>(); 	// 儲存相關區域索引
//			
//			// 檢查是否是同一個區域
//			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
//				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
//					if (this.bMap[y][x] == cset.get_index() && lastMap.bMap[y][x] > 0){
//						if (blockIndex.isEmpty() == true){
//							blockIndex.addLast(lastMap.bMap[y][x]);
//						}else {
//							for (int index = 0 ; index < blockIndex.size() ; index++){
//								if (blockIndex.get(index) == lastMap.bMap[y][x]){
//									break;
//								}else if (index == blockIndex.size()-1){
//									blockIndex.addLast(lastMap.bMap[y][x]);
//								}
//							}
//						}
//					}else if ((this.bMap[y][x] == cset.get_index() && lastMap.bMap[y][x] == 0)	// newMap的區域比較大
//							|| (this.bMap[y][x] == 0 && lastMap.bMap[y][x] > 0)){				// newMap的區域比較小
//						isSomeRange &= false;
//					}
//				}
//			}
//			
//			return (isSomeRange && blockIndex.size() == 1);
//		}
		
		private void little_combine(CombineMap last_map){
			if (last_map == null){
				this.little_combine();
			}else {
				/*
				 * 一個子區域的資料能夠再次使用的條件有兩個
				 * 1. 未知點的範圍地圖是相同的，也就是說影響的範圍是一樣大的
				 * 2. 周圍的資訊沒有改變，資訊點的組合相同且周圍沒有新的資訊點
				 */
				Block_combineMap lastMap = last_map.bcMaplink;
				LinkedList<Integer> blockIndex = new LinkedList<Integer>(); 	// 儲存相關區域索引
				LinkedList<CombineGrid> newPoint = new LinkedList<CombineGrid>(); 	// 儲存新的點
				boolean isSomeBlock, isNoNewMissPointCheck, isNewPoint;
				
				// 將自已的分區地圖一一與lastMap做比較
				for (CombineGridSet cSet : this.bcGrid.gsetlink){
					blockIndex.clear();
					isSomeBlock = true;
					
					someBlockCheck:
					for (int y = 0 ; y < this.get_map().getMapY() ; y++){
						for (int x = 0 ; x < this.get_map().getMapX() ; x++){
							// 檢查是否是同一個區域，檢查cSet的範圍是否也存在lastMap中
							if (this.bMap[y][x] == cSet.get_index() && lastMap.bMap[y][x] > 0){			// cSet的範圍和newMap的範圍重疊
								if (blockIndex.isEmpty() == true){
									blockIndex.addLast(lastMap.bMap[y][x]);	//儲存lastMap的索引編號
								}else {
									for (int index = 0 ; index < blockIndex.size() ; index++){
										if (blockIndex.get(index) == lastMap.bMap[y][x]){
											break;
										}else if(index == blockIndex.size()-1){
											blockIndex.addLast(lastMap.bMap[y][x]);
											isSomeBlock = false;		// 已檢查出資訊點的構成是不同的！
											break someBlockCheck;
										}
									}
								}
							}else if (this.bMap[y][x] == cSet.get_index() && lastMap.bMap[y][x] == 0){	// newMap的區域比較大
								isSomeBlock = false;
								break someBlockCheck;
							}else if (this.bMap[y][x] == 0 && lastMap.bMap[y][x] > 0){					// newMap的區域比較小
								for (int index = 0 ; index < blockIndex.size() ; index++){
									if (blockIndex.get(index) == lastMap.bMap[y][x]){
										break;
									}else if(index == blockIndex.size()-1){
										blockIndex.addLast(lastMap.bMap[y][x]);
									}
								}
								isSomeBlock = false;
								break someBlockCheck;
							}
						}
					}
					
					isNoNewMissPointCheck = false;
					
					if (isSomeBlock == true){
						if (newPoint.isEmpty() == true){
							for (int index = 0 ; index < last_map.gridlink.size() ; index++){
								for (int newindex = index ; newindex < this.caller.gridlink.size() ; newindex++){
									if (last_map.gridlink.get(index).equals(this.caller.gridlink.get(newindex)) == false){
										// 找到新的點
										newPoint.addLast(this.caller.gridlink.get(newindex));
									}
								}
							}
						}
						
						for (CombineGrid c : newPoint){
							// 檢查是否在受影響的5*5範圍，對a來說就是檢查是否在b的位置
							/*
							 * b b b b b
							 * b b b b b
							 * b b a b b
							 * b b b b b
							 * b b b b b
							 * 
							 * 18	13	10	9	10	13	18
							 * 13	8	5	4	5	8	13
							 * 10	5	2	1	2	5	10
							 * 9	4	1	0	1 	4	9
							 * 10	5	2	1	2	5	10
							 * 13	8	5	4	5	8	13
							 * 18	13	10	9	10	13	18
							 */
							for (CombineGrid g : cSet.gsetlink){
								isNoNewMissPointCheck |= 
										(Math.pow(g.getX()-c.getX(), 2) + Math.pow(g.getY()-c.getY(), 2) <= 8);	// 是否有影響到
								if (isNoNewMissPointCheck == true) break;
							}
						}
					}
					
					
					if (isSomeBlock == true && isNoNewMissPointCheck == false){
						System.out.println("省到囉！");
						if (this.isSimulation() == true){
							try {
								// 直接使用非模擬時的舊地圖的複制品
								this.cMapLink.addLast((CombineMap)lastMap.cMapLink.get(blockIndex.getFirst()-1).clone());		
							}catch (CloneNotSupportedException cse) {
								cse.printStackTrace();
							}
						}else {
							this.cMapLink.addLast(lastMap.cMapLink.get(blockIndex.getFirst()-1));		// 直接使用舊地圖
						}
						
						this.cMapLink.getLast().set_index(this.cMapLink.size());		// 改變索引
					}else {
						// 刪除lastMap的暫存地圖
//						System.out.println("刪掉囉！");
						if (this.isSimulation() == false){	// 非模擬時才可以去刪掉重疊區域
							for (int index : blockIndex){
								RecordMapDatabase rd = lastMap.cMapLink.get(index-1).rMap;
								for (RecordMap rm : rd.mapLink){
									if (Experiment.isShowNowSitulation == true){
										// 更新儲存地圖次數
										TeddyMethod.total_copyMap =	TeddyMethod.total_copyMap.subtract(BigDecimal.valueOf(rm.copyMap.size()));
									}
									
									rm.copyMap.clear();		// 清除無用的暫存地圖，根據2013/3/13的實驗結果，可以加速110倍
								}
							}
						}
						
						this.cMapLink.addLast(new CombineMap(cSet.get_index(), cSet.gsetlink, this.caller));
						this.cMapLink.getLast().combine();
					}
				}
			}
		}

		private void showMap(int[][] outputMap){
			for (int y = this.get_map().getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					System.out.print(outputMap[y][x] + "\t");
				}
				System.out.println("");
			}
			System.out.println("");
		}
	
	
	}

	private class Block_combineGrid{
		LinkedList<CombineGridSet> gsetlink;

		private Block_combineGrid(){
			this.gsetlink = new LinkedList<CombineGridSet>();
		}
		
	    private void finalize_By_Teddy() {
	    	if (this.gsetlink != null){
	    		for (CombineGridSet cset : this.gsetlink){
					cset.finalize_By_Teddy();
				}
				this.gsetlink.clear();
				this.gsetlink = null;
	    	}
		}

		private void add(int index, CombineGrid cgrid){	//加入新集合
			this.gsetlink.addLast(new CombineGridSet(index, cgrid));
			
//			if (this.gsetlink.isEmpty() == true || isNewSet == true){
//				
//			}else {
//				this.gsetlink.getLast().add(cgrid);
//			}
		}
		
		private void add(CombineGrid cgrid){		// 加入舊集合
			this.gsetlink.getLast().add(cgrid);
		}
	}
	
	private class CombineGridSet{
		private int index;
		LinkedList<CombineGrid> gsetlink;
		private CombineGridSet(int index, CombineGrid cgrid){
			this.index = index;
			this.gsetlink = new LinkedList<CombineGrid>(); 
			this.gsetlink.addLast(cgrid);
		}
		
	    private void finalize_By_Teddy() {
	    	if (this.gsetlink != null){
	    		for (CombineGrid cgrid : this.gsetlink){
		    		cgrid.finalize_By_Teddy();
		    	}
				this.gsetlink.clear();
				this.gsetlink = null;
	    	}
		}
		
		private int get_index(){
			return this.index;
		}

		private void add(CombineGrid cgrid){	//依照index順序不重覆加入
			for (int index = 0 ; index < this.gsetlink.size() ; index++){
				if (cgrid.getIndex() < this.gsetlink.get(index).getIndex()){
					this.gsetlink.add(index, cgrid);
					break;
				}else {
					if (Experiment.isLogicTest == true){
						if (cgrid.getIndex() == this.gsetlink.get(index).getIndex()){
							System.out.println("error_in_add of combineGridSet，因為重覆加入了");
							if (Experiment.isStepByStepDebug == true){
								UI.safeMap_In_debug("error_in_add_of_combineGridSet，因為重覆加入了");
							}
							break;
						}else if (index == this.gsetlink.size() - 1){
							this.gsetlink.addLast(cgrid);
							System.out.println("error_in_add of combineGridSet，因為後來加入的點索引比較大");
							if (Experiment.isStepByStepDebug == true){
								UI.safeMap_In_debug("error_in_add_of_combineGridSet，因為後來加入的點索引比較大");
							}
							break;
						}
					}
				}
			}
		}
	}

	private class RecordMapDatabase implements Cloneable{
		CombineMap caller;
		LinkedList<RecordMap> mapLink;
		BigDecimal totalPossibleN;	//所有的case數
		double[][][] possibleMap;
		int usedL;		//所使用的格子數
		private int usedIndex;		//目前案例所使用的編號
		private boolean isCarried;	//已經進位過了嗎？

		private RecordMapDatabase(CombineMap caller){
			this.caller = caller;
			this.mapLink = new LinkedList<RecordMap>();
			this.possibleMap = new double[11][this.get_map().getMapY()][this.get_map().getMapX()];
			/*
			 * 一共有11張地圖，0-8是出現數字0-8的機率，9是地雷的機率，10是評價的分數
			 */
			this.totalPossibleN = BigDecimal.valueOf(0);
			this.usedIndex = 0;
			this.isCarried = false;
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			RecordMapDatabase rd = null;
			try {
				rd = (RecordMapDatabase)super.clone();
				rd.mapLink = new LinkedList<RecordMap>();	// 區域地圖只需複製記錄資料即可
				for (RecordMap rmap : this.mapLink){
					rd.mapLink.addLast((RecordMap)rmap.clone());
				}
				
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return rd;
		}
		
	    private void finalize_By_Teddy() { 
			this.caller = null;
			if (this.mapLink != null){
				for (RecordMap rmap : this.mapLink){
					rmap.finalize_By_Teddy();
				}
				this.mapLink.clear();
				this.mapLink = null;
			}
			this.possibleMap = null;
		}
		
		private boolean isUseGuessMethod_forecast(){
			return this.caller.isUseGuessMethod_forecast();
		}

		private boolean isLittleCombine(){
			return this.caller.isLittleCombine();
		}
		
		private boolean isUseBlockCombine(){
			return this.caller.isUseBlockCombine();
		}
		
		private boolean isUseBlockCombine_complete(){
			return this.caller.isUseBlockCombine_complete();
		}
		
		private boolean isUseBlockCombine_unique(){
			return this.caller.isUseBlockCombine_unique();
		}
		
		private boolean isUseCombineInEvaluateJudge(){
			return this.caller.isUseCombineInEvaluateJudge();
		}

		private boolean isNeedCombineInSimulation(Player_Grid grid, int evaluateNumber){
			return this.caller.isNeedCombineInSimulation(grid, evaluateNumber);
		}
		
		private Player_Board get_map(){
			return this.caller.get_map();
		}

		private Teddy_Map get_root(){
			return this.caller.get_root();
		}
		
//		private CombineMap get_combineMap(){
//			return this.caller;
//		}
//		
//		private double getEvaluateValue(int y, int x){
//			return this.possibleMap[10][y][x];
//		}
		
		private double getMinePossible(int y, int x){		// 回傳地雷機率
			return this.possibleMap[9][y][x];
		}
		
		private LinkedList<Player_Grid> find_least_mine_possible_above_leastp(LinkedList<Player_Grid> plink, double min_mineP){	//	找到比min_mineP大的最小地雷機率點 
			plink.clear();
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (this.get_map().isInitial(y, x) == true && this.getMinePossible(y, x) > min_mineP){
						if (plink.isEmpty() == true){
							plink.addLast(this.get_map().getGrid(y, x));
						}else if (this.getMinePossible(y, x) 
								< this.getMinePossible(plink.getFirst().getY(), plink.getFirst().getX())){
							plink.clear();	// 找到地雷機率比之前更小的點，之前全面刪掉
							plink.addLast(this.get_map().getGrid(y, x));
						}else if (this.getMinePossible(y, x) // 等於的話也加入
								== this.getMinePossible(plink.getFirst().getY(), plink.getFirst().getX())){
							plink.addLast(this.get_map().getGrid(y, x));
						}
					}
				}
			}
			return plink;
		}
		
		private void Find_least_mine_possible(Answer answer) {	// 找到最低地雷機率的點
			// 得出地雷機率最小的一些點
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (this.get_map().isInitial(y, x) == true){
						if (Experiment.isLogicTest == true){
							if (this.possibleMap[9][y][x] > 1.01 
									|| this.possibleMap[9][y][x] < 0){
								System.out.println("error_in_getBests，因為機率異常，possible = " + this.possibleMap[9][y][x]);
								if (Experiment.isStepByStepDebug == true){
									UI.safeMap_In_debug("error_in_getBests_因為機率異常");
								}
							}
						}

						if (this.possibleMap[9][y][x] == 0.0){
							answer.add(this.get_map().getGrid(y, x), TeddyMethod.notMine);
						}else if (this.possibleMap[9][y][x] == 1.0){
							answer.add(this.get_map().getGrid(y, x), TeddyMethod.isMine);
						}else {
							if (answer.guessPoint.isEmpty() == true){
								answer.add(this.get_map().getGrid(y, x));
							}else if (this.possibleMap[9][y][x] 
									< this.possibleMap[9]
											[answer.guessPoint.getFirst().getY()][answer.guessPoint.getFirst().getX()]){
								answer.guessPoint.clear();	// 找到地雷機率比之前更小的點，之前全面刪掉
								answer.add(this.get_map().getGrid(y, x));
							}else if (this.possibleMap[9][y][x] 
									== this.possibleMap[9]
											[answer.guessPoint.getFirst().getY()][answer.guessPoint.getFirst().getX()]){
								answer.add(this.get_map().getGrid(y, x));
							}
						}
					}
				}
			}
		}
		
//		private void find_highest_worth(LinkedList<Player_Grid> plink, Answer answer){
//			plink = this.guess_forecast(plink);		// 預測猜測
//
//			if (plink.size() > 0){		// 如果都沒出現有價值的點，就只好照原來的猜了！
//				answer.guessPoint.clear();
//				for (Player_Grid pgrid : plink){
//					answer.add(pgrid);		// 加回guess中
//				}
//			}
//		}

		private boolean isStrangeGridInForecast(int y, int x){	//是否是完全陌生點
			boolean isstrange = true;
			if (this.get_map().isInitial(y, x) == true){
				for (int j = y - 2 ; j <= y + 2 ; j++){
					for (int i = x - 2 ; i <= x + 2 ; i++){
						if (i != x || j != y){		//中間的點不用理
							if (this.get_map().isInBoard(j, i) == true && this.get_map().isVisit(j, i) == true){
								isstrange = false;
								break;
							}
						}
					}
					if (isstrange == false) break;
				}
			}else {
				isstrange = false;
			}

			return isstrange;
		}

		private boolean isStrangeGridInForecast(Player_Grid pgrid){
			return this.isStrangeGridInForecast(pgrid.getY(), pgrid.getX());
		}

		private LinkedList<Player_Grid> guess_forecast(LinkedList<Player_Grid> plink){
			boolean isCorner, isCenter, isStrange;

			// 最佳化處理
			RegGrid corner = new RegGrid();
			RegGrid edge = new RegGrid();
			RegGrid center = new RegGrid();

			// 計算每個點的價值
			for (Player_Grid pgrid : plink){
				//				if (isDebug == true && pgrid.getCoordinateX() == 25 && pgrid.getCoordinateY() == 0){
				//				}
				isStrange = this.isStrangeGridInForecast(pgrid);
				// 是否是角落
				isCorner = ((pgrid.getY() == 0 || pgrid.getY() == (this.get_map().getMapY() - 1))
						&& (pgrid.getX() == 0 || pgrid.getX() == (this.get_map().getMapX() - 1))) ? true : false;
				// 是否是中央
				isCenter = ((pgrid.getY() > 0 && pgrid.getY() < this.get_map().getMapY() - 1)
						&& (pgrid.getX() > 0 && pgrid.getX() < this.get_map().getMapX() - 1)) ? true : false;

				if (isStrange == true && isCorner == true && corner.isSimulation() == true){
					this.possibleMap[10][pgrid.getY()][pgrid.getX()]
							= corner.get_weight();
				}else if (isStrange == true	&& isCenter == true && center.isSimulation() == true){
					this.possibleMap[10][pgrid.getY()][pgrid.getX()]
							= center.get_weight();
				}else if (isStrange == true && isCorner == false && isCenter == false && edge.isSimulation() == true){
					this.possibleMap[10][pgrid.getY()][pgrid.getX()]
							= edge.get_weight();
				}else {
					if (Experiment.isLogicTest == true){
						if (this.possibleMap[10][pgrid.getY()][pgrid.getX()] != 0){
							System.out.println("error_in_guess_forecast，因為digValue初始值不為零");
							if (Experiment.isStepByStepDebug == true){
								UI.safeMap_In_debug("error_in guess_forecast_因為digValue初始值不為零");
							}
						}
					}

					if (isUseGuessMethod_forecast_allPossible == true){
						this.guess_forecast_evaluate(pgrid);
					}else if (isUseGuessMethod_forecast_maxPossible == true){
						this.guess_forecast_evaluate_MaxPossible(pgrid);
					}else {
						if (Experiment.isLogicTest == true){
							System.out.println("error_in_guess_forecast, set error!");
							if (Experiment.isStepByStepDebug == true){
								UI.safeMap_In_debug("error_in_guess_forecast_設定模式錯誤");
							}
						}
					}


					if (isStrange == true){
						if (isCorner == true && corner.isSimulation() == false){
							corner.set_weight(this.possibleMap[10][pgrid.getY()][pgrid.getX()]);
							corner.setSimulationed();
						}else if (isCenter == true && center.isSimulation() == false){
							center.set_weight(this.possibleMap[10][pgrid.getY()][pgrid.getX()]);
							center.setSimulationed();
						}else if (isCorner == false && isCenter == false && edge.isSimulation() == false){
							edge.set_weight(this.possibleMap[10][pgrid.getY()][pgrid.getX()]);
							edge.setSimulationed();
						}
					}
				}
			}
//			$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
			if (plink.size() > 0 && this.get_map().getRemainInitialGrid() < (this.get_map().getMapY() * this.get_map().getMapX())
					&& this.caller.missCombineLink.size() > 0 && this.mapLink.size() > 1){
				int x, y;
				double digValue = 0;
				for (int index = 0 ; index < plink.size() ; index++){
					x = plink.get(index).getX();
					y = plink.get(index).getY();
					if (digValue == 0){
						if (this.possibleMap[10][y][x] > 0){
							digValue = this.possibleMap[10][y][x]; 
						}
					}else {
//						if (this.possibleMap[10][y][x] > 0 && this.possibleMap[10][y][x] != digValue && this.caller.isSimulation() == false){
//							System.out.println("符合條件1_4_5的範例");
//							UI.safeMap_In_debug("符合條件1_4_5的CSP範例");
//							TeddyMethod.condition145 = true;
//							break;
//						}
					}
				}
			}
			
//			if (TeddyMethod.condition145 == true && TeddyMethod.condition2 == true && TeddyMethod.condition3 == true){
//				System.out.println("找到符合所有條件的範例");
//				UI.safeMap_In_debug("符合所有條件的CSP範例");
//			}
			
//			$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

			LinkedList<Player_Grid> bestlink = new LinkedList<Player_Grid>();
			// 重新加入最有價值的點
			for (Player_Grid pgrid : plink){
				if (this.possibleMap[10]
						[pgrid.getY()][pgrid.getX()] > 0){
					//					if (isDebug == true && bestlink.isEmpty() == false){
					//						System.out.println("point value = " + this.possibleMap[10]
					//									[pgrid.getCoordinateY()][pgrid.getCoordinateX()]);
					//						System.out.println("best value = " + this.possibleMap[10]
					//								  [bestlink.getFirst().getCoordinateY()][bestlink.getFirst().getCoordinateX()]);
					//					}
					if (bestlink.isEmpty() == true){
						bestlink.addLast(pgrid);
					}else if (this.possibleMap[10]
							[pgrid.getY()][pgrid.getX()]
									> this.possibleMap[10]
											[bestlink.getFirst().getY()][bestlink.getFirst().getX()]){
						bestlink.clear();
						bestlink.addLast(pgrid);
					}else if (this.possibleMap[10]
							[pgrid.getY()][pgrid.getX()]
									== this.possibleMap[10]
											[bestlink.getFirst().getY()][bestlink.getFirst().getX()]){
						bestlink.addLast(pgrid);
					}
				}
			}

			return bestlink;
		}

		private void guess_forecast_evaluate(Player_Grid pgrid){		// 每個機率都評估
			if (isUseGuessMethod_forecast_stepValue == true){
				for (int evaluateNumber = 0 ; evaluateNumber <= 8 ; evaluateNumber++){
					if (this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] > 0){
						this.possibleMap[10][pgrid.getY()][pgrid.getX()]
								+= (this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] 
										* this.get_digValue_in_simulation_stypeMode(pgrid, evaluateNumber
												, this.isNeedCombineInSimulation(pgrid, evaluateNumber)));
						System.gc();	//釋放記憶體
					}
				}
			}else if (isUseGuessMethod_forecast_answerValue == true){
				for (int evaluateNumber = 0 ; evaluateNumber <= 8 ; evaluateNumber++){
					if (this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] > 0){
						this.possibleMap[10][pgrid.getY()][pgrid.getX()]
								+= (this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] 
										* this.get_digValue_in_simulation_answerMode(pgrid, evaluateNumber
												, this.isNeedCombineInSimulation(pgrid, evaluateNumber)));
						System.gc();	//釋放記憶體
					}
				}
			}else if (isUseGuessMethod_forecast_step_and_answer_Value == true){
				for (int evaluateNumber = 0 ; evaluateNumber <= 8 ; evaluateNumber++){
					if (this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] > 0){
						this.possibleMap[10][pgrid.getY()][pgrid.getX()]
								+= (this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] 
										* this.get_get_digValue_in_simulation_step_and_answerMode(pgrid, evaluateNumber
												, this.isNeedCombineInSimulation(pgrid, evaluateNumber)));
						System.gc();	//釋放記憶體
					}
				}
			}else {
				if (Experiment.isLogicTest == true){
					System.out.println("error_in_guess_forecast_evaluate, set error!");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_guess_forecast_evaluate_設定模式錯誤");
					}
				}
			}

			if (isDebug == true){
				//				System.out.println("show the possible of " + evaluateNumber + " is " + this.possibleMap[evaluateNumber][pgrid.getCoordinateY()][pgrid.getCoordinateX()]);
				//				System.out.println("show add value in " + evaluateNumber + " of " + pgrid.getCoordinate() + " is " + digValue);
				//				System.out.println("show total value in " + evaluateNumber + " of " + pgrid.getCoordinate() + " is " + this.possibleMap[10][pgrid.getCoordinateY()][pgrid.getCoordinateX()]);
			}
		}

		private void guess_forecast_evaluate_MaxPossible(Player_Grid pgrid){		// 只評估最大的機率
			double maxPossible = 0;
			LinkedList<Integer> evaluateValue = new LinkedList<Integer>();
			for (int evaluateNumber = 0 ; evaluateNumber <= 8 ; evaluateNumber++){
				//				System.out.println(evaluateNumber + ", " + this.possibleMap[evaluateNumber][pgrid.getCoordinateY()][pgrid.getCoordinateX()]);
				if (this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] > maxPossible){
					maxPossible = this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()];
					evaluateValue.clear();
					evaluateValue.add(evaluateNumber);
				}else if(this.possibleMap[evaluateNumber][pgrid.getY()][pgrid.getX()] == maxPossible){
					evaluateValue.add(evaluateNumber);
				}
			}

			int evaluateNumber = evaluateValue.get((int)(Math.random()*evaluateValue.size())).intValue();

			if (isUseGuessMethod_forecast_stepValue == true){
				this.possibleMap[10][pgrid.getY()][pgrid.getX()]
						= this.get_digValue_in_simulation_stypeMode(pgrid, evaluateNumber, this.isNeedCombineInSimulation(pgrid, evaluateNumber));
				System.gc();	//釋放記憶體
			}else if (isUseGuessMethod_forecast_answerValue == true){
				this.possibleMap[10][pgrid.getY()][pgrid.getX()]
						= this.get_digValue_in_simulation_answerMode(pgrid, evaluateNumber, this.isNeedCombineInSimulation(pgrid, evaluateNumber));
				System.gc();	//釋放記憶體
			}else if (isUseGuessMethod_forecast_step_and_answer_Value == true){
				this.possibleMap[10][pgrid.getY()][pgrid.getX()]
						= this.get_get_digValue_in_simulation_step_and_answerMode(pgrid, evaluateNumber, this.isNeedCombineInSimulation(pgrid, evaluateNumber));
			}else {
				if (Experiment.isLogicTest == true){
					System.out.println("error_in_guess_forecast_evaluate_MaxPossible, set error!");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_guess_forecast_evaluate_MaxPossible_設定模式錯誤");
					}
				}
			}
		}

		private double get_digValue_in_simulation_stypeMode(Player_Grid pgrid, int evaluateNumber, boolean isNeedCombine){	// 階段性分數
			if (evaluateNumber == 0){		// 零的分數
				return this.caller.caller.value_of_SP;
			}else {
				Player_Board sMap = new Player_Board(this.get_map(), pgrid, (short)evaluateNumber);
				Teddy_Map sroot = null;
				CombineMap scomMap = this.caller;
				try {
					sroot = (Teddy_Map)this.get_root().clone();
				} catch (CloneNotSupportedException cns) {
					System.out.println("error_in_clone of sroot");
					cns.printStackTrace();
				}

				TeddyMethod cfSolutioner = new TeddyMethod(sMap, sroot, scomMap, isNeedCombine);
				return cfSolutioner.get_simulation_digValue();
			}
		}

		private int get_digValue_in_simulation_answerMode(Player_Grid pgrid, int evaluateNumber, boolean isNeedCombine){	// 答案總數分數
			if (evaluateNumber == 0){		// 零的分數
				return this.get_map().getBesireInitialNumber(pgrid);
			}else {
				Player_Board sMap = new Player_Board(this.get_map(), pgrid, (short)evaluateNumber);
				Teddy_Map sroot = null;
				CombineMap scomMap = this.caller;
				try {
					sroot = (Teddy_Map)this.get_root().clone();
				} catch (CloneNotSupportedException cns) {
					System.out.println("error_in_clone of sroot");
					cns.printStackTrace();
				}

				TeddyMethod cfSolutioner = new TeddyMethod(sMap, sroot, scomMap, isNeedCombine);
				return cfSolutioner.get_simulation_answerNumber();
			}
		}
		
		private double get_get_digValue_in_simulation_step_and_answerMode(Player_Grid pgrid, int evaluateNumber, boolean isNeedCombine){
			if (evaluateNumber == 0){		// 零的分數
				return this.caller.caller.value_of_SP * this.get_map().getBesireInitialNumber(pgrid);
			}else {
				Player_Board sMap = new Player_Board(this.get_map(), pgrid, (short)evaluateNumber);
				Teddy_Map sroot = null;
				CombineMap scomMap = this.caller;
				try {
					sroot = (Teddy_Map)this.get_root().clone();
				} catch (CloneNotSupportedException cns) {
					System.out.println("error_in_clone of sroot");
					cns.printStackTrace();
				}
				TeddyMethod cfSolutioner = new TeddyMethod(sMap, sroot, scomMap, isNeedCombine);
				return cfSolutioner.get_simulation_step_and_answerValue();
			}
		}
		
		private void record_global(int[][] iMap, boolean[][] mMap, int usedL, int usedM){
			if (this.mapLink.isEmpty() == true){
				this.usedL = usedL;
				this.mapLink.addLast(new RecordMap(usedL, usedM, this));
//				if (this.isLittleCombine() == false){
					this.mapLink.getLast().record(iMap, mMap, 
							(this.get_map().getRemainInitialGrid() - usedL), (this.get_map().getRemainMineNumber() - usedM));
//				}
			}else {
				for (int index = 0; index < this.mapLink.size() ; index++){
					if (usedM < this.mapLink.get(index).get_usedM()){
						this.mapLink.add(index, new RecordMap(usedL, usedM, this));
//						if (this.isLittleCombine() == false){
							this.mapLink.get(index).record(iMap, mMap, 
									(this.get_map().getRemainInitialGrid() - usedL), (this.get_map().getRemainMineNumber() - usedM));
//						}
						break;
					}else if (usedM == this.mapLink.get(index).get_usedM()){
//						if (this.isLittleCombine() == false){
							this.mapLink.get(index).record(iMap, mMap, 
									(this.get_map().getRemainInitialGrid() - usedL), (this.get_map().getRemainMineNumber() - usedM));
//						}
						break;
					}else if (index == this.mapLink.size() - 1){
						this.mapLink.addLast(new RecordMap(usedL, usedM, this));
//						if (this.isLittleCombine() == false){
							this.mapLink.getLast().record(iMap, mMap, 
									(this.get_map().getRemainInitialGrid() - usedL), (this.get_map().getRemainMineNumber() - usedM));
//						}
						break;
					}
				}
			}
		}

		// 為了transferToGlobalRecord的方法------------------
		private RecordMap get_recordMap(){
			return this.mapLink.get(this.usedIndex);
		}
		
		private void initail_record_index(){		// 索引初始化
			this.usedIndex = 0;
			this.isCarried = false;
		}
		
		private boolean isCarryied(){	//是否已經進位過了
			return this.isCarried;
		}
		
		private void indexAddOne(){			// 編號加一
			this.usedIndex++;
		}
		
		private boolean isCarry(){		// 是否要進位了？
			return (this.usedIndex == this.mapLink.size()) ? true : false;
		}
		
		private void setCarryed() {		// 設定進位
			if (this.isCarried == false) this.isCarried = true;
			this.usedIndex = 0;
		}
		
		// 在使用地雷數為usedM的記錄的記錄次數上加上newCombineNumber
		private void record_multiple(int usedL, int usedM, double newCombineNumber){	
			if (this.mapLink.isEmpty() == true){
				this.usedL = usedL;
				this.mapLink.addLast(new RecordMap(usedL, usedM, this));
				this.mapLink.getLast().add_recordTimes(newCombineNumber);
			}else {
				for (int index = 0; index < this.mapLink.size() ; index++){
					if (usedM < this.mapLink.get(index).get_usedM()){
						this.mapLink.add(index, new RecordMap(usedL, usedM, this));
						this.mapLink.get(index).add_recordTimes(newCombineNumber);
						break;
					}else if (usedM == this.mapLink.get(index).get_usedM()){
						this.mapLink.get(index).add_recordTimes(newCombineNumber);
						break;
					}else if (index == this.mapLink.size() - 1){
						this.mapLink.addLast(new RecordMap(usedL, usedM, this));
						this.mapLink.getLast().add_recordTimes(newCombineNumber);
						break;
					}
				}
			}
		}
				
		// 完全分區專用，將cmap的rmap的記錄乘於multipleCombine倍之後，加到原有的記錄裏
		private void record_multiple_on_CPM(CombineMap cmap, int[][] all_iMap, int usedL, int usedM, double multipleCombine){
			if (this.mapLink.isEmpty() == true){
				this.usedL = usedL;
				this.mapLink.addLast(new RecordMap(usedL, usedM, this));
				this.mapLink.getLast().record_multiple_on_CPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
							(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
			}else {
				for (int index = 0; index < this.mapLink.size() ; index++){
					if (usedM < this.mapLink.get(index).get_usedM()){
						this.mapLink.add(index, new RecordMap(usedL, usedM, this));
						this.mapLink.get(index).record_multiple_on_CPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
								(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
						break;
					}else if (usedM == this.mapLink.get(index).get_usedM()){
						this.mapLink.get(index).record_multiple_on_CPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
								(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
						break;
					}else if (index == this.mapLink.size() - 1){
						this.mapLink.addLast(new RecordMap(usedL, usedM, this));
						this.mapLink.getLast().record_multiple_on_CPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
								(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
						break;
					}
				}
			}
		}
		
		// 獨立分區專用，將cmap的rmap的記錄乘於multipleCombine倍之後，加到原有的記錄裏
		private void record_multiple_on_IPM(CombineMap cmap, int[][] all_iMap, int usedL, int usedM, double multipleCombine){
			if (this.mapLink.isEmpty() == true){
				this.usedL = usedL;
				this.mapLink.addLast(new RecordMap(usedL, usedM, this));
				this.mapLink.getLast().record_multiple_on_IPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
						(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
			}else {
				for (int index = 0; index < this.mapLink.size() ; index++){
					if (usedM < this.mapLink.get(index).get_usedM()){
						this.mapLink.add(index, new RecordMap(usedL, usedM, this));
						this.mapLink.get(index).record_multiple_on_IPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
								(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
						break;
					}else if (usedM == this.mapLink.get(index).get_usedM()){
						this.mapLink.get(index).record_multiple_on_IPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
								(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
						break;
					}else if (index == this.mapLink.size() - 1){
						this.mapLink.addLast(new RecordMap(usedL, usedM, this));
						this.mapLink.getLast().record_multiple_on_IPM(cmap, all_iMap, (this.get_map().getRemainInitialGrid() - usedL), 
								(this.get_map().getRemainMineNumber() - usedM), multipleCombine);
						break;
					}
				}
			}
		}
		
		// ------------------為了transferToGlobalRecord的方法
		
		// 為了transferToGlobalRecord_number_probability的方法------------------
		private void initial_copyMap_index(){
			this.initail_record_index();
			for (RecordMap rmap : this.mapLink){
				rmap.initail_copyMap_index();
			}
		}
		
//		將numberProbabilityMap乘於multipleCombine倍之後，加到原本的記錄中，完成分區化算數字機率專用
		private void record_multiple(double[][][] numberProbabilityMap, int usedM, double multipleCombine){
			for (int index = 0; index < this.mapLink.size() ; index++){
				if (usedM == this.mapLink.get(index).get_usedM()){
					this.mapLink.get(index).record_multiple(numberProbabilityMap, multipleCombine);
					break;
				}else if (index == this.mapLink.size() - 1){
					if (Experiment.isLogicTest == true){ 
						System.out.println("error_in_record_multiple，找不到相符的地雷數記錄");
						if (Experiment.isStepByStepDebug == true){
							UI.safeMap_In_debug("error_in_record_multiple，找不到相符的地雷數記錄");
						}
					}
				}
			}
		}
		// ------------------為了transferToGlobalRecord_number_probability的方法
		
		private void showRecordMap(int recordType){
			System.out.println("used lattice = " + this.usedL);
			for (int index = 0 ; index < this.mapLink.size() ; index++){
				System.out.println("used mine = " + this.mapLink.get(index).get_usedM());
				System.out.println("record times = " + this.mapLink.get(index).get_recordTimes());
				this.mapLink.get(index).showRecordMap(recordType);
			}
		}
	}

	private class RecordMap implements Cloneable{
		private RecordMapDatabase caller;
		private final int usedL;
		private final int usedM;
		private double[][][] recordMap;
		LinkedList<boolean [][]> copyMap;	// 複製的地圖
		private double recordTimes;		//紀錄次數
		private BigDecimal caseNumber;		//案例的總數
		private double caseRatio;			// 案例的比例
		private int usedIndex;		//目前案例所使用的編號
		private boolean isCarried;	//已經進位過了嗎？

		private RecordMap(int usedL, int usedM, RecordMapDatabase caller){
			this.caller = caller;
			this.usedL = usedL;
			this.usedM = usedM;
			this.recordMap = new double[10][this.get_map().getMapY()][this.get_map().getMapX()];
			/*
			 * 一共記錄10張地圖，0-8分別是出現0到8的次數，9是地圖次數
			 */
			if (caller.isLittleCombine() == true) { this.copyMap = new LinkedList<boolean [][]>(); }
			this.recordTimes = 0;
			this.caseNumber =  BigDecimal.valueOf(0);
			this.caseRatio = 0;
			this.usedIndex = 0;
			this.isCarried = false;
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			RecordMap rmap = null;
			try {
				rmap = (RecordMap)super.clone();
				rmap.recordMap = new double[10][this.get_map().getMapY()][this.get_map().getMapX()];
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						rmap.recordMap[9][y][x] = this.recordMap[9][y][x];		// 只需要複製地雷記錄
					}
				}
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return rmap;
		}

	    private void finalize_By_Teddy() {
	    	if (this.copyMap != null){
	    		this.copyMap.clear();
	    		this.copyMap = null;
	    	}
	    	
			this.caller = null;
			this.recordMap = null;
			this.caseNumber = null;
		}
		
		private boolean isLittleCombine(){
			return this.caller.isLittleCombine();
		}

		private boolean isUseGuessMethod_forecast(){
			return this.caller.isUseGuessMethod_forecast();
		}
		
		private boolean isUseBlockCombine(){
			return this.caller.isUseBlockCombine();
		}
		
		private boolean isUseBlockCombine_unique(){
			return this.caller.isUseBlockCombine_unique();
		}
		
		private boolean isUseBlockCombine_complete(){
			return this.caller.isUseBlockCombine_complete();
		}
		
		private boolean isUseCombineInEvaluateJudge(){
			return this.caller.isUseCombineInEvaluateJudge();
		}
		
		private Player_Board get_map(){
			return this.caller.get_map();
		}

		public double get_recordTimes() {
			return this.recordTimes;
		}

		private int get_usedM(){
			return this.usedM;
		}
		
		private int get_usedL(){
			return this.usedL;
		}
		
//		private LinkedList<boolean [][]> get_copyMap(){
//			return this.copyMap;
//		}
		
		private BigDecimal get_caseNumber(){
			return this.caseNumber;
		}

		private double get_caseRatio(){
			return this.caseRatio;
		}
		
		private void showRecordMap(int recordType) {
			for (int y = this.get_map().getMapY() - 1 ; y >= 0 ; y--){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					System.out.printf("%.2f", this.recordMap[recordType][y][x]);
//					System.out.print(this.recordMap[recordType][y][x]);
					System.out.print("\t");
				}
				System.out.println("");
			}
			System.out.println("");
		}

		private void record(int[][] iMap, boolean[][] mMap, int remainL, int remainM){
			int besideL, besideM;
			boolean isInformationGrid;
			this.recordTimes++;
			double strangeMinePossible = (double)remainM / remainL;
			double strangeNoMinePossible = (1 - strangeMinePossible);
			double noMinePossible;
			
			// 當有預測數字時，才需要儲存盤面
			if (this.isUseGuessMethod_forecast() == true && this.isLittleCombine() == true 
					&& (this.isUseBlockCombine_complete() == true || this.isUseBlockCombine_unique() == true)){
				// 複製假設地圖
				boolean[][] copyMmap = new boolean[this.get_map().getMapY()][this.get_map().getMapX()];
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (mMap[y][x] == true) copyMmap[y][x] = true;
					}
				}
				
				this.copyMap.addLast(copyMmap);
				
				if (Experiment.isShowNowSitulation == true){
					TeddyMethod.total_copyMap = TeddyMethod.total_copyMap.add(BigDecimal.valueOf(1));
//					// 每隔一秒印出一次copyMap次數
//					if ((System.currentTimeMillis() - CommonField.lastShowTime > CommonField.showTimePeriod)//){
//							|| (CommonField.total_copyMap.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.valueOf(0)) == 0)){
//						System.out.println("\nThis is record");
//						System.out.println("Time = " + (double)(System.currentTimeMillis()-startTime)/1000 + "s");
//						System.out.println("Now save map number = " + CommonField.total_copyMap.toString());
//						CommonField.lastShowTime = System.currentTimeMillis();		// 更新時間
//					}
				}
			}
			
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (this.get_map().isInitial(y, x) == true){	// 只紀錄初始點，且若是小組合則只儲存假設的資訊
						if (iMap[y][x] > 0 && mMap[y][x] == TeddyMethod.isMine){
							this.recordMap[9][y][x]++;
						}else if (iMap[y][x] == 0 && this.isLittleCombine() == false){
							this.recordMap[9][y][x] += strangeMinePossible;
						}
						
//						this.isUseBlockCombine() == false && this.isUseBlockCombine_complete() == false
//								&& this.isUseBlockCombine_unique() == false
//								&& this.isUseGuessMethod_forecast() == true 

						// 求出未知點和陌生點的數字機率
						if (this.isLittleCombine() == false && this.isUseGuessMethod_forecast() == true
								&& ((iMap[y][x] > 0 && mMap[y][x] == TeddyMethod.notMine) || (iMap[y][x] == 0))){
							besideL = 0;
							besideM = 0;
							for (int j = y-1 ; j <= y+1 ; j++){
								for (int i = x-1 ; i <= x+1 ; i++){
									if (i != x || j != y){		//中間的點不用理
										if (this.get_map().isInBoard(j, i) == true){
											if (iMap[j][i] == 0 && this.get_map().isInitial(j, i) == true){
												besideL++;	//未假設的未打開空格數
											}else if ((iMap[j][i] > 0 && mMap[j][i] == TeddyMethod.isMine) || this.get_map().isFlag(j, i) == true){
												besideM++;
											}
										}
									}
								}
							}

							//是否是資訊點
							isInformationGrid = (iMap[y][x] > 0) ? true : false;
							noMinePossible = (iMap[y][x] > 0) ? 1 : strangeNoMinePossible;

							//							if (isLittleCombine == true){
							//								noMinePossible = 1;			// 數字機率等到全域計算時擁有正確的地雷機率才會進行正確的計算
							//							}else {
							//								
							//							}

							// 算出數字機率
							for (int index = 0 ; index <= 8 ; index++){
								if (isDebug == true && index == 0 && this.usedM == 5 && x == 3 && y == 5){
									System.out.println("show number possible in record");
									double possible = this.caller.caller.detail_possible_forecast(index, besideL, besideM
											, (this.get_map().getRemainInitialGrid() - this.usedL)
											, (this.get_map().getRemainMineNumber() - this.usedM), isInformationGrid);
									System.out.println("index = " + index);
									System.out.println("besideL = " + besideL);
									System.out.println("besideM = " + besideM);
									System.out.println("remainL = " + (this.get_map().getRemainInitialGrid() - this.usedL));
									System.out.println("remainM = " + (this.get_map().getRemainMineNumber() - this.usedM));
									System.out.println("possible = " + possible);
									if (isDebug == true && x == 1 && y == 4 && index == 2 && possible > 0){
									}
								}
								this.recordMap[index][y][x] 
										+= (noMinePossible * this.caller.caller.detail_possible_forecast(index, besideL, besideM
												, (this.get_map().getRemainInitialGrid() - this.usedL)
												, (this.get_map().getRemainMineNumber() - this.usedM), isInformationGrid));
							}
							
							// 為了isNeedCombineInSimulation而搜集需要的資料
							if (this.isUseCombineInEvaluateJudge() == true){
								if (besideL > this.caller.caller.besideL_Map[y][x]){
									this.caller.caller.besideL_Map[y][x] = besideL;
								}

								if (besideM > this.caller.caller.besideM_max_Map[y][x]){
									this.caller.caller.besideM_max_Map[y][x] = besideM;
								}

								if (besideM < this.caller.caller.besideM_min_Map[y][x]){
									this.caller.caller.besideM_min_Map[y][x] = besideM;
								}	
							}
						}
					}
				}
			}
		}

		private void countCaseNumber(){
//			TeddyMethodTool tool = new TeddyMethodTool();
			if (Experiment.isLogicTest == true){
				if (this.caseNumber.intValue() != 0){
					System.out.println("error_in_countCaseNumber，因為初始值不為零");
				}
			}
			this.caseNumber = (new TeddyMethodTool().conbineNumber(this.get_map().getRemainInitialGrid() - this.usedL, this.get_map().getRemainMineNumber() - this.usedM));
			if (Experiment.isLogicTest == true){
				if (this.caseNumber.intValue() == 0){
					System.out.println("error_in_countCaseNumber，因為case總數為 0");
					System.out.println("remainL = " + this.get_map().getRemainInitialGrid());
					System.out.println("remainM = " + this.get_map().getRemainMineNumber());
					System.out.println("usedL = " + this.usedL);
					System.out.println("usedM = " + this.usedM);
				}
			}
		}

		private void countCaseRatio(BigDecimal totalPossibleN){
			this.caseRatio = this.caseNumber.multiply(BigDecimal.valueOf(this.recordTimes)).divide(totalPossibleN, 16, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
	
		// 為了transferToGlobalRecord的方法------------------
				
		private void add_recordTimes(double newCombineNumber){
			this.recordTimes += newCombineNumber;
		}
		
		private void multiply_recordTimes(double newCombineNumber){
			this.recordTimes *= newCombineNumber;
		}
		
		// 將mulmap的記錄加入原本的記錄中，獨立分區化專用
		private void record_multiple(RecordMap mulmap){
			if (this.isUseGuessMethod_forecast() == false){
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (mulmap.recordMap[9][y][x] > 0){
							this.recordMap[9][y][x] = mulmap.recordMap[9][y][x] * this.get_recordTimes(); 
						}else if (this.recordMap[9][y][x] > 0){
							this.recordMap[9][y][x] *= mulmap.get_recordTimes();
						}
					}
				}
			}else {
				CombineMap cmap = mulmap.caller.caller;
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (cmap.isInformationGrid(y, x) == true){
							// 資訊點
							for (int index = 0 ; index <= 9 ; index++){
								this.recordMap[index][y][x] =
										mulmap.recordMap[index][y][x] * this.get_recordTimes();
							}
						}else if (cmap.isSubinformationGrid(y, x) == true){
							// 次資訊點
							this.recordMap[9][y][x] *= mulmap.get_recordTimes();
							for (int index = 0 ; index < 9 ; index++){
								this.recordMap[index][y][x] =
										mulmap.recordMap[index][y][x] * this.get_recordTimes();
							}
						}else {
							// 完全陌生點
							for (int index = 0 ; index <= 9 ; index++){
								this.recordMap[index][y][x] *= mulmap.get_recordTimes();
							}
						}
					}
				}
			}
//			CombineMap cmap = mulmap.caller.caller;
//			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
//				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
//					if (cmap.isInformationGrid(y, x) == true){
//						for (int index = 0 ; index <= 9 ; index++){
//							this.recordMap[index][y][x] =
//									mulmap.recordMap[index][y][x] * this.get_recordTimes();
//						}
//					}else if (this.isUseGuessMethod_forecast() == true && cmap.isSubinformationGrid(y, x) == true){
//						this.recordMap[9][y][x] *= mulmap.get_recordTimes();
//						for (int index = 0 ; index < 9 ; index++){
//							this.recordMap[index][y][x] =
//									mulmap.recordMap[index][y][x] * this.get_recordTimes();
//						}
//					}else if (this.recordMap[TeddyMethod.9][y][x] > 0){
//						for (int index = 0 ; index <= 9 ; index++){
//							this.recordMap[index][y][x] *= mulmap.get_recordTimes();
//						}
//					}
//				}
//			}
			this.multiply_recordTimes(mulmap.get_recordTimes());	// 總計錄次數調整
		}
		
		// 將cmap的記錄乘於multipleCombine倍之後，加到原本的記錄中，完全分區化專用
		private void record_multiple_on_CPM(CombineMap cmap, int[][] all_iMap, int remainL, int remainM, double multipleCombine){
			if (cmap != null){
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (this.get_map().isInitial(y, x) == true){	// 未知點
							if (cmap.isInformationGrid(y, x) == true){		// 需要加乘的點
								this.recordMap[9][y][x]
										+= (cmap.rMap.get_recordMap().recordMap[9][y][x] * multipleCombine);
							}
						}
					}
				}
			}else {		// 處理陌生點
				double strangeMinePossible = (double)remainM / remainL;
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (this.get_map().isInitial(y, x) == true){	// 未知點
							if (all_iMap[y][x] == 0){		// 沒有被假設過的陌生點
								this.recordMap[9][y][x] += (strangeMinePossible * multipleCombine);
							}
						}
					}
				}
			}
		}
		
		// 將cmap的記錄乘於multipleCombine倍之後，加到原本的記錄中，獨立分區化專用
		private void record_multiple_on_IPM(CombineMap cmap, int[][] all_iMap, int remainL, int remainM, double multipleCombine){
//			參考方法：private void transferToGlobalRecord_number_probability_on_CPM(LinkedList<Player_Grid> plink)
			
			double strangeMinePossible = (double)remainM / remainL;
			if (cmap != null){
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (this.get_map().isInitial(y, x) == true){	// 未知點
							if (cmap.isInformationGrid(y, x) == true){		// 資訊點
								if (this.isUseGuessMethod_forecast() == false){
									this.recordMap[9][y][x]
											+= (cmap.rMap.get_recordMap().recordMap[9][y][x] * multipleCombine);
								}else {
									for (int index = 0 ; index <= 9 ; index++){
										this.recordMap[index][y][x]
												+= (cmap.rMap.get_recordMap().recordMap[index][y][x] * multipleCombine);
									}
								}
							}else if (cmap.isSubinformationGrid(y, x) == true){	// 次資訊點
//								this.recordMap[9][y][x]
//										+= (strangeMinePossible * multipleCombine);
								
								if (this.isUseGuessMethod_forecast() == true){
									for (int index = 0 ; index < 9 ; index++){
										this.recordMap[index][y][x]
												+= (cmap.rMap.get_recordMap().recordMap[index][y][x] * multipleCombine);
									}
								}
							}
						}
					}
				}
			}else {		// 處理陌生點
				CombineMap parentMap = this.caller.caller;
				LinkedList<Player_Grid> completeStrangeGridLink = null, strangeGridLink = null;
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if (this.get_map().isInitial(y, x) == true){	// 完全陌生點
							if (all_iMap[y][x] == 0){		// 沒有被假設過的陌生點
								// 地雷出現次數
								this.recordMap[9][y][x] += (strangeMinePossible * multipleCombine);
							}
							
							// 計算完全陌生點的數字機率
							if (this.isUseGuessMethod_forecast() == true){
								if (this.get_map().isCompleteStrangeGrid(y, x) == true){
									if (parentMap.isCompleteStrangeGrid(y, x) == true){ // 是否是完全陌生點
										//  加入完全陌生點
										if (completeStrangeGridLink == null){
											completeStrangeGridLink = new LinkedList<Player_Grid>(); 
										}
										completeStrangeGridLink.addLast(this.get_map().getGrid(y, x));
									}else {
										// 加入非完全陌生點
										if (strangeGridLink == null){
											strangeGridLink = new LinkedList<Player_Grid>();
										}
										strangeGridLink.addLast(this.get_map().getGrid(y, x));
									}
								}
							}
						}
					}
				}
				
				if (this.isUseGuessMethod_forecast() == true){
					int besideL, besideM, y, x;
					double noMinePossible, possible;
					
					if (strangeGridLink != null){				// 如果有非完全陌生點
						for (Player_Grid p : strangeGridLink){
							y = p.getY();
							x = p.getX();
							
							besideL = 0;
							besideM = 0;

							for (int j = y-1 ; j <= y+1 ; j++){
								for (int i = x-1 ; i <= x+1 ; i++){
									if (i != x || j != y){		//中間的點不用理
										if (this.get_map().isInBoard(j, i) == true){
											if (this.get_map().isInitial(j, i) == true){
												besideL++;	//未假設的未打開空格數
											}else if (this.get_map().isFlag(j, i) == true){
												besideM++;
											}
										}
									}
								}
							}
							
							// 為了isNeedCombineInSimulation而搜集需要的資料
							if (parentMap.isUseCombineInEvaluateJudge() == true){
								if (besideL > parentMap.besideL_Map[y][x]){
									parentMap.besideL_Map[y][x] = besideL;
								}
								
								if (besideM > parentMap.besideM_max_Map[y][x]){
									parentMap.besideM_max_Map[y][x] = besideM;
								}
								
								if (besideM < parentMap.besideM_min_Map[y][x]){
									parentMap.besideM_min_Map[y][x] = besideM;
								}	
							}
							
							noMinePossible = 1 - (double)remainM / remainL;
							for (int index = 0 ; index < 9 ; index++){
								possible = this.caller.caller.detail_possible_forecast(index, besideL, besideM, remainL, remainM, false);
								parentMap.rMap.possibleMap[index][y][x] += (noMinePossible * possible);
							}
						}
					}
					
					if (completeStrangeGridLink != null){		// 如果有完全陌生點
						boolean isCorner, isCenter;
						// 最佳化處理
						RegGrid reg = null;				// 暫存
						parentMap.corner = new RegGrid();	// 角落
						parentMap.center = new RegGrid();	// 中央
						parentMap.edge = new RegGrid();		// 邊
						
						
						for (Player_Grid p : completeStrangeGridLink){
							y = p.getY();
							x = p.getX();
							// 是否是角落
							isCorner = ((y == 0 || y == (this.get_map().getMapY() - 1))
									&& (x == 0 || x == (this.get_map().getMapX() - 1))) ? true : false;
							// 是否是中央
							isCenter = ((y > 0 && y < this.get_map().getMapY() - 1)
									&& (x > 0 && x < this.get_map().getMapX() - 1)) ? true : false;
							
							if ((isCorner == true && parentMap.corner.isSimulation() == true) 								// 在角落是否有記錄
									|| (isCenter == true && parentMap.center.isSimulation() == true) 							// 在中央是否有記錄
									|| (isCorner == false && isCenter == false && parentMap.edge.isSimulation() == true)){	// 在邊是否有記錄
								if (isCorner == true){
									reg = parentMap.corner;
								}else if (isCenter == true){
									reg = parentMap.center;
								}else {
									reg = parentMap.edge;
								}
								
								// 為了isNeedCombineInSimulation而搜集需要的資料
								if (parentMap.isUseCombineInEvaluateJudge() == true){
									parentMap.besideL_Map[y][x] = reg.get_besideL();
									parentMap.besideM_max_Map[y][x] = reg.get_besideM_max();
									parentMap.besideM_min_Map[y][x] = reg.get_besideM_min();
								}
								
								// 複製數值
								for (int index = 9 ; index >= 0 ; index--){
									parentMap.rMap.possibleMap[index][y][x] = reg.get_possible(index);
								}
							}else {
								besideL = 0;
								besideM = 0;

								for (int j = y-1 ; j <= y+1 ; j++){
									for (int i = x-1 ; i <= x+1 ; i++){
										if (i != x || j != y){		//中間的點不用理
											if (this.get_map().isInBoard(j, i) == true){
												if (this.get_map().isInitial(j, i) == true){
													besideL++;	//未假設的未打開空格數
												}else if (this.get_map().isFlag(j, i) == true){
													besideM++;
												}
											}
										}
									}
								}
								
								// 為了isNeedCombineInSimulation而搜集需要的資料
								if (parentMap.isUseCombineInEvaluateJudge() == true){
									if (besideL > parentMap.besideL_Map[y][x]){
										parentMap.besideL_Map[y][x] = besideL;
									}
									
									if (besideM > parentMap.besideM_max_Map[y][x]){
										parentMap.besideM_max_Map[y][x] = besideM;
									}
									
									if (besideM < parentMap.besideM_min_Map[y][x]){
										parentMap.besideM_min_Map[y][x] = besideM;
									}	
								}
								
								noMinePossible = 1 - (double)remainM / remainL;
								for (int index = 0 ; index < 9 ; index++){
									possible = this.caller.caller.detail_possible_forecast(index, besideL, besideM, remainL, remainM, false);
									parentMap.rMap.possibleMap[index][y][x] += (noMinePossible * possible);
								}
							}
							
							// 登記暫存資料
							if (((isCorner == true && parentMap.corner.isSimulation() == false) 
									|| (isCenter == true && parentMap.center.isSimulation() == false) 
									|| (isCorner == false && isCenter == false && parentMap.edge.isSimulation() == false))){
								if (isCorner == true){
									reg = parentMap.corner;
								}else if (isCenter == true){
									reg = parentMap.center;
								}else {
									reg = parentMap.edge;
								}
								
								// 為了isNeedCombineInSimulation而搜集需要的資料
								if (parentMap.isUseCombineInEvaluateJudge() == true){
									reg.set_besideInfomation(parentMap.besideL_Map[y][x], parentMap.besideM_max_Map[y][x], parentMap.besideM_min_Map[y][x]);
								}
								
								for (int index = 9 ; index >= 0 ; index--){
									reg.set_possible(index, parentMap.rMap.possibleMap[index][y][x]);
								}
								
								reg.setSimulationed();
							}
						}
					}
				}
			}
		}
		
		// 將numberProbabilityMap乘於multipleCombine倍之後，加到原本的記錄中，完成分區化算數字機率專用
		private void record_multiple(double[][][] numberProbabilityMap, double multipleCombine){
			for (int y = 0 ; y < this.get_map().getMapY() ; y++){
				for (int x = 0 ; x < this.get_map().getMapX() ; x++){
					if (this.get_map().isInitial(y, x) == true){	// 未知點
						for (int index = 0 ; index < 9 ; index++){
							if (numberProbabilityMap[index][y][x] > 0){		// 需要加乘的點
								this.recordMap[index][y][x] += numberProbabilityMap[index][y][x] * multipleCombine;
							}
						}
					}
				}
			}
		}
				
		// ------------------為了transferToGlobalRecord的方法
		
		// 為了transferToGlobalRecord_number_probability的方法------------------
		private boolean [][] get_safeMap(){
			return this.copyMap.get(this.usedIndex);
		}
		
		private void initail_copyMap_index(){		// 索引初始化
			this.usedIndex = 0;
			this.isCarried = false;
		}
		
//		private boolean isCarryied(){	//是否已經進位過了
//			return this.isCarried;
//		}
		
		private void indexAddOne(){			// 編號加一
			this.usedIndex++;
		}
		
		private boolean isCarry(){		// 是否要進位了？
			return (this.usedIndex == this.copyMap.size()) ? true : false;
		}
		
		private void setCarryed() {		// 設定進位
			if (this.isCarried == false) this.isCarried = true;
			this.usedIndex = 0;
		}
		
		// ------------------為了transferToGlobalRecord_number_probability的方法
	}

	private class RegGrid{
		private double weight;
		private double possible[];
		private boolean isSimulationed;
		int besideL, besideM_max, besideM_min;
		private RegGrid(){
			this.weight = 0;
			this.possible = new double[10];
			this.isSimulationed = false;
			this.besideL = Integer.MIN_VALUE;
			this.besideM_max = Integer.MIN_VALUE;
			this.besideM_min = Integer.MAX_VALUE;
		}

		private void set_weight(double point){
			if (this.isSimulationed == false){
				this.weight += point;
			}else {
				if (Experiment.isLogicTest == true){
					System.out.println("error_in_set_possible，因為已經設紀錄完成，卻要再紀錄");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_set_possible_因為已經設紀錄完成_卻要再紀錄");
					}
				}
			}
		}

		public double get_weight() {
			if (Experiment.isLogicTest == true){
				if (this.isSimulationed == false){
					System.out.println("error_in_get_weight，因為還沒有紀錄完成就取出資訊");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_set_possible_因為還沒有紀錄完成就取出資訊");
					}
				}
			}
			return this.weight;
		}

		private void set_possible(int index, double value){
			if (this.isSimulationed == false){
				this.possible[index] = value;
			}else {
				if (Experiment.isLogicTest == true){
					System.out.println("error_in_set_possible，因為已經設紀錄完成，卻要再紀錄");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_set_possible_因為已經設紀錄完成_卻要再紀錄");
					}
				}
			}
		}

		private double get_possible(int index){
			if (Experiment.isLogicTest == true){
				if (this.isSimulationed == false){
					System.out.println("error_in_get_possible，因為還沒有紀錄完成就取出資訊");
					if (Experiment.isStepByStepDebug == true){
						UI.safeMap_In_debug("error_in_set_possible_因為還沒有紀錄完成就取出資訊");
					}
				}
			}
			return this.possible[index];
		}

		private void set_besideInfomation(int besideL, int besideM_max, int besideM_min){
			this.besideL = besideL;
			this.besideM_max = besideM_max;
			this.besideM_min = besideM_min;
		}
		
		private int get_besideL(){
			return this.besideL;
		}
		
		private int get_besideM_max(){
			return this.besideM_max;
		}
		
		private int get_besideM_min(){
			return this.besideM_min;
		}
		
		private void setSimulationed(){
			this.isSimulationed = true;
		}

		private boolean isSimulation(){
			return this.isSimulationed;
		}
	}
	
	private class Big_block_combineMap{		// 專為完全分區時計算數字機率時使用
		private CombineMap caller;
		private int index;
		private LinkedList<CombineMap> blockLink = new LinkedList<CombineMap>();
		private LinkedList<Player_Grid> gridLink = new LinkedList<Player_Grid>();
		
		private Big_block_combineMap(int index, CombineMap caller){
			this.index = index;
			this.caller = caller;
		}
		
		@Override
	    protected void finalize() {
	    	this.caller = null;
//			for (CombineMap cmap : this.blockLink){
//				cmap.finalize_By_Teddy();
//			}
			this.blockLink.clear();
			this.blockLink = null;
			this.gridLink.clear();
			this.gridLink = null;
		}
		
		private int get_index(){
			return this.index;
		}
		
		private void index_Subtract_one(){
			this.index--;
		}
		
		private LinkedList<CombineMap> get_blockLink(){
			return this.blockLink;
		}
		
//		private LinkedList<Player_Grid> get_gridLink(){
//			return this.gridLink;
//		}
		
		private void initial(){
			for (CombineMap cmap : this.blockLink){
				cmap.rMap.initial_copyMap_index();
			}
		}
		
		private void add_block(CombineMap cmap){	// 增加區域
			this.blockLink.addLast(cmap);
		}
		
		private void add_gridLink(Player_Grid grid){	// 增加候選點
			this.gridLink.addLast(grid);
		}
		
		private Player_Board get_map(){
			return this.caller.get_map();
		}
		
		private void merge(Big_block_combineMap bcmap){	// 合併其他的區域
			for (CombineMap cmap : bcmap.blockLink){
				this.blockLink.addLast(cmap);
			}
			
			for (Player_Grid p : bcmap.gridLink){
				this.gridLink.addLast(p);
			}
		}
		
		private boolean isCombineOver(){
			return this.blockLink.getFirst().rMap.isCarryied();
		}
		
		private boolean[][] getNextCombineMap(){		// 回傳下一個組合地圖
			boolean[][] combineMap = new boolean[this.get_map().getMapY()][this.get_map().getMapX()];
			boolean[][] blockMap;
			
			for (CombineMap cmap : this.blockLink){
				blockMap = cmap.rMap.get_recordMap().get_safeMap();		//取得區域地圖
				for (int y = 0 ; y < this.get_map().getMapY() ; y++){
					for (int x = 0 ; x < this.get_map().getMapX() ; x++){
						if(cmap.isInformationGrid(y, x) == true){
							combineMap[y][x] = blockMap[y][x];			// 覆蓋地圖
						}
					}
				}
			}
			return combineMap;
		}
		
		private int getUsedMine(){						// 回傳回傳地圖所使用的地雷數
			int usedM = 0;
			for (CombineMap cmap : this.blockLink){
				usedM += cmap.rMap.get_recordMap().get_usedM();
			}
			return usedM;
		}
		
		private void indexAddOne(){			// 編號加一
			
//			maplink.getLast().rMap.indexAddOne();
//
//			if (maplink.getLast().rMap.isCarry() == true){	// 確實的跑完每一個組合
//				int index = maplink.size() - 1;
//				do {
//					maplink.get(index).rMap.setCarryed();
//					index--;	//倒退一位
//					if (index < 0) {
//						break;	// 代表已經進位到第一個了！
//					}else {
//						maplink.get(index).rMap.indexAddOne();
//					}
//				}while (maplink.get(index).rMap.isCarry() == true);
//			}
			
			int blockIndex = this.blockLink.size() - 1;	// 預設最後一個元素
			
//			if (this.blockLink.get(blockIndex).rMap.get_recordMap().isCarry() == true){		// 如果區域的其中一種使用地雷數的copyMap已經跑完了
//				this.blockLink.get(blockIndex).rMap.get_recordMap().setCarryed();			// 設定進位
//				this.blockLink.get(blockIndex).rMap.indexAddOne();							// 設定區域的其他使用地雷數
//				while(this.blockLink.get(blockIndex).rMap.isCarry() == true){				// 如果區域的所有使用地雷數的copyMap已經跑完了
//					this.blockLink.get(blockIndex).rMap.setCarryed();
//					blockIndex--;
//					if (blockIndex < 0){
//						break;	// 代表已經進位到第一個了！
//					}else {
//						this.blockLink.get(blockIndex).rMap.indexAddOne();
//					}
//				}
//			}
			
			do {
				this.blockLink.get(blockIndex).rMap.get_recordMap().indexAddOne();		// 區域的其中一種使用地雷數的copyMap index加一
				if (this.blockLink.get(blockIndex).rMap.get_recordMap().isCarry() == true){		// 如果區域的其中一種使用地雷數的copyMap已經跑完了
					this.blockLink.get(blockIndex).rMap.get_recordMap().setCarryed();			// 設定進位
					this.blockLink.get(blockIndex).rMap.indexAddOne();							// 設定區域的其他使用地雷數
					if (this.blockLink.get(blockIndex).rMap.isCarry() == true){					// 如果區域的所有使用地雷數的copyMap已經跑完了
						this.blockLink.get(blockIndex).rMap.setCarryed();						// 設定進位
						blockIndex--;
						if (blockIndex >= 0){
							continue;
						}else {
							break;	// 代表已經進位到第一個了！
						}
					}else {
						break;
					}
				}else {
					break;
				}
			}while (true);
		}
	}
	
//	private enum possibleJudge {		//opfjGrid的權重參考
//		haveZeroMines(0), haveOneMines(1), haveTwoMines(2), 
//		haveThreeMines(3), haveFourMines(4), haveFiveMines(5), 
//		haveSixMines(6), haveSevenMines(7), haveEightMines(8), 
//		isMine(9), digValue(10);
//
//		private final int code;
//
//		possibleJudge(int c) {code = c;}
//
//		private int getCode() {return code;}
//	}

//	private enum guess_Weight{			//審局的權重
//		isZero(20), have_sol_in_Probability(5), have_sol_in_CM_Judge(10), have_sol_in_CF_Judge(15), have_sol_in_Single_Judge(20);
//
//		private final int weight;
//
//		guess_Weight(int w) {weight = w;}
//
//		private int getWeight() {return weight;}
//	}
	// ---------------------------------------inner calss area
}
