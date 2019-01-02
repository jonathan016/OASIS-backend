package com.oasis.web_controller.test;

import com.oasis.model.constant.service_constant.RoleConstant;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UserNotAuthenticatedException;
import com.oasis.service.api.entry_point.EntryPointServiceApi;
import com.oasis.service.tool.helper.ActiveComponentManager;
import com.oasis.web_controller.configuration.MvcConfiguration;
import com.oasis.web_controller.configuration.WebSecurityConfiguration;
import com.oasis.web_controller.mapper.response.EntryPointResponseMapper;
import com.oasis.web_controller.mapper.response.FailedResponseMapper;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.failed.FailedResponse;
import com.oasis.web_model.response.success.entry_point.EntryPointResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.UNAUTHENTICATED_USER;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebSecurityConfiguration.class, MvcConfiguration.class })
public class EntryPointControllerTest {

    final private String incorrectMappingOrMethodMessage = "Incorrect mapping/method!";

    private MockMvc mockMvc;

    @Mock
    private EntryPointResponseMapper entryPointResponseMapper;
    @Mock
    private FailedResponseMapper failedResponseMapper;

    @Mock
    private EntryPointServiceApi entryPointServiceApi;

    @Mock
    private ActiveComponentManager activeComponentManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void init() {

        initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    public void getLoginData_UserIsAuthenticatedWithoutPhoto_ReturnsLoginResponse()
            throws
            Exception {

        final String username = "admin";
        final String password = "GDNadmin";
        final String name = "Administrator";
        final String role = RoleConstant.ROLE_ADMINISTRATOR;

        mockLoginRequest(username, name,
                         "\"http://localhost:8085/oasis/api/employees/jonathan/photo_not_found?extension=jpg\"",
                         role
        );
        mockSuccessResponse(username, name, role);

        mockMvc.perform(post("/api/login").with(httpBasic(username, password))).andExpect(status().isOk())
               .andExpect(
                       content().string("{\"code\":200,\"success\":\"true\",\"components\":null," +
                                        "\"value\":{\"username\":\"" + username + "\",\"name\":\"" + name + "\"," +
                                        "\"photo\":\"http://localhost:8085/oasis/api/employees/admin" +
                                        "/photo_not_found?extension=jpg\",\"role\":\"" + role + "\"}}"
                       ));
    }

    @Test
    public void returnIncorrectMappingCalls_UnauthenticatedUserRequestedIncorrectMappingOrMethod_ReturnsFailedResponse()
            throws
            Exception {

        mockLoginFailedResponse(HttpStatus.UNAUTHORIZED, UNAUTHENTICATED_USER.getErrorCode(),
                                UNAUTHENTICATED_USER.getErrorMessage()
        );

        mockMvc.perform(get("/api/test")).andExpect(status().isUnauthorized()).andExpect(
                content().string(
                        "{\"code\":401,\"success\":\"false\",\"components\":null," +
                        "\"value\":{\"errorCode\":\"UNAUTHENTICATED_USER\",\"errorMessage\":\"User is not " +
                        "authenticated\"}}"
                ));
    }

    @Test
    @WithMockUser(username = "admin",
                  password = "GDNadmin",
                  authorities = RoleConstant.ROLE_ADMINISTRATOR)
    public void returnIncorrectMappingCalls_AuthenticatedUserRequestedIncorrectMappingOrMethod_ReturnsFailedResponse()
            throws
            Exception {

        mockLoginFailedResponse(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.name(), incorrectMappingOrMethodMessage);

        mockMvc.perform(get("/api/login")).andExpect(status().isBadRequest()).andExpect(
                content().string(
                        "{\"code\":400,\"success\":\"false\",\"components\":null," +
                        "\"value\":{\"errorCode\":\"BAD_REQUEST\",\"errorMessage\":\"Incorrect mapping/method!\"}}"
                ));

        //        verify(failedResponseMapper, times(1)).produceFailedResult(HttpStatus.BAD_REQUEST.value(),
        //                                                                   HttpStatus.BAD_REQUEST.name(),
        //                                                                   incorrectMappingOrMethodMessage, null
        //        );
        //        verifyNoMoreInteractions(failedResponseMapper);
    }

    private void mockLoginRequest(final String username, final String name, final String photoURL, final String role)
            throws
            BadRequestException,
            UserNotAuthenticatedException,
            DataNotFoundException {

        Map< String, String > data = new HashMap<>();

        data.put("username", username);
        data.put("name", name);
        data.put("photo", photoURL);
        data.put("role", role);

        when(entryPointServiceApi.getLoginData(username)).thenReturn(data);
    }

    private void mockSuccessResponse(final String username, final String name, final String role) {

        NoPagingResponse< EntryPointResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new EntryPointResponse(username, name,
                                                        "http://localhost:8085/oasis/api/employees/" + username +
                                                        "/photo_not_found?extension=jpg", role
        ));

        when(entryPointResponseMapper.produceLoginSuccessResponse(HttpStatus.OK.value(), username, name,
                                                                  "http://localhost:8085/oasis/api/employees/" +
                                                                  username + "/photo_not_found?extension=jpg", role
        )).thenReturn(successResponse);
    }

    private void mockLoginFailedResponse(
            final HttpStatus httpstatus, final String errorCode, final String errorMessage
    ) {

        NoPagingResponse< FailedResponse > failedResponseSample = new NoPagingResponse<>();

        failedResponseSample.setCode(httpstatus.value());
        failedResponseSample.setSuccess(ResponseStatus.FAILED);
        failedResponseSample.setComponents(null);
        failedResponseSample.setValue(new FailedResponse(errorCode, errorMessage));

        when(failedResponseMapper.produceFailedResult(httpstatus.value(), errorCode, errorMessage, null)).thenReturn(
                failedResponseSample);
    }

}
