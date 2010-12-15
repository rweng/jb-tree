/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;

import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.internal.cglib.proxy.MethodInterceptor;


public class IsOpenEnsurer implements org.aopalliance.intercept.MethodInterceptor {

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(!((ResourceManager)invocation.getThis()).isOpen()){
			throw new IOException("ResourceManager is not open!");
		}
		
		return invocation.proceed();
	}



}
