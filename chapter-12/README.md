# 12. JDBC 데이터 읽기

![.](./img/1.png)
![.](./img/2.png)

## 테스트 데이터 세팅
h2 디비에 테스트 스키마와 데이터 스크립트를 실행시키기 위해 다음과 같이 설정한다.

```yml
spring:
  batch:
    job:
      names: ${job.name:chunkJob}
    initialize-schema: always
  datasource:
    driver-class-name: org.h2.Driver
    data:
    - classpath:person.sql
```

## 예제코드
JdbcCursorItemReader를 사용하여 데이터를 읽어오는 예제이다.
```java
Configuration
@Slf4j
public class  ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public ItemReaderConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public Job itemReaderJob() throws Exception {        
        return jobBuilderFactory.get("chunkJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcStep())
                .build();
    }

    @Bean
    public Step jdbcStep() throws Exception {
        return stepBuilderFactory.get("jdbcStep")
                .<Person, Person>chunk(10)
                .reader(JdbcCusorItemReader())
                .writer(itemWriter())
                .build();
    }

    /** JdbcCursorItemReader 설정 */
    private JdbcCursorItemReader<Person> JdbcCusorItemReader() throws Exception {
        JdbcCursorItemReaderBuilder<Person> itembBuilder = new JdbcCursorItemReaderBuilder<Person>();
        JdbcCursorItemReader<Person> itemReader 
                = itembBuilder
                    .name("jdbcCursorItemReader")
                    
                    /** dataSource 설정 */
                    .dataSource(dataSource)

                    /** JdbcCursorItemReader는 sql 메서드로 데이터 가져옴 */
                    .sql("select id, name, age, address from person") 

                    /** row와 객체 매핑 */
                    .rowMapper((rs, rowNum) -> {
                        return new Person(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
                    })
                    .build();
        /** 객체의 프로퍼티가 모두 올바르게 설정되어 있는 지 확인 */
        itemReader.afterPropertiesSet();
        return itemReader;
    }

    private ItemWriter<Person> itemWriter() {
        return items -> log.info("item size: {}, {}", items.size(), items.stream().map(Person::getName).collect(Collectors.joining(",")));
    }

}
```