/*
 ReserveRepositoryImplTest.java * Copyright (C) 2013-2018 NTT DATA Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.terasoluna.tourreservation.domain.repository.reserve;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.terasoluna.tourreservation.domain.util.DBUnitUtils.*

import javax.inject.Inject

import org.dbunit.operation.DatabaseOperation
import org.joda.time.DateTime
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.terasoluna.tourreservation.domain.model.TourInfo

import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration(locations = "classpath:test-context.xml")
@Transactional
@Rollback
class ReserveRepositoryImplSpec extends Specification {

    @Inject
    NamedParameterJdbcTemplate jdbcTemplate;

    @Inject
    ReserveRepository reserveRepository;

    @Unroll
    def testFindWithDetail01() {
        given:
        // DBUnitのデータ投入
        def xls = loadXls('setup_ReserveRepositoryImplSpec_1.xlsx', this)
        def connection = getConnection(jdbcTemplate)
        def operation = DatabaseOperation.REFRESH
        operation.execute(connection, xls)


        def tourCode = "0000000001";
        def reservedDay = new DateTime(2016, 1, 1, 0, 0, 0);
        def tourInfo = new TourInfo();
        tourInfo.setTourCode(tourCode);
        def customerCode = "00000001";
        def reserveNo = "10000000";

        when:
        def reserve = reserveRepository.findOneWithDetail(reserveNo);

        then:
        reserve != null
        reserve.reserveNo == reserveNo
        reserve.reservedDay == reservedDay.toDate()
        reserve.adultCount == 1
        reserve.childCount == 2
        reserve.transfer == "0"
        reserve.sumPrice == 1000
        reserve.remarks == "TEST"
        reserve.customer != null
        reserve.customer.customerCode == customerCode
        reserve.tourInfo != null
        reserve.tourInfo.tourCode == tourCode
    }

}
