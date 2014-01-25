package ameba.db.ebean.transaction;

import ameba.db.TransactionFilter;
import com.avaje.ebean.Ebean;


/**
 * Ebean事务拦截器
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
public class EbeanTransactional extends TransactionFilter {
    @Override
    protected void begin() {
        Ebean.beginTransaction();
    }

    @Override
    protected void commit() {
        Ebean.commitTransaction();
    }

    @Override
    protected void rollback() {
        Ebean.rollbackTransaction();
    }

    @Override
    protected void end() {
        Ebean.endTransaction();
    }
}
