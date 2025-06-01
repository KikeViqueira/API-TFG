package com.api.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.FlagEntityDTO;
import com.api.api.service.ConfigurationUserFlagsService;

@RestController
@PreAuthorize("hasPermission(#idUser, 'owner')")
@RequestMapping("/api/users")
public class ConfigurationUserFlagsController {

    @Autowired
    private ConfigurationUserFlagsService configurationUserFlagsService;

    //Endpoint para cambiar el valor de una bandera del rango posible de ellas
    @PutMapping("/{idUser}/flags/ConfigurationFlags/{flag}")
    public ResponseEntity<FlagEntityDTO> changeFlag(@PathVariable("idUser") Long idUser, @PathVariable("flag") String flag, @RequestBody Map<String, String> flagData) {
        FlagEntityDTO flagChanged = this.configurationUserFlagsService.changeFlag(idUser, flag, flagData.get("flagValue"));
        return ResponseEntity.ok(flagChanged);
    }
}
