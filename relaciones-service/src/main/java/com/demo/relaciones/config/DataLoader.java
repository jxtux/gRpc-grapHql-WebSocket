package com.demo.relaciones.config;

import com.demo.relaciones.entity.*;
import com.demo.relaciones.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner initRelations(RelationshipStatusRepository statusRepo, UserRelationshipRepository relRepo) {
        return args -> {
            if (statusRepo.count() == 0) {
                createStatus(statusRepo, "PENDING", "Pendiente", "Solicitud o relación pendiente");
                createStatus(statusRepo, "ACCEPTED", "Aceptado", "Relación aceptada");
                createStatus(statusRepo, "BLOCKED", "Bloqueado", "Relación bloqueada");
            }
            if (relRepo.count() > 0) return;
            var accepted = statusRepo.findByCode("ACCEPTED").orElseThrow();
            var pending = statusRepo.findByCode("PENDING").orElseThrow();
            var blocked = statusRepo.findByCode("BLOCKED").orElseThrow();
            createRel(relRepo, 1L, 2L, RelationshipType.FRIEND, accepted);
            createRel(relRepo, 1L, 3L, RelationshipType.FRIEND, pending);
            createRel(relRepo, 1L, 4L, RelationshipType.FOLLOWER, accepted);
            createRel(relRepo, 1L, 5L, RelationshipType.FOLLOWER, accepted);
            createRel(relRepo, 1L, 6L, RelationshipType.FRIEND, blocked);
            // Algunas relaciones extra para probar otros usuarios
            createRel(relRepo, 2L, 1L, RelationshipType.FRIEND, accepted);
            createRel(relRepo, 2L, 7L, RelationshipType.FOLLOWER, accepted);
        };
    }

    private void createStatus(RelationshipStatusRepository repo, String code, String name, String description) {
        RelationshipStatusEntity s = new RelationshipStatusEntity();
        s.setCode(code); s.setName(name); s.setDescription(description); repo.save(s);
    }

    private void createRel(UserRelationshipRepository repo, Long source, Long target, RelationshipType type, RelationshipStatusEntity status) {
        UserRelationshipEntity r = new UserRelationshipEntity();
        r.setSourceUserId(source); r.setTargetUserId(target); r.setRelationshipType(type); r.setStatus(status); repo.save(r);
    }
}
