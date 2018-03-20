package cn.cjm.fourth;

import java.util.HashMap;
import java.util.Map;



/**
 * 
 * @ClassName: FourthReadWriteLock
 * @Description: 同测试三的理，这里要实现的是写锁重入
 * @version 4.0 测试结果：
 * 					简单测试之后，发现综合了三、四两个案例之后，可以实现读写锁的重入了
 * 				be continue...
 * @author cjm
 * @date 2018年3月20日 下午2:43:16
 *
 */
public class FourthReadWriteLock {
	// 记录线程持有读锁的信息
	private Map<Thread, Integer> readerMap = new HashMap<Thread, Integer>();

	private int writerCount = 0;
	private int writeRequest = 0;
	private Thread writingThread = null;
	// 记录当前写进程的重入次数
	private int writeAccess = 0;
	
	public synchronized void lockRead() throws InterruptedException {
		Thread currentThread = Thread.currentThread();

		while (!canGetReadAccess(currentThread)) {
			wait();
		}

		readerMap.put(currentThread, getAccessCount(currentThread) + 1);
	}

	public synchronized void unlockRead() {
		Thread currentThread = Thread.currentThread();
		int count = getAccessCount(currentThread);
		if (count == 1) {
			readerMap.remove(currentThread);
		} else {
			readerMap.put(currentThread, count - 1);
		}
		// readers--;
		notifyAll();
	}

	public synchronized void lockWrite() throws InterruptedException {
		writeRequest++;// 与第一个不同的地方
		Thread currentThread = Thread.currentThread();
		while (!canGetWriteAccess(currentThread)) {
			wait();
		}
		// 已经取到锁了，所以这个写请求可以-1了
		writeRequest--;// 与第一个不同的地方
		writeAccess++;// 重入次数+1
		writerCount++;
		writingThread = currentThread;
	}



	public synchronized void unlockWrite() {
		writerCount--;
		writeAccess--;
		if(writeAccess ==0) {
			writingThread = null;// 锁释放完了，当前写进程置空
		}
		notifyAll();
	}

	// 判断线程能否获得写许可
	private boolean canGetWriteAccess(Thread currentThread) {
		if(readerMap.size() >0)//有读者线程
			return false;
		if(writingThread == null)// 没有线程获取过写锁
			return true;
		if(writingThread != currentThread)// 获取写锁的不是当前线程 
			return false;
		return true;
	}
	
	// 获得该线程获取读锁的次数
	private int getAccessCount(Thread currentThread) {
		Integer count = readerMap.get(currentThread);
		if (count == null)
			return 0;
		else
			return count;
	}

	// 判断该线程是否能获得读许可
	private boolean canGetReadAccess(Thread currentThread) {
		if (writerCount > 0)
			return false;
		// 解决读锁重入
		if (readerMap.containsKey(currentThread))
			return true;
		if (writeRequest > 0)
			return false;
		return true;
	}

	public static void main(String[] args) {
		FourthReadWriteLock lock = new FourthReadWriteLock();

		// 写者，每隔三秒写一次
		new Thread(() -> {
			while (true) {

				try {
					lock.lockWrite();
					lock.lockWrite();
					System.out.println(Thread.currentThread().getName() + "在写数据");
					lock.unlockWrite();
					lock.unlockWrite();
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		// 创建5个读线程，每隔一秒读一次
		for (int i = 0; i < 5; i++) {
			new Thread(() -> {
				while (true) {
					// 每隔一秒读一次
					try {
						lock.lockRead();
						// 连续读两次
						lock.lockRead();
						System.out.println(Thread.currentThread().getName() + "在读数据");
						lock.unlockRead();
						lock.unlockRead();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
}
