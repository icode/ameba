package ameba;

import ameba.db.dsl.QueryDSL;
import ameba.db.dsl.QueryExprMeta;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author icode
 */
public class QueryExprMetaParserTest {

    private static final Logger logger = LoggerFactory.getLogger(QueryExprMetaParserTest.class);

    @Test
    public void parseTest() {
        String expr = "x1.x2.w.not." +
                "  or(" +
                "    a_1_1." +
                "      b_1_2(" +
                "        j(h.n.m.b()m(1))" +
                "      )eq1(asd)dd.w.eq4(asd344)" +
                "    d_1_3." +
                "      in(" +
                "         1!2!3!4" +
                "      )x.eq(m.call)eq2(w.d.w.a.cal)" +
                "  )" +
                "xx.dd.dd." +
                "  ww(" +
                "    ddddd!'nil'!nil!12343!2ww!errf" +
                "  )l.w2.f.g.or";
        List<QueryExprMeta> queryExprMetaList = QueryDSL.parse(expr);
        logger.debug(
                StringUtils.join(Collections2.transform(queryExprMetaList, new Function<QueryExprMeta, String>() {
                    @Override
                    public String apply(QueryExprMeta input) {
                        return String.valueOf(input);
                    }
                }), ""));
    }
}
