package com.api.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.api.DTO.FlagEntityDTO;
import com.api.api.service.DailyUserFlagsService;

@RestController
@PreAuthorize("hasPermission(#idUser, 'owner')")
@RequestMapping("/api/users")
public class DailyUserFlagsController {

    @Autowired
    private DailyUserFlagsService dailyUserFlagsService;

    //Endpoint para insertar una bandera diaria
    @PostMapping("/{idUser}/flags/DailyFlags/{flag}")
    public ResponseEntity<FlagEntityDTO> insertDailyFlag(@PathVariable("idUser") Long idUser, @PathVariable("flag") String flag, @RequestBody Map<String, String> flagData) {
        FlagEntityDTO flagChanged = this.dailyUserFlagsService.insertFlag(idUser, flag, flagData.get("flagValue"));
        return ResponseEntity.ok(flagChanged);
    }

    //Endpoint para eliminar una bandera diaria
    @DeleteMapping("/{idUser}/flags/DailyFlags/{flag}")
    public ResponseEntity<FlagEntityDTO> deleteDailyFlag(@PathVariable("idUser") Long idUser, @PathVariable("flag") String flag) {
        FlagEntityDTO flagDeleted = this.dailyUserFlagsService.deleteFlag(idUser, flag);
        return ResponseEntity.ok(flagDeleted);
    }
}
