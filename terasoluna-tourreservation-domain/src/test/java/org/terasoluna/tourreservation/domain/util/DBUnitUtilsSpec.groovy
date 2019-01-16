package org.terasoluna.tourreservation.domain.util;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.terasoluna.tourreservation.domain.util.DBUnitUtils.*

import javax.inject.Inject

import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.operation.DatabaseOperation
import org.joda.time.DateTime
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional

import spock.lang.Specification

@ContextConfiguration(locations = "classpath:test-context.xml")
@Transactional
@Rollback
class DBUnitUtilsSpec extends Specification {

    @Inject
    NamedParameterJdbcTemplate jdbcTemplate;

    def "DB登録内容の確認"() {
        given: "変更前のテーブル取り出し"

        def connection = getConnection(jdbcTemplate)

        // 変更前データ
        IDataSet databaseDataSet = connection.createDataSet();
        ITable before = databaseDataSet.getTable("customer");

        // データ投入
        def setupxlsfile = 'setup_and_expect_insert_Customer.xlsx'
        def setupDataSet = loadXls(setupxlsfile, this)
        DatabaseOperation operation =  DatabaseOperation.REFRESH
        operation.execute(connection, setupDataSet)

        String q;
        Map<String, Object> param;

        q = "UPDATE customer SET customer_name = :customerName, customer_kana = :customerKana WHERE customer_code = :customerCode";
        param = new HashMap<String, Object>();
        param.put("customerCode", "00000001");
        param.put("customerName", "木村　次郎");
        param.put("customerKana", "キムラ　ジロウ");
        jdbcTemplate.update(q, param);

        q = "UPDATE customer SET customer_birth = :customerBirth WHERE customer_code = :customerCode";
        param = new HashMap<String, Object>();
        param.put("customerCode", "00000002");
        param.put("customerBirth", new DateTime(2012, 7, 10, 0, 0, 0).toDate());
        jdbcTemplate.update(q, param);

        q = "DELETE FROM customer WHERE customer_code = :customerCode";
        param = new HashMap<String, Object>();
        param.put("customerCode", "00000009");
        jdbcTemplate.update(q, param);

        q = "DELETE FROM customer WHERE customer_code = :customerCode";
        param = new HashMap<String, Object>();
        param.put("customerCode", "00000010");
        jdbcTemplate.update(q, param);

        // 変更後データ
        ITable after = databaseDataSet.getTable("customer");

        // 差分
        def actual = diff(before, after)
        println "差分 ${actual}"

        when:
        // 期待値
        def xlsfile = 'setup_and_expect_insert_Customer.xlsx'
        def expectedDataSet = loadXls(xlsfile, this)
        def expectedInsert = expectedDataSet.getTable("customer");

        then:
        diff(actual.inserted, expectedInsert) == null
        //diff(actual.deleted, expectedDelete) == null
        //diff(actual.updated, expectedUpdate) == null
    }
}
