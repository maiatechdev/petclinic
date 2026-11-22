package org.springframework.samples.petclinic.web;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** Exercises date binding and persistence without depending on the domain date type. */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("WebControllerTests-config.xml")
@Transactional
public class PetVisitDateTests {

    private static final String OWNER_DETAILS = "/owners/6";
    private static final String NEW_PET = OWNER_DETAILS + "/pets/new";
    private static final String EDIT_PET = OWNER_DETAILS + "/pets/7/edit";
    private static final String NEW_VISIT = OWNER_DETAILS + "/pets/7/visits/new";
    private static final String PET_FORM = "pets/createOrUpdatePetForm";
    private static final String VISIT_FORM = "pets/createOrUpdateVisitForm";
    private static final String LEAP_DAY = "2024/02/29";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;

    /** Uses the application's MVC configuration, including its date formatters. */
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    /** A leap-day birth date submitted with slashes is stored as the same calendar date. */
    @Test
    public void storesBirthDateOnCreation() throws Exception {
        MockHttpSession session = openForm(NEW_PET, PET_FORM);
        Pet pet = (Pet) session.getAttribute("pet");
        this.mockMvc.perform(post(NEW_PET).session(session).param("name", "Leap Day")
                .param("type", "cat").param("birthDate", LEAP_DAY))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(OWNER_DETAILS));
        assertStoredDate("select birth_date from pets where id = ?", pet.getId());
    }

    /** Editing a pet persists the replacement birth date. */
    @Test
    public void storesBirthDateOnEdit() throws Exception {
        MockHttpSession session = openForm(EDIT_PET, PET_FORM);
        Pet pet = (Pet) session.getAttribute("pet");
        this.mockMvc.perform(post(EDIT_PET).session(session).param("name", "Samantha")
                .param("type", "cat").param("birthDate", LEAP_DAY))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(OWNER_DETAILS));
        assertStoredDate("select birth_date from pets where id = ?", pet.getId());
    }

    /** A submitted visit date is stored independently of the default date. */
    @Test
    public void storesSubmittedVisitDate() throws Exception {
        MockHttpSession session = openForm(NEW_VISIT, VISIT_FORM);
        Visit visit = (Visit) session.getAttribute("visit");
        this.mockMvc.perform(post(NEW_VISIT).session(session).param("description", "Date check")
                .param("date", LEAP_DAY))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(OWNER_DETAILS));
        assertStoredDate("select visit_date from visits where id = ?", visit.getId());
    }

    /** An impossible visit date stays on the form with a date binding error. */
    @Test
    public void rejectsInvalidVisitDate() throws Exception {
        MockHttpSession session = openForm(NEW_VISIT, VISIT_FORM);
        this.mockMvc.perform(post(NEW_VISIT).session(session).param("description", "Date check")
                .param("date", "2024/02/30"))
                .andExpect(status().isOk()).andExpect(view().name(VISIT_FORM))
                .andExpect(model().attributeHasFieldErrors("visit", "date"));
    }

    /** Reads existing SQL DATE rows without shifting the calendar date. */
    @Test
    public void readsSeedDatesWithoutTimeZoneShift() {
        Pet pet = this.entityManager.find(Pet.class, 7);
        Visit visit = this.entityManager.find(Visit.class, 4);
        assertEquals(LocalDate.of(2012, 9, 4), pet.getBirthDate());
        assertEquals(LocalDate.of(2013, 1, 4), visit.getDate());
    }

    private MockHttpSession openForm(String path, String formView) throws Exception {
        MockHttpSession session = (MockHttpSession) this.mockMvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(view().name(formView)).andReturn().getRequest().getSession(false);
        // The real GET ends its service transaction before the form is submitted.
        this.entityManager.clear();
        return session;
    }

    private void assertStoredDate(String sql, Integer id) {
        this.entityManager.flush();
        Date actual = new JdbcTemplate(this.dataSource).queryForObject(sql, Date.class, id);
        assertEquals(Date.valueOf("2024-02-29"), actual);
    }
}
