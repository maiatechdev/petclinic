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

import java.util.Collection;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
@SessionAttributes(types = Owner.class)
public class OwnerController {

    private static final String VIEW_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String VIEW_FIND_OWNERS = "owners/findOwners";
    private static final String VIEW_OWNERS_LIST = "owners/ownersList";
    private static final String VIEW_OWNER_DETAILS = "owners/ownerDetails";
    private static final String REDIRECT_OWNER_DETAILS = "redirect:/owners/";

    private final ClinicService clinicService;

    /** Creates a controller backed by the clinic service. */
    @Autowired
    public OwnerController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /** Prevents a form submission from changing an owner's ID. */
    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    /** Opens the form with a new owner. */
    @RequestMapping(value = "/owners/new", method = RequestMethod.GET)
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEW_OWNER_FORM;
    }

    /** On successful creation, redirects to the newly assigned owner ID. */
    @RequestMapping(value = "/owners/new", method = RequestMethod.POST)
    public String processCreationForm(@Valid Owner owner, BindingResult result, SessionStatus status) {
        return saveValidOwner(owner, result, status) ? REDIRECT_OWNER_DETAILS + owner.getId() : VIEW_OWNER_FORM;
    }

    /** Opens the owner search form. */
    @RequestMapping(value = "/owners/find", method = RequestMethod.GET)
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        return VIEW_FIND_OWNERS;
    }

    /** Searches by last-name prefix and selects the appropriate result view. */
    @RequestMapping(value = "/owners", method = RequestMethod.GET)
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {
        Collection<Owner> results = findOwnersByLastName(owner);
        if (results.isEmpty()) {
            result.rejectValue("lastName", "notFound");
            return VIEW_FIND_OWNERS;
        }
        return showMatchingOwners(results, model);
    }

    /** Loads the existing owner into the edit form. */
    @RequestMapping(value = "/owners/{ownerId}/edit", method = RequestMethod.GET)
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        model.addAttribute(owner);
        return VIEW_OWNER_FORM;
    }

    /** On successful update, redirects using the owner ID from the request path. */
    @RequestMapping(value = "/owners/{ownerId}/edit", method = RequestMethod.PUT)
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, SessionStatus status) {
        return saveValidOwner(owner, result, status) ? REDIRECT_OWNER_DETAILS + "{ownerId}" : VIEW_OWNER_FORM;
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelAndView with the model attributes for the view
     */
    @RequestMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        ModelAndView mav = new ModelAndView(VIEW_OWNER_DETAILS);
        mav.addObject(this.clinicService.findOwnerById(ownerId));
        return mav;
    }

    private boolean saveValidOwner(Owner owner, BindingResult result, SessionStatus status) {
        if (result.hasErrors()) {
            return false;
        }
        this.clinicService.saveOwner(owner);
        status.setComplete();
        return true;
    }

    private Collection<Owner> findOwnersByLastName(Owner owner) {
        if (owner.getLastName() == null) {
            owner.setLastName(""); // An empty prefix lists every owner.
        }
        return this.clinicService.findOwnerByLastName(owner.getLastName());
    }

    private String showMatchingOwners(Collection<Owner> results, Map<String, Object> model) {
        if (results.size() == 1) {
            return REDIRECT_OWNER_DETAILS + results.iterator().next().getId();
        }
        model.put("selections", results);
        return VIEW_OWNERS_LIST;
    }

}
