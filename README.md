## DBUnit+Spock (+jdbc.resultsettable like output tools) on TERASOLUNA Sample Application

## Contents
See [changes (Added environment setting and sample of "Eclipse + DBUnit + Spock")](https://github.com/hide212131/terasoluna-tourreservation-mybatis3/commit/600311a7549ae56786398ca86c9691d704b7ec7c)

## Tools
[DBUnitUtils.java](https://github.com/hide212131/terasoluna-tourreservation-mybatis3/blob/600311a7549ae56786398ca86c9691d704b7ec7c/terasoluna-tourreservation-domain/src/test/java/org/terasoluna/tourreservation/domain/util/DBUnitUtils.java)

### jdbc.resultsettable like output
```
println toString(itable)

|customer_code|customer_name|     customer_kana|                                               customer_pass|customer_birth|customer_job|         customer_mail| customer_tel|customer_post|         customer_add|
|-------------|-------------|------------------|------------------------------------------------------------|--------------|------------|----------------------|-------------|-------------|---------------------|
|     00000001|   木村　太郎|    キムラ　タロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1975-01-05|  プログラマ|     tarou@example.com|111-1111-1111|     276-0022| 千葉県八千代市上高野|
|     00000002|   前田　五郎|    マエダ　ゴロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1980-12-13|      建設業|     gorou@example.com|222-2222-2222|     135-8671|東京都江東区豊洲3-3-9|
|     00000003|   鈴木　花子|    スズキ　ハナコ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1960-10-21|        主婦|                      |333-3333-3333|     616-0000|         京都市右京区|
|     00000004|   加藤　一郎|  カトウ　イチロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1965-03-12|      自営業|   irasyai@example.com|444-4444-4444|     039-1500|         三戸郡五戸町|
|     00000005| 田中　南都化|  タナカ　ナントカ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1970-06-30|      自営業|   nantoka@example.com|555-5555-5555|     039-1500|         三戸郡五戸町|
|     00000006|     渡辺　梟|ワタナベ　フクロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1966-11-03|      自営業|watanabehu@example.com|666-6666-6666|     135-8671|東京都江東区豊洲3-3-9|
|     00000007| データ　太郎|    データ　タロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1970-04-05|        営業|     data1@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|     00000008| データ　次郎|    データ　ジロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1972-05-15|        営業|     data2@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|     00000009| データ　三郎|  データ　サブロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1974-06-02|        営業|     data3@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|     00000010| データ　四郎|    データ　シロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1976-12-30|        営業|     data4@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|     00000011| データ　五郎|    データ　ゴロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|    1978-05-25|        営業|     data5@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
```

### Two ITable's diff
```
println diff(before, after)

|CUD|customer_code|           customer_name|                   customer_kana|                                               customer_pass|          customer_birth|customer_job|    customer_mail| customer_tel|customer_post|         customer_add|
|---|-------------|------------------------|--------------------------------|------------------------------------------------------------|------------------------|------------|-----------------|-------------|-------------|---------------------|
| D |     00000009|            データ　三郎|                データ　サブロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|              1974-06-02|        営業|data3@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
| D |     00000010|            データ　四郎|                  データ　シロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|              1976-12-30|        営業|data4@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
| C |     00000012|            データ　八郎|              データ　ジュウロウ|                                                    password|              1987-05-25|        営業|data5@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
| U |     00000002|              前田　五郎|                  マエダ　ゴロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|1980-12-13 >> 2012-07-10|      建設業|gorou@example.com|222-2222-2222|     135-8671|東京都江東区豊洲3-3-9|
| U |     00000001|木村　太郎 >> 木村　次郎|キムラ　タロウ >> キムラ　ジロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|              1975-01-05|  プログラマ|tarou@example.com|111-1111-1111|     276-0022| 千葉県八千代市上高野|
```

### with Spock report
```
in where clause:

diff(actual.inserted, expectedInsert) == null
|    |      |         |               |
|    |      |         |               false
|    |      |          
|    |      |         |customer_code|customer_name|     customer_kana|customer_pass|customer_birth|customer_job|    customer_mail| customer_tel|customer_post|         customer_add|
|    |      |         |-------------|-------------|------------------|-------------|--------------|------------|-----------------|-------------|-------------|---------------------|
|    |      |         |     00000012| データ　八郎|データ　ジュウロウ|     password|    1987-05-26|        営業|data8@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|    |       
|    |      |customer_code|customer_name|     customer_kana|customer_pass|customer_birth|customer_job|    customer_mail| customer_tel|customer_post|         customer_add|
|    |      |-------------|-------------|------------------|-------------|--------------|------------|-----------------|-------------|-------------|---------------------|
|    |      |     00000012| データ　八郎|データ　ジュウロウ|     password|    1987-05-25|        営業|data5@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|     
|    |CUD|customer_code|           customer_name|                   customer_kana|                                               customer_pass|          customer_birth|customer_job|    customer_mail| customer_tel|customer_post|         customer_add|
|    |---|-------------|------------------------|--------------------------------|------------------------------------------------------------|------------------------|------------|-----------------|-------------|-------------|---------------------|
|    | D |     00000009|            データ　三郎|                データ　サブロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|              1974-06-02|        営業|data3@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|    | D |     00000010|            データ　四郎|                  データ　シロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|              1976-12-30|        営業|data4@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|    | C |     00000012|            データ　八郎|              データ　ジュウロウ|                                                    password|              1987-05-25|        営業|data5@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
|    | U |     00000002|              前田　五郎|                  マエダ　ゴロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|1980-12-13 >> 2012-07-10|      建設業|gorou@example.com|222-2222-2222|     135-8671|東京都江東区豊洲3-3-9|
|    | U |     00000001|木村　太郎 >> 木村　次郎|キムラ　タロウ >> キムラ　ジロウ|$2a$12$Jfwcv/ZpfE0QjVlLT9CB9eqTLrYdAsvfGxXKvRMlpkfEn.9Uirgou|              1975-01-05|  プログラマ|tarou@example.com|111-1111-1111|     276-0022| 千葉県八千代市上高野|
 
|CUD|customer_code|customer_name|     customer_kana|customer_pass|          customer_birth|customer_job|                         customer_mail| customer_tel|customer_post|         customer_add|
|---|-------------|-------------|------------------|-------------|------------------------|------------|--------------------------------------|-------------|-------------|---------------------|
| U |     00000012| データ　八郎|データ　ジュウロウ|     password|1987-05-25 >> 1987-05-26|        営業|data5@example.com >> data8@example.com|123-1234-1234|     135-8671|東京都江東区豊洲3-3-9|
```


## Tour Reservation Sample Application
This is a reference application built completely using TERASOLUNA Server Framework for Java (5.x) ([http://terasoluna.org](http://terasoluna.org "http://terasoluna.org")).

This application shows **how an IDEAL project configuration and package structure must be like.** It also shows working sample of best practices recommended in TERASOLUNA Server Framework for Java (5.x) Development Guideline.

**This sample uses MyBatis3.**

[![Build Status for master](https://travis-ci.org/terasolunaorg/terasoluna-tourreservation-mybatis3.svg?branch=master)](https://travis-ci.org/terasolunaorg/terasoluna-tourreservation-mybatis3)

### Getting started

#### Download

Download source code from [here](https://github.com/terasolunaorg/terasoluna-tourreservation-mybatis3/releases "here").
Extract the zip file at any location of choice.

#### Run PostgreSQL

Install and start PostgreSQL.
select 'P0stgres' as password for db user or select any password of choice. Be sure to remember the password. 
If 'P0stgres' is not used, some changes will be required in configuration files. Hence be sure to remember it.

### Run PostgreSQL

Install and start PostgreSQL.

create database 'tourreserve'.

#### Insert test data

It is assumed that maven is already installed.
Move to the directory where the downloaded source-code is unzipped.
If password of db user is set to 'P0stgres' its not required to edit any file and directly execute the below command.
If it is set to any other password, then update the password in `terasoluna-tourreservation-initdb/pom.xml`.

Execute the below command:

```console
$ mvn -f terasoluna-tourreservation-initdb/pom.xml sql:execute
```

Test data is currently available in Japanese only.

#### Install jars and build war

If db user password is not set to 'P0stgres', then go to `terasoluna-tourreservation-env/src/main/resources/META-INF/spring/terasoluna-tourreservation-infra.properties` and update the password. If it is set to 'P0stgres', no changes are required.

```console
$ mvn clean install
```

#### Run server and deploy war

Deploy `terasoluna-tourreservation-web/target/terasoluna-tourreservation-web.war` to your Application server (e.g. Tomcat8)

You can also use `mvn cargo:run` to test this application quickly with option `MAVEN_OPTS=-XX:MaxPermSize=256m` in environment variable.

```console
$ mvn -f terasoluna-tourreservation-web/pom.xml cargo:run
```

access [http://localhost:8080/terasoluna-tourreservation-web/](http://localhost:8080/terasoluna-tourreservation-web/)

Alternatively, these project can also be imported into Eclipse and application can be run using WTP.

#### Test with selenium

Install Firefox to run test.

Run test.

```console
$ mvn -f terasoluna-tourreservation-selenium/pom.xml clean test
```
