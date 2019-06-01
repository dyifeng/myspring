package framework.context;

import framework.beans.MyBeanFactory;
import framework.beans.config.MyBeanDefinition;
import framework.beans.support.MyBeanDefinitionReader;
import framework.beans.support.MyDefaultListableBeanFactory;

import java.util.List;

public class MyApplicationContext extends MyDefaultListableBeanFactory implements MyBeanFactory {

    private String[] configLocations;
    public MyBeanDefinitionReader reader;

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

    public Object getBean(String beanName) throws Exception {
        return null;
    }
}
