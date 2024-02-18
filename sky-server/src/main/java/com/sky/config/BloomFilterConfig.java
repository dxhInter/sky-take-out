package com.sky.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dxh
 * @version 1.0
 * @project sky-take-out
 * @date 2024/2/18 15:04:13
 */
@Configuration
public class BloomFilterConfig {
    private final long expectedInsertions = 1000;
    private final double fpp = 0.03;
    @Bean
    public BloomFilter<Long> categoryBloomFilter(){
        return BloomFilter.create(Funnels.longFunnel(),
                expectedInsertions,fpp);
    }
}
