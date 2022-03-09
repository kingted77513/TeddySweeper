package allSolve;
import java.math.*;

public class MapLinkedList {
	private BigDecimal size;	// 節點數量
	private Node start;			// 起點
	private Node end;			// 終點
	private Node point;		// 暫存指標
	
	public MapLinkedList(){
		this.size = BigDecimal.valueOf(0);
		this.start = null;
		this.end = null;
		
	}
	
	public void addLast(int[] data){
		Node n = new Node(data);
		if (this.size.compareTo(BigDecimal.valueOf(0)) == 0){
			this.start = n;
			this.end = n;
			this.point = this.start;
		}else {
			this.end.setNext(n);
			this.end = n;
		}
		this.size = this.size.add(BigDecimal.valueOf(1));
	}
	
	boolean isOver(){
		if (this.point == null){
			this.point = this.start;
			return true;
		}else {
			return false;
		}
	}
	
	int[] getNextData(){
		if (this.point == null){
			// 已經沒有資料了
			this.point = this.start;
			return null;
		}else {
			int[] data = this.point.getIntegerArrayData();
			this.point = this.point.getNext();
			return data;
		}
	}
	
	public String size(){
		return this.size.toString();
	}
	
	private class Node{
		private Object data;
		private Node next;
		private Node(int[] data){
			this.data = data;
			this.next = null;
		}
		
		private void setNext(Node next){
			this.next = next;
		}
		
		private Node getNext(){
			return this.next;
		}
		
//		Object getData(){
//			return this.data;
//		}
		
		int[] getIntegerArrayData(){
			return (int[])this.data;
		}
	}

	public int[] getFirst() {
		return this.start.getIntegerArrayData();
	}
	
}
