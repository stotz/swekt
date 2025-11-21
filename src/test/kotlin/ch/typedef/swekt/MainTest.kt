package ch.typedef.swekt

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MainTest {

    @Test
    fun `application should start successfully`() {
        val app = Swekt()
        Assertions.assertNotNull(app)
    }

    @Test
    fun `run should execute without exceptions`() {
        val app = Swekt()
        Assertions.assertDoesNotThrow {
            app.run()
        }
    }
}
