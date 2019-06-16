package com.dyf.spring.framework.aop.intercept;


import com.dyf.spring.framework.aop.aspect.MyJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMethodInvocation implements MyJoinPoint {
    private Object proxy;
    private Method method;
    private Object target;
    private Object[] arguments;
    private List<Object> interceptorsAndDynamicMethodMatchers;
    private Class<?> targetClass;

    // 定义一个索引，从-1开始来记录当前拦截器执行的位置
    private int currentInterceptorIndex = -1;

    private Map<String, Object> userAttributes;

    public MyMethodInvocation(Object proxy, Object target, Method method,
                              Object[] arguments, Class<?> targetClass,
                              List<Object> interceptorAndDynamicMethodMatchers){
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.targetClass = targetClass;
        this.interceptorsAndDynamicMethodMatchers = interceptorAndDynamicMethodMatchers;
    }

    // 入口方法，真正执行拦截器链中每个方法
    public Object proceed() throws Throwable{
        // 如果Interceptor执行完了，或者没有，则执行joinPoint
        if(this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1){
            return this.method.invoke(this.target, this.arguments);
        }

        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);

        // 如果要动态匹配joinPoint
        if(interceptorOrInterceptionAdvice instanceof MyMethodInterceptor){
            MyMethodInterceptor interceptor = (MyMethodInterceptor) interceptorOrInterceptionAdvice;
            return interceptor.invoke(this);
        } else {
            // 动态匹配失败时，略过当前Interceptor，调用下一个Interceptor
            return proceed();
        }


    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if(value != null){
            if(this.userAttributes == null){
                this.userAttributes = new HashMap<>();
            }
            this.userAttributes.put(key, value);
        } else {
            if(this.userAttributes != null){
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }
}
