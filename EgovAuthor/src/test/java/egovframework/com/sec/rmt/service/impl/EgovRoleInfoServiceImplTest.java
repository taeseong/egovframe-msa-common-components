package egovframework.com.sec.rmt.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import egovframework.com.sec.rmt.service.EgovRoleInfoService;
import egovframework.com.sec.rmt.service.RoleInfoVO;

@SpringBootTest
@Transactional
class EgovRoleInfoServiceImplTest {

    @Autowired
    EgovRoleInfoService roleInfoService;

    @Test
    void updatePreservesCreationDate() {
        RoleInfoVO role = new RoleInfoVO();
        role.setRoleNm("등록일 보존 테스트");
        role.setRolePttrn("/test/**");
        role.setRoleDc("수정 전");
        role.setRoleTy("url");
        role.setRoleSort("99");

        RoleInfoVO inserted = roleInfoService.insert(role);
        String creationDate = inserted.getRoleCreatDe();
        inserted.setRoleDc("수정 후");

        RoleInfoVO updated = roleInfoService.update(inserted);

        assertThat(updated.getRoleCreatDe()).isEqualTo(creationDate);
        assertThat(updated.getRoleDc()).isEqualTo("수정 후");
    }
}
