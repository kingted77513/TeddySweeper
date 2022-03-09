package game;

import ai.TeddyMethod;

public class OliverProgramMergeTest {
	public static void main(String args[]){
		System.out.println("This is OliverProgramMergeTest");
		int mapY = 3, mapX = 4, mineNumber = 4;
		int combineLimit = 2;
		int[][] showMap = {
			{-1, -1, 2, -1}, 
			{-1, -1, -1, 1}, 
			{-1, -1, 2, -1}
		};
		
		// 函數的使用方法
		double[][][] possibleMap = TeddyMethod.getPossibleMapForOliverProgram(mapY, mapX, mineNumber, showMap, combineLimit);
		
		System.out.println("show possible Map in OliverProgramMergeTest");
		for (int recordType = 0 ; recordType < 11 ; recordType ++){
			if (recordType < 10){
				if (recordType < 9){
					System.out.println("show the map of number " + recordType +" possible");
				}else {
					System.out.println("show the map of mine possible");
				}

				double possible;
				for (int y = 0 ; y < mapY ; y++){
					for (int x = 0 ; x < mapX ; x++){
						if (showMap[y][x] == Board.flag && recordType == 9){
							possible = 100;
						}else {
							possible = 100 * possibleMap[recordType][y][x];
						}
						System.out.printf("%.2f", possible);
						System.out.print("%" + "\t");
					}
					System.out.println("");
				}
			}else {
				System.out.println("show the map of digValue");
				for (int y = 0 ; y < mapY ; y++){
					for (int x = 0 ; x < mapX ; x++){
						System.out.printf("%.2f", possibleMap[recordType][y][x]);
						System.out.print("\t");
					}
					System.out.println("");
				}
			}
		}
		System.out.println("是否大於組合限制：" + TeddyMethod.isCombineAboveLimit());
	}
}
