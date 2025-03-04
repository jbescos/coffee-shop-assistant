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

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.example.coffee.shop.assistant.data.OrderService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

/**
 * AI services producers.
 */
@ApplicationScoped
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties CONFIG = new Properties();

    static {
        try {
            StringBuilder builder = new StringBuilder();
            CONFIG.load(AppConfig.class.getResourceAsStream("/ai.properties"));
            CONFIG.forEach((key, value) -> builder.append(key + " = " + value));
            LOGGER.info(builder.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load ai.properties", e);
        }
    }

    @Produces
    public ChatAiService createChatAiService(OrderService orderService) {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(CONFIG.getProperty("openai.apikey"))
                .modelName(CONFIG.getProperty("openai.model"))
                .temperature(Double.parseDouble(CONFIG.getProperty("openai.temperature")))
                .build();
        return AiServices.builder(ChatAiService.class).chatLanguageModel(model).tools(orderService).build();
    }
}
