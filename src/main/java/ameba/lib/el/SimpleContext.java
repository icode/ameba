package ameba.lib.el;

import javax.el.*;

/**
 * @author icode
 */
public class SimpleContext extends ELContext {
    private static final ELResolver DEFAULT_RESOLVER = new CompositeELResolver() {
        {
            this.add(new ArrayELResolver(true));
            this.add(new ListELResolver(true));
            this.add(new MapELResolver(true));
            this.add(new ResourceBundleELResolver());
            this.add(new BeanELResolver(true));
        }
    };
    private final MapBasedFunctionMapper functions;
    private final VariableMapper variableMapper;
    private final ELResolver resolver;

    public SimpleContext(ExpressionFactory expressionFactory) {
        this.putContext(ExpressionFactory.class, expressionFactory);
        this.functions = new MapBasedFunctionMapper();
        this.variableMapper = new MapBasedVariableMapper();
        this.resolver = DEFAULT_RESOLVER;
    }

    @Override
    public ELResolver getELResolver() {
        return resolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return functions;
    }

    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper;
    }
}
