package org.springframework.samples.petclinic.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("ClinicServiceCacheTests-config.xml")
public class ClinicServiceCacheTests {

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private VetRepository vetRepository;

    @Autowired
    private CacheManager cacheManager;

    @Before
    public void clearCacheAndCalls() {
        this.cacheManager.getCache("vets").clear();
        reset(this.vetRepository);
    }

    @Test
    public void secondCallIsServedFromCache() {
        this.clinicService.findVets();
        this.clinicService.findVets();
        verify(this.vetRepository, times(1)).findAll();
    }

    @Test
    public void findVetsStoresResultInVetsCache() {
        this.clinicService.findVets();
        assertNotNull(this.cacheManager.getCache("vets").get(SimpleKey.EMPTY));
    }
}