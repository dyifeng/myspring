package com.dyf.spring.framework.webmvc;

import com.dyf.spring.framework.annotation.MyController;
import com.dyf.spring.framework.annotation.MyRequestMapping;
import com.dyf.spring.framework.context.MyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDispatcherServlet extends HttpServlet {
    private MyApplicationContext context;
    private final String CONTEXT_CONFIG_LOCSTION = "contextConfigLocation";

    private List<MyHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<MyHandlerMapping, MyHandlerAdapter> handlerAdapters = new HashMap<MyHandlerMapping, MyHandlerAdapter>();

    private List<MyViewResolver> viewResolvers = new ArrayList<>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception Detals\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1、通过从request中拿到URL，去匹配一个HandlerMapping
        MyHandlerMapping handler = getHandler(req);

        if(handler == null){
            processDispatchResult(req, resp, new MyModelAndView("404"));

            return;
        }

        // 2、准备好调用前的参数
        MyHandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        // 3、真正调用方法， 返回ModelAndView存储了要传页面上值，和页面模板的名称
        MyModelAndView mv = handlerAdapter.handle(req, resp, handler);

        // 真正的输出
        processDispatchResult(req, resp, mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView mv) throws Exception {
        // 把ModelAndView变成一个HTML、OutputStream、json、freemark
        // ContextType
        if(null == mv){
            return;
        }

        // 如果ModelAndView不为null，需要做渲染
        if(this.viewResolvers.isEmpty()){
            return;
        }

        for (MyViewResolver viewResolver : this.viewResolvers){
            MyView view = viewResolver.resolveVewName(mv.getViewName(), null);
            view.render(mv.getModel(), req, resp);
            return;
        }
    }

    private MyHandlerAdapter getHandlerAdapter(MyHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){
            return null;
        }
        MyHandlerAdapter handlerAdapter = this.handlerAdapters.get(handler);
        if(handlerAdapter.supports(handler)){
            return handlerAdapter;
        }
        return null;
    }

    private MyHandlerMapping getHandler(HttpServletRequest request) throws Exception{
        if(this.handlerMappings.isEmpty()){
            return null;
        }
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        for (MyHandlerMapping handler : this.handlerMappings){
            try {
                Matcher matcher = handler.getPattern().matcher(url);
                if(!matcher.matches()){
                    continue;
                }
                return handler;
            } catch (Exception e){
                throw e;
            }
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、初始化ApplicationContext
        context = new MyApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCSTION));

        //2、初始化Spring MVC的九大组件
        initStrategies(context);
    }

    //初始化策略
    protected void initStrategies(MyApplicationContext context) {
        //多文件上传的组件
        initMultipartResolver(context);
        //初始化本地语言环境
        initLocaleResolver(context);
        //初始化模板处理器
        initThemeResolver(context);
        //handlerMappping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
        //
        initFlashMapManager(context);
    }

    private void initFlashMapManager(MyApplicationContext context) { }

    private void initViewResolvers(MyApplicationContext context) {
        // 拿到模板存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir=new File(templateRootPath);
        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new MyViewResolver(templateRoot));
        }

    }

    private void initRequestToViewNameTranslator(MyApplicationContext context) { }

    private void initHandlerExceptionResolvers(MyApplicationContext context) { }

    // 分解request值
    private void initHandlerAdapters(MyApplicationContext context) {
        // 把一个request请求变成一个handler，参数都是字符串的，自动匹配到handler的形参

        // 拿到HandlerMapping才能运行，即有机构HandlerMapping就有机构HandlerAdapter
        for (MyHandlerMapping handlerMapping : this.handlerMappings){
            this.handlerAdapters.put(handlerMapping, new MyHandlerAdapter());
        }
    }

    // 将HandlerMapping封装到List中，保存url和method的对应关系
    private void initHandlerMappings(MyApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames){
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();

                if(!clazz.isAnnotationPresent(MyController.class)){
                    continue;
                }

                String baseUrl = "";
                // 获取Controller的url配置
                if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                    MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                // 获取Method的url配置
                Method[] methods = clazz.getMethods();
                for (Method method : methods){
                    // 么有加RequestMapping注解的直接忽略
                    if(!method.isAnnotationPresent(MyRequestMapping.class)){
                        continue;
                    }

                    MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);

                    String url = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                    Pattern pattern = Pattern.compile(url);
                    this.handlerMappings.add(new MyHandlerMapping(controller, method, pattern));

                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initThemeResolver(MyApplicationContext context) { }

    private void initLocaleResolver(MyApplicationContext context) { }

    private void initMultipartResolver(MyApplicationContext context) { }
}
