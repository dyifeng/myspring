package com.dyf.spring.framework.context;


import com.dyf.spring.framework.annotation.MyAutowired;
import com.dyf.spring.framework.annotation.MyController;
import com.dyf.spring.framework.annotation.MyService;
import com.dyf.spring.framework.aop.MyAopProxy;
import com.dyf.spring.framework.aop.MyCglibAopProxy;
import com.dyf.spring.framework.aop.MyJdkDynamicAopProxy;
import com.dyf.spring.framework.aop.config.MyAopConfig;
import com.dyf.spring.framework.aop.support.MyAdvisedSupport;
import com.dyf.spring.framework.beans.MyBeanFactory;
import com.dyf.spring.framework.beans.MyBeanWrapper;
import com.dyf.spring.framework.beans.config.MyBeanDefinition;
import com.dyf.spring.framework.beans.support.MyBeanDefinitionReader;
import com.dyf.spring.framework.beans.support.MyDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext extends MyDefaultListableBeanFactory implements MyBeanFactory {

    private String[] configLocations;
    public MyBeanDefinitionReader reader;

    //用来保证注册时单例的容器，单例IOC缓存
    private Map<String, Object> singletonBeanCacheMap = new ConcurrentHashMap<>();
    //用来存储所有的被代理过的对象
    private Map<String, MyBeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();


    public MyApplicationContext(String... configLoacations){
        this.configLocations = configLoacations;

        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() throws Exception {
        //1.定位，定位配置文件（策略模式）
        reader = new MyBeanDefinitionReader(this.configLocations);
        //2.加载配置文件，扫描相关的类，把他们封装成BeanDefinition
        List<MyBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        //3.注册，把配置信息放到容器里面（伪ioc容器）
        // 真正的IOC容器是BeanWrapper
        doRegisterBeanDefinition(beanDefinitions);

    }

    private void doRegisterBeanDefinition(List<MyBeanDefinition> beanDefinitions) {
        for (MyBeanDefinition beanDefinition : beanDefinitions){
            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }


    //依赖注入，开始，通过读取BeanDefinition中的信息，然后通过反射创建一个实例并返回
    //Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper进行一个包装
    //装饰器模式
    //1、保留原来的OOP关系
    //2、需要对他进行扩展，增强（为以后AOP打基础）
    @Override
    public Object getBean(String beanName) throws Exception {
        //1、初始化
        MyBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        Object instance = null;
        instance = instantiateBean(beanName,beanDefinition);
        //将对象分装到BeanWrapper中
        MyBeanWrapper beanWrapper = new MyBeanWrapper(instance);
        //2、拿到BeanWrapper之后，把BeabWrapper保存到IOC容器中
        this.beanWrapperMap.put(beanName, beanWrapper);
        //3、注入，DI操作
        populateBean(beanName, new MyBeanDefinition(), beanWrapper);
        return this.beanWrapperMap.get(beanName).getWrappedInstance();
    }

    private void populateBean(String beanName, MyBeanDefinition myBeanDefinition, MyBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrappedInstance();

        Class<?> clazz = beanWrapper.getWrapperClass();
        //判断只有加了注解的类才执行依赖注入
        if (clazz.isAnnotationPresent(MyController.class)||clazz.isAnnotationPresent(MyService.class)) {
            return;
        }
        //获取所有的fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(MyAutowired.class)) {
                continue;
            }
            MyAutowired autowired = field.getAnnotation(MyAutowired.class);
            String autowiredBeanName = autowired.value();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);
            try {
                if(this.beanWrapperMap.get(autowiredBeanName) == null){
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //负责读取BeanDefinition的配置，将配置的信息转化成实体对象，存到真正的IOC容器中
    //IOC容器的Key就是beanName，Value就是beanDefinition存储的class的实例
    private Object instantiateBean(String beanName, MyBeanDefinition beanDefinition) {
        //1、拿到要实例化的对象的类名
        String className = beanDefinition.getBeanClassName();
        //2、反射实例化，得到对象
        Object instance = null;
        try {
            if (this.singletonBeanCacheMap.containsKey(className)) {
                instance = this.singletonBeanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();


                // 解析配置文件
                MyAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);

                // 查看当前类是否在切面规则之内
                // 符合PointCut的规则的话，将创建代理对象
                if(config.pointCutMatch()){
                    // 创建代理策略，看是用CGLib还是JDK
                    instance = createProxy(config).getProxy();
                }

                this.singletonBeanCacheMap.put(className, instance);
                this.singletonBeanCacheMap.put(beanDefinition.getFactoryBeanName(),instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //3、将对象封装到BeanWrapper中
        //4、把BeanWrapper存到IOC容器中
        return instance;
    }

    //====================MVC===========================
    public String[] getBeanDefinitionNames(){
        //伪IOC容器，存储注册信息的BeanDefinition
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }

    //===================AOP===========================
    private MyAdvisedSupport instantionAopConfig(MyBeanDefinition beanDefinition) {
        MyAopConfig config = new MyAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
//        config.setAspectAround(this.reader.getConfig().getProperty("aspectAround"));
        return new MyAdvisedSupport(config);
    }

    private MyAopProxy createProxy(MyAdvisedSupport config) {
        Class targetClass = config.getTargetClass();
        if(targetClass.getInterfaces().length> 0){
            return new MyJdkDynamicAopProxy(config);
        }
        return new MyCglibAopProxy(config);
    }
}
