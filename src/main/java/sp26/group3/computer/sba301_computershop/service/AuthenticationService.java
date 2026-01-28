package sp26.group3.computer.sba301_computershop.service;

import com.nimbusds.jose.JOSEException;
import sp26.group3.computer.sba301_computershop.dto.request.AuthenticationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.IntrospectRequest;
import sp26.group3.computer.sba301_computershop.dto.request.LogoutRequest;
import sp26.group3.computer.sba301_computershop.dto.request.RefreshRequest;
import sp26.group3.computer.sba301_computershop.dto.response.AuthenticationResponse;
import sp26.group3.computer.sba301_computershop.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticated(AuthenticationRequest request);

    IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException;

    void logout(LogoutRequest request)
            throws ParseException, JOSEException;

    AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException;
}
