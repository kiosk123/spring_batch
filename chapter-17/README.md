# 17. JPA 데이터 쓰기

![.](./img/1.png)

```java
@Configuration
@Slf4j
public class  ItemWriterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public ItemWriterConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job itemReaderJob() throws Exception {        
        return jobBuilderFactory.get("jpaJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaStep())
                .build();
    }

    @Bean
    public Step jpaStep() throws Exception {
        return stepBuilderFactory.get("jpaStep")
                .<Person, Person>chunk(10)
                .reader(new CustomItemReader<Person>(getItems()))
                .writer(JpaItemWriter())
                .build();
    }

    /** JpaItemWriter 생성 */
    private ItemWriter<Person> JpaItemWriter() throws Exception {
        JpaItemWriter<Person> item = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(entityManagerFactory)
                /** 
                 * @Id로 설정된 프로퍼티 값을 직접 설정 할때 
                 * 별다른 설정 없이 실행할 경우 entityManager의 merge 메서드를 호출
                 * @Id로 설정된 값이 이미 디비에 있는지 비교하고 없으면 insert 있으면 update 하기 때문에
                 * 단순 insert인 경우 select 쿼리를 하게 되므로 실제 쿼리 요청은 2배
                 * 이때 usePersist(true)를 설정하면 select쿼리 없이 insert한다 
                 */
                .usePersist(true)
                .build();
        item.afterPropertiesSet();
        return item;
    }


    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new Person("test name - " + i, "test age", "test address"));
        }
        return items;
    }
}
```