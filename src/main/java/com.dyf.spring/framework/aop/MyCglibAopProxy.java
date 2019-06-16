package com.dyf.spring.framework.aop;


import com.dyf.spring.framework.aop.support.MyAdvisedSupport;

public class MyCglibAopProxy implements MyAopProxy {
    public MyCglibAopProxy(MyAdvisedSupport config) {

    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
