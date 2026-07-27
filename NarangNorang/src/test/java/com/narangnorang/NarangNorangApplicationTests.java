package com.narangnorang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // H2 설정이 있는 application-test.properties 적용
class NarangNorangApplicationTests {

	@Test
	void contextLoads() {
	}

}
