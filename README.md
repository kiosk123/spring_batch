# 스프링 배치 연습

스프링 프레임워크에서 제공하는 배치 실습 정리

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

