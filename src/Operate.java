
public class Operate {
	
	private int Ti ;  //哪一个事务
	private int Di;  //dataIndex 列表中的下标
	private int type;  // 读还是写
	
	
	public Operate(int ti, int di, int type) {
		Ti = ti;
		Di = di;
		this.type = type;
	}
	public int getTi() {
		return Ti;
	}
	public void setTi(int ti) {
		Ti = ti;
	}
	public int getDi() {
		return Di;
	}
	public void setDi(int di) {
		Di = di;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	
	

}
