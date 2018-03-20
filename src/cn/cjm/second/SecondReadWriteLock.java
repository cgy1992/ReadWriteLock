package cn.cjm.second;



/**
 * 
 * @ClassName: SecondReadWriteLock
 * @Description: 针对第一个读写锁出现的问题，我们可以考虑： 当有线程请求锁准备执行写操作时，或者有进行正在进行写操作的时候，
 *               读操作不能继续，即读线程应当被阻塞
 * @version 2.0 测试结果：
 * 					跟预想的结果一样，写线程在一定时间内就取得了资源的执行权
 * 					但是!把读线程的readLock从一个变成两个，程序运行一段时间后，线程"死锁"了！！！
 * 					综合分析，是出现了以下这种情况：
 * 						1. 线程1获得了读锁
 * 						2. 线程2申请获得写锁，但是已经被线程1占用了，所以只能阻塞
 * 						3. 线程1想再次获得读锁，但是线程2处于请求状态，所以当前线程也会被阻塞！
 * 					从而产生了这种“死锁”。
 * 					明显，这是读锁和写锁的不可重入导致的。
 * @author cjm
 * @date 2018年3月20日 上午8:46:57
 *
 */
public class SecondReadWriteLock {
	// 记录读者的个数
	private int readerCount = 0;
	// 记录写者的个数
	private int writerCount = 0;
	// 记录写者请求(与第一个不同的地方)
	private int writeRequest = 0;
	
	
	public synchronized void lockRead() throws InterruptedException {
		while (writerCount > 0 || writeRequest >0) {// 与第一个不同的地方
			wait();
		}
		readerCount++;
	}

	public synchronized void unLockRead() {
		readerCount--;
		notifyAll();
	}

	public synchronized void lockWrite() throws InterruptedException {
		writeRequest++;// 与第一个不同的地方
		while (readerCount > 0 || writerCount > 0) {
			wait();
		}
		// 已经取到锁了，所以这个写请求可以-1了
		writeRequest--;//与第一个不同的地方
		writerCount++;
	}

	public synchronized void unlockWrite() {
		writerCount--;
		notifyAll();
	}

	public static void main(String[] args) {
		SecondReadWriteLock lock = new SecondReadWriteLock();

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
						System.out.println(Thread.currentThread().getName() + "在读数据");
						lock.unLockRead();
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
