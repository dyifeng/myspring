package com.dyf.spring.framework.aop.aspect;

import com.dyf.spring.framework.aop.intercept.MyMethodInterceptor;
import com.dyf.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

public class MyAroundAdviceInterceptor extends MyAbstractAspectAdvice implements MyAdvice, MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    public MyAroundAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MyMethodInvocation invocation) throws Throwable {
        System.out.println("环绕之前");
        Object retVal = invocation.proceed();
        invocation.getMethod().invoke(invocation.getThis());
        System.out.println("环绕之后");
        return null;
    }
}
