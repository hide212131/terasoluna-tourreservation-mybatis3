package org.terasoluna.tourreservation.domain.service.customer;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.terasoluna.tourreservation.domain.util.DBUnitUtils.*

import javax.inject.Inject

import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.joda.time.DateTime
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.terasoluna.tourreservation.domain.model.Customer

import spock.lang.Specification

@ContextConfiguration(locations = "classpath:test-context.xml")
@Transactional
@Rollback
class CustomerServiceImplDBUnitSpec extends Specification {

    @Inject
    CustomerService customerService;

    @Inject
    NamedParameterJdbcTemplate jdbcTemplate;

    def "DB登録内容の確認"() {
        given: ""
        def connection = getConnection(jdbcTemplate)
        IDataSet databaseDataSet = connection.createDataSet();
        ITable before = databaseDataSet.getTable("customer");

        // シーケンス番号初期化
        jdbcTemplate.queryForList("select setval ('customer_code_seq', 11);", new HashMap())

        def c = new Customer(
                customerName: 'データ　八郎',
                customerKana: 'データ　ジュウロウ',
                customerPass: 'pass',
                customerBirth: new DateTime(1987, 5, 25, 0, 0, 0).toDate(),
                customerJob: '営業',
                customerMail: 'data5@example.com',
                customerTel: '123-1234-1234',
                customerPost: '135-8671' ,
                customerAdd: '東京都江東区豊洲3-3-9',
                );
        customerService.register(c, "foo");

        when:
        ITable after = databaseDataSet.getTable("customer");
        def actual = diff(before, after).inserted

        def expectedDataSet = loadXls('expect_insert_CustomerServiceImplDBUnitSpec.xlsx', this)
        def expected = expectedDataSet.getTable("customer");

        then:
        diff(actual, expected) == null
    }
}
