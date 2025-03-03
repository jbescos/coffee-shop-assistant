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
package com.example.coffee.shop.assistant.ai;

import java.util.logging.Logger;

import com.example.coffee.shop.assistant.data.MenuItem;
import com.example.coffee.shop.assistant.data.MenuItemsService;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A simple ingestor that populates the embedding store with menu items.
 *
 * This service reads menu items from a JSON file, converts them into text-based
 * representations, generates embeddings using an {@link EmbeddingModel}, and
 * stores them in the specified {@link EmbeddingStore}.
 */
@ApplicationScoped
public class MenuItemsIngestor {
    private static final Logger LOGGER = Logger.getLogger(MenuItemsIngestor.class.getName());

    private final MenuItemsService menuItemsService;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * Constructs a {@code MenuItemsIngestor} instance.
     *
     * @param embeddingStore    the embedding store where generated embeddings are stored
     * @param embeddingModel    the embedding model used for generating embeddings
     * @param menuItemsService  the service for retrieving menu items from a JSON file
     */
    @Inject
    public MenuItemsIngestor(MenuItemsService menuItemsService,
                             EmbeddingModel embeddingModel,
                             @Named("EmbeddingStore") EmbeddingStore<TextSegment> embeddingStore) {
        this.menuItemsService = menuItemsService;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * Produces the embedding model used for generating embeddings.
     *
     * @return a new instance of {@link AllMiniLmL6V2EmbeddingModel}
     */
    @Produces
    @ApplicationScoped
    public EmbeddingModel produceEmbeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * Produces the embedding store where embeddings are stored.
     *
     * @return an instance of {@link InMemoryEmbeddingStore}
     */
    @Produces
    @ApplicationScoped
    @Named("EmbeddingStore")
    public EmbeddingStore<TextSegment> produceEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Initializes the embedding store by processing menu items.
     *
     * This method is triggered automatically after the CDI {@link ApplicationScoped} context
     * is fully initialized. It retrieves menu items from the configured source, converts them
     * into text representations, generates embeddings using the provided {@link EmbeddingModel},
     * and stores them in the {@link EmbeddingStore}.
     *
     * @param initEvent an initialization event indicating that the {@link ApplicationScoped}
     *                  context has been fully initialized (not used in the method)
     */
    public void ingest(@Observes @Initialized(ApplicationScoped.class) Object initEvent) {
        // Create ingestor with given embedding model and embedding storage
        var ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // Read menu items from JSON
        var menuItems = menuItemsService.getMenuItems();

        // Create text representations of menu items
        var documents = menuItems.stream()
                .map(this::generateDocument)
                .toList();

        // Feed it to the ingestor to create embeddings and store them in embedding storage
        ingestor.ingest(documents);

        LOGGER.info("Ingested menu items: " + documents.size());
    }

    /**
     * Converts a {@link MenuItem} into a text-based document for embedding generation.
     *
     * @param item the menu item to convert
     * @return a {@link Document} containing a formatted text representation of the menu item
     */
    private Document generateDocument(MenuItem item) {
        var str = String.format(
                "%s: %s. Category: %s. Price: $%.2f. Tags: %s. Add-ons: %s.",
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getPrice(),
                String.join(", ", item.getTags()),
                String.join(", ", item.getAddOns())
        );

        return Document.from(str);
    }
}
