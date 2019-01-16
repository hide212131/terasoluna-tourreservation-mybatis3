package org.terasoluna.tourreservation.domain.repository.tourinfo

import static org.terasoluna.tourreservation.domain.util.DBUnitUtils.*

import javax.inject.Inject

import org.dbunit.operation.DatabaseOperation
import org.joda.time.DateTime
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.terasoluna.tourreservation.domain.service.tourinfo.TourInfoService

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration(locations = "classpath:test-context.xml")
@Transactional
@Rollback
class TourInfoRepositoryImplSpec extends Specification {

    @Inject
    TourInfoService tourInfoService

    @Inject
    NamedParameterJdbcTemplate jdbcTemplate;

    // 初期設定した上でwhereに使いたい値はここで@Share付きのインスタンス変数で宣言しておく
    @Shared String tourCode

    @Shared DateTime depDay

    @Shared DateTime plannedDay

    @Shared String tourAbs

    @Shared String tourName

    @Shared int basePrice

    def setupSpec() {
        // whereで使いたい値はここで設定する。変数は、@Share付きのインスタンス変数である必要がある。
        tourCode = "8888888888"
        depDay = new DateTime(2014, 2, 2, 0, 0, 0)
        plannedDay = new DateTime(2013, 12, 31, 0, 0, 0)
        tourAbs = "wonderful travel !"
        tourName = "test tour"
        basePrice = 20000
    }

    /**
     * BasePrice and TourDays dont set value Case
     */
    @Unroll
    def "TourInfoの検索でページが #totalPages のテスト" () {
        given: "初期データ投入"
        // DBUnitのデータ投入
        def connection = getConnection(jdbcTemplate)
        def setupDataSet = loadXls(xlsfile, this) // whereパラメータ
        def operation = DatabaseOperation.REFRESH
        operation.execute(connection, setupDataSet)

        // 初期オブジェクトの一部を設定
        // pageableはここで設定し、criteriaは where から設定する。
        def pageable = PageRequest.of(0, 10)

        when: "検索処理が実行された時（テスト対象の実行）"
        def page = tourInfoService.searchTour(criteria, pageable)
        then: "こうなるべき（assert）"
        page.totalPages == totalPages
        page.number == number

        when: "検索が１件以上あれば"
        if (page.totalPages == 0) return

        def tour = page.content.get(0)
        then: "こうなるべき（assert）"
        tour.avaRecMax == avaRecMax
        tour.basePrice == basePrice
        tour.conductor == conductor
        tour.depDay == depDay.toDate()
        tour.plannedDay == plannedDay.toDate()
        tour.tourAbs == tourAbs
        tour.tourCode == tourCode
        tour.tourDays == tourDays
        tour.tourName == tourName
        tour.accommodation.accomCode == accomCode
        tour.accommodation.accomName == accomName
        tour.accommodation.accomTel == accomTel
        tour.departure.depCode == depCode
        tour.departure.depName == depName

        where: "初期値・検索条件パターンと期待値"
        // ここで設定した配列の回数分、テストが繰り返される。
        // Spockの紹介サイトでよく見られるテーブル形式（区切り文字'|')は変数の数が多くなると見にくくなるので
        // 配列形式にしたほうが無難。
        // 注意： この処理はメソッド内で最初に呼ばれる。別の変数を参照する場合、あらかじめ、setupSpec()と@Shareで設定しておく
        //       必要がある。（例）depDay.toDate()
        // 初期値
        // DBUnitを用いてExcelファイルから初期DB値
        xlsfile << [
            "setup_TourInfoRepositoryImplTestDBUnit_1.xlsx" ,
            "setup_TourInfoRepositoryImplTestDBUnit_1.xlsx"
        ]
        // 検索条件パターン
        criteria << [
            new TourInfoSearchCriteria(depDate: depDay.toDate(),
            adultCount:1, arrCode:"01", basePrice:0, childCount:1, depCode:"01", tourDays:0),
            new TourInfoSearchCriteria(depDate: new DateTime(2012, 7, 10, 0, 0, 0).toDate(),
            adultCount:1, arrCode:"01", basePrice:10, childCount:1, depCode:"01", tourDays:2)
        ]
        // ここから期待値
        // ページ数と番号
        totalPages << [1, 0]
        number << [0, 0]
        // 検索結果
        avaRecMax << [2147483647 , _]
        conductor << ["1" , _]
        tourDays << [1 , _]
        accomCode << ["0001" , _]
        accomName << ["TERASOLUNAホテル第一荘" , _]
        accomTel << ["018-123-4567" , _]
        depCode << ["01" , _]
        depName << ["北海道" , _]
    }
}