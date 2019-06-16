package com.dyf.spring.framework.aop.aspect;

import com.dyf.spring.framework.aop.intercept.MyMethodInterceptor;
import com.dyf.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

public class MyMethodBeforeAdviceInterceptor extends MyAbstractAspectAdvice implements MyAdvice, MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    public MyMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method, Object[] args, Object target) throws Throwable{
        // 传送给织入的参数
//        method.invoke(target);
        super.invokeAdviceMethod(this.joinPoint, null, null);
    }

    @Override
    public Object invoke(MyMethodInvocation invocation) throws Throwable {
        this.joinPoint = invocation;
        // 参数从被织入的代码种才能拿到，JoinPoint
        before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return invocation.proceed();
    }


}
