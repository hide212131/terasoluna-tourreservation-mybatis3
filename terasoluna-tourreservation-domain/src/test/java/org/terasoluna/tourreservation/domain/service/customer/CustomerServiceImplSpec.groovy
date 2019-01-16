package org.terasoluna.tourreservation.domain.service.customer;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.terasoluna.tourreservation.domain.model.Customer;
import org.terasoluna.tourreservation.domain.repository.customer.CustomerRepository;

import spock.lang.Specification

class CustomerServiceImplSpec extends Specification {

    CustomerServiceImpl customerService;

    CustomerRepository customerRepository;

    /*
     * https://qiita.com/aya_eiya/items/d12f72b16fcd1d619192
     *
     * テストを通じて振る舞いが変わらないMock対象
     * setupSpecでMockオブジェクトを用意する
     * 同様にsetupSpecで振る舞いまで定義する
     * ことによって、テスト全体の前提となるMock定義を一箇所で管理する
     *
     * それぞれのテストにおいて振る舞いを変える必要のあるMock対象
     * setupでMockオブジェクトを用意する
     * 各テストメソッドのsetupまたはwhenで動作を定義する
     * ことによって、whereによるDataDrivenなテストをやりやすくする
     */
    def setup() {
        customerService = new CustomerServiceImpl();
        customerRepository = Mock();
        customerService.customerRepository = customerRepository;
        customerService.passwordEncoder = new BCryptPasswordEncoder();
    }


    def testFindOne01() {
        given:
        def c = new Customer()
        customerRepository.findOne("xxx") >> c

        when:
        def result = customerService.findOne("xxx")

        then:
        result == c
    }

    def testFindOne02() {
        given:
        customerRepository.findOne("xxx") >> null

        when:
        def result = customerService.findOne("xxx")

        then:
        result == null
    }

    def testRegister01() {
        given:
        def c = new Customer();

        when:
        customerService.register(c, "foo");

        then: "insertメソッドが1回は呼ばれること"
        1 * customerRepository.insert(c)
    }

}
