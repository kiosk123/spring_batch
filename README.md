# 스프링 배치 연습

스프링 프레임워크에서 제공하는 배치 실습 정리  
[Spring Batch Event Listeners Example](https://howtodoinjava.com/spring-batch/spring-batch-event-listeners/#skip)

## 프로젝트 구성
java 11+  
lombok  
spring boot batch 2.4+  
spring data jpa  
spring data jdbc  
mysql driver  
h2 db  
gradle 6.5+  

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

## 챕터
01. 스프링 배치란?  
02. 스프링 배치 프로젝트 생성  
03. 스프링 배치 기본 구조  
04. 스프링 배치 테이블의 이해  
05. Job, JobInstance, JobExecution, Step, StepExecution 이해  
06. ExecutionContext 이해  
07. Task 기반 배치와 Chunk 기반 배치 차이점  
08. JobParameter의 이해  
09. @JobScope와 @StepScope 이해  
10. ItemReader의 이해  
11. CSV 파일 읽기  
12. JDBC 데이터 읽기  
13. JPA 데이터 읽기  
14. ItemWriter interface 구조 이해  
15. CSV 파일 쓰기  
16. JDBC 데이터 쓰기  
17. JPA 데이터 쓰기  
18. ItemProcessor Interface 구조 이해  
19. CSV 파일 데이터를 읽어 H2 DB에 저장하는 배치 개발 - 내가한 것  
20. CSV 파일 데이터를 읽어 H2 DB에 저장하는 배치 개발 - 강사님이 한 것  
21. 테스트 코드 작성하기  
22. 스프링 배치에서 전처리 후처리를 위한 JobExecutionListener, StepExecutionListener 이해  
23. StepListener의 이해  
24. Skip 예외 처리  
25. Retry 예외 처리  
26. 회원등급 프로젝트  
27. 주문 금액 집계 프로젝트 - JobExecutionDecision 사용    
28. 성능 개선 계획 이해 - SimpleStep  
29. Async Step 적용하기  
30. Multi-Thread Step 적용하기  
31. Partition Step 적용하기  
32. Parallel Step 적용하기
33. Jar 파일로 만들고 실행하기
