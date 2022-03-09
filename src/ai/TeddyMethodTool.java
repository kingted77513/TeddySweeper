package ai;

import java.math.BigDecimal;

public class TeddyMethodTool {
	public BigDecimal conbineNumber(int m, int n){	//組合數
		// 參考網頁：http://www.start.helishi.net/archives/computing-combinations-with-java-and-python/
	
		if ((float)m/2 < n){
			n = (m - n);
		}
	
		if (m < n || n < 0){
			System.out.println("error_in_conbineNumber，因為參數不正常：" + "m = " + m + ", n = " + n);
			return BigDecimal.valueOf(0);
		}else if (m == n || n == 0){
			return BigDecimal.valueOf(1);
		}else {		// m > n && n > 0
			BigDecimal fenzi = BigDecimal.valueOf(1);
			BigDecimal fenmu = BigDecimal.valueOf(1);
			for (int a = m ; a >= (m-n+1) ; a--){
				fenzi = fenzi.multiply(BigDecimal.valueOf(a));
			}
	
			for (int b = 1 ; b <= n ; b++){
				fenmu = fenmu.multiply(BigDecimal.valueOf(b));
			}
			return fenzi.divide(fenmu);
		}
	}

	public BigDecimal permutation(int m, int n){		//排列數
		BigDecimal per;
		if (m >= n){
			per = BigDecimal.valueOf(1);
			for (int a = m ; a >= (m-n+1) ; a--){
				per = per.multiply(BigDecimal.valueOf(a));
			}
			return per;
		}else {
			System.out.println("error_in_permutation, becouse m < n,  m = " + m + " , n = " + n);
			return BigDecimal.valueOf(0);
		}
	}
}
