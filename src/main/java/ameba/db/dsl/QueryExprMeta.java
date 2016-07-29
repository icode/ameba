package ameba.db.dsl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * @author icode
 */
public class QueryExprMeta {
    private String field;
    private String operator;
    private List<Val<?>> arguments;
    private QueryExprMeta parent;

    private QueryExprMeta() {
    }

    public static QueryExprMeta create() {
        return new QueryExprMeta();
    }

    public String operator() {
        return operator;
    }

    public String field() {
        return field;
    }

    public List<Val<?>> arguments() {
        return arguments;
    }

    public QueryExprMeta parent() {
        return parent;
    }

    protected QueryExprMeta field(String field) {
        this.field = field;
        return this;
    }

    protected QueryExprMeta operator(String operator) {
        this.operator = operator;
        return this;
    }

    protected QueryExprMeta arguments(List<Val<?>> arguments) {
        this.arguments = arguments;
        return this;
    }

    protected QueryExprMeta arguments(Val<?>... arguments) {
        if (this.arguments == null) this.arguments = Lists.newArrayList();
        Collections.addAll(this.arguments, arguments);
        return this;
    }

    protected QueryExprMeta arguments(QueryExprMeta meta) {
        if (this.arguments == null) this.arguments = Lists.newArrayList();
        this.arguments.add(Val.of(meta));
        return this;
    }

    protected QueryExprMeta parent(QueryExprMeta parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public String toString() {
        return (field == null ? "" : field + ".") +
                operator + "(" +
                (arguments == null ? "" :
                        StringUtils.join(
                                Collections2.transform(arguments,
                                        new Function<Val, String>() {
                                            @Nullable
                                            @Override
                                            public String apply(@NotNull Val input) {
                                                if (input.object() == null) {
                                                    return "nil";
                                                } else if (input.object() instanceof String) {
                                                    return "'" + input.string() + "'";
                                                }
                                                return input.object().toString();
                                            }
                                        }), "!"
                        )
                ) +
                ")";
    }

    public static class Val<E> {
        private Object value;

        Val(Object value) {
            this.value = value;
        }

        public static <E> Val<E> of() {
            return new Val<>(null);
        }

        public static <E> Val<E> of(String value) {
            return new Val<>(value);
        }

        public static <E> Val<E> of(QueryExprMeta value) {
            return new Val<>(value);
        }

        public static <E> Val<E> of(E value) {
            return new Val<>(value);
        }

        public static <E> Val<E> ofDecimal(String s) {
            return new Val<>(new BigDecimal(s));
        }

        public static <E> Val<E> ofBool(String s) {
            return new Val<>("true".equals(s));
        }

        public Double doubleV() {
            return ((BigDecimal) value).doubleValue();
        }

        public Float floatV() {
            return ((BigDecimal) value).floatValue();
        }

        public Long longV() {
            return ((BigDecimal) value).longValue();
        }

        public Integer integer() {
            return ((BigDecimal) value).intValue();
        }

        public Boolean bool() {
            if (value instanceof Boolean) {
                return ((Boolean) value);
            } else if (value instanceof BigDecimal) {
                return BooleanUtils.toBooleanObject(integer());
            }
            return null;
        }

        public String string() {
            return (String) value;
        }

        public QueryExprMeta meta() {
            return (QueryExprMeta) value;
        }

        public <T extends Enum<T>> T enumV(Class<T> eClass) {
            if (value instanceof BigDecimal) {
                return eClass.getEnumConstants()[integer()];
            } else if (value instanceof String) {
                return Enum.valueOf(eClass, string());
            }
            throw new IllegalArgumentException(
                    "No enum constant " + eClass.getCanonicalName()
            );
        }

        @SuppressWarnings("unchecked")
        public E expr() {
            return (E) value;
        }

        public Object object() {
            return value;
        }
    }
}