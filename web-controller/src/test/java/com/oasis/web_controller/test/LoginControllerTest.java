package com.oasis.web_controller.test;

import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.response_mapper.LoginResponseMapper;
import com.oasis.service.api.login.LoginServiceApi;
import com.oasis.tool.helper.ActiveComponentManager;
import com.oasis.web_controller.LoginController;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.failed.FailedResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class LoginControllerTest {

    final private String incorrectMappingOrMethodMessage = "Incorrect mapping/method!";

    private MockMvc mockMvc;

    @InjectMocks
    private LoginController loginController;

    @Mock
    private LoginResponseMapper loginResponseMapper;
    @Mock
    private FailedResponseMapper failedResponseMapper;

    @Mock
    private LoginServiceApi loginServiceApi;

    @Mock
    private ActiveComponentManager activeComponentManager;

    @Before
    public void init() {

        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    public void returnIncorrectMappingCalls_IncorrectMappingOrMethodRequested_ReturnsFailedResponse()
            throws
            Exception {

        mockFailedResponse();

        mockMvc.perform(get("/api/test")).andExpect(status().isBadRequest()).andExpect(
                content().string(
                        "{\"code\":400,\"success\":\"false\",\"components\":null," +
                        "\"value\":{\"errorCode\":\"BAD_REQUEST\",\"errorMessage\":\"Incorrect mapping/method!\"}}"
                ));

        verify(failedResponseMapper, times(1)).produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                   HttpStatus.BAD_REQUEST.name(),
                                                                   incorrectMappingOrMethodMessage, null
        );
        verifyNoMoreInteractions(failedResponseMapper);
    }

    private void mockFailedResponse() {

        NoPagingResponse< FailedResponse > failedResponseSample = new NoPagingResponse<>();

        failedResponseSample.setCode(HttpStatus.BAD_REQUEST.value());
        failedResponseSample.setSuccess(ResponseStatus.FAILED);
        failedResponseSample.setComponents(null);
        failedResponseSample.setValue(
                new FailedResponse(HttpStatus.BAD_REQUEST.name(), incorrectMappingOrMethodMessage));

        when(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                                                      incorrectMappingOrMethodMessage, null
        )).thenReturn(failedResponseSample);
    }

}
