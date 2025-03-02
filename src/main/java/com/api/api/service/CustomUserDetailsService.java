package com.api.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.api.api.auth.CustomUserDetails;
import com.api.api.model.User;
import com.api.api.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    //Sobreescribimos el método loadUserByUsername para que Spring Security pueda cargar un usuario en base a su email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User appUser = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        //Convertimos el user de nuestra app a un objeto de Spring Security
        return new CustomUserDetails(appUser);
    }
    
}
