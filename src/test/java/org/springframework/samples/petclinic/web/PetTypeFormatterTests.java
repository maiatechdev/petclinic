
package org.springframework.samples.petclinic.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;

public class PetTypeFormatterTests {

    private ClinicService clinicService;
    private PetTypeFormatter formatter;
    private PetType dog;

    @Before
    public void setUp() {
        clinicService = mock(ClinicService.class);
        formatter = new PetTypeFormatter(clinicService);

        dog = new PetType();
        dog.setName("dog");

        when(clinicService.findPetTypes()).thenReturn(Arrays.asList(dog));
    }

    @Test
    public void shouldParseExistingPetType() throws ParseException {
        PetType result = formatter.parse("dog", Locale.ENGLISH);
        assertSame(dog, result);
    }

    @Test(expected = ParseException.class)
    public void shouldRejectUnknownPetType() throws ParseException {
        formatter.parse("unknown", Locale.ENGLISH);
    }

    @Test
    public void shouldPrintPetTypeName() {
        String result = formatter.print(dog, Locale.ENGLISH);
        assertEquals("dog", result);
    }
}
