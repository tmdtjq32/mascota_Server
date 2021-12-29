package com.example.demo.src.user;


import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.AES128;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;
import static com.example.demo.config.BaseResponseStatus.*;


//Provider : Read의 비즈니스 로직 처리
@Service
@Transactional
public class UserProvider {

    private final JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public User getUser(Integer userIdx) throws BaseException {
        try {
            Optional<User> result = userRepository.findById(userIdx);
            if (result.isPresent()) {
                User user = result.get();
                return user;
            }
            else {
                return null;
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public ResponseUserDto login(SaveUserDto user) throws BaseException{
        Optional<User> chk = userRepository.findById(user.getId());
        String chkPassword, password;
        if (chk.isPresent()) {
            chkPassword = chk.get().getPassword();
        }
        else{
            throw new BaseException(NONE_USER_EXIST);
        }
        try {
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(chkPassword);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }

        if(user.getPassword().equals(password)){
            String jwt = jwtService.createJwt(chk.get().getIdx());
            chk.get().setJwt(jwt);
            ResponseUserDto result = new ResponseUserDto(chk.get());
            return result;
        }
        else{
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }


}
