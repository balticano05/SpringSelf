package my.project.limit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class RateLimitBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> beanClass = bean.getClass();
        boolean hasRateLimit = false;

        for (Method method : beanClass.getMethods()) {

            if (AnnotationUtils.findAnnotation(method, RateLimit.class) != null) {
                hasRateLimit = true;
                break;
            }
        }

        if (!hasRateLimit || beanClass.getInterfaces().length == 0) {
            return bean;
        }

        return Proxy.newProxyInstance(
                beanClass.getClassLoader(),
                beanClass.getInterfaces(),
                new RateLimitInvocationHandler(bean));
    }

}