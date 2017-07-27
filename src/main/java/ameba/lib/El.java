package ameba.lib;

import ameba.lib.el.MapBasedFunctionMapper;
import ameba.lib.el.SimpleContext;
import ameba.util.bean.BeanInvoker;
import ameba.util.bean.BeanMap;

import javax.el.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author icode
 */
public class El {
    private static final ExpressionFactory expressionFactory = ExpressionFactory.newInstance();

    public static String parse(String text, ELContext context, Object root, String prefix, Class... funcClasses) {
        addVariables(context, new PropBeanMap<>(root));
        Stream.of(funcClasses).forEach(fn -> addFunctions(context, prefix, fn));
        return parse(text, context);
    }

    private El() {
    }

    public static void addVariables(ELContext elContext, Map<String, Object> variables) {
        VariableMapper mapper = elContext.getVariableMapper();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();
            ValueExpression valueExpression = expressionFactory
                    .createValueExpression(value, value != null ? value.getClass() : Object.class);
            mapper.setVariable(entry.getKey(), valueExpression);
        }
    }

    public static void addFunctions(ELContext elContext, String prefix, Map<String, Method> functions) {
        FunctionMapper mapper = elContext.getFunctionMapper();
        if (mapper instanceof MapBasedFunctionMapper && functions != null && !functions.isEmpty()) {
            MapBasedFunctionMapper functionMapper = (MapBasedFunctionMapper) mapper;
            functions.forEach((s, method) -> functionMapper.setFunction(prefix, s, method));
        }
    }

    public static void addFunctions(ELContext elContext, String prefix, Class funcClass) {
        if (funcClass != null) {
            addFunctions(
                    elContext,
                    prefix,
                    getFunctions(funcClass)
            );
        }
    }

    public static Map<String, Method> getFunctions(Class funcClass) {
        return Stream.of(funcClass.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .collect(Collectors.toMap(Method::getName, m -> m));
    }

    public static String parse(String text, Object root, String prefix, Class... funcClasses) {
        return parse(text, createContext(), root, prefix, funcClasses);
    }

    public static String parse(String text, Object root) {
        return parse(text, createContext(), root, null);
    }

    public static String parse(String text, ELContext context, Object root,
                               String prefix, Map<String, Method> functions) {
        addVariables(context, new PropBeanMap<>(root));
        addFunctions(context, prefix, functions);
        return parse(text, context);
    }

    public static String parse(String text, Object root, String prefix, Map<String, Method> functions) {
        ELContext context = createContext();
        addVariables(context, new PropBeanMap<>(root));
        addFunctions(context, prefix, functions);
        return parse(text, context);
    }

    private static class PropBeanMap<T> extends BeanMap<T> {
        public PropBeanMap(T bean) {
            super(bean);
        }

        @Override
        protected Object transform(BeanInvoker invoker) throws Throwable {
            return invoker.invoke();
        }
    }

    public static String parse(String text, Object root, Map<String, Method> functions) {
        return parse(text, createContext(), root, "", functions);
    }

    public static String parse(String text, ELContext context) {
        return (String) expressionFactory
                .createValueExpression(context, text, String.class)
                .getValue(context);
    }

    public static ELContext createContext() {
        return new SimpleContext(expressionFactory);
    }
}
