package com.dyf.spring.framework.aop;

import com.dyf.spring.framework.aop.intercept.MyMethodInvocation;
import com.dyf.spring.framework.aop.support.MyAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class MyJdkDynamicAopProxy implements MyAopProxy, InvocationHandler {

    private MyAdvisedSupport advised;
    public MyJdkDynamicAopProxy(MyAdvisedSupport config){
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.advised.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.advised.getTargetClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获得拦截器链
        List<Object> interceptorAndDynamicMethodMatchers = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, this.advised.getTargetClass());
        MyMethodInvocation invocation = new MyMethodInvocation(proxy, this.advised.getTarget(),
                method, args, this.advised.getTargetClass(), interceptorAndDynamicMethodMatchers);
        return invocation.proceed();
    }
}
