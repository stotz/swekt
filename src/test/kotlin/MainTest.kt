package ch.typedef

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainTest {
    
    @Test
    fun `application should start successfully`() {
        val app = Swekt()
        assertNotNull(app)
    }
    
    @Test
    fun `run should execute without exceptions`() {
        val app = Swekt()
        assertDoesNotThrow {
            app.run()
        }
    }
}
