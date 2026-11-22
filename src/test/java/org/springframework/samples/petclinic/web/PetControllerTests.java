
package org.springframework.samples.petclinic.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.support.SessionStatus;

public class PetControllerTests {

    private ClinicService clinicService;
    private PetController controller;
    private SessionStatus sessionStatus;

    @Before
    public void setUp() {
        clinicService = mock(ClinicService.class);
        controller = new PetController(clinicService);
        sessionStatus = mock(SessionStatus.class);
    }

    @Test
    public void shouldShowCreationForm() {
        Owner owner = new Owner();
        Map<String, Object> model = new HashMap<String, Object>();

        when(clinicService.findOwnerById(1)).thenReturn(owner);

        String view = controller.initCreationForm(1, model);

        assertEquals("pets/createOrUpdatePetForm", view);
        assertNotNull(model.get("pet"));
    }

    @Test
    public void shouldShowUpdateForm() {
        Pet pet = new Pet();
        Map<String, Object> model = new HashMap<String, Object>();

        when(clinicService.findPetById(1)).thenReturn(pet);

        String view = controller.initUpdateForm(1, model);

        assertEquals("pets/createOrUpdatePetForm", view);
        assertSame(pet, model.get("pet"));
    }

    @Test
    public void shouldRejectCreationWithEmptyName() {
        Pet pet = createPet("");
        BeanPropertyBindingResult result = validate(pet);

        String view = controller.processCreationForm(
                pet, result, sessionStatus);

        assertEquals("pets/createOrUpdatePetForm", view);
        assertTrue(result.hasFieldErrors("name"));
        verify(clinicService, never()).savePet(pet);
    }

    @Test
    public void shouldRejectUpdateWithEmptyName() {
        Pet pet = createPet("");
        pet.setId(1);

        BeanPropertyBindingResult result = validate(pet);

        String view = controller.processUpdateForm(
                pet, result, sessionStatus);

        assertEquals("pets/createOrUpdatePetForm", view);
        assertTrue(result.hasFieldErrors("name"));
        verify(clinicService, never()).savePet(pet);
    }

    @Test
    public void shouldSavePetOnCreation() {
        Pet pet = createPet("Rex");
        BeanPropertyBindingResult result = validate(pet);

        String view = controller.processCreationForm(
                pet, result, sessionStatus);

        assertEquals("redirect:/owners/{ownerId}", view);
        verify(clinicService).savePet(pet);
        verify(sessionStatus).setComplete();
    }

    @Test
    public void shouldSavePetOnUpdate() {
        Pet pet = createPet("Rex");
        pet.setId(1);

        BeanPropertyBindingResult result = validate(pet);

        String view = controller.processUpdateForm(
                pet, result, sessionStatus);

        assertEquals("redirect:/owners/{ownerId}", view);
        verify(clinicService).savePet(pet);
        verify(sessionStatus).setComplete();
    }

    @Test
    public void shouldLoadPetTypes() {
        controller.populatePetTypes();

        verify(clinicService).findPetTypes();
    }

    private Pet createPet(String name) {
        Owner owner = new Owner();
        Pet pet = new Pet();

        pet.setName(name);
        pet.setBirthDate(new LocalDate(2020, 5, 10));

        PetType type = new PetType();
        type.setName("dog");
        pet.setType(type);

        owner.addPet(pet);
        return pet;
    }

    private BeanPropertyBindingResult validate(Pet pet) {
        return new BeanPropertyBindingResult(pet, "pet");
    }
}

}
