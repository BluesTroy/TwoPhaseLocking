import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class TwoPhaseLocking {

	private int DB [] = {1,2,3,4,5,6};  //初始数据库状态
	private Transation T[] = new Transation[3]; //三个事务
	private Random random = new Random();
	private List<Operate> operateList = new ArrayList<Operate>();  //保存生成的随机操作序列，有并发控制和无并发控制使用同一操作序列便于比较
	
	private List<Integer> DBLock = new ArrayList<Integer>();  //记录数据库中的元素是否已加锁
	
	//初始化每个数据库元素都没有加锁
	public void initDBLock(){
		for(int i = 0 ;i<DB.length;i++){
			DBLock.add(Transation.UNLOCK);  
		}
	}
	
	public void initTransation(){
		for(int i = 0;i<3;i++){
			T[i] = new Transation();
		}
	}
	
	public void generateThreeTransation(){
		initTransation();
		for(int i=0;i<T.length;i++){
			int n = Math.abs(random.nextInt())%3+2;   //约定每个事务至少操作数据库中三个以上的数据库元素
			for(int k = 0;k<n;k++){
				int value = Math.abs(random.nextInt())%DB.length;
				while(T[i].getDataIndex().contains(value)){		//不能出现重复冗余的动作
					value = Math.abs(random.nextInt())%DB.length;
				}
				T[i].addToDataIndexList(value);
				T[i].addToDataToWriteList(Math.abs(random.nextInt())%20);  ////待写入的20以内的一个随机数
			}
			Collections.sort(T[i].getDataIndex());  //排序是为了按照顺序访问数据库元素x1~x5
//			for(int m = 0;m<T[i].getDataIndex().size();m++){
//				System.out.print(T[i].getDataIndex().get(m)+" ");
//			}
//			System.out.println();
			
			//初始化当前事务对数据库元素的写操作都未完成-->用于2PL
			for(int m=0;m<T[i].getDataIndex().size();m++){
				T[i].getTlockList().add(Transation.UNLOCK);
			}
		}
	}
	
	//数据库重置为初态
	public void resetDB(){
		for(int j = 0;j<DB.length;j++){ 
			DB[j]=j+1;
		}
	}
	
	public void sequenceSchedule(){
		//三个事务顺序执行共有全排列6中情况
		for(int i = 0;i<3;i++){
			for(int k = 0;k<3;k++){
				if(i==k) continue;
				for(int m=0;m<3;m++){
					if(m==k || m==i) continue;
					System.out.println("(T"+(i+1)+",T"+(k+1)+",T"+(m+1)+"):  >>  Original DB[]="+Arrays.toString(DB));
					T[i].executeTransation(DB, i);   //对事务i按顺序对x1~x2元素进行读、写操作，这里对读写操作进行了封装
					T[k].executeTransation(DB, k);	//对事务k按顺序对x1~x2元素进行读、写操作，这里对读写操作进行了封装
					T[m].executeTransation(DB, m);	//对事务m按顺序对x1~x2元素进行读、写操作，这里对读写操作进行了封装
					System.out.println("--->> Final DB[]="+Arrays.toString(DB));
					System.out.println();
					resetDB();   //重置数据库的元素
					
				}
			}
		}
	}
	
	public void concurrencyScheduleWithoutControl(){
		System.out.println("------->>> Original DB:  " + Arrays.toString(DB));
		int readIndex[] = {0,0,0};    //三个整数分别记录三个事务的下一个读操作的下标
		int writeIndex[] = {0,0,0};   //三个整数分别记录三个事务的下一个写操作的下标
		int actionNum = 0;
		while(actionNum!=-6){   //actionNum=-6表示所有事务的所有读写操作都进行完了
			actionNum = 0;
			int whichTransation = Math.abs(random.nextInt())%3;   //下一个执行动作的是哪一个事务中的读或写
			if(readIndex[whichTransation]+writeIndex[whichTransation] == -2){   //此事务的读和写都进行完了
				continue;
			}
			if(Math.abs(random.nextInt())%2 == Transation.READ && readIndex[whichTransation]!=-1){ //如果是读操作且此事务的读操作还没有进行完
				T[whichTransation].read(DB, readIndex[whichTransation]);
				int r = T[whichTransation].getDataIndex().get(readIndex[whichTransation])+1;  //操作的数据库的元素下标+1
				operateList.add(new Operate(whichTransation, readIndex[whichTransation], Transation.READ));
				System.out.println("r"+(whichTransation+1)+"(x"+r+")="+T[whichTransation].getReadData().get(readIndex[whichTransation]));
				readIndex[whichTransation]++;
				if(readIndex[whichTransation]>=T[whichTransation].getDataIndex().size()){   //此事务的读操作完后将标记其下标的值置-1
					readIndex[whichTransation] = -1;
				}												////如果是写操作且此事务的读操作还没有进行完 且（read操作已完； 或者 对此元素read操作先于write操作，保证write写时的数据不为空）
			}else if(writeIndex[whichTransation]!=-1 && (readIndex[whichTransation]==-1 || readIndex[whichTransation]>writeIndex[whichTransation])){ 
				int toWrite = T[whichTransation].write(DB, writeIndex[whichTransation]);
				int w = T[whichTransation].getDataIndex().get(writeIndex[whichTransation])+1; ////操作的数据库的元素下标+1
				operateList.add(new Operate(whichTransation, writeIndex[whichTransation], Transation.WRITE));
				System.out.print("w"+(whichTransation+1)+"(x"+w+",x"+w+"+"+toWrite+")");
				System.out.println("-------->"+Arrays.toString(DB));
				writeIndex[whichTransation]++;
				if(writeIndex[whichTransation]>=T[whichTransation].getDataIndex().size()){   //此事务的读操作完后将标记其下标的值置-1
					writeIndex[whichTransation] = -1;
				}
			} 
			for(int i=0;i<3;i++){
				actionNum+=readIndex[i]+writeIndex[i];
			}
//			System.out.println("..actionNum:"+actionNum);
		}
		System.out.println("\n-------->>>Final DB:  "+Arrays.toString(DB));
		System.out.println();
		resetDB();
//		for( Operate o:operateList){
//			System.out.println("@@@: "+"T"+(o.getTi()+1)+" DB"+(o.getDBi()+1)+(o.getType()==0? " r": " w"));
//		}
	}
	
	public void concurrencyScheduleWith2PL(){
		initDBLock();
		
		for(int i =0;i<T.length;i++){//将ReadData list清空并初始化
			T[i].getReadData().clear();		
			for(int k=0;k<T[i].getDataIndex().size();k++){
				T[i].getReadData().add(0);
			}
		}
		for(int i =0;i<operateList.size();i++){
			final int Di = operateList.get(i).getDi();  //DataIndex 列表的下标
			final int Ti = operateList.get(i).getTi();	//事务数组的下标
			final int DBi = T[Ti].getDataIndex().get(Di); //要操作的数据库元素的下标
			final int type = operateList.get(i).getType();  //读或者写
//			System.out.println(type==Transation.READ? "r":"w");
			if(DBLock.get(DBi)== Transation.UNLOCK  || T[Ti].getTlockList().get(Di) == Transation.LOCK){
				if(type == Transation.READ){
					System.out.println("l"+(Ti+1)+"(x"+(DBi+1)+")");
					DBLock.set(DBi, Transation.LOCK); //事务读之前加锁
					T[Ti].read2PL(DB, Di);
					System.out.println("r"+(Ti+1)+"(x"+(DBi+1)+")="+DB[DBi]);
					T[Ti].getTlockList().set(Di, Transation.LOCK);
					int rLockNum = 0;
					for(int temp: T[Ti].getTlockList()){
						rLockNum +=temp;
					}
					if(rLockNum == T[Ti].getDataIndex().size()){  
						T[Ti].lockFinishFlag=true;
					}
//					System.out.println("T"+(Ti+1)+">>>rLockNum:"+rLockNum+"   ,lockFinishFlag: "+T[Ti].lockFinishFlag);
				}else if(type==Transation.WRITE && T[Ti].getTlockList().get(Di) == Transation.LOCK){  //写操作一定在读操作加锁之后执行
					System.out.print("w"+(Ti+1)+"(x"+(DBi+1)+",x"+(DBi+1)+"+"+T[Ti].getDataToWrite().get(Di)+")");
					T[Ti].write(DB, Di);
					System.out.println("-------->"+Arrays.toString(DB));
					
					T[Ti].getWriteFinishList().put(Di, Transation.writeFinish);
					if(T[Ti].lockFinishFlag == true){
						for(int m=0;m<T[Ti].getWriteFinishList().size();m++){
							if(T[Ti].getWriteFinishList().get(m) ==Transation.writeFinish){
								System.out.println("u"+(Ti+1)+"(x"+(T[Ti].getDataIndex().get(m)+1)+")");
								DBLock.set(T[Ti].getDataIndex().get(m), Transation.UNLOCK); //事务写之后解锁
								T[Ti].getWriteFinishList().replace(m, 0);
								
							}
						}
					}

				}
			}else{     //数据元素已加锁则开启一个线程等待执行操作
				if(type == Transation.READ){
					System.out.println(" >l"+(Ti+1)+"(x"+(DBi+1)+")"+"r"+(Ti+1)+"(x"+(DBi+1)+")="+DB[DBi]+"               ........ Refuse! Waiting...");
				}else{
					System.out.println(" >w"+(Ti+1)+"(x"+(DBi+1)+",x"+(DBi+1)+"+"+T[Ti].getDataToWrite().get(Di)+")                 ........ Refuse! Waiting...");
				}

				new Thread(){
					public void run() {
						if(type == Transation.READ){     //读操作，要一直等待数据库元素解锁
							while(DBLock.get(DBi) == Transation.LOCK){}
							//数据库元素解锁后，有可能有两个以上的线程在等待这个元素，那谁先谁后呢？（这里事务共三个，故最多有两个）
							//不能不管让它们同时开始，这样输出不准确。那就随机等待一定的时间。（有可能出现两个线程都执行了syso(l)，但还没实行真实的加锁
							try {
								Thread.sleep(Math.abs(random.nextInt())%1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							//必定有一个先执行，那么再看一下是否还是解锁状态，然后就可以执行操作了
							while(DBLock.get(DBi) == Transation.LOCK){}
							DBLock.set(DBi, Transation.LOCK); //事务读之前加锁
							System.out.println("l"+(Ti+1)+"(x"+(DBi+1)+")                            ........ Redo");
							T[Ti].read2PL(DB, Di);
							System.out.println("r"+(Ti+1)+"(x"+(DBi+1)+")="+DB[DBi]);
							T[Ti].getTlockList().set(Di, Transation.LOCK);  //读操作完成之后设置读锁
							int rLockNum = 0;
							for(int temp: T[Ti].getTlockList()){
								rLockNum +=temp;
							}
							if(rLockNum == T[Ti].getDataIndex().size()){  
								T[Ti].lockFinishFlag=true;
							}
//							System.out.println("T"+(Ti+1)+">>>rLockNum:"+rLockNum+"   ,lockFinishFlag: "+T[Ti].lockFinishFlag);
							
						}else{			//写操作要一直等待相应的读之后加锁
							while(T[Ti].getTlockList().get(Di) == Transation.UNLOCK){}
							System.out.print("w"+(Ti+1)+"(x"+(DBi+1)+",x"+(DBi+1)+"+"+T[Ti].getDataToWrite().get(Di)+")");
							T[Ti].write(DB, Di);
							System.out.println("-------->"+Arrays.toString(DB)+"    ........ Redo");
							T[Ti].getWriteFinishList().put(Di, Transation.writeFinish);
							if(T[Ti].lockFinishFlag == true){
								for(int m=0;m<T[Ti].getWriteFinishList().size();m++){
									if(T[Ti].getWriteFinishList().get(m) ==Transation.writeFinish){
										System.out.println("u"+(Ti+1)+"(x"+(T[Ti].getDataIndex().get(m)+1)+")");
										DBLock.set(T[Ti].getDataIndex().get(m), Transation.UNLOCK); //事务写之后解锁
										T[Ti].getWriteFinishList().replace(m, 0);
										
									}
								}
							}
						}
					};
				}.start();
			}
		}
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println();
		Map map=Thread.getAllStackTraces();
		if(map.size()>5){
			System.out.println("出现死锁，程序退出！");
			System.exit(0);
		}else{
			System.out.println(" -------->>> Final DB[]: "+Arrays.toString(DB));
		}
				
	}
	
	public static void main(String[] args) {
		TwoPhaseLocking pl = new TwoPhaseLocking();
		pl.generateThreeTransation();
		
		pl.sequenceSchedule();
		System.out.println("************************************************************");
		pl.concurrencyScheduleWithoutControl();
		System.out.println("************************************************************");
		pl.concurrencyScheduleWith2PL();
	}

}
