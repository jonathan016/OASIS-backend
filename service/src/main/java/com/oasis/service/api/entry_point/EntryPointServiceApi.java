package com.oasis.service.api.entry_point;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UserNotAuthenticatedException;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface EntryPointServiceApi {

    Map< String, String > getLoginData(
            final String username
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UserNotAuthenticatedException;

    void logout(final HttpSession session, final User user);

}
