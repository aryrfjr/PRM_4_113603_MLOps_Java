package org.doi.prmv4p113603.mlops.data.request;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) for authentication requests.
 * <p>
 * This class is used to encapsulate the login credentials
 * (username and password) sent from the client to the backend.
 * </p>
 * <p>
 * Typically consumed by {@link org.doi.prmv4p113603.mlops.security.auth.AuthController}
 * in the {@code /auth/login} endpoint.
 */
@Getter
@Setter
public class AuthRequest {

    private String username;
    private String password;

}
