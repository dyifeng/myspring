package com.dyf.spring.framework.aop.aspect;

import java.lang.reflect.Method;

public abstract class MyAbstractAspectAdvice implements MyAdvice {

    private Method aspectMethod;
    private Object aspectTarget;
    /**
     *
     * @param aspectMethod
     * @param aspectTarget：执行切面目标的方法
     */
    public MyAbstractAspectAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    protected void invokeAdviceMethod(MyJoinPoint joinPoint, Object returnValue, Throwable throwable) throws Throwable{
        Class<?>[] paramTypes = this.aspectMethod.getParameterTypes();
        if(null == paramTypes || paramTypes.length == 0){
            this.aspectMethod.invoke(aspectTarget);
        } else {
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if(paramTypes[i] == MyJoinPoint.class){
                    args[i] =joinPoint;
                } else if(paramTypes[i] == Throwable.class){
                    args[i] = throwable;
                } else if(paramTypes[i] == Object.class){
                    args[i] = returnValue;
                }
            }
            this.aspectMethod.invoke(aspectTarget, args);

        }
    }


}
