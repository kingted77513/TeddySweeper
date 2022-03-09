package game;

public final class TimeCounter {
	private long start;
	private long end;
	private boolean isPause;
	private long totalTime;
	private long totalTimeIncludeError;
	
	public TimeCounter(){
		this.totalTime = 0;
		this.totalTimeIncludeError = 0;
		this.isPause = false;
	}
	
	public void initial(){
		this.totalTime = 0;
		this.totalTimeIncludeError = 0;
		this.isPause = false;
	}
	
	public void start(){
		this.start = System.currentTimeMillis();		//取得系統時間
	}
	
	public void end(){
		this.end = System.currentTimeMillis();
	}
	
	public void pause(){
		if (this.isPause == false){
			this.totalTime += (this.end - this.start);
		}else {
			
		}
		this.isPause = !this.isPause;
	}
	
	public double getSingleCountTime(){
		return (double)(this.end - this.start)/1000;
	}
	
	public void setCountTotalTime(){
		this.totalTime += (this.end - this.start);
		this.totalTimeIncludeError += (this.end - this.start);
	}
	
	public void setCountTotalTime(String error){
		if (error.equals("error")){
			this.totalTimeIncludeError += (this.end - this.start);
		}
	}
	
	public double getCountTotalTime(){
		return ((double)this.totalTime/1000);
	}
	
	public double getCountTotalTimeIncludeError(){
		return ((double)this.totalTimeIncludeError/1000);
	}

}
