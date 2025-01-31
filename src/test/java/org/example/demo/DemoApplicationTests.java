package org.example.demo;

import jakarta.persistence.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTestWithProfile("test")
class DemoApplicationTests {

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Test
    @DisplayName("H2 데이터베이스 실행 테스트 ")
    @Transactional
    void contextLoads() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            DemoEntity demo = new DemoEntity();
            demo.name = "testing";
            em.persist(demo);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
        em.close();
    }

    @Entity
    static class DemoEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;
    }

}
