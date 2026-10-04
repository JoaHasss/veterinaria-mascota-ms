package pe.edu.upeu.pago;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requiere Config Server, PostgreSQL y Kafka levantados")
class VeterinariaPagoApplicationTests {

	@Test
	void contextLoads() {
	}

}
