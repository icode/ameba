package ameba;

import ameba.db.dsl.QueryDSL;
import ameba.db.dsl.QueryExpr;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author icode
 */
public class QueryExprParserTest {

    private static final Logger logger = LoggerFactory.getLogger(QueryExprParserTest.class);

    @Test
    public void parseTest() {
        String expr = "eeex1.x2.w.not." +
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
                "    ddddd!'nil'!12343!2ww!errf" +
                "  )l.w2.f.g.or";
        List<QueryExpr> queryExprList = QueryDSL.parse(expr);
        logger.debug(
                StringUtils.join(Collections2.transform(queryExprList, new Function<QueryExpr, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable QueryExpr input) {
                        return String.valueOf(input);
                    }
                }), ""));
    }
}
