## Projeto referencia para RestAssured tests

### RestAssured static imports
```java
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.ResponseAwareMatcher.*;
import static org.hamcrest.Matchers.*;
```
---
### Mudar porta para não conflitar com a porta da aplicação principal:

`application.properties:`

```properties
properties
server.port=8081
```
---
### dependencies:
```xml
        <dependency>
			<groupId>io.rest-assured</groupId>
			<artifactId>rest-assured</artifactId>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>com.googlecode.json-simple</groupId>
			<artifactId>json-simple</artifactId>
			<version>1.1.1</version>
		</dependency>

		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter</artifactId>
			<version>5.10.0</version>
			<scope>test</scope>
		</dependency>
```

