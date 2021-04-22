package cn.parzulpan.shopping.search.thread;

import java.util.concurrent.*;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.thread
 * @desc 初始化线程的四种方式
 */

public class ThreadTest {

    // 创建线程池方式一
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);
    // 创建线程池方式二
    public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 200, 10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(10000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) {
        System.out.println("main start...");

        // 1. 继承 Thread
//        Thread01 thread01 = new Thread01();
//        thread01.start();

        // 2. 实现 Runnable 接口
//        Runnable01 runnable01 = new Runnable01();
//        new Thread(runnable01).start();

        // 3. 实现 Callable 接口 + Future Task
//        FutureTask<Double> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//        Double aDouble = null;
//        try {
//            aDouble = futureTask.get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//        System.out.println("阻塞等待整个线程执行完成，获得返回结果为：" + aDouble);

        // 4. 使用线程池
//        executorService.execute(new Runnable01());
//        executorService.submit(new Runnable01());
        threadPoolExecutor.execute(new Runnable01());


        System.out.println("main end...");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程 Id：" + Thread.currentThread().getId());
            double v = 3.14 * 3.14;
            System.out.println("运行结果：" + v);
        }
    }

    public static class Runnable01 implements Runnable{
        @Override
        public void run() {
            System.out.println("当前线程 Id：" + Thread.currentThread().getId());
            double v = 3.14 * 3.14 * 3.14;
            System.out.println("运行结果：" + v);
        }
    }

    public static class Callable01 implements Callable<Double>{

        @Override
        public Double call() throws Exception {
            System.out.println("当前线程 Id：" + Thread.currentThread().getId());
            double v = 3.14 * 3.14 * 3.14 * 3.14;
            System.out.println("运行结果：" + v);
            return v;
        }
    }
}
