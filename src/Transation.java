import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Transation {
	public static final int READ = 0;
	public static final int WRITE = 1;
	public static final int LOCK = 1;
	public static final int UNLOCK=0;
	public static final int writeFinish = 1;
	
	public Boolean lockFinishFlag = false;  //标记第一阶段加锁是否结束，已结束才可进行第二阶段的解锁
	public static Boolean unlockFinishFlag = false; //标记第二阶段解锁是否结束，已结束说明此事务执行完毕，可以commit
	
	private List<Integer> dataIndex;   //操作数据的下标索引
	private List<Integer> dataToWrite;	//随机产生的将要执行加法操作的值
	private List<Integer> readData;		//保存从数据库中读取的值
	private List<Integer> TlockList;	//锁表，该事务对需要操作的数据元素加锁情况
	private Map<Integer, Integer> writeFinishList; //该事务对数据元素的写操作是否完成，完成之后才可进行对该元素的解锁
	Random random = new Random();
	public Transation(){
		this.dataIndex = new ArrayList<Integer>();
		this.dataToWrite = new ArrayList<Integer>();
		this.readData = new ArrayList<Integer>();
		this.TlockList = new ArrayList<Integer>();
		this.setWriteFinishList(new HashMap<Integer, Integer>());
	}
	public void read2PL(int db [], int i){
		this.getReadData().set(i, db[this.getDataIndex().get(i)]);
	}
	public void read(int db [], int i){
		this.getReadData().add(db[this.getDataIndex().get(i)]);
	}
	public int write(int db [], int index){
		db[this.getDataIndex().get(index)] = this.getDataToWrite().get(index)+this.getReadData().get(index);
		return this.getDataToWrite().get(index);
	}
	public void addToDataToWriteList(int a){
		this.getDataToWrite().add(a);
	}
	public void addToDataIndexList(int a){
		this.getDataIndex().add(a);
	}
	
	
	public void executeTransation(int db [], int i){
		System.out.print("T"+(i+1)+": ");
//		System.out.print("*******");
//		for(int a : this.getReadData()){
//			System.out.print(a+" ");
//		}
		for(int index=0;index<this.getDataIndex().size();index++){
			this.read(db,index);
			System.out.print("r(x"+(this.getDataIndex().get(index)+1)+")");
			System.out.print("w(x"+(this.getDataIndex().get(index)+1)+","+"x"+(this.getDataIndex().get(index)+1)+"+"+write(db,index)+") ");
		}
		System.out.println("     --->DB[]="+Arrays.toString(db));
		this.getReadData().clear();
//		for(int n=0;n<this.getReadData().size();n++){
//			this.getReadData().set(n, 0);
//		}
	}
	
	public List<Integer> getDataIndex() {
		return dataIndex;
	}
	public void setDataIndex(List<Integer> dataIndex) {
		this.dataIndex = dataIndex;
	}


	public List<Integer> getDataToWrite() {
		return dataToWrite;
	}

	public void setDataToWrite(List<Integer> dataToWrite) {
		this.dataToWrite = dataToWrite;
	}

	public List<Integer> getReadData() {
		return readData;
	}

	public void setReadData(List<Integer> readData) {
		this.readData = readData;
	}

	public List<Integer> getTlockList() {
		return TlockList;
	}

	public void setTlockList(List<Integer> tlockList) {
		TlockList = tlockList;
	}



	public Map<Integer, Integer> getWriteFinishList() {
		return writeFinishList;
	}



	public void setWriteFinishList(Map<Integer, Integer> writeFinishList) {
		this.writeFinishList = writeFinishList;
	}
}
