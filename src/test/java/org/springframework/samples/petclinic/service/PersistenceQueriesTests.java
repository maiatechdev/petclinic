package org.springframework.samples.petclinic.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Characterizes the queries of the Spring Data JPA repositories against the sample data
 * (db/hsqldb/populateDB.sql): searches with no result, with several results and with
 * the empty prefix, owners that must not appear twice, ordering and uniqueness of pet
 * types, lookups of ids that do not exist, vets with and without specialties, and visits
 * by pet. These cases complement {@link AbstractClinicServiceTests}, which covers the
 * basic find, insert and update operations.
 * <p>
 * The tests avoid date fields on purpose, so they stay valid when the date type changes.
 */
@ContextConfiguration(locations = {"classpath:spring/business-config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class PersistenceQueriesTests {

    private static final int OWNERS_IN_SAMPLE_DATA = 10;
    private static final int PET_TYPES_IN_SAMPLE_DATA = 6;
    private static final int VETS_IN_SAMPLE_DATA = 6;
    private static final int UNKNOWN_ID = 9999;

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private VisitRepository visitRepository;

    // ---- owners: search by last name -------------------------------------------------

    @Test
    public void searchWithNoMatchReturnsEmptyCollection() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Zzz");
        assertNotNull(owners);
        assertTrue(owners.isEmpty());
    }

    @Test
    public void searchMatchesThePrefixOfTheLastName() {
        // Escobito and Estaban share the prefix "Es"
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Es");
        assertEquals(2, owners.size());
        for (Owner owner : owners) {
            assertTrue(owner.getLastName().startsWith("Es"));
        }
    }

    @Test
    public void searchDoesNotMatchTheMiddleOfTheLastName() {
        // "avis" occurs inside "Davis", but the search is by prefix
        assertTrue(this.clinicService.findOwnerByLastName("avis").isEmpty());
    }

    @Test
    public void emptyPrefixListsEveryOwner() {
        // OwnerController relies on this: an empty last name is the broadest search
        assertEquals(OWNERS_IN_SAMPLE_DATA, this.clinicService.findOwnerByLastName("").size());
    }

    @Test
    public void ownerWithSeveralPetsIsReturnedOnlyOnce() {
        // Coleman has two pets (Samantha and Max); the query joins the pets and uses DISTINCT
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Coleman");
        assertEquals(1, owners.size());
        assertEquals(2, owners.iterator().next().getPets().size());
    }

    @Test
    public void searchNeverReturnsTheSameOwnerTwice() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("");
        Set<Integer> ids = new HashSet<Integer>();
        for (Owner owner : owners) {
            assertTrue("owner " + owner.getId() + " is duplicated", ids.add(owner.getId()));
        }
    }

    // ---- owners: lookup by id --------------------------------------------------------

    @Test
    public void findOwnerByIdLoadsTheOwnersPets() {
        Owner coleman = this.clinicService.findOwnerById(6);
        assertEquals("Coleman", coleman.getLastName());
        assertEquals(2, coleman.getPets().size());
    }

    @Test
    public void findOwnerByUnknownIdReturnsNull() {
        // The service does not translate "not found" into an exception; the caller gets null
        assertNull(this.clinicService.findOwnerById(UNKNOWN_ID));
    }

    // ---- pets and pet types ----------------------------------------------------------

    @Test
    public void findPetByUnknownIdReturnsNull() {
        assertNull(this.clinicService.findPetById(UNKNOWN_ID));
    }

    @Test
    public void petTypesAreOrderedByName() {
        List<String> names = namesOf(this.clinicService.findPetTypes());
        List<String> sorted = new ArrayList<String>(names);
        Collections.sort(sorted);
        assertEquals(PET_TYPES_IN_SAMPLE_DATA, names.size());
        // the sample data inserts cat first, so this proves the ORDER BY of the query
        assertEquals(sorted, names);
        assertEquals("bird", names.get(0));
    }

    @Test
    public void petTypesHaveNoDuplicates() {
        Collection<PetType> types = this.clinicService.findPetTypes();
        Set<Integer> ids = new HashSet<Integer>();
        Set<String> names = new HashSet<String>();
        for (PetType type : types) {
            ids.add(type.getId());
            names.add(type.getName());
        }
        assertEquals(types.size(), ids.size());
        assertEquals(types.size(), names.size());
    }

    @Test
    public void petKeepsItsOwnerAndType() {
        Pet samantha = this.clinicService.findPetById(7);
        assertEquals("Samantha", samantha.getName());
        assertEquals("cat", samantha.getType().getName());
        assertEquals(Integer.valueOf(6), samantha.getOwner().getId());
    }

    // ---- vets ------------------------------------------------------------------------

    @Test
    public void findVetsReturnsEveryVet() {
        assertEquals(VETS_IN_SAMPLE_DATA, this.clinicService.findVets().size());
    }

    @Test
    public void vetsMayHaveNoSpecialties() {
        // Carter (1) and Jenkins (6) have none; Stevens (5) has radiology
        Collection<Vet> vets = this.clinicService.findVets();
        assertEquals(0, vetById(vets, 1).getNrOfSpecialties());
        assertEquals(0, vetById(vets, 6).getNrOfSpecialties());
        assertEquals(1, vetById(vets, 5).getNrOfSpecialties());
    }

    // ---- visits ----------------------------------------------------------------------

    @Test
    public void findVisitsByPetIdReturnsOnlyThatPetsVisits() {
        List<Visit> visits = this.visitRepository.findByPetId(7);
        assertEquals(2, visits.size());
        for (Visit visit : visits) {
            assertEquals(Integer.valueOf(7), visit.getPet().getId());
        }
    }

    @Test
    public void petWithoutVisitsHasNone() {
        assertTrue(this.visitRepository.findByPetId(1).isEmpty());
    }

    @Test
    public void savedVisitIsFoundByPetId() {
        Pet samantha = this.clinicService.findPetById(7);
        int before = this.visitRepository.findByPetId(7).size();
        Visit visit = new Visit();
        samantha.addVisit(visit);
        visit.setDescription("persistence query test");
        this.clinicService.saveVisit(visit);
        assertNotNull(visit.getId());
        assertEquals(before + 1, this.visitRepository.findByPetId(7).size());
    }

    // ---- helpers ---------------------------------------------------------------------

    private static List<String> namesOf(Collection<PetType> types) {
        List<String> names = new ArrayList<String>();
        for (PetType type : types) {
            names.add(type.getName());
        }
        return names;
    }

    private static Vet vetById(Collection<Vet> vets, int id) {
        for (Vet vet : vets) {
            if (vet.getId() == id) {
                return vet;
            }
        }
        throw new AssertionError("vet " + id + " not found");
    }
}
