# 02. 스프링 배치 프로젝트 생성

## 참고 - [Spring Batch Job Flow](https://jojoldu.tistory.com/328)

## 스프링 배치 프로젝트 생성
스프링 배치 프로젝트를 생성한다.

```gradle
plugins {
	id 'org.springframework.boot' version '2.4.0'
	id 'io.spring.dependency-management' version '1.0.11.RELEASE'
	id 'java'
}

group = 'batch'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-batch'
	implementation 'org.springframework.boot:spring-boot-starter-data-jdbc'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	runtimeOnly 'mysql:mysql-connector-java'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'org.springframework.batch:spring-batch-test'
}

test {
	useJUnitPlatform()
}

```

## HelloConfiguration.java 작성

@EnableBatchProcessing 어노테이션을 부트 애플리케이션 시작 클래스에 설정한다.
```java
@SpringBootApplication
@EnableBatchProcessing
public class BatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

}
```

배치처리를 위한 구성 클래스를 작성한다.

```java
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class  HelloConfiguration {

    /** Job을 만드는 jobBuilderFactory */
    private final JobBuilderFactory jobBuilderFactory;

    /** Job에 필요한 스텝을 만든다  */
    private final StepBuilderFactory stepBuilderFactory;

    public HelloConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job helloJob() {
                /** Job 이름은 HelloJob으로 설정함 - Job이름은 스프링 배치를 실행시킬 수 있는 key이기도 함*/
        return jobBuilderFactory.get("HelloJob")
                /** incrementer - Job 실행 단위 구분 */
                .incrementer(new RunIdIncrementer()) /**RunIdIncrementer는 Job이 실행할때마다 파라미터 아이디를 자동으로 생성 */

                /** Job 실행시 최초로 실행될 스텝을 설정한다.*/
                .start(this.helloStep())
                .build();
    }

    /** Job에서 사용할 Step을 선언한다. */
    @Bean
    public Step helloStep() {
        return stepBuilderFactory.get("helloStep")
                .tasklet((contribution, chunckContext) -> {
                    log.info("hello spring batch");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}

```

## 배치 실행시 파라미터로 특정 배치 Job만 실행하기

프로그램 실행시 다음과 같이 배치 job이름을 program.arguments로 넘긴다.
` --spring.batch.job.names=helloJob` 

## 지정한 배치 Job만 실행하도록 application.yml 파일에 추가한다.

스프링 배치는 기본적으로 모든 특정 배치 Job만 실행하도록 설정하지 않으면 모든 배치 Job을 실행하는 것이 기본이다.  
특정 배치 Job만을 실행하기 위해 다음과 같이 yml파일에 설정해준다. 

`spring.batch.job.names: ${job.name:NONE}`

```yml
spring:
  batch:
    job:
      names: ${job.name:NONE}
```

- 배치 실행시 job.name 파라미터에 아무것도 넘어오는 게 없으면 NONE으로 세팅된다
  - NONE으로 세팅시 아무런 배치도 실행하지 않는다.

- 배치 실행시 `--job.name=helloJob` 형태로 실행시킬 배치 job을 넘기면 된다.



