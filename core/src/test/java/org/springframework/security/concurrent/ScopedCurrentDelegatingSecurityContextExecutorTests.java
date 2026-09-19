package org.springframework.security.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.ScopedSecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextHolder;

public class ScopedCurrentDelegatingSecurityContextExecutorTests {
	
	@Test
	public void testThatFails() throws InterruptedException {
		SecurityContextHolder.setContextHolderStrategy(new ScopedSecurityContextHolderStrategy());
		final DelegatingSecurityContextExecutor executor = new DelegatingSecurityContextExecutor(Executors.newFixedThreadPool(5));
		
		final CountDownLatch latch = new  CountDownLatch(1);
		executor.execute(() -> {
			latch.countDown();
		});
			
		Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Test hangs");
	}
	
	@Test
	public void testThatSucceds() throws InterruptedException {
		SecurityContextHolder.setContextHolderStrategy(new ScopedSecurityContextHolderStrategy());
		final DelegatingSecurityContextExecutor executor = new DelegatingSecurityContextExecutor(Executors.newFixedThreadPool(5));
		
		final CountDownLatch latch = new  CountDownLatch(1);
		
		ScopedSecurityContextHolderStrategy.getSecuriyContextCarrier().run(() -> {
			executor.execute(() -> {
				latch.countDown();
			});
			
		});
		
		Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Test hangs");
	}

}
