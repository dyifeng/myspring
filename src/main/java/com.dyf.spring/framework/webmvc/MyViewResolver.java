package com.dyf.spring.framework.webmvc;

import java.io.File;
import java.util.Locale;

public class MyViewResolver {
    private File templateRootDir;

    private final String DEFAULT_TEMPLATE_SUFFX = ".html";

    public MyViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(templateRootPath);
    }

    public MyView resolveVewName(String viewName, Locale locale) throws Exception{
        if (viewName == null || "".equals(viewName.trim())){
            return null;
        }

        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFX);
        File templateFile = new File((templateRootDir.getPath()+ "/" + viewName).replaceAll("/+","/"));

        return new MyView(templateFile);
    }

}
