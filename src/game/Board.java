package game;

public abstract class Board {
	static public final short noMine = 0;			//旁邊都無地雷
	static public final short initial = -1;			//初始值
	static public final short flag = -3;			//插旗子
	static public final short mine = -2;			//地雷
	
	protected final int mapX;	//長
	protected final int mapY;	//寬
	protected final int mineNumber;	//地雷
	protected final int mapSize;
	
	protected Grid[] board;
	protected Board (int mapY, int mapX, int mineNumber){
		this.mapX = mapX;
		this.mapY = mapY;
		this.mineNumber = mineNumber;
		this.mapSize = this.mapX * this.mapY;
		
//		this.board = new Grid[this.mapSize];
//		
//		for (int y = 0 ; y < mapY ; y++){
//			for (int x = 0 ; x < mapX ; x++){
//				this.board[this.getRealSituation(y, x)] = new Grid(y, x);
//			}
//		}
	}
	
	public static int getRealSituation(int mapX, int y, int x){
		return ((y * mapX) + x);
	}
	
	public int getRealSituation(int y, int x){
		return ((y * this.mapX) + x);
	}
	
	public boolean isInBoard(int y, int x){
		return (y >=0 && y < this.mapY && x >= 0 && x < this.mapX);
	}
	
	public int getMapX(){
		return this.mapX;
	}
	
	public int getMapY(){
		return this.mapY;
	}
	
	public int getMineNumber(){
		return this.mineNumber;
	}
	
	abstract protected short getState(int y, int x);	//得到某格的狀態
	
	abstract protected Grid getGrid(int y, int x);
	
	protected class Grid {
		//記錄座標所用
		final private int coordinateX;
		final private int coordinateY;
		//記錄狀態
		private short state;
		
		protected Grid (int y, int x){
			this.coordinateX = x;
			this.coordinateY = y;
			this.state = Board.initial;
		}
		
		public int getX(){
			return this.coordinateX;
		}
		
		public int getY(){
			return this.coordinateY;
		}
		
		protected void setState(short state){
			this.state = state;
		}
		
		public short getState(){
			return this.state;
		}
		
//		public boolean equals(int y, int x){
//			return (this.coordinateX == x && this.coordinateY == y);
//		}
//		
//		public boolean equals(Grid g){
//			return (this.coordinateX == g.getX() && this.coordinateY == g.getY());
//		}
	}
}
