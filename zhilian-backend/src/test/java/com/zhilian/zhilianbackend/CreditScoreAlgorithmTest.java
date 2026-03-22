package com.zhilian.zhilianbackend;

import com.zhilian.zhilianbackend.entity.AbroadCase;
import com.zhilian.zhilianbackend.entity.Certification;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.AbroadCaseMapper;
import com.zhilian.zhilianbackend.mapper.CertificationMapper;
import com.zhilian.zhilianbackend.mapper.EvaluationMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.algorithm.CreditScoreAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CreditScoreAlgorithmTest {

    @Mock
    private CertificationMapper certificationMapper;

    @Mock
    private AbroadCaseMapper abroadCaseMapper;

    @Mock
    private EvaluationMapper evaluationMapper;

    @Mock
    private ServiceProviderMapper serviceProviderMapper;

    @InjectMocks
    private CreditScoreAlgorithm creditScoreAlgorithm;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculate_InvalidParam() {
        assertThrows(BusinessException.class, () -> creditScoreAlgorithm.calculate(null));
        assertThrows(BusinessException.class, () -> creditScoreAlgorithm.calculate(0L));
        assertThrows(BusinessException.class, () -> creditScoreAlgorithm.calculate(-1L));
    }

    @Test
    void testCalculate_NoData() {
        Long serviceId = 1L;

        ServiceProvider sp = new ServiceProvider();
        sp.setId(serviceId);
        sp.setCompanyName("Test Company");
        when(serviceProviderMapper.selectById(serviceId)).thenReturn(sp);

        when(certificationMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(abroadCaseMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(evaluationMapper.getAvgScoreByServiceId(eq(serviceId), any())).thenReturn(null);
        when(evaluationMapper.getCountByServiceId(eq(serviceId), any())).thenReturn(0);

        CreditScoreAlgorithm.CreditScoreResult result = creditScoreAlgorithm.calculate(serviceId);

        assertEquals((byte) 0, result.getQualScore(), "资质分应为0");
        assertEquals((byte) 0, result.getCaseScore(), "案例分应为0");
        assertEquals((byte) 60, result.getEvalScore(), "无评价时评价分基础应为60");
        
        // 0*0.3 + 0*0.3 + 60*0.4 = 24
        assertEquals((byte) 24, result.getTotalScore(), "总分计算错误");
    }

    @Test
    void testCalculate_MaxScore() {
        Long serviceId = 1L;

        ServiceProvider sp = new ServiceProvider();
        sp.setId(serviceId);
        sp.setCompanyName("Test Company");
        when(serviceProviderMapper.selectById(serviceId)).thenReturn(sp);

        Certification c1 = new Certification(); c1.setCertName("Normal");
        Certification c2 = new Certification(); c2.setCertName("CNAS");
        when(certificationMapper.selectList(any())).thenReturn(Arrays.asList(c1, c1, c1, c1, c1, c1, c2));

        AbroadCase ac1 = new AbroadCase(); ac1.setPublishTime(new Date());
        when(abroadCaseMapper.selectList(any())).thenReturn(Arrays.asList(ac1, ac1, ac1, ac1, ac1, ac1, ac1, ac1));

        when(evaluationMapper.getAvgScoreByServiceId(eq(serviceId), any())).thenReturn(5.0);
        when(evaluationMapper.getCountByServiceId(eq(serviceId), any())).thenReturn(50);

        CreditScoreAlgorithm.CreditScoreResult result = creditScoreAlgorithm.calculate(serviceId);

        assertEquals((byte) 100, result.getQualScore());
        assertEquals((byte) 100, result.getCaseScore());
        assertEquals((byte) 100, result.getEvalScore());
        
        // 100*0.3 + 100*0.3 + 100*0.4 = 100
        assertEquals((byte) 100, result.getTotalScore());
    }
}