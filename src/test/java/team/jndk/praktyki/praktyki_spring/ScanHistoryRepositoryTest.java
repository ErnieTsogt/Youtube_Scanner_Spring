package team.jndk.praktyki.praktyki_spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import team.jndk.praktyki.praktyki_spring.model.data.ScanHistory;
import team.jndk.praktyki.praktyki_spring.model.data.ScanStatus;
import team.jndk.praktyki.praktyki_spring.repository.ScanHistoryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest(properties = "spring.flyway.enabled=false")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ScanHistoryRepositoryTest {

    @Autowired
    private ScanHistoryRepository scanHistoryRepository;

    @Test
    public void saveAndFindScanHistory() {
        // Save a ScanHistory without channel to avoid DDL differences in test DB
        ScanHistory h = new ScanHistory(null, System.currentTimeMillis(), ScanStatus.SUCCESS, "ok", 5);
        ScanHistory saved = scanHistoryRepository.save(h);

        assertThat(saved.getId()).isNotNull();
        assertThat(scanHistoryRepository.findAll()).isNotEmpty();
    }
}
