package com.demo.usuario.config;

import com.demo.usuario.entity.UserEntity;
import com.demo.usuario.entity.UserProfileEntity;
import com.demo.usuario.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner initUsers(UserRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String[] names = {"Antoni","Carlos","Lucía","Mateo","Valeria","Diego","Camila","Sebastián","Sofía","Andrés","María","Joaquín","Fernanda","Nicolás","Daniela","Bruno","Ana","Gabriel","Paula","Renato"};
            String[] professions = {"Estudiante","Desarrollador","Diseñadora","Arquitecto","Ingeniera","Analista","Médico","Profesor","QA Tester","Scrum Master"};
            for (int i = 1; i <= 20; i++) {
                UserEntity u = new UserEntity();
                u.setUsername("usuario" + i);
                u.setPassword(encoder.encode("1234"));
                u.setEnabled(true);
                UserProfileEntity p = new UserProfileEntity();
                p.setUser(u);
                p.setFirstName(names[i - 1]);
                p.setLastName("Demo " + i);
                p.setEmail("usuario" + i + "@demo.com");
                p.setPhone("+51 9" + String.format("%08d", 10000000 + i));
                p.setProfession(professions[(i - 1) % professions.length]);
                p.setDescription("Perfil de prueba del usuario " + i + " para la demo de GraphQL y microservicios.");
                p.setAvatarUrl("https://api.dicebear.com/9.x/adventurer/svg?seed=usuario" + i);
                u.setProfile(p);
                repository.save(u);
            }
        };
    }
}
