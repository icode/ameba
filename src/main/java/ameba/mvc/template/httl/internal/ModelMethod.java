package ameba.mvc.template.httl.internal;

import ameba.message.filtering.EntityFieldsUtils;
import ameba.util.bean.BeanMap;

import java.util.Collection;

/**
 * @author icode
 */
public class ModelMethod {
    private ModelMethod() {
    }

    public static BeanMap filter(Object src) {
        return EntityFieldsUtils.filterRequestFields(src);
    }

    public static BeanMap[] filter(Object[] src) {
        return EntityFieldsUtils.filterRequestFields(src);
    }

    public static Collection filter(Collection src) {
        return EntityFieldsUtils.filterRequestFields(src);
    }

}
