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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Vets;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
  * Shows the list of veterinarians.
 * <p>
 * The same handler serves {@code /vets.html}, {@code /vets.xml} and {@code /vets.atom}. The
 * {@code ContentNegotiatingViewResolver} in {@code mvc-view-config.xml} picks the JSP, the XML view or
 * {@link VetsAtomView} from the URL extension; without an extension the response is always HTML.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
public class VetController {

    /** Logical view name shared by the HTML, XML and Atom representations. */
    static final String VIEWS_VET_LIST = "vets/vetList";

    /** Model attribute read by the JSP, the XML view and {@link VetsAtomView}. */
    static final String MODEL_VETS = "vets";

    private final ClinicService clinicService;

    /**
     * Creates the controller.
     *
     * @param clinicService service that loads the veterinarians
     */
    @Autowired
    public VetController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Puts all veterinarians in the model.
     *
     * @param model model that receives the {@value #MODEL_VETS} attribute
     * @return the logical view name, {@value #VIEWS_VET_LIST}
     */
    @RequestMapping("/vets")
    public String showVetList(Map<String, Object> model) {
        // Here we are returning an object of type 'Vets' rather than a collection of Vet objects 
        // so it is simpler for Object-Xml mapping
        Vets vets = new Vets();
        vets.getVetList().addAll(this.clinicService.findVets());
        model.put(MODEL_VETS, vets);
        return VIEWS_VET_LIST;
    }

}