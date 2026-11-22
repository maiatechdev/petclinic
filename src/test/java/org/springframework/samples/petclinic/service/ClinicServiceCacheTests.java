package org.springframework.samples.petclinic.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/business-config.xml",
        "classpath:spring/tools-config.xml"})
public class ClinicServiceCacheTests {

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private CacheManager cacheManager;

    @Before
    public void clearCache() {
        this.cacheManager.getCache("vets").clear();
    }

    @Test
    public void secondCallIsServedFromCache() {
        Collection<Vet> first = this.clinicService.findVets();
        Collection<Vet> second = this.clinicService.findVets();
        assertSame(first, second);
    }

    @Test
    public void findVetsStoresResultInVetsCache() {
        this.clinicService.findVets();
        assertNotNull(this.cacheManager.getCache("vets").get(SimpleKey.EMPTY));
    }
}