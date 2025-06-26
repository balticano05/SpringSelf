package my.project.regex;

import my.project.exception.InvalidFieldValueException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

@Component
public class ValidateRegexAnnotationBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field : fields) {

            if (field.isAnnotationPresent(ValidateRegex.class) && field.getType() == String.class) {

                String regex = field.getAnnotation(ValidateRegex.class).value();
                Pattern pattern = Pattern.compile(regex);

                field.setAccessible(true);

                try {

                    Object fieldValue = field.get(bean);

                    if (fieldValue == null) {
                        throw new InvalidFieldValueException("Field '" + field.getName() + "' in bean '" + beanName + "' is null");
                    }

                    String value = fieldValue.toString();

                    if (!pattern.matcher(value).matches()) {
                        throw new InvalidFieldValueException("Field '" + field.getName() + "' value '" + value + "' does NOT match regex '" + regex + "'");
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        return bean;
    }

}