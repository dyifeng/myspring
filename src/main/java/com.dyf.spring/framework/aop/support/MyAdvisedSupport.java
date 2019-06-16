package com.dyf.spring.framework.aop.support;

import com.dyf.spring.framework.aop.aspect.MyAfterReturningAdviceInterceptor;
import com.dyf.spring.framework.aop.aspect.MyAfterThrowingAdviceInterceptor;
import com.dyf.spring.framework.aop.aspect.MyMethodBeforeAdviceInterceptor;
import com.dyf.spring.framework.aop.config.MyAopConfig;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyAdvisedSupport {
    private Object target;

    private MyAopConfig config;

    private Class<?> targetClass;

    private Pattern pointCutClassPattern;

    private Map<Method, List<Object>> methodCache;

    public MyAdvisedSupport(MyAopConfig config) {
        this.config = config;

    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object getTarget(){
        return this.target;
    }

    // 获得所有符合pointCut表达式定义的类
    public Class<?> getTargetClass(){
        return this.targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    private void parse() {
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        // 表达式：public .* com.study.spring.business.service.. *Service..*(.*)
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " +
                pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        try {
            methodCache = new HashMap<>();

            Pattern pattern = Pattern.compile(pointCut);
            // 获取切面的类
            Class aspectClass = Class.forName(this.config.getAspectClass());
            // 获取切面的方法
            Map<String, Method> aspectMethods = new HashMap<>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }


            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();
                if (methodString.contains("throws")) {
                    // 获取完整的方法名
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();

                }
                Matcher matcher = pattern.matcher(methodString);
                if(matcher.matches()){
                    // 把每个方法包装成 MethodInterceptor 拦截器链
                    // before、after、afterThrowing
                    List<Object> advices = new LinkedList<>();
                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        // 创建一个Advice对象，将前置通知变成前置拦截器
                        advices.add(new MyMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()), aspectClass.newInstance()));
                    }
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
                        // 创建一个Advice对象
                        advices.add(new MyAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()), aspectClass.newInstance()));
                    }
                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
                        // 创建一个Advice对象
                        MyAfterThrowingAdviceInterceptor afterThrowingAdviceInterceptor =
                                new MyAfterThrowingAdviceInterceptor(aspectMethods.get(config.getAspectAfterThrow()), aspectClass.newInstance());
                        afterThrowingAdviceInterceptor.setThrowingName(config.getAspectAfterThrowingName());
                        advices.add(afterThrowingAdviceInterceptor);
                    }
                    methodCache.put(method, advices);
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    // 解析配置文件的方法
    // 每一个method都对应一个执行链，如：before -> add -> after -> afterThrowing
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception{
        List<Object> cached = methodCache.get(method);
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cached = methodCache.get(m);
            this.methodCache.put(m, cached);
        }
        return cached;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
