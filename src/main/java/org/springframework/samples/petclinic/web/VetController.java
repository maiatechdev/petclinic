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
 * Exibe a lista de veterinários.
 * <p>
 * O mesmo método atende {@code /vets.html}, {@code /vets.xml} e {@code /vets.atom}. O
 * {@code ContentNegotiatingViewResolver} de {@code mvc-view-config.xml} escolhe o JSP, a view XML ou a
 * {@link VetsAtomView} pela extensão da URL; sem extensão, a resposta é sempre HTML.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
public class VetController {

    /** Nome lógico da view, compartilhado pelas representações HTML, XML e Atom. */
    static final String VIEWS_VET_LIST = "vets/vetList";

    /** Atributo do modelo lido pelo JSP, pela view XML e pela {@link VetsAtomView}. */
    static final String MODEL_VETS = "vets";

    private final ClinicService clinicService;

    /**
     * Cria o controller.
     *
     * @param clinicService serviço que consulta os veterinários
     */
    @Autowired
    public VetController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Coloca todos os veterinários no modelo.
     *
     * @param model modelo que recebe o atributo {@value #MODEL_VETS}
     * @return o nome lógico da view, {@value #VIEWS_VET_LIST}
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