import java.lang.annotation.*;

/**
 * @Target 用于描述注解的使用范围（即：被描述的注解可以用在什么地方）
 * PARAMETER:用于描述参数
 *
 * @Retention 表示需要在什么级别保存该注释信息，用于描述注解的生命周期（即：被描述的注解在什么范围内有效）
 * RUNTIME:在运行时有效（即运行时保留）注解处理器可以通过反射，获取到该注解的属性值，从而去做一些运行时的逻辑处理
 *
 * @Documented 用于描述其它类型的annotation应该被作为被标注的程序成员的公共API，因此可以被例如javadoc此类的工具文档化。
 * Documented是一个标记注解，没有成员。
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    String value() default "";
}
