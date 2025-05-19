package org.example.demo.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class BulkInsertRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //@PostConstruct
    @Transactional
    public void runBulkInsert() {
        long start = System.currentTimeMillis();

        // 1. 테이블/DB 설정 최적화
        jdbcTemplate.execute("ALTER TABLE prj.posts DISABLE KEYS");
        jdbcTemplate.execute("SET foreign_key_checks = 0");
        jdbcTemplate.execute("SET unique_checks = 0");

        // 2. 벌크 인서트
        final int START_ID = 100; // PK 시작값
        final int TOTAL_ROWS = 1_000_000;
        final int BATCH_SIZE = 1000;
        String sql = "INSERT INTO prj.posts " +
                "(created_at, id, post_category_id, updated_at, user_id, created_by, title, updated_by, content) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (int i = 0; i < TOTAL_ROWS; i += BATCH_SIZE) {
            int batchStartId = START_ID + i;
            int currentBatchSize = Math.min(BATCH_SIZE, TOTAL_ROWS - i);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int j) throws SQLException {
                    int currentId = batchStartId + j;
                    Timestamp now = new Timestamp(System.currentTimeMillis());

                    // 값 설정 (고정값 적용)
                    ps.setTimestamp(1, now);       // created_at
                    ps.setInt(2, currentId);       // id
                    ps.setInt(3, 1);               // post_category_id (고정값)
                    ps.setTimestamp(4, now);       // updated_at
                    ps.setInt(5, 1);               // user_id (고정값)
                    ps.setString(6, "user_" + currentId); // created_by
                    ps.setString(7, "Title " + currentId); // title
                    ps.setString(8, "editor_" + currentId); // updated_by
                    ps.setString(9, "content");    // content
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            // 메모리 관리 (선택)
            if ((i / BATCH_SIZE) % 100 == 0) {
                System.gc();
            }
        }

        // 3. 후처리 (인덱스, 체크 복구)
        jdbcTemplate.execute("ALTER TABLE prj.posts ENABLE KEYS");
        jdbcTemplate.execute("SET foreign_key_checks = 1");
        jdbcTemplate.execute("SET unique_checks = 1");
        jdbcTemplate.execute("OPTIMIZE TABLE prj.posts");

        long end = System.currentTimeMillis();
        System.out.println("총 소요 시간: " + ((end - start) / 1000) + "초");
    }
}