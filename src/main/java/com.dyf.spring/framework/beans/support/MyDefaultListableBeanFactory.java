package com.dyf.spring.framework.beans.support;

import com.dyf.spring.framework.beans.config.MyBeanDefinition;
import framework.context.support.MyAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC容器实例化的默认实现，可以扩展，但不可以没有
 */
public class MyDefaultListableBeanFactory extends MyAbstractApplicationContext {
    //IOC容器，存储注册信息的BeanDefinition，伪IOC容器
    protected final Map<String, MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, MyBeanDefinition>();
}
