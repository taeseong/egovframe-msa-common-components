package egovframework.com.cop.brd.web;

import egovframework.com.cop.brd.service.EgovFileService;
import egovframework.com.cop.brd.service.FileVO;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EgovFileAPIControllerTest {

    @Test
    void fileDownloadReturnsNotFoundWhenFileInformationDoesNotExist() throws Exception {
        EgovFileService fileService = mock(EgovFileService.class);
        EgovEnvCryptoServiceImpl cryptoService = mock(EgovEnvCryptoServiceImpl.class);
        EgovFileAPIController controller = new EgovFileAPIController(fileService, cryptoService);

        when(cryptoService.decrypt("fileToken")).thenReturn("FILE_1|UNIQ_1|0");
        when(cryptoService.decrypt("uniqToken")).thenReturn("UNIQ_1");
        when(fileService.detailFileInf(any(FileVO.class))).thenReturn(null);

        FileVO fileVO = new FileVO();
        fileVO.setAtchFileId("fileToken");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-UNIQ-ID", "uniqToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.fileDownload(fileVO, request, response);

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
