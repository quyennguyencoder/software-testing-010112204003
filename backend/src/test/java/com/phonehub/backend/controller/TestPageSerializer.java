package com.phonehub.backend.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;

public class TestPageSerializer extends JsonSerializer<Page> {
    
    @Override
    public void serialize(Page value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("content", value.getContent());
        gen.writeNumberField("totalElements", value.getTotalElements());
        gen.writeNumberField("totalPages", value.getTotalPages());
        gen.writeNumberField("size", value.getSize());
        gen.writeNumberField("number", value.getNumber());
        gen.writeBooleanField("first", value.isFirst());
        gen.writeBooleanField("last", value.isLast());
        gen.writeEndObject();
    }

    public static MappingJackson2HttpMessageConverter createPageMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addSerializer(Page.class, new TestPageSerializer());
        mapper.registerModule(module);
        return new MappingJackson2HttpMessageConverter(mapper);
    }
}
