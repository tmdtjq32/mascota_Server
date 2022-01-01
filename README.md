## ✨Structure

```text
api-server-spring-boot
  > * build
  > gradle
  > * logs
    | app.log // warn, error 레벨에 해당하는 로그가 작성 되는 파일
    | app-%d{yyyy-MM-dd}.%i.gz
    | error.log // error 레벨에 해당하는 로그가 작성 되는 파일
    | error-%d{yyyy-MM-dd}.%i.gz
  > src.main.java.com.example.demo
    > config
      > secret
        | Secret.java // git에 추적되지 않아야 할 시크릿 키 값들이 작성되어야 하는 곳
      | BaseException.java // Controller, Service, Provider 에서 Response 용으로 공통적으로 사용 될 익셉션 클래스
      | BaseResponse.java // Controller 에서 Response 용으로 공통적으로 사용되는 구조를 위한 모델 클래스
      | BaseResponseStatus.java // Controller, Service, Provider 에서 사용 할 Response Status 관리 클래스 
      | Constant.java // 공통적으로 사용될 상수 값들을 관리하는 곳
    > src
      > test
        | TestController.java // logger를 어떻게 써야하는지 보여주는 테스트 클래스
      > user
        > models
          | Pet.java        
          | User.java 
          | UserDto.java 
          | PetDto.java 
          | SaveUserDto.java 
          | SaveBookDto.java 
        | UserController.java
        | UserProvider.java
        | UserService.java
        | UserRepository.java
      | WebSecurityConfig.java // spring-boot-starter-security, jwt 를 사용하기 위한 클래스 
    > utils
      | AES128.java // 암호화 관련 클래스
      | JwtService.java // jwt 관련 클래스
      | ValidateRegex.java // 정규표현식 관련 클래
    | DemoApplication // SpringBootApplication 서버 시작 지점
  > resources
    | application.yml // Database 연동을 위한 설정 값 세팅 및 Port 정의 파일
    | logback-spring.xml // logger 사용시 console, file 설정 값 정의 파일
build.gradle // gradle 빌드시에 필요한 dependency 설정하는 곳
.gitignore // git 에 포함되지 않아야 하는 폴더, 파일들을 작성 해놓는 곳

```
## ✨Description

### Lombok
Java 라이브러리로 반복되는 getter, setter, toString 등의 메서드 작성 코드를 줄여주는 라이브러리이다. 기본적으로 각 도메인의 model 폴더 내에 생성하는 클래스에 lombok을 사용하여 코드를 효율적으로 짤 수 있도록 구성했다. 


### src - main - resources
템플릿은 크게 log 폴더와 src 폴더로 나뉜다. log는 통신 시에 발생하는 오류들을 기록하는 곳이다. 

`application.yml`

에서 **포트 번호를 정의**하고 **DataBase 연동**을 위한 값을 설정한다.

`logback-spring.xml`

logs 폴더에 로그 기록을 어떤 형식으로 남길 것인지 설정한다. 

### src - main - java

`com.example.demo` 패키지에는 크게 `config` 폴더, `src` 폴더와 이 프로젝트의 시작점인 `DemoApplication.java`가 있다.

`DemoApplication.java` 은 스프링 부트 프로젝트의 시작을 알리는 `@SpringBootApplication` 어노테이션을 사용하고 있다. 

`src`폴더에는 실제 **API가 동작하는 프로세스**를 담았고 `config` 폴더에는 `src`에서 필요한 Secret key, Base 클래스, 상수 클래스를, `util` 폴더에는 JWT, 암호화, 정규표현식 등의 클래스가 존재한다.

전반적인 API 통신 프로세스는 다음과 같다.

> **Request** → `Controller.java`(=Router+Controller) → `Service` (CUD) / `Provider` (R) (=Business Logic) → `Repository` (DB) → **Response**

#### 1. Controller / @RestController

> 1) API 통신의 **Routing** 처리
> 2) Request를 다른 계층에 넘기고 처리된 결과 값을 Response 해주는 로직
>  + Request의 **형식적 Validation** 처리 (DB를 거치지 않고도 검사할 수 있는)

**1) `@Autowired`**

UserController의 생성자에 `@Autowired` 어노테이션이 붙어있다. 이는 **의존성 주입**을 위한 것으로, `UserController`  뿐만 아니라 다음에 살펴볼 `UserService`, `UserProvider`의 생성자에도 각각 붙어 있는 것을 확인할 수 있다.

**2) `BaseResponse`**

Response할 때, 공통 부분은 묶고 다른 부분은 제네릭을 통해 구현함으로써 반복되는 코드를 줄여준다. (`BaseResponse.java` 코드 살펴 볼 것. 여기에 쓰이는`BaseResponseStatus` 는 `enum`을 통해 Status 값을 관리하고 있다.)

#### 2. Service 와 Provider / @Service

> 1) **비즈니스 로직**을 다루는 곳 (DB 접근[CRUD], DB에서 받아온 것 형식화)
>  + Request의 **의미적** **Validation** 처리 (DB를 거쳐야 검사할 수 있는)
>  정상적으로 작업수행시 하나의 트랜젝션 단위로 처리된다.

`Service`와 `Provider`는 비즈니스 로직을 다루는 곳이다. **CRUD** 중 **R(Read)** 에 해당하는 코드가 긴 경우가 많기 때문에 **R(Read)** 만 따로 분리해 `Service`는 **CUD(Create, Update, Delete)** 를, `Provider`는 **R(Read)** 를 다루도록 했다. 

`Provider`
> **R(Read)** 와 관련된 곳이다. DB에서 select 해서 얻어온 값을 DTO를 활용해 반환한다.

`Service`
> **CUD(Create, Update, Delete)** 와 관련된 곳이다. **CUD**에서 **R**이 필요한 경우가 있는데, 그럴 때는 `Provider`에 구성되어 있는 것을 `Service`에서 사용한다.

**2) BaseException**

`BaseException`을 통해 `Service`나 `Provider`에서 `Controller`에 Exception을 던진다. 마찬가지로 Status 값은 `BaseResponseStatus` 의 `enum`을 통해 관리한다.

#### 3. Repository 
Spring Data JPA를 사용하여 구성되어 있다. 자세한 내용은 이곳 [공식 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#reference) 참고했다.

### Spring Data JPA
ORM을 활용해 클래스와 데이터베이스의 테이블 사이의 맵핑 정보를 기술한 메타데이터를 사용하여 객체를 데이터베이스 테이블에 자동으로 영속화할 수 있다. SQL 중심이 아닌 객체 중심으로 개발가능하고 Repository 인터페이스를 활용해 좀 더 비즈니스 로직에 집중할 수 있어 사용했다.

### nohup
무중단 서비스를 위해 nohup을 사용한다.
