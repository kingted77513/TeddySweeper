package game;

public final class Referee_Board extends Board implements Cloneable{
	private int remainMineNumber;	//剩下的地雷數
	private int remainInitialGrid;	//剩下的空格數
	Referee_Board(int mapY, int mapX, int mineNumber){
		super(mapY, mapX, mineNumber);
		this.remainMineNumber = mineNumber;
		this.remainInitialGrid = mapY * mapX;
		//宣告地圖
		this.board = new Referee_Grid[this.mapSize];
		for (int y = 0 ; y < mapY ; y++){
			for (int x = 0 ; x < mapX ; x++){
				this.board[this.getRealSituation(y, x)] = new Referee_Grid(y, x); 
			}
		}
	}
	
	public Referee_Board(int mapY, int mapX, int mineNumber, boolean[] problemMap){		// 為了儲存問題而設計的
		super(mapY, mapX, mineNumber);
		this.remainMineNumber = mineNumber;
		this.remainInitialGrid = mapY * mapX;
		//宣告地圖
		this.board = new Referee_Grid[this.mapSize];
		
		// 設定地雷
		for (int y = 0 ; y < mapY ; y++){
			for (int x = 0 ; x < mapX ; x++){
				if (problemMap[this.getRealSituation(y, x)] == true){
					this.board[this.getRealSituation(y, x)] = new Referee_Grid(y, x, Board.mine);
				}else {
					this.board[this.getRealSituation(y, x)] = new Referee_Grid(y, x);
				}
			}
		}
		
		short showNumber;
		
		// 設定數字
		for (int y = 0 ; y < mapY ; y++){
			for (int x = 0 ; x < mapX ; x++){
				if (problemMap[(y * mapX) + x] == false){
					showNumber = 0;
					for (int j = y-1 ; j <= y+1 ; j++){
						for (int i = x-1 ; i <= x+1 ; i++){
							if (j != y || i != x){		//中間的點不用理
								if (j >=0 && j < mapY && i >= 0 && i < mapX && problemMap[(j * mapX) + i] == true){
									showNumber++;
								} 
							}
						}
					}
					this.getGrid(y, x).setInState(showNumber);
				}
			}
		}
	}
	
	void setRemainNumber(int remainL, int remainM){
		this.remainInitialGrid = remainL;
		this.remainMineNumber = remainM;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException{
		Referee_Board cloneMap = null;
		try{
			cloneMap = (Referee_Board)super.clone();
			cloneMap.board = (Referee_Grid[])board.clone();
		}catch (CloneNotSupportedException cse) {
			cse.printStackTrace();
		}
		return cloneMap;
	}
	
	protected Referee_Grid getGrid(int y, int x){
		return (Referee_Grid)this.board[this.getRealSituation(y, x)];
	}
	
	void finalize_By_Teddy(){
		for (int index = 0 ; index < this.mapSize ; index++){
			this.board[index] = null;
		}
	}
	
	boolean isVisit(int y, int x){
		return this.getGrid(y, x).isVisit();
	}
	
	int getremainMineNumber(){
		return this.remainMineNumber;
	}
	
	int getremainInitialGrid(){
		return this.remainInitialGrid;
	}
	
	boolean isInStateInitial(int y, int x){		//得到某格是否有被設定過
		return this.getGrid(y, x).isInStateInitial();
	}
	
	boolean isInStateMine(int y, int x){
		return this.getGrid(y, x).isInStateMine();
	}
	
	void setInState(int y, int x, short state){
		this.getGrid(y, x).setInState(state);
	}

	short getInState(int y, int x){
		return this.getGrid(y, x).getInState();
	}

	void setFlag(int y, int x, boolean isSuperFlag){		//設定旗子
		if (this.getGrid(y, x).isVisit() == false){
			if (this.getGrid(y, x).getState() == Board.flag){
				if (isSuperFlag == false){		//如果不是superFlag，則將旗子除去
					this.getGrid(y, x).setState(Board.initial);
					this.remainMineNumber++;
				}
			}else {
				this.getGrid(y, x).setState(Board.flag);
				this.remainMineNumber--;
			}
		}
	}
	
	@Override
	protected short getState(int y, int x) {
		return this.getGrid(y, x).getState();
	}	

	void setState(int y, int x, short state){
		this.getGrid(y, x).setState(state);
	}
	
	void dig (int y, int x){
		if (this.getGrid(y, x).isVisit() == false){
			this.getGrid(y, x).dig();
			this.remainInitialGrid--;
			if (this.getGrid(y, x).getState() == Board.noMine){
				for (int j = y-1 ; j <= y+1 ; j++){
					for (int i = x-1 ; i <= x+1 ; i++){
						if (i != x || j != y){		//中間的點不用理
							if (this.isInBoard(j, i) == true){
								this.dig(j, i);
							}
						}
					}
				}
			}
		}
	}
	
	String showGridInfo(int y, int x){
		return this.getGrid(y, x).shwoInfo();
	}

	class Referee_Grid extends Grid{
		private boolean isVisit;		//是否已經被挖過
		private short inState;
		
		Referee_Grid(int y, int x){
			super(y, x);
			this.inState = Referee_Board.initial;
			this.isVisit = false;
		}
		
		Referee_Grid(int y, int x, short state){
			this(y, x);
			this.setInState(state);
		}
		
		void setInState(short state){
			if (this.inState == Referee_Board.initial) this.inState = state;
		}
		
		short getInState(){
			return this.inState;
		}
		
		boolean isInStateInitial(){		//回傳是否有被設定過
			return (this.inState == Referee_Board.initial) ? true : false;
		}
		
		boolean isInStateMine(){
			return (this.inState == Referee_Board.mine) ? true : false;
		}
		
		@Override
		protected void setState(short state){
			if (this.isVisit == false){
				if (state == this.inState){
					super.setState(this.inState);
					this.isVisit = true;
				}else {
					super.setState(state);
				}
			}
		}
		
		boolean isVisit(){		//檢查是否有被拜訪過
			return this.isVisit;
		}

		void dig() {					//挖此格
			if (this.isVisit == false){
				this.setState(this.inState);
				this.isVisit = true;
			}
		}
		
		String shwoInfo(){
			String info = "";
			System.out.println("coordinateX = " + this.getX());
			info += "\n" + "coordinateX = " + this.getX();
			System.out.println("coordinateY = " + this.getY());
			info += "\n" + "coordinateY = " + this.getY();
			System.out.println("inState = " + this.inState);
			info += "\n" + "inState = " + this.inState;
			System.out.println("outState = " + this.getState());
			info += "\n" + "outState = " + this.getState();
			System.out.println("isVisit = " + this.isVisit);
			info += "\n" + "isVisit = " + this.isVisit;
			return info;
		}
	}
}
