package com.example;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SpElUtil {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 解析 SpEL 表达式
     * @param method 目标方法
     * @param args   方法参数值
     * @param spELs  表达式数组
     * @return 解析后的值列表
     */
    public static List<String> parse(Method method, Object[] args, String[] spELs) {
        List<String> values = new ArrayList<>();
        EvaluationContext context = new MethodBasedEvaluationContext(null, method, args, DISCOVERER);

        for (String spEL : spELs) {
            if (!ObjectUtils.isEmpty(spEL)) {
                Object value = PARSER.parseExpression(spEL).getValue(context);
                values.add(ObjectUtils.nullSafeToString(value));
            }
        }
        return values;
    }
}
