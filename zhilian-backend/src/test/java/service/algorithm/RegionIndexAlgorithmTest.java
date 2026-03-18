package service.algorithm;

import com.zhilian.zhilianbackend.service.algorithm.RegionIndexAlgorithm;
import com.zhilian.zhilianbackend.service.algorithm.impl.RegionIndexAlgorithmImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RegionIndexAlgorithmTest {

    private RegionIndexAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new RegionIndexAlgorithmImpl();
    }

    @Test
    void testCalculateCoopDensity() {
        // 测试正常情况
        BigDecimal result = algorithm.calculateCoopDensity(100, 50);
        assertEquals(new BigDecimal("2.0000"), result);

        // 测试制造企业为0的情况
        result = algorithm.calculateCoopDensity(100, 0);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateServiceRate() {
        // 测试正常情况
        BigDecimal result = algorithm.calculateServiceRate(30, 100);
        assertEquals(new BigDecimal("0.3000"), result);

        // 测试制造企业为0的情况
        result = algorithm.calculateServiceRate(30, 0);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateCrossRate() {
        // 测试正常情况
        BigDecimal result = algorithm.calculateCrossRate(20, 100);
        assertEquals(new BigDecimal("0.2000"), result);

        // 测试总合作为0的情况
        result = algorithm.calculateCrossRate(20, 0);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateTotalIndex() {
        BigDecimal coopDensity = new BigDecimal("2.0");
        BigDecimal serviceRate = new BigDecimal("0.3");
        BigDecimal crossRate = new BigDecimal("0.2");

        // 预期： (2.0*0.4 + 0.3*0.35 + 0.2*0.25) * 100 = (0.8 + 0.105 + 0.05) * 100 = 95.5
        BigDecimal result = algorithm.calculateTotalIndex(coopDensity, serviceRate, crossRate);
        assertEquals(new BigDecimal("95.50"), result);
    }
}