package cn.cjm.third;
/**
 * 
 * @ClassName: ThirdReadWriteLock 
 * @Description: 分析案例二的代码，我们可以发现问题是读锁和写锁都不能重入所导致的，
 * 			针对这种情况，我们这次先来讨论如何实现读锁重入
 * 			要确定一个线程是否已经持有了这个（读）锁，我们可以用一个map来存储这个信息，
 * 			map的key为当前线程对象，value是获取锁的次数。
 * @version 3.0 读锁重入
 * 			测试结果：
 * 				可以看出，程序运行了较长一段时间也没出现问题，证明这是可以解决读锁的重入问题的
 * @author cjm
 * @date 2018年3月20日 上午10:02:05 
 *
 */

import java.util.HashMap;
import java.util.Map;



public class ThirdReadWriteLock {
	// 记录线程持有读锁的信息
	private Map<Thread, Integer> readerMap = new HashMap<Thread, Integer>();

	private int writerCount = 0;
	private int writeRequest = 0;
	// 这个读者的个数，可以用map的size获取，所以这个变量可以注释掉
	// private int readerCount = 0;

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
		while (readerMap.size() > 0 || writerCount > 0) {
			wait();
		}
		// 已经取到锁了，所以这个写请求可以-1了
		writeRequest--;// 与第一个不同的地方
		writerCount++;
	}

	public synchronized void unlockWrite() {
		writerCount--;
		notifyAll();
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
		ThirdReadWriteLock lock = new ThirdReadWriteLock();

		// 写者，每隔三秒写一次
		new Thread(() -> {
			while (true) {

				try {
					lock.lockWrite();
					System.out.println(Thread.currentThread().getName() + "在写数据");
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
