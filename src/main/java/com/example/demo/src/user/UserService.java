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
import org.springframework.transaction.annotation.*;

import javax.sql.DataSource;

import java.util.List;
import java.util.Optional;
import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
@Transactional
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserProvider userProvider;
    private final JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    public UserService(UserProvider userProvider, JwtService jwtService) {
        this.userProvider = userProvider;
        this.jwtService = jwtService;
    }

    public UserDto createUser(SaveUserDto user) throws BaseException {
        try{
            String pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(user.getPassword());
            user.setPassword(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        try{
            User saveUser = userRepository.save(user.toEntity());
            UserDto result = new UserDto(saveUser);
            String jwt = jwtService.createJwt(saveUser.getIdx());
            result.setJwt(jwt);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void createBook(SaveBookDto saveBookDto, Integer userIdx) throws BaseException {
        try{
            Optional<User> result = userRepository.findById(userIdx);
            if (result.isPresent()) {
                User user = result.get();
                user.setBook(saveBookDto);
                userRepository.save(user);
            }

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
