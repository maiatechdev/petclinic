
package org.springframework.samples.petclinic.web;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.validation.BeanPropertyBindingResult;

public class PetValidatorTests {

    private PetValidator validator;
    private Owner owner;

    @Before
    public void setUp() {
        validator = new PetValidator();
        owner = new Owner();
    }

    @Test
    public void shouldRejectEmptyName() {
        Pet pet = createPet("");

        BeanPropertyBindingResult errors = validate(pet);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("required",
                errors.getFieldError("name").getCode());
    }

    @Test
    public void shouldRejectDuplicateName() {
        createPet("Rex");
        Pet pet = createPet("Rex");

        BeanPropertyBindingResult errors = validate(pet);

        assertTrue(errors.hasFieldErrors("name"));
        assertEquals("duplicate",
                errors.getFieldError("name").getCode());
    }

    @Test
    public void shouldRejectMissingType() {
        Pet pet = createPet("Rex");
        pet.setType(null);

        BeanPropertyBindingResult errors = validate(pet);

        assertTrue(errors.hasFieldErrors("type"));
        assertEquals("required",
                errors.getFieldError("type").getCode());
    }

    @Test
    public void shouldAcceptValidName() {
        Pet pet = createPet("Rex");

        BeanPropertyBindingResult errors = validate(pet);

        assertFalse(errors.hasFieldErrors("name"));
    }

    @Test
    public void shouldAcceptDefinedType() {
        Pet pet = createPet("Rex");

        BeanPropertyBindingResult errors = validate(pet);

        assertFalse(errors.hasFieldErrors("type"));
    }

    private Pet createPet(String name) {
        Pet pet = new Pet();
        pet.setName(name);

        PetType type = new PetType();
        type.setName("dog");
        pet.setType(type);

        owner.addPet(pet);
        return pet;
    }

    private BeanPropertyBindingResult validate(Pet pet) {
        BeanPropertyBindingResult errors =
                new BeanPropertyBindingResult(pet, "pet");

        validator.validate(pet, errors);

        return errors;
    }
}
