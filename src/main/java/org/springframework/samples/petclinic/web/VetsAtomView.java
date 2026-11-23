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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Vets;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

/**
 * Feed Atom dos veterinários, gerado para {@code /vets.atom}.
 * <p>
 * Registrada em {@code mvc-view-config.xml} com o nome de bean {@code vets/vetList.atom}. O content type da resposta
 * ({@code application/atom+xml}) é definido pela {@link AbstractAtomFeedView} depois que as entradas são montadas.
 *
 * @author Alef Arendsen
 * @author Arjen Poutsma
 */
public class VetsAtomView extends AbstractAtomFeedView {

    private static final String TAG_AUTHORITY = "tag:springsource.org";
    private static final String FEED_TITLE = "Veterinarians";

    /** Define o id e o título do feed */
    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
        feed.setId(TAG_AUTHORITY);
        feed.setTitle(FEED_TITLE);
    }

    /** Cria uma entrada do feed para cada veterinário do atributo {@code vets} do modelo. */
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model,
                                           HttpServletRequest request, HttpServletResponse response) {
        List<Vet> vetList = ((Vets) model.get(VetController.MODEL_VETS)).getVetList();
        List<Entry> entries = new ArrayList<Entry>(vetList.size());
        for (Vet vet : vetList) {
            entries.add(toEntry(vet));
        }
        return entries;
    }

    /** Monta a entrada do feed de um veterinário, com as especialidades no resumo. */
    private Entry toEntry(Vet vet) {
        Entry entry = new Entry();
        // see http://diveintomark.org/archives/2004/05/28/howto-atom-id#other
        entry.setId(String.format("%s,%s", TAG_AUTHORITY, vet.getId()));
        entry.setTitle(String.format("Vet: %s %s", vet.getFirstName(), vet.getLastName()));

        Content summary = new Content();
        summary.setValue(vet.getSpecialties().toString());
        entry.setSummary(summary);
        return entry;
    }

}