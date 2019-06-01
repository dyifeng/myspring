package framework.context;

/**
 * 通过解耦方式获得IOC容器的顶层设计
 * 后面将通过一个监听器去扫描所有的类，只要实现了此接口，（观察者模式）
 * 将自动调用setApplicationContext方法，从而将IOC容器注入到目标类中
 */
public interface MyApplicationContextAware {

    //把IOC容器注入进来
    void setApplicationContext(MyApplicationContext applicationContext) throws Exception;
}
