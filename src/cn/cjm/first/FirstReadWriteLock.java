package cn.cjm.first;

/**
 * 
 * @ClassName: FirstReadWriteLock
 * @Description: 多个读者与写者并发访问共享数据，读读不互斥，读写互斥，写写互斥
 * @version 1.0 读者优先，写者有可能发生饥饿
 * @result 测试结果:
 * 				除了第一次写线程能抢到执行权以外，其他时候读线程持续占用资源，
 * 			导致读线程饥饿。
 * @author cjm
 * @date 2018年3月20日 上午7:11:55
 *
 */
public class FirstReadWriteLock {
	// 记录读者的个数
	private int readerCount = 0;
	// 记录写者的个数
	private int writerCount = 0;

	public synchronized void lockRead() throws InterruptedException {
		while (writerCount > 0) {
			wait();
		}
		readerCount++;
	}

	public synchronized void unLockRead() {
		readerCount--;
		notifyAll();
	}

	public synchronized void lockWrite() throws InterruptedException {
		while (readerCount > 0 || writerCount > 0) {
			wait();
		}
		writerCount++;
	}

	public synchronized void unlockWrite() {
		writerCount--;
		notifyAll();
	}

	public static void main(String[] args) {
		FirstReadWriteLock lock = new FirstReadWriteLock();

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
