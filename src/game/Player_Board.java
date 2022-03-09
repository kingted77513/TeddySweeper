package game;

public class Player_Board extends Board{
	private int remainMineNumber;	//剩下的地雷數
	private int remainInitialGrid;	//剩下的空格數
	
	protected Player_Board(int mapY, int mapX, int mineNumber){
		super (mapY, mapX, mineNumber);
		this.board = new Player_Grid[this.mapSize];
		
		for (int x = 0 ; x < this.mapX ; x++){
			for (int y = 0 ; y < this.mapY ; y++){
				this.board[this.getRealSituation(y, x)] = new Player_Grid(y, x);
			}
		}
	}
	
	public Player_Board(Player_Board sMap, Player_Grid grid, short value){	//模擬專用
		super(sMap.getMapY(), sMap.getMapX(), sMap.getMineNumber());
		
		this.board = new Player_Grid[this.mapSize];
		
		for (int y = 0 ; y < this.mapY ; y++){
			for (int x = 0 ; x < this.mapX ; x++){
				try {
					this.board[this.getRealSituation(y, x)] = (Player_Grid)sMap.getGrid(y, x).clone();
				}catch (CloneNotSupportedException cns){
					cns.printStackTrace();
				}
			}
		}
		
		this.remainInitialGrid = sMap.remainInitialGrid - 1;
		this.remainMineNumber = sMap.remainMineNumber;
		
		this.getGrid(grid.getY(), grid.getX()).setState(value);
	}
	
	protected Player_Board(Player_Board iMap){		//AI的inner class專用
		super (iMap.getMapY(), iMap.getMapX(), iMap.getMineNumber());
	}
	
	// 結合Oliver的程式專用的程式-----------------------------------
	public Player_Board (int mapY, int mapX, int mineNumber, int[][] showMap){
		super (mapY, mapX, mineNumber);
		this.remainInitialGrid = mapY * mapX;
		this.remainMineNumber = mineNumber;
		this.board = new Player_Grid[this.mapSize];
		for (int y = 0 ; y < this.mapY ; y++){
			for (int x = 0 ; x < this.mapX ; x++){
				if (showMap[y][x] == Board.initial){
					this.board[this.getRealSituation(y, x)] = new Player_Grid(y, x);
				}else {
					this.board[this.getRealSituation(y, x)] = new Player_Grid(y, x, (short) showMap[y][x]);
					if (showMap[y][x] == Board.flag){
						this.remainMineNumber--;
					}else {
						this.remainInitialGrid--;
					}
				}
			}
		}
	}
	// -----------------------------------結合Oliver的程式專用的程式
	
	protected void finalize_By_Teddy(){
		for (int index = 0 ; index < this.mapSize ; index++){
			this.board[index] = null;
		}
	}
	
	@Override
	public short getState(int y, int x) {		//得到狀態
		return this.getGrid(y, x).getState();
	}
	
//	public void setState(int y, int x, short state){		//設定狀態
//		this.getGrid(y, x).setState(state);
//	}
	
	public boolean isVisit(int y, int x){		//某格是否有踩過
		return this.getGrid(y, x).isVisit();
	}
	
	public boolean isVisit(Player_Grid grid){
		return this.isVisit(grid.getY(), grid.getX());
	}
	
	public boolean isFlag(int y, int x){		//某格是否已插旗
		return (this.getState(y, x) == Board.flag);
	}
	
	public boolean isFlag(Player_Grid grid){
		return this.isFlag(grid.getY(), grid.getX()); 
	}
	
	public boolean isInitial(int y, int x){
		return (this.getState(y, x) == Board.initial) ? true : false;
	}
	
	public boolean isInitial(Player_Grid grid){
		return this.isInitial(grid.getY(), grid.getX());
	}
	
	void setRemainMineNumber(int remainMineNumber){
		this.remainMineNumber = remainMineNumber; 
	}
	
	void setRemainInitialGrid (int remainInitialGrid){
		this.remainInitialGrid = remainInitialGrid;
	}
	
	public int getRemainMineNumber(){
		return this.remainMineNumber;
	}
	
	public int getRemainInitialGrid(){		//回傳所有未設定空格，不包括插旗子。
		return this.remainInitialGrid - (this.mineNumber - this.remainMineNumber);
	}
	
	public Player_Grid getGrid(int y, int x){
		return (Player_Grid)this.board[this.getRealSituation(y, x)];
//		try {
//			
//		}catch (ArrayIndexOutOfBoundsException aio){
//			System.out.println("x = " + x);
//			System.out.println("y = " + y);
//			System.out.println("real = " + this.getRealSituation(y, x));
//			return null;
//		}
	}

	public boolean isUnknownGrid(int y, int x){		//自已是未開點且旁邊至少有一個已開點
		boolean isUnknown = false;
		if (this.isInitial(y, x) == true){
			for (int j = y-1 ; j <= y+1 ; j++){
				for (int i = x-1 ; i <= x+1 ; i++){
					if (i != x || j != y){		//中間的點不用理
						if (this.isInBoard(j, i) == true && this.isVisit(j, i) == true){
							isUnknown = true;
							break;
						}
					}
				}
				if (isUnknown == true) break;
			}
		}else {
			isUnknown = false;
		}
		return isUnknown;
	}
	
	public boolean isInformationGrid(int y, int x) {	//自己是已開點且旁邊至少有一個未開點
		boolean isInformation = false;
		if (this.isVisit(y, x) == true){
			for (int j = y-1 ; j <= y+1 ; j++){
				for (int i = x-1 ; i <= x+1 ; i++){
					if (i != x || j != y){		//中間的點不用理
						if (this.isInBoard(j, i) == true && this.isInitial(j, i) == true){
							isInformation = true;
							break;
						}
					}
				}
				if (isInformation == true) break;
			}
		}else {
			isInformation = false;
		}
		
		return isInformation;
	}
	
	public boolean isStrangeGrid(int y, int x){		//本身是未開點，且旁邊沒有一個是已開點
		boolean isStrange = true;
		if (this.isInitial(y, x) == true){
			for (int j = y-1 ; j <= y+1 ; j++){
				for (int i = x-1 ; i <= x+1 ; i++){
					if (i != x || j != y){		//中間的點不用理
						if (this.isInBoard(j, i) == true && this.isVisit(j, i) == true){
							isStrange = false;
							break;
						}
					}
				}
				if (isStrange == false) break;
			}
		}else {
			isStrange = false;
		}
		
		return isStrange;
	}
	
	public boolean isCompleteStrangeGrid(int y, int x){		//本身是未開點，且旁邊二層沒有一個是已開點
		boolean isStrange = true;
		if (this.isInitial(y, x) == true){
			for (int j = y-2 ; j <= y+2 ; j++){
				for (int i = x-2 ; i <= x+2 ; i++){
					if (i != x || j != y){		//中間的點不用理
						if (this.isInBoard(j, i) == true && this.isVisit(j, i) == true){
							isStrange = false;
							break;
						}
					}
				}
				if (isStrange == false) break;
			}
		}else {
			isStrange = false;
		}
		
		return isStrange;
	}
	
	public boolean isAllKnowGrid(int y, int x){		//自已已經打開，且旁邊沒有未打開的格子
		boolean isAllKnow = true;
		if (this.isVisit(y, x) == true){
			for (int j = y-1 ; j <= y+1 ; j++){
				for (int i = x-1 ; i <= x+1 ; i++){
					if (i != x || j != y){		//中間的點不用理
						if (this.isInBoard(j, i) == true && this.isInitial(j, i) == true){
							isAllKnow = false;
							break;
						}
					}
				}
				if (isAllKnow == false) break;
			}
		}else {
			isAllKnow = false;
		}
		
		return isAllKnow;
	}
	
	public int getBesireInitialNumber(int y, int x) {		//得到周圍的初始空格數
		int initailNumber = 0;
		for (int j = y-1 ; j <= y+1 ; j++){
			for (int i = x-1 ; i <= x+1 ; i++){
				if (i != x || j != y){		//中間的點不用理
					if (this.isInBoard(j, i) == true && this.isInitial(j, i) == true){
						initailNumber++;
					}
				}
			}
		}
		return initailNumber;
	}
	
	public int getBesireInitialNumber(Player_Grid pgrid) {
		return this.getBesireInitialNumber(pgrid.getY(), pgrid.getX());
	}

	public int getBesireFlagNumber(int y, int x) {			//得到周圍的旗子總數
		int flagNumber = 0;
		for (int j = y-1 ; j <= y+1 ; j++){
			for (int i = x-1 ; i <= x+1 ; i++){
				if (i != x || j != y){		//中間的點不用理
					if (this.isInBoard(j, i) == true && this.isFlag(j, i) == true){
						flagNumber++;
					}
				}
			}
		}
		return flagNumber;
	}
	
	public class Player_Grid  extends Grid implements Cloneable{
		private boolean isVisit;
		
		protected Player_Grid (int y, int x){
			super(y, x);
			this.isVisit = false;
		}
		
		protected Player_Grid (Player_Grid pgrid){	//AI的inner class專用
			this(pgrid.getY(), pgrid.getX());
			this.setState(pgrid.getState());
		}
		
		protected Player_Grid (int coordinateY, int coordinateX, short state){
			this(coordinateY, coordinateX);
			this.setState(state);
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException{
			Player_Grid grid = null;
			try {
				grid = (Player_Grid)super.clone();
			}catch (CloneNotSupportedException cse) {
				cse.printStackTrace();
			}
			return grid;
		}
		
		public String getCoordinate(){
			return this.getX() + " " + this.getY();
		}
		
		@Override
		public void setState(short state){
			if (this.isVisit == false){
				super.setState(state);
				this.isVisit = (state == Board.flag || state == Board.initial) ? false : true; 
			}
		}
		
		boolean isVisit(){
			return this.isVisit;
		}
	
//		private boolean equals(Player_Grid grid){	//比較是否相同
//			return (this.coordinateX == grid.getCoordinateX() && this.coordinateY == grid.getCoordinateY()) ? true : false;
//		}
	}

	public void showMap() {
		for (int y = this.getMapY() - 1 ; y >= 0 ; y--){
			for (int x = 0 ; x < this.getMapX() ; x++){
				if (this.getState(y, x) == Board.initial){
					System.out.print("#" + "\t");
				}else if (this.getState(y, x) == Board.flag){
					System.out.print("P" + "\t");
				}else if (this.getState(y, x) == Board.mine){
					System.out.print("@" + "\t");
				}else {
					System.out.print(this.getState(y, x) +"\t");
				}
			}
			System.out.println();
		}
		System.out.println();
	}
}
