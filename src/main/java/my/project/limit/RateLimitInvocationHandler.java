package my.project.limit;

import my.project.exception.RateLimitExceededException;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitInvocationHandler implements InvocationHandler {

    private final Object target;
    private final ConcurrentHashMap<Method, AtomicInteger> counters;

    public RateLimitInvocationHandler(Object target) {
        this.target = target;
        this.counters = new ConcurrentHashMap<>();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        RateLimit rateLimit = AnnotationUtils.findAnnotation(targetMethod, RateLimit.class);

        if (rateLimit != null) {

            AtomicInteger counter = counters.computeIfAbsent(targetMethod, m -> new AtomicInteger(0));

            int currentCount = counter.incrementAndGet();

            if (currentCount > rateLimit.count()) {
                throw new RateLimitExceededException(
                        "Rate limit exceeded for method " + method.getName() +
                                ". Max allowed: " + rateLimit.count());
            }

        }

        return method.invoke(target, args);
    }

}