package org.springframework.security.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ScopedSecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ScopedDelegatingSecurityContextExecutorTests {
	
	@Test
	public void testNoPreexistingSecurityContext() throws InterruptedException {
		SecurityContextHolder.setContextHolderStrategy(new ScopedSecurityContextHolderStrategy());
		final CountDownLatch latch = new  CountDownLatch(1);

		final DelegatingSecurityContextExecutor executor = new DelegatingSecurityContextExecutor(Executors.newFixedThreadPool(5));
		executor.setRunnableWrapper((runnable) -> new ScopedSecurityContextHolderStrategy.DelegatingSecurityContextRunnable(null, runnable));
		executor.execute(() -> {
			SecurityContext ctxt = SecurityContextHolder.getContext();
			Authentication auth = new UsernamePasswordAuthenticationToken("admin", "admin");
			ctxt.setAuthentication(auth);
			SecurityContextHolder.setContext(ctxt);

			ctxt = SecurityContextHolder.getContext();
			Authentication auth1 = ctxt.getAuthentication();
			
			latch.countDown();
		});
		
		Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Test hangs"); 
	}
	
	@Test
	public void testPreexistingSecurityContext() throws InterruptedException {
		SecurityContextHolder.setContextHolderStrategy(new ScopedSecurityContextHolderStrategy());
		final CountDownLatch latch = new  CountDownLatch(1);

		final DelegatingSecurityContextExecutor executor = new DelegatingSecurityContextExecutor(Executors.newFixedThreadPool(5));
		
		ScopedSecurityContextHolderStrategy.getSecuriyContextCarrier().run(() -> {
			SecurityContext context = SecurityContextHolder.getContext();
			executor.setRunnableWrapper((runnable) -> new ScopedSecurityContextHolderStrategy.DelegatingSecurityContextRunnable(context, runnable));
			executor.execute(() -> {
				SecurityContext ctxt = SecurityContextHolder.getContext();
				Authentication auth = new UsernamePasswordAuthenticationToken("admin", "admin");
				ctxt.setAuthentication(auth);
				SecurityContextHolder.setContext(ctxt);

				ctxt = SecurityContextHolder.getContext();
				Authentication auth1 = ctxt.getAuthentication();
				
				latch.countDown();
			});
		});
		
		Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Test hangs"); 

	}
	
}
