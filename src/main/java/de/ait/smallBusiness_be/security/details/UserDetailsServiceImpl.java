package de.ait.smallBusiness_be.security.details;
import de.ait.smallBusiness_be.users.model.User;

import de.ait.smallBusiness_be.users.dao.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = usersRepository.findByEmail(email)

                .orElseThrow(() -> new UsernameNotFoundException("User with email <" + email + "> not found"));

        return new AuthenticatedUser(user);
    }
}
