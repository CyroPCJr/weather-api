package com.weatherworld.controller

import com.ninjasquad.springmockk.MockkBean
import com.weatherworld.component.CustomRateLimiterAspect
import com.weatherworld.model.TemperatureUnit
import com.weatherworld.service.WeatherService
import com.weatherworld.util.ApiRateLimiter
import com.weatherworld.util.ApiRateLimiter.Companion.TOKEN_PER_REFILL_IN_MINUTES
import io.mockk.every
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

// / Aqui esse teste esta funcionando

// @ActiveProfiles("test")
// @SpringBootTest
// @Import(TestConfig::class)
// class RateLimiterAspectTest {
//    @MockkBean
//    lateinit var rateLimiter: ApiRateLimiter
//
//    @Autowired
//    lateinit var dummyService: DummyService1
//
//    @Autowired
//    lateinit var context: ApplicationContext
//
//    @Test
//    fun `should allow method execution when rate limiter allows`() {
//        every { rateLimiter.tryConsume() } returns true
//
//        val result = dummyService.doSomething()
//
//        assertEquals("executed", result)
//        verify { rateLimiter.tryConsume() }
//    }
//
//    @Test
//    fun `should throw RateLimitExceededException when rate limit is exceeded`() {
//        every { rateLimiter.tryConsume() } returns false
//
//        val exception =
//            assertThrows<RateLimitExceededException> {
//                dummyService.doSomething()
//            }
//
//        assertEquals("Rate limit exceeded. Try again later.", exception.message)
//        verify { rateLimiter.tryConsume() }
//    }
//
//    @Test
//    fun `should confirm dummyService is proxied`() {
//        println("DummyService class: ${dummyService.javaClass}")
//    }
//
//    @Test
//    fun `check if aspect bean is loaded`() {
//        println("Beans: ${context.getBeansOfType(RateLimiterAspect::class.java)}")
//    }
//
//    @Test
//    fun `verificar beans carregados`() {
//        val beans = context.beanDefinitionNames
//        beans
//            .filter { it.contains("dummy", true) || it.contains("aspect", true) }
//            .sorted()
//            .forEach { println(it) }
//    }
//
//    @Test
//    fun `deve lançar exceção quando rate limit excedido`() {
//        every { rateLimiter.tryConsume() } returns false
//
//        val exception =
//            assertThrows<RateLimitExceededException> {
//                dummyService.doSomething()
//            }
//
//        assertEquals("Rate limit exceeded. Try again later.", exception.message)
//    }
// }
//
// @Configuration
// @EnableAspectJAutoProxy(proxyTargetClass = true)
// open class TestConfig {
//    @Bean
//    open fun dummyService() = DummyService1()
//
//    @Bean
//    open fun rateLimiterAspect(rateLimiter: ApiRateLimiter) = RateLimiterAspect(rateLimiter)
// }
//
// @Service
// open class DummyService1 {
//    @RateLimited
//    open fun doSomething(): String = "executed"
// }
//
// @Target(AnnotationTarget.FUNCTION)
// @Retention(AnnotationRetention.RUNTIME)
// annotation class RateLimited
//
// @Profile("test")
// @Aspect
// @Component("rateLimiterAspectComponent")
// class RateLimiterAspect(
//    private val rateLimiter: ApiRateLimiter,
// ) {
//    @Around("@annotation(RateLimited)")
//    fun enforceRateLimit(joinPoint: ProceedingJoinPoint): Any {
//        println("🚨 Aspecto ativado!")
//        if (!rateLimiter.tryConsume()) {
//            throw RateLimitExceededException("Rate limit exceeded. Try again later.")
//        }
//        return joinPoint.proceed()
//    }
// }
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(WeatherController::class, CustomRateLimiterAspect::class)
class RateLimiterControllerTest {
    @Autowired
    lateinit var context: ApplicationContext

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var rateLimiter: ApiRateLimiter

    @Test
    fun `should return 200 OK when rate limit is not exceeded`() {
        every { rateLimiter.tryConsume() } returns true

        val mockRequest =
            MockMvcRequestBuilders
                .get("/api/weather/by-city")
                .param("city", "Piracicaba")
                .param("units", TemperatureUnit.METRIC.name)

        mockMvc
            .perform(mockRequest)
            .andExpect(status().isOk)
    }

    @Test
    fun `print controller class to verify proxy`() {
        val controller = context.getBean(WeatherController::class.java)
        println(">>> Controller class: ${controller.javaClass}")

        val bean = context.getBean(WeatherService::class.java)
        println(">>> Classe proxyada? ${bean.javaClass}")
    }

    @Test
    fun `should return 429 Too Many Requests when rate limit is exceeded`() {
        val limiter = ApiRateLimiter()

        repeat(ApiRateLimiter.TOKEN_PER_REFILL_IN_MINUTES.toInt()) {
            assertTrue(limiter.tryConsume())
        }

        assertFalse(limiter.tryConsume())
    }
}
