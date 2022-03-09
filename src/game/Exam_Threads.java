package game;

class Exam_Threads{
	private static final int testTimes = 100;
	private static int runTimes = 0;
	private static int winTimes = 0;
//	Exam_Threads (int mineNumber, int mapX, int mapY, String rule){
//		super(mineNumber, mapX, mapY, rule);
//		this.className = "Exam_Threads";
//		this.runTimes = 0;
//		this.winTimes = 0;
//	}
	
	public static void main(String[] args){
			
		class MyRunnable implements Runnable{
			@Override
			public synchronized void run() {
//				for(int i = 1; i<=100;i++) {
//					System.out.println(Thread.currentThread().getName() +"("+i+")");
//				}
//				System.out.println("count = " + Thread.activeCount());
//				System.out.println("#" + Thread.currentThread().getName());
				Exam exam = new Exam(4, 4, 4, Referee.rule_2);
				exam.setExam();
				exam.setUseTeddyMethod();
				exam.run();
				Exam_Threads.runTimes++;
				if (exam.isWin() == true) Exam_Threads.winTimes++;
//				System.out.println("this is " + Thread.currentThread().getName());
//				System.out.println(Thread.currentThread().getName() + " runTimes = " + Exam_Threads.runTimes);
//				System.out.println("winTimes = " + Exam_Threads.winTimes);
			}
		}
	
//		MyRunnable r = new MyRunnable();
//		MyRunnable r2 = new MyRunnable();
//		MyRunnable r3 = new MyRunnable();
//		MyRunnable r4 = new MyRunnable();
		
		int no=Runtime.getRuntime().availableProcessors();
		TimeCounter timeCounter = new TimeCounter();	//計時器
		timeCounter.initial();
		timeCounter.start();
		
		while (runTimes < testTimes){
			if (Thread.activeCount() <= no){
				//System.out.println("count " + Thread.activeCount());
				Thread t = new Thread(new MyRunnable(), String.valueOf(runTimes));
				t.start();
//				Thread t1 = new Thread(r1, "t1");
//				t1.start();
//				Thread t2 = new Thread(r2, "t2");
//				t2.start();
			}
		}
		
//		boolean lock = true;
//		while (runTimes < testTimes){
//			if (lock){
//				for (int index = 0 ; index < testTimes ; index++){
////					System.out.println("count " + Thread.activeCount());
//					MyRunnable r = new MyRunnable();
//					Thread t = new Thread(r);
//					t.start();
//				}
//				 
//				lock = false;
//				
//			}
//		
//			System.out.println();
//		}
		
		timeCounter.end();
		timeCounter.setCountTotalTime();
		//System.out.println("runTimes = " + Exam_Threads.runTimes);
		//System.out.println("winTimes = " + Exam_Threads.winTimes);
		//System.out.println("runTime = " + timeCounter.getCountTotalTime() + " s");
		
		
		
//		System.out.println("no = " + no);
//		
//		Thread t1 = new Thread(r, "r1");
//		t1.start();
//		Thread t2 = new Thread(r, "r2");
//		t2.start();
//		Thread t3 = new Thread(r, "r3");
//		t3.start();
//		Thread t4 = new Thread(r, "r4");
//		t4.start();
//		if (Exam_Threads.runTimes == Exam_Threads.testTimes){
//			System.out.println("runTimes = " + Exam_Threads.runTimes);
//			System.out.println("winTimes = " + Exam_Threads.winTimes);
//		}
	}
	
	
	
	
	
	

}
