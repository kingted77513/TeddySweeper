/*
 * 基本的使用者功能
 */
package game;
import java.math.BigDecimal;
import ai.*;
public class Player {
	final private int mapX, mapY, mineNumber;
//	final private String rule;
	private Referee referee;
	private String order;
	private Player_Board playerMap;
	private boolean isUseSinglePoint, isUseTM, isUseCSP;	// , isUseFOPJ
	private AI sp, csp, tm;
	
	public Player(int mapX, int mapY, int mineNumber, String rule, Referee referee){
		this.mapX = mapX;
		this.mapY = mapY;
		this.mineNumber = mineNumber;
//		this.rule = rule;
		this.playerMap = new Player_Board(this.mapY, this.mapX, this.mineNumber);
		this.referee = referee;
		
		// AI設定
		this.isUseSinglePoint = false;
		this.isUseTM = false;
		this.isUseCSP = false;
//		this.isUseFOPJ = false;
	}
	
	public boolean isGuess(){
		return ((TeddyMethod)this.tm).isGuess();
	}
	
	public String answer(){
		this.order = "no_answer";
		try {
			this.reReadMap();		//重讀地圖
			if (this.isUseSinglePoint == true){
				this.order = ((SinglePoint)this.sp).answer();
			}
			
			if (this.isUseCSP == true && this.order.equals("no_answer") == true){
				this.order = ((CSP)this.csp).answer();
			}
			
			if (this.isUseTM == true  && this.order.equals("no_answer") == true){
				this.order = ((TeddyMethod)this.tm).answer();
			}
			
	//		if (this.isUseFOPJ == true && this.order.equals("no_answer") == true){
	//			this.order = this.fopj.answer();
	//		}
		}catch (Exception e){
			System.out.println("error_in_answer，原因不明");
			UI.safeMap_In_debug("error_in_answer，原因不明");
			e.printStackTrace();
		}

		return this.order;
	}
	
	public double[][] getHintMap(){
		double[][] hint;
		
		if (this.isUseSinglePoint == true && ((SinglePoint)this.sp).isAnswer() == true){
			hint = ((SinglePoint)this.sp).getHintMap();
		}else if (this.isUseCSP == true && ((((CSP)this.csp).isAnswer() == true) || ((CSP)this.csp).isGuess() == true)){
			hint = ((CSP)this.csp).getHintMap();
		}else if (this.isUseTM == true  && (((TeddyMethod)this.tm).isAnswer() == true || ((TeddyMethod)this.tm).isUseGuess() == true)){
			hint = ((TeddyMethod)this.tm).getHintMap();
		}else {
			hint = new double[this.mapY][this.mapX];
			for (int y = 0 ; y < this.mapY ; y++){
				for (int x = 0 ; x < this.mapX ; x++){
					hint[y][x] = Board.initial;
				}
			}
		}
		
		return hint;
	}
	
	public double[][] getDigValueMap(){
		double[][] digValue;
		
		if (this.isUseTM == true  && (((TeddyMethod)this.tm).isAnswer() == false || ((TeddyMethod)this.tm).isUseGuess() == true)){
			digValue = ((TeddyMethod)this.tm).getDigValueMap();
		}else {
			digValue = new double[this.mapY][this.mapX];
		}
		
		return digValue;
	}
	
	private void reReadMap(){		//重新讀取地圖資訊
		for (int y = 0 ; y < this.mapY ; y++){
			for (int x = 0 ; x < this.mapX ; x++){
				if (this.playerMap.isVisit(y, x) == false && (this.playerMap.getState(y, x) != this.referee.getState(y, x))){
					this.playerMap.getGrid(y, x).setState(this.referee.getState(y, x));
				}
			}
		}
		this.playerMap.setRemainMineNumber(this.referee.getremainMineNumber());		//重設地雷餘數
		this.playerMap.setRemainInitialGrid(this.referee.getremainInitialGrid());	//重設空格餘數
	}

	public void setAI (String AIName, boolean isSet){	//設定所使用的AI
		if (AIName.equals(UI.usedAI.SinglePoint.toString()) == true){		// 設定使用Single Point
			this.isUseSinglePoint = isSet;
			if (isSet == true){
				if (this.sp == null) this.sp = new SinglePoint(this.playerMap);
			}else {
				this.sp = null;
			}
		}else if (AIName.equals(UI.usedAI.CSP.toString()) == true){
			this.isUseCSP = isSet;
			if (isSet == true){
				if (this.csp == null) this.csp = new CSP(this.playerMap);
			}else {
				this.csp = null;
			}
		}else if (AIName.equals(UI.usedAI.CSP_Guess.toString()) == true){
			if (this.csp != null) ((CSP)this.csp).setGuess(isSet);
		}else if (AIName.equals(UI.usedAI.TeddyMethod.toString()) == true){			// 設定使用CSP
			this.isUseTM = isSet;
			if (isSet == true){
				if (this.tm == null) this.tm = new TeddyMethod(this.playerMap);
			}else {
				this.tm = null;
			}
		}else if (AIName.equals(UI.usedAI.TeddyMethod_Guess.toString()) == true){
			if (this.tm != null) ((TeddyMethod)this.tm).setGuess(isSet);
		}
//		else if (AIName.equals("FOPJ") == true){
//			this.isUseFOPJ = isSet;
//			if (isSet == true){
//				if (this.fopj == null) this.fopj = new FOPJ(this.playerMap);
//			}else {
//				this.fopj = null;
//			}
//		}else if (AIName.equals("FOPJ_Guess") == true){		//設定是否能猜
//			if (this.fopj != null) this.fopj.setGuess(isSet);
//		}
	}
	
	public void setAI (String AIName, boolean isSet, double value_SP, double value_EQ, double value_CM, double value_Other, double value_NoAns){	//設定所使用的AI
		if (AIName.equals(UI.usedAI.TeddyMethod.toString()) == true){			// 設定使用CSP
			this.isUseTM = isSet;
			if (isSet == true){
				if (this.tm == null) this.tm = new TeddyMethod(this.playerMap, value_SP, value_EQ, value_CM, value_Other, value_NoAns);
			}else {
				this.tm = null;
			}
		}else if (AIName.equals(UI.usedAI.TeddyMethod_Guess.toString()) == true){
			if (this.tm != null) ((TeddyMethod)this.tm).setGuess(isSet);
		}
	}
	
	public void finalize_By_Teddy(){
		if (this.playerMap != null){
			this.playerMap.finalize_By_Teddy();
			this.playerMap = null;
		}
		
		if (this.tm != null) {
			((TeddyMethod)this.tm).finalize_By_Teddy();
			this.tm = null;
		}
	}

	public String getStrategy(String strategy) {
		if (this.isUseSinglePoint == true){
			strategy = ((SinglePoint)this.sp).getStrategy(strategy);
		}
		
		if (this.isUseCSP == true){
			strategy = ((CSP)this.csp).getStrategy(strategy);
		}
		
		if (this.isUseTM == true){
			strategy = ((TeddyMethod)this.tm).getStrategy(strategy);
		}
		
//		if (this.isUseFOPJ == true){
//			strategy = this.fopj.getStrategy(strategy);
//		}
		return strategy;
	}

	public BigDecimal getUseCombineTimes() {
		return TeddyMethod.total_useCombineTimes;
	}
	
	public BigDecimal getAllCombineTimes() {
		return TeddyMethod.total_combineTimes;
	}
}
