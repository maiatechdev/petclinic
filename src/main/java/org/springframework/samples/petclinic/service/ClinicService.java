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
package org.springframework.samples.petclinic.service;

import java.util.Collection;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;


/**
 * Business facade used by all PetClinic controllers.
 * <p>
 * Groups the operations on owners, pets, visits and vets and hides the repositories from the controllers. Data access
 * failures are reported as {@link DataAccessException}, without exposing the persistence technology.
 *
 * @author Michael Isvy
 */
public interface ClinicService {

    /**
     * Lists the pet types, used by the pet creation and update form.
     *
     * @return the pet types in alphabetical order
     * @throws DataAccessException if the query fails
     */
    Collection<PetType> findPetTypes() throws DataAccessException;

    /**
     * Finds an owner by id, with the owner's pets already loaded.
     *
     * @param id the owner id
     * @return the owner, or {@code null} if none exists with this id
     * @throws DataAccessException if the query fails
     */
    Owner findOwnerById(int id) throws DataAccessException;

    /**
     * Finds a pet by id.
     *
     * @param id the pet id
     * @return the pet, or {@code null} if none exists with this id
     * @throws DataAccessException if the query fails
     */
    Pet findPetById(int id) throws DataAccessException;

    /**
     * Creates a new pet or saves the changes to an existing one.
     *
     * @param pet the pet to save; a pet without id is inserted
     * @throws DataAccessException if the save fails
     */
    void savePet(Pet pet) throws DataAccessException;

    /**
     * Records a visit for a pet.
     *
     * @param visit the visit to save, already associated with its pet
     * @throws DataAccessException if the save fails
     */
    void saveVisit(Visit visit) throws DataAccessException;

    /**
     * Lists all vets with their specialties.
     *
     * @return all vets
     * @throws DataAccessException if the query fails
     */
    Collection<Vet> findVets() throws DataAccessException;

    /**
     * Creates a new owner or saves the changes to an existing one.
     *
     * @param owner the owner to save; an owner without id is inserted and receives a new id
     * @throws DataAccessException if the save fails
     */
    void saveOwner(Owner owner) throws DataAccessException;

    /**
     * Finds the owners whose last name starts with the given text.
     *
     * @param lastName the beginning of the last name; an empty string returns all owners
     * @return the matching owners with their pets, or an empty collection
     * @throws DataAccessException if the query fails
     */
    Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException;

}