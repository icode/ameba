package ameba.db.ebean.transaction;

import ameba.db.DataSource;
import ameba.db.TransactionInterceptor;
import ameba.db.annotation.Transactional;
import ameba.db.model.ModelManager;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxIsolation;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;


/**
 * Ebean事务拦截器
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
public class EbeanTransactional extends TransactionInterceptor {

    @Context
    private ResourceInfo resourceInfo;

    private Transaction[] transactions;

    @Override
    protected void begin() {
        Transactional transactional = resourceInfo.getResourceMethod().getAnnotation(Transactional.class);
        String[] serverNames = transactional.servers();
        if (serverNames.length > 0) {
            transactions = new Transaction[serverNames.length];
            Transactional.Isolation[] isolation = transactional.isolations();
            for (int i = 0; i < serverNames.length; i++) {
                TxIsolation txIsolation = isolation.length > i ? TxIsolation.fromLevel(isolation[i].getLevel()) : TxIsolation.DEFAULT;
                transactions[i] = Ebean.getServer(serverNames[i]).beginTransaction(txIsolation);
            }
        } else {
            transactions = new Transaction[]{Ebean.getServer(DataSource.getDefaultDataSourceName()).beginTransaction()};
        }
    }

    @Override
    protected void commit() {
        for (Transaction transaction : transactions)
            transaction.commit();
    }

    @Override
    protected void rollback() {
        for (Transaction transaction : transactions)
            transaction.rollback();
    }

    @Override
    protected void end() {
        for (Transaction transaction : transactions)
            transaction.end();
    }
}
