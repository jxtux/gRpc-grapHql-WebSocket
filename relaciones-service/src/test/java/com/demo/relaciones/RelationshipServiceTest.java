package com.demo.relaciones;

import com.demo.relaciones.service.RelationshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RelationshipServiceTest {
    @Autowired RelationshipService relationshipService;

    @Test
    void usuario1DebeTenerCincoRelacionesDePrueba() {
        var relations = relationshipService.getByUser(1L);
        assertThat(relations).hasSize(5);
    }
}
