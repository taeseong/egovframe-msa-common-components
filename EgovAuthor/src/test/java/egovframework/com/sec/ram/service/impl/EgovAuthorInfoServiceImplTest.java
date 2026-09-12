package egovframework.com.sec.ram.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import egovframework.com.sec.ram.service.AuthorInfoVO;
import egovframework.com.sec.ram.service.EgovAuthorManageService;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class EgovAuthorInfoServiceImplTest {

    @Autowired
    EgovAuthorManageService authorManageService;

    @Autowired
    EntityManager entityManager;

    @Test
    void updatePreservesCreationDate() {
        AuthorInfoVO authority = new AuthorInfoVO();
        authority.setAuthorCode("ROLE_CREATION_DATE_TEST");
        authority.setAuthorNm("등록일 보존 테스트");
        authority.setAuthorDc("수정 전");
        AuthorInfoVO inserted = authorManageService.insert(authority);
        String creationDate = inserted.getAuthorCreatDe();
        entityManager.flush();
        entityManager.clear();

        inserted.setOriginalAuthorCode(inserted.getAuthorCode());
        inserted.setAuthorDc("수정 후");
        authorManageService.update(inserted);
        entityManager.flush();
        entityManager.clear();
        AuthorInfoVO updated = authorManageService.detail(inserted, java.util.Map.of("uniqId", "TEST"));

        assertThat(updated.getAuthorCreatDe()).isEqualTo(creationDate);
        assertThat(updated.getAuthorDc()).isEqualTo("수정 후");
    }
}
