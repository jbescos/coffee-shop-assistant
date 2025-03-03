/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.coffee.shop.assistant.data;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.enterprise.context.ApplicationScoped;

/**
 * A service for managing and retrieving menu items from a JSON file.
 *
 * This service loads menu items from a specified JSON file path, which is provided
 * via the application configuration.
 */
@ApplicationScoped
public class MenuItemsService {

    private static final String MENU = "/menu.json";
    /**
     * Retrieves the list of menu items from the JSON file.
     *
     * @return a list of {@link MenuItem} objects
     * @throws RuntimeException if an error occurs while reading or parsing the file
     */
    public List<MenuItem> getMenuItems() {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(getClass().getResourceAsStream(MENU), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read menu items from file: " + MENU, e);
        }
    }
}
