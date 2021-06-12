# 21. 테스트 코드 작성하기

![.](./img/1.png)

JobLauncherTestUtils 클래스는 JobLauncher를 이용해서 테스트를 가능하게 끔 만들게 하는 클래스  
JobLauncherTestUtils 클래스는 내부적으로 JobLauncher를 포함하고 있고, 테스트 코드에서 Jobr과 Step을 자유롭게 실행할 수 있다.  
  
  
테스트 코드에서 **@EnableAutoConfiguration**을 사용하는데 이것에 관련된 내용을 다음과 같다

AutoConfiguration은 결국 Configuration이다. 즉, Bean을 등록하는 자바 설정 파일이다.  
spring.factories 내부에 여러 Configuration 들이 있고, 조건에 따라 Bean을 등록한다.  
따라서 메인 클래스(@SpringBootApplication)를 실행하면, @EnableAutoConfiguration에 의해  
spring.factories 안에 들어있는 수많은 자동 설정들이 조건에 따라 적용이 되어 수 많은 Bean들이 생성되고,  
스프링 부트 어플리케이션이 실행되는 것이다

