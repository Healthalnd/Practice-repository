package com.qvc.selenium.data;

import org.apache.log4j.Logger;

import java.util.concurrent.*;


public class TestDataLoadManager<A, V> implements TestDataLoad<A, V> {

	// This is a static class to manage data running across multiple parallel
	// test cases at once
	private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();

	private final TestDataLoad<A, V> c;
	private static Logger logger = Logger.getLogger(TestDataLoadManager.class);
	public TestDataLoadManager(TestDataLoad<A, V> c) {
        cache.clear();
		this.c = c;
	}

    @Override
    public V getData(final A type) throws InterruptedException {
        while (true) {
            Future<V> f = cache.get(type);

            if (f == null) {

                Callable<V> eval = new Callable<V>() {
                    @Override
                    public V call() throws InterruptedException, Exception {
                        return c.getData(type);
                    }
                };
                FutureTask<V> ft = new FutureTask<V>(eval);

                f = cache.putIfAbsent(type, ft);
                if (f == null) {
                    f = ft;
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                logger.debug(e);
                cache.remove(type, f);
            } catch (ExecutionException e) {

                logger.fatal(e);

                InterruptedException ie = new InterruptedException();
                ie.setStackTrace(e.getStackTrace());
                throw ie;
            }
        }

    }

}