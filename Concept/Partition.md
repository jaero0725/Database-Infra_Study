## 📋 Oracle Partition Table 
### Partition
![image](https://user-images.githubusercontent.com/55049159/217259224-b6656c55-7b15-4fd8-8d08-e53abbdef084.png)
 - 오라클 파티션테이블은 하나의 큰 테이블을 물리적으로 나눠놓은 것입니다.
 - 물리적으로 나눠놨지만, 논리적으로는 하나의 테이블로 간주됩니다.
 - 왼쪽 그림처럼 Sales 테이블에 1월데이터부터 5월데이터까지 하나의 통테이블에 몰아서 넣을수 있습니다.
 - 하지만, 파티션테이블을 사용하게되면 오른쪽 그림처럼 월마다 다른 세그먼트에 Sales 데이터를 나눠서 넣을 수 있습니다.
 - 이렇게 나눠서 넣어놔도 사용자는 1월~5월치 데이터가 마치 하나의 Sales 테이블에 들어있는 것처럼 사용할 수 있습니다.
 - 오라클에서는 Object, Segment 라는 개념을 사용합니다. 저장공간을 가지는 개념이 Segment 에 해당합니다.
 - 즉, 위 파티션된 Sales 테이블에서는 5개의 Segment 에 데이터가 나뉘어 들어가게 됩니다.
 - 각각 세그먼트가 다르기 때문에 1월데이터만 압축해서 보관하거나, 5월데이터만 좀더 빠른 디스크에 저장할 수 있습니다.
 
 #### 사용이유 
 -  1) 가용성 증가 : 특정 block 깨진 경우
 -  2) 관리비용 감소 : 특정 Partition만 작업하는 경우
 -  3) 성능 향상 : 해당 파티션만 scan 하여 성능향상됨
 * Block 경합 방지 *Hot spot의 분산, Online)
 - Hot Block 방지 : 한 블록에 Insert, select 가 몰리는 경우 Hash Partition을 적용함으로 한 블럭에 집중되는 부하를 여러 개의 블록으로 분산할 수 있음
 * 해당 파티션만 Truncate (관리, 성능 측면)
 
 * Partition Pruning 
 - Index가 없어도 조건절에 Partition key가 들어오면, 특정 파티션만 scan 함. 파티셔닝이 되어 있찌 않으면 전체 테이블 Full scan이 진행됨
 
 #### 목적
 1) 관리의 편의성
 1월 데이터만 삭제할때, 파티션이 없는 경우 전체 데이터를 읽어 건건히 DELETE 를 해준다.
 하지만, 파티션을 월별로 생성한 경우(Range Partition) Truncate, Drop을 해주면 빠르게 작업을 처리할 수 있다.
 관리가 편해진다.
 
 2) 성능
 인덱스를 사용안해서 Full scan 할경우 예를 들어 5월 4일 을 찾는다 할 경우 파티션 없으면 전체 데이터 조회하므로 느려진다. 
 하지만 파티션 테이블에서는 5월 만 조회하여 찾기 떄문에 빠르게 작업을 할 수 있다. 
 
 Hash 파티션은 Range, List 파티션과 다르게 성능 보다는 Dist I/O 분산 효과를 노린다고 한다. 
 
 #### 파티션 Key 
 파티션을 나눌때 기준이 되는 키컬럼(Key Column)을 파티션키라고 부릅니다. 당연히 테이블에 있는 컬럼들 중에서 선택됩니다.
 파티션키로는 Lob을 제외한 Number, Date, Varchar 타입이 모두 가능하고, 여러개의 컬럼으로 구성할 수도 있습니다.
 
 #### 파티션을 사용하는 경우?, 언제 파티션 테이블을 사용해야되나? 
 1)  2GB 이상인 테이블
 - 최소한 크기가 2GB 이상의 테이블의 경우 파티션을 사용하는 것을 고려해보기
 
 2) 이력 데이터 
 - 로그 데이터 History 데이터일 경우 파티션을 사용해 보는 것을 고려.. 지속적으로 계속 늘어나는 데이터면서 과거 데이터가 변경이 없는 경우
 
 3) 테이블의 데이터를 여러종류의 스토리지에 나눠서 저장할 필요가 있는 경우
 - 자주 쓰는 데이터는 고가의 빠른 스토리지
 - 잘 안쓰는 데이터는 저가의 느린 스토리지
 


 ## 파티션의 종류 
 ### 1) Range Partition
 - Range Partition 은 범위로 구분되는 파티션 테이블 입니다.
 - 범위(Range)에는 숫자, 날짜, 문자가 모두 가능합니다.

``` sql

   create table SALES (
    sales_no       number,
    sale_year      number,
    sale_month     number,
    sale_day       number,
    customer_name  varchar2(30),
    birth_date     date,
    price          number
  )
  partition by range (sales_no)
  (
    partition SALES_P1 values less than (3),
    partition SALES_P2 values less than (5),
    partition SALES_P3 values less than (maxvalue)
  );
  
  -- 나는 이렇게 사용함 

  CREATE TABLE ZEROCO.TB_ZERO2179S(
    PRC_DT  CHAR(8) NOT NULL
    ...
  )
  TABLE SPACE TSD_ZEROCO01
  NOLOGGING
  PARTITON BY RANGE(PRC_DT)
  (
    PARTITON TSD_Z202201 VALUES LESS THAN ('202201')
    TABLESPACE TSD_ZERCO01
    STORAGE
    (
      INITIAL 64K
      NEXT 1M
    ),
    ...
  );
  CREATE INDEX ...
  (
  ...
  ) LOCAL
  ...

```

 
### 2) List Partition
- List Partition 은 범위가 아닌 특정한 값으로 구분되는 파티션 테이블 입니다.
- 주로 특정 구분자로 데이터의 구분이 가능한 경우 사용합니다.

``` sql
create table SALES (
  sales_no       number,
  sale_year      number,
  sale_month     number,
  sale_day       number,
  customer_name  varchar2(30),
  birth_date     date,
  price          number,
  state          varchar2(2)
)
partition by list (state)
(
  partition P_EAST    values ('MA','NY','CT','NH','ME','MD','VA','PA','NJ'),
  partition P_WEST    values ('CA','AZ','NM','OR','WA','UT','NV','CO'),
  partition P_SOUTH   values ('TX','KY','TN','LA','MS','AR','AL','GA'),
  partition P_CENTRAL values ('OH','ND','SD','MO','IL','MI','IA')
);

-- 4개 파티션 생성 

insert into SALES values (1, 2004, 05, 02, 'Sophia', to_date('19740502','yyyymmdd'), 65000, 'WA');
insert into SALES values (2, 2005, 03, 02, 'Emily',  to_date('19750302','yyyymmdd'), 23000, 'OR');
insert into SALES values (3, 2006, 08, 02, 'Olivia', to_date('19760802','yyyymmdd'), 34000, 'TX');
insert into SALES values (4, 2007, 02, 02, 'Amelia', to_date('19770202','yyyymmdd'), 12000, 'CA');
insert into SALES values (5, 2008, 04, 02, 'Chloe',  to_date('19780402','yyyymmdd'), 55000, 'FL');

-- 이렇게 하면 에러남 ORA-14400


create table SALES (
  sales_no       number,
  sale_year      number,
  sale_month     number,
  sale_day       number,
  customer_name  varchar2(30),
  birth_date     date,
  price          number,
  state          varchar2(2)
)
partition by list (state) 
(
  partition P_EAST    values ('MA','NY','CT','NH','ME','MD','VA','PA','NJ'),
  partition P_WEST    values ('CA','AZ','NM','OR','WA','UT','NV','CO'),
  partition P_SOUTH   values ('TX','KY','TN','LA','MS','AR','AL','GA'),
  partition P_CENTRAL values ('OH','ND','SD','MO','IL','MI','IA'),
  partition P_NULL    values (null),
  partition P_UNKNOWN values (default) -- LIST default 해줘야됨
);

``` 
### 3) Hash Partition
- Hash Partition 은 해시함수에 의해 자동으로 파티션 갯수만큼 데이터가 분할되는 파티션 테이블 입니다.
- 해시 파티션키로 사용할 수 있는 컬럼은 아무 타입이나 가능합니다. 숫자, 문자, 날짜 타입 모두 다 가능합니다.
- Range 나 List 파티션과 달리 Hash 파티션의 경우에는 내 데이터가 어느 파티션으로 들어갈 지 알 수 없기 때문에,
- 전혀 관리 목적에는 맞지 
- Hash 파티션을 사용하는 이유는 데이터를 여러 위치에 분산배치해서 Disk I/O 성능을 개선하기 위함입니다.
- 스토리지의 특정 위치에 I/O 가 몰리는 현상을 핫블럭(Hot Block) 현상이라고 하는데, 이때 Reverse Index 와 함께 Hash Partition 이 해결책!

``` sql


create table SALES (
  sales_no       number,
  sale_year      number,
  sale_month     number,
  sale_day       number,
  customer_name  varchar2(30),
  birth_date     date,
  price          number,
  state          varchar2(2)
)
partition by hash (birth_date) 
partitions 4; -- 파티션 4개

~ 파티션 생성시 파티션 명 지정할 경우?
partition by hash (sales_no) 
(
  partition SALES_P1, 
  partition SALES_P2,
  partition SALES_P3,
  partition SALES_P4
);

insert into SALES values (1, 2004, 05, 02, 'Sophia', to_date('19740502','yyyymmdd'), 65000, 'WA');
insert into SALES values (2, 2005, 03, 02, 'Emily',  to_date('19750302','yyyymmdd'), 23000, 'OR');
insert into SALES values (3, 2006, 08, 02, 'Olivia', to_date('19760802','yyyymmdd'), 34000, 'TX');
insert into SALES values (4, 2007, 02, 02, 'Amelia', to_date('19770202','yyyymmdd'), 12000, 'CA');
insert into SALES values (5, 2008, 04, 02, 'Chloe',  to_date('19780402','yyyymmdd'), 55000, 'FL');
```

## 오라틀 파티션 인덱스
1) Local Partitioned Index (로컬 파티션 인덱스)
- 로컬 인덱스는 파티션테이블과 똑같은 구성으로 인덱스를 파티션하는 것
- 즉, 인덱스의 파티션을 구분하는 파티션키를 테이블의 파티션키와 같은 것을 사용하는다는 의미

``` sql
-- 1) 파티션 테이블 생성
create table SALES (
  sales_no       number,
  sale_year      number,
  sale_month     number,
  sale_day       number,
  customer_name  varchar2(30),
  birth_date     date,
  price          number
)
partition by range (sales_no)
(
  partition SALES_P1 values less than (3),
  partition SALES_P2 values less than (5),
  partition SALES_P3 values less than (maxvalue)
);

-- 2) 로컬 파티션 인덱스 생성
create index IDX_SALES_01 on SALES (sales_no) LOCAL;

-- 보통의 인덱스 생성문 SQL 맨뒤에 LOCAL 키워드 붙이기

```

2) Global Partitioned Index (전역 파티션 인덱스, 글로벌 인덱스)

``` sql

-- 1) 파티션 테이블 생성
create table SALES (
  sales_no       number,
  sale_year      number,
  sale_month     number,
  sale_day       number,
  customer_name  varchar2(30),
  birth_date     date,
  price          number
)
partition by range (sales_no)
(
  partition SALES_P1 values less than (3),
  partition SALES_P2 values less than (5),
  partition SALES_P3 values less than (maxvalue)
);

-- 2) 글로벌 파티션 인덱스 생성
create index IDX_SALES_02 on SALES (customer_name) GLOBAL
partition by hash (customer_name)
partitions 4;

```

## Partitioning 성능 비교
### Index Range Scan과 파티션 성능비교 
- 위 그림은 Index Range Scan을 그림으로 표현한 것이다. 파티션도 조건절에 Key가 들어와야 특정 파티션만 읽을 수 있으므로 성능이 좋아진다. 그렇다면 파티션을 적용하는 것 대신 인덱스를 걸면 더빠르지 않을거라는 생각을 할 수 있다. <br>

- Index Range Scan동작에서는 Root -> Branch -> Leaf 로 내려가면서 범위조회를 순차적으로 Scan을 하면서 ROWID를 가지고 Random Access Block IO 하는 형태이기 떄문에, 클러스터링 백터가 최악일 경우 조회건수 만큼의 IO가 발생 할 수 있다.<br>

- 만약 특정 조건으로 넓은 범위를 읽어야 하고 그 조건이 파티션 키이면 해당 파티션만 FULL SCAN으로 Multi Block IO를 할 수 있끼 떄문에 Index Scan보다 훨씬 유리하다. 
Full Scan이므로 Parallel도 가능하다. <br>
