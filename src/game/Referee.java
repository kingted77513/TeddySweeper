package game;
import game.Referee_Board;

public class Referee {
	public static final String rule_1 = "the_first_move_can_be_a_mine";
	public static final String rule_2 = "the_first_move_can't_be_a_mine";
	public static final String rule_3 = "the_first_move_is_zero";
	
	private String rule;		//使用的規則
	private Referee_Board map;		//使用的地圖
	boolean isFirst;		//是否地圖還沒挖過
	final boolean isRecord;	//是否為讀檔
	boolean isGameOver;		//是否遊戲結束
	boolean isWin;			//是否勝利
	short lastStepState;
	int loopTimes;			// 紀錄這是第幾次輪迴
	int testTimes;			// 紀錄這是第幾次測試
	int playTimes;
	
	public Referee(int mapY, int mapX, int mineNumber, String rule){	//一般的宣告
		this.rule = rule;
		this.isFirst = true;
		this.isRecord = false;
		this.isGameOver = false;
		this.isWin = false;
		this.map = new Referee_Board (mapY, mapX, mineNumber);
	}
	
	public Referee(int mapY, int mapX, int mineNumber, String rule, int loopTimes, int testTimes){	//一般的宣告
		this(mapY, mapX, mineNumber, rule);
		this.loopTimes = loopTimes;
		this.testTimes = testTimes;
		this.playTimes = 0;
	}
	
	public Referee(Referee_Board map){		//使用殘局讀檔的宣告
		this.isFirst = this.isAllInitial(map);
		this.isRecord = true;
		this.rule = Referee.rule_1;
		this.map = map;
	}
	
	public void set_PlayTimes(int times){
		this.playTimes = times;
	}
	
	void finalize_By_Teddy(){
		this.rule = null;
		if (this.map != null){
			this.map.finalize_By_Teddy();
			this.map = null;
		}
	}
	
	private boolean isAllInitial(Referee_Board map){		//測試所讀取的地圖是否未曾打開過
		boolean isall = true;
		for (int y = 0 ; y < map.getMapY() ; y++){
			for (int x = 0 ; x < map.getMapX() ; x++){
				if (map.getState(y, x) != Board.initial){
					isall = false;
					break;
				}
			}
			if (isall == false) break;
		}
		return isall;
	}
	
	public void dig(int y, int x){		//挖
		if (this.map.getState(y, x) == Board.initial){
			if (this.isFirst == false || this.isRecord == true){		//不是第一步
				this.map.dig(y, x);
				if (this.map.getState(y, x) == Board.mine) {
					this.isGameOver = true;	//踩到地雷就結束了
				}
			}else if (this.isRecord == false){		//不是讀檔的地圖
				this.mapCreate(this.map, y, x, this.rule);	//創造地圖
				this.isFirst = false;
				this.map.dig(y, x);
				if (this.map.getState(y, x) == Board.mine) this.isGameOver = true;	//踩到地雷就結束了
			}
			this.lastStepState = this.map.getState(y, x);
		}
	}
	
	public void superDig(int y, int x){			//全挖
		if (this.superDigHandle(y, x) == true){
			for (int j = y-1 ; j <= y+1 ; j++){
				for (int i = x-1 ; i <= x+1 ; i++){
					if (i != x || j != y){		//中間的點不用理
						if (this.map.isInBoard(j, i) == true){
							this.dig(j, i);
						}
					}
				}
			}
		}
	}
	
	private boolean superDigHandle(int y, int x){
		int flagNum = 0;
		boolean isDig = false;
		if (this.map.isVisit(y, x) == true){
			for (int j = y-1 ; j <= y+1 ; j++){
				for (int i = x-1 ; i <= x+1 ; i++){
					if (i != x || j != y){		//中間的點不用理
						if (this.map.isInBoard(j, i) == true && this.map.getState(j, i) == Board.flag){
							flagNum++;
						}
					}
				}
			}
			
			if (this.map.getState(y, x) == flagNum) isDig = true;
		}
		
		return isDig;
	}
	
	public boolean isGameOver(){
		if (this.map.getremainInitialGrid() == this.map.getMineNumber()){
			this.isGameOver = true;
			if (this.lastStepState != Board.mine){
				this.isWin = true;
			}
		}
		return this.isGameOver;
	}
	
	public boolean isWin(){
		return this.isWin;
	}
	
	public void setFlag(int y, int x){
		this.map.setFlag(y, x, false);
	}
	
	public void superFlag(int y, int x){
		for (int j = y-1 ; j <= y+1 ; j++){
			for (int i = x-1 ; i <= x+1 ; i++){
				if (i != x || j != y){		//中間的點不用理
					if (this.map.isInBoard(j, i) == true){
						this.map.setFlag(j, i, true);
					}
				}
			}
		}
	}
	
	private void mapCreate(Referee_Board map, int firstMapY, int firstMapX, String rule){		//依照規則創造地圖
		int mineY, mineX;
		if (this.isLegalMapCreate(map, rule) == true){
			for (int index = 0 ; index < map.getMineNumber() ; index++){
				do {
					// 隨機選擇地雷位置
					mineY = (int)(Math.random()*map.getMapY());
					mineX = (int)(Math.random()*map.getMapX());
					
					if (rule.equals(Referee.rule_1)){
						if (this.map.isInStateInitial(mineY, mineX) == true){
							this.map.setInState(mineY, mineX, Referee_Board.mine);	//設定地雷
							break;	//設定成功，所以跳出do while loop
						}
					}else if (rule.equals(Referee.rule_2)){
						if ((mineY != firstMapY || mineX != firstMapX) 
								&& (this.map.isInStateInitial(mineY, mineX) == true)){
							map.setInState(mineY, mineX, Referee_Board.mine);	//設定地雷
							break;	//設定成功，所以跳出do while loop
						}
					}else if (rule.equals(Referee.rule_3)){
						if (this.isBesireGrid(firstMapY, firstMapX, mineY, mineX) == false 
								&& this.map.isInStateInitial(mineY, mineX) == true){
							map.setInState(mineY, mineX, Referee_Board.mine);	//設定地雷
							break;	//設定成功，所以跳出do while loop
						}
					}else {
						System.out.println("mapCreate出現例外規則");
					}
				}while (true);
			}
			
			//依照旁邊八格的地雷編號
			for (int y = 0 ; y < this.map.getMapY() ; y++){
				for (int x = 0 ; x < this.map.getMapX() ; x++){
					if(this.map.isInStateMine(y, x) == false){
						this.map.setInState(y, x, this.findBesideMine(map, y, x));
					}
				}
			}
		}else {
//			System.out.println("mapCreate出現不合理的地圖大小");
		}
	}

	private boolean isLegalMapCreate(Referee_Board map, String rule) {	//檢查地圖的創造是否合法
		boolean isLegal = false;
		if (rule.equals(Referee.rule_1)){
			if ((map.getMapY() > 0 || map.getMapX() > 0)
					&& (map.getMapX() * map.getMapY() - map.getMineNumber()) >= 0){
				isLegal = true;
			}
		}else if (rule.equals(Referee.rule_2)){
			if ((map.getMapY() > 1 || map.getMapX() > 1) 
					&& ((map.getMapX() * map.getMapY() - map.getMineNumber()) > 0)){
				isLegal = true;
			}
		}else if (rule.equals(Referee.rule_3)){
			if ((map.getMapY() > 3 || map.getMapX() > 3)
				&& ((map.getMapX() * map.getMapY() - map.getMineNumber()) > 1)){
				isLegal = true;
			}
		}

		return isLegal;
	}
	
	private boolean isBesireGrid(int firstMapY, int firstMapX, int mineY, int mineX){		//測試是否在旁邊
		boolean isBesire = false;
		if ((Math.abs(firstMapY - mineY) <= 1 && Math.abs(firstMapX - mineX) <= 1)){ 
			isBesire = true;
		}
		return isBesire;
	}
	
	private short findBesideMine(Referee_Board usedMap, int y, int x){		//找旁邊的地雷總數目
			short mineNumber = 0;
			int i, j;
			for (j = y-1 ; j <= y+1 ; j++){
				for (i = x-1 ; i <= x+1 ; i++){
					if (j != y || i != x){		//中間的點不用理
						if (this.map.isInBoard(j, i) == true && this.map.isInStateMine(j, i) == true){
							mineNumber++;
						}
					}
				}
			}				
			return mineNumber;
		}
	
	Referee_Board getBoard(){
		return this.map;
	}

	public int getremainMineNumber(){
		return this.map.getremainMineNumber();
	}
	
	public int getremainInitialGrid(){
		return this.map.getremainInitialGrid();
	}

	public boolean isVisit(int y, int x){
		return this.map.isVisit(y, x);
	}

	short getInState(int y, int x) {
		return this.map.getInState(y, x);
	}
	
	public short getState(int y, int x){
		return this.map.getState(y, x);
	}
	
	public int getMapX(){
		return this.map.mapX;
	}
	
	public int getMapY(){
		return this.map.mapY;
	}
	
	public int getMineNumber(){
		return this.map.getMineNumber();
	}
	
	public String getRule() {
		return this.rule;
	}
	
	public String showGridInfo(int y, int x){
		return this.map.showGridInfo(y, x);
	}

	
}
