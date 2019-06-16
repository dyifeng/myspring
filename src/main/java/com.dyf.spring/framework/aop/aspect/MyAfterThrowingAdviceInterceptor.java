package com.dyf.spring.framework.aop.aspect;

import com.dyf.spring.framework.aop.intercept.MyMethodInterceptor;
import com.dyf.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

public class MyAfterThrowingAdviceInterceptor extends MyAbstractAspectAdvice implements MyAdvice, MyMethodInterceptor {

    private String throwingName;

    public MyAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MyMethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable e){
            invokeAdviceMethod(invocation, null, e.getCause());
            throw e;
        }

    }

    public void setThrowingName(String throwingName){
        this.throwingName = throwingName;
    }
}
