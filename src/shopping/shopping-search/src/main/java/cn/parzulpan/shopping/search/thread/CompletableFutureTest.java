package cn.parzulpan.shopping.search.thread;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.checkerframework.checker.units.UnitsTools.s;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.thread
 * @desc 异步编排
 */

public class CompletableFutureTest {
    public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 100, 3L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("start...");

//        runAsyncT();
//        supplyAsyncT();
//        thenApplyT();
//        thenAcceptT();
//        thenRunT();
        thenCombineT();


        System.out.println("end...");
        threadPoolExecutor.shutdown();

    }

    /**
     * runAsync 示例
     */
    public static void runAsyncT() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("计算结果：" + i);
        }, threadPoolExecutor);

        // 什么都不返回，调用get方法，就变成了阻塞操作！
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        future.whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void unused, Throwable throwable) {
                System.out.println("whenComplete 执行完成！" + Thread.currentThread().getName());
            }
        });

        future.whenCompleteAsync(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void unused, Throwable throwable) {
                System.out.println("whenCompleteAsync 执行完成！" + Thread.currentThread().getName());
            }
        });

        future.exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable throwable) {
                System.out.println("执行失败！" + throwable.getMessage());
                return null;
            }
        });

    }

    /**
     * supplyAsync 示例
     */
    public static void supplyAsyncT() {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("计算结果：" + i);
            return i;
        }, threadPoolExecutor);

        future.whenComplete((result, exception) -> {
            // 虽然能得到异常信息，但是无法修改返回数据
            System.out.println("whenComplete异步任务完成了，结果是：" + result + ";异常是：" + exception);
        });

        future.whenCompleteAsync((result, exception) -> {
            System.out.println("whenCompleteAsync异步任务完成了，结果是：" + result + ";异常是：" + exception);
        });


        // handle 与 exceptionally 都可以控制返回值，谁先被调用就以谁的为准（先被调用者的返回值为准）
        future.handle((result, throwable) -> {
            if (result != null) {
                return result * 2;
            }
            if (throwable != null) {
                return 1;
            }
            return 0;
        });

        future.exceptionally(throwable -> {
            // 如果执行失败，可以设置默认返回值
            return 10;
        });

        try {
            // 获取返回结果，调用get方法，就变成了阻塞操作
            Integer integer = future.get();
            System.out.println("返回结果：" + integer);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * thenApply 示例
     */
    public static void thenApplyT() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("计算结果：" + i);
            return i;
        }, threadPoolExecutor).thenApplyAsync(result -> {
            System.out.println("任务 2 开启了，上一步的结果为：" + result);
            return "thenApplyAsync的新结果";
        }, threadPoolExecutor);

        try {
            String s = future.get();
            System.out.println("返回结果：" + s);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * thenAccept 示例
     */
    public static void thenAcceptT() {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("计算结果：" + i);
            return i;
        }, threadPoolExecutor).thenAcceptAsync(result -> {
            System.out.println("任务 2 开启了，上一步的结果为：" + result);
        }, threadPoolExecutor);
    }

    /**
     * thenRun 示例
     */
    public static void thenRunT() {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("计算结果：" + i);
            return i;
        }, threadPoolExecutor).thenRunAsync(() -> {
            System.out.println("任务 2 开启了");
        }, threadPoolExecutor);
    }

    public static void thenCombineT() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(new Supplier<String>() {

            @Override
            public String get() {
                return "hello";
            }
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(new Supplier<String>() {

            @Override
            public String get() {
                return "world";
            }
        });
        CompletableFuture<String> result = future1.thenCombine(future2, new BiFunction<String, String, String>() {
            @Override
            public String apply(String s, String s2) {
                return s + " " + s2;
            }
        });

        System.out.println(result.get());
    }

}
