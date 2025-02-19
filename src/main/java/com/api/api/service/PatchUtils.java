package com.api.api.service;

import com.api.api.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PatchUtils {
    private final ObjectMapper mapper;

    @Autowired
    public PatchUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    public <T> T patch(T data, List<Map<String, Object>> updates) throws JsonPatchException {
        try {
            //mapper.convertValue convierte la lista de mapas (updates) a un objeto JsonPatch, que representa un conjunto de operaciones JSON Patch.
            JsonPatch operations = mapper.convertValue(updates, JsonPatch.class);
            /*El objeto (data) se convierte en un JsonNode (una representación genérica de JSON). Esto facilita aplicar las operaciones
            de parche directamente sobre un objeto JSON sin modificar el objeto original.*/
            System.out.println("POLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLASSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS2");

            JsonNode json = mapper.convertValue(data, JsonNode.class);

            // Apply operations to the JSON node
            JsonNode updatedJson = operations.apply(json);

            // Volvemos a transformar o JSON nunha instancia de usuario empregando Jackson
            return (T) mapper.convertValue(updatedJson, data.getClass());
        } catch (JsonPatchException e) {
            throw new JsonPatchException("Error al aplicar JSON Patch: "+ e.getMessage());
        }
    }
}
