/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;

/** Characterizes owner and visit requests against the legacy MVC configuration. */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("WebControllerTests-config.xml")
@ActiveProfiles("jdbc")
@Transactional
public class OwnerVisitControllerTests {

    private static final String OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String VISIT_FORM = "pets/createOrUpdateVisitForm";
    private static final String NEW_OWNER = "/owners/new";
    private static final int FRANKLIN_ID = 1;
    private static final int VISIT_PET_ID = 7;
    private static final String FRANKLIN_DETAILS = "/owners/" + FRANKLIN_ID;
    private static final String EDIT_OWNER = FRANKLIN_DETAILS + "/edit";
    private static final String VISIT_OWNER_DETAILS = "/owners/6";
    private static final String VISITS = VISIT_OWNER_DETAILS + "/pets/" + VISIT_PET_ID + "/visits";
    private static final String NEW_VISIT = VISITS + "/new";
    private static final String VALID_PHONE = "5551234567";
    private static final String INVALID_PHONE = "invalid";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

    /** Uses the same method override as the owner edit form in the application. */
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .addFilters(new HiddenHttpMethodFilter()).build();
    }

    /** A failed search returns the form with a last-name error. */
    @Test
    public void findsNoOwners() throws Exception {
        this.mockMvc.perform(get("/owners").param("lastName", "Unknown"))
                .andExpect(status().isOk()).andExpect(view().name("owners/findOwners"))
                .andExpect(model().attributeHasFieldErrors("owner", "lastName"));
    }

    /** A single result redirects to the owner's details. */
    @Test
    public void findsOneOwner() throws Exception {
        this.mockMvc.perform(get("/owners").param("lastName", "Frank"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(FRANKLIN_DETAILS));
    }

    /** Multiple results are displayed in the owner list. */
    @Test
    public void findsMultipleOwners() throws Exception {
        this.mockMvc.perform(get("/owners").param("lastName", "Dav"))
                .andExpect(status().isOk()).andExpect(view().name("owners/ownersList"))
                .andExpect(model().attribute("selections", hasSize(2)));
    }

    /** A search without a last-name parameter lists the fixture owners. */
    @Test
    public void listsOwnersWithoutLastName() throws Exception {
        this.mockMvc.perform(get("/owners"))
                .andExpect(status().isOk()).andExpect(view().name("owners/ownersList"))
                .andExpect(model().attribute("selections", hasSize(10)));
    }

    /** A valid form creates an owner and redirects to the generated ID. */
    @Test
    public void createsValidOwner() throws Exception {
        MockHttpSession session = openForm(NEW_OWNER, OWNER_FORM, "owner");
        Owner owner = (Owner) session.getAttribute("owner");
        this.mockMvc.perform(ownerForm(NEW_OWNER, session, VALID_PHONE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/" + owner.getId()));
        assertNotNull(owner.getId());
        assertNull(session.getAttribute("owner"));
    }

    /** An invalid creation form returns its validation error without assigning an ID. */
    @Test
    public void rejectsInvalidOwnerCreation() throws Exception {
        MockHttpSession session = openForm(NEW_OWNER, OWNER_FORM, "owner");
        this.mockMvc.perform(ownerForm(NEW_OWNER, session, INVALID_PHONE))
                .andExpect(status().isOk()).andExpect(view().name(OWNER_FORM))
                .andExpect(model().attributeHasFieldErrors("owner", "telephone"));
        assertNull(((Owner) session.getAttribute("owner")).getId());
    }

    /** A valid edit updates the existing owner. */
    @Test
    public void updatesValidOwner() throws Exception {
        MockHttpSession session = openForm(EDIT_OWNER, OWNER_FORM, "owner");
        this.mockMvc.perform(ownerForm(EDIT_OWNER, session, VALID_PHONE).param("_method", "PUT"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(FRANKLIN_DETAILS));
        assertEquals(VALID_PHONE, this.clinicService.findOwnerById(FRANKLIN_ID).getTelephone());
        assertNull(session.getAttribute("owner"));
    }

    /** An invalid edit shows the error and keeps the form in the session. */
    @Test
    public void rejectsInvalidOwnerUpdate() throws Exception {
        MockHttpSession session = openForm(EDIT_OWNER, OWNER_FORM, "owner");
        this.mockMvc.perform(ownerForm(EDIT_OWNER, session, INVALID_PHONE).param("_method", "PUT"))
                .andExpect(status().isOk()).andExpect(view().name(OWNER_FORM))
                .andExpect(model().attributeHasFieldErrors("owner", "telephone"));
        assertNotNull(session.getAttribute("owner"));
    }

    /** The visit form starts with a new, dated visit attached to the requested pet. */
    @Test
    public void opensVisitForm() throws Exception {
        MockHttpSession session = openForm(NEW_VISIT, VISIT_FORM, "visit");
        Visit visit = (Visit) session.getAttribute("visit");
        assertNull(visit.getId());
        assertNotNull(visit.getDate());
        assertEquals(Integer.valueOf(VISIT_PET_ID), visit.getPet().getId());
    }

    /** A valid visit is saved and redirects to the owner. */
    @Test
    public void savesValidVisit() throws Exception {
        MockHttpSession session = openForm(NEW_VISIT, VISIT_FORM, "visit");
        Visit visit = (Visit) session.getAttribute("visit");
        this.mockMvc.perform(post(NEW_VISIT).session(session).param("description", "Check-up"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(VISIT_OWNER_DETAILS));
        assertNotNull(visit.getId());
        assertNull(session.getAttribute("visit"));
    }

    /** A blank description keeps the unsaved visit on the form. */
    @Test
    public void rejectsVisitWithoutDescription() throws Exception {
        MockHttpSession session = openForm(NEW_VISIT, VISIT_FORM, "visit");
        this.mockMvc.perform(post(NEW_VISIT).session(session).param("description", ""))
                .andExpect(status().isOk()).andExpect(view().name(VISIT_FORM))
                .andExpect(model().attributeHasFieldErrors("visit", "description"));
        assertNull(((Visit) session.getAttribute("visit")).getId());
    }

    /** The obsolete URL reaches static fallback without invoking a controller or view. */
    @Test
    public void fallsBackForRemovedVisitList() throws Exception {
        MvcResult result = this.mockMvc.perform(get(VISITS)).andReturn();
        assertThat(result.getHandler(), instanceOf(DefaultServletHttpRequestHandler.class));
        assertNull(result.getModelAndView());
    }

    /** The owner's details still include the pet's visit history. */
    @Test
    public void showsVisitHistoryInOwnerDetails() throws Exception {
        MvcResult result = this.mockMvc.perform(get(VISIT_OWNER_DETAILS))
                .andExpect(status().isOk()).andExpect(view().name("owners/ownerDetails"))
                .andExpect(model().attributeExists("owner")).andReturn();
        Owner owner = (Owner) result.getModelAndView().getModel().get("owner");
        assertThat(owner.getPet("Samantha").getVisits(), hasSize(2));
    }

    private MockHttpSession openForm(String url, String formView, String attribute) throws Exception {
        return (MockHttpSession) this.mockMvc.perform(get(url))
                .andExpect(status().isOk()).andExpect(view().name(formView))
                .andExpect(model().attributeExists(attribute))
                .andReturn().getRequest().getSession(false);
    }

    private MockHttpServletRequestBuilder ownerForm(String url, MockHttpSession session, String phone) {
        return post(url).session(session).param("firstName", "Casey").param("lastName", "Morgan")
                .param("address", "10 Main Street").param("city", "Madison").param("telephone", phone);
    }
}
