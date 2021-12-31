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
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import static com.example.demo.config.BaseResponseStatus.*;


//Provider : Read의 비즈니스 로직 처리
@Service
@Transactional
public class UserProvider {

    private final JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PetRepository petRepository;

    @Autowired
    DiaryListRepository diaryListRepository;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public UserDto getUser(Integer userIdx) throws BaseException {
        try {
            Optional<User> result = userRepository.findByIdx(userIdx);
            if (result.isPresent()) {
                return new UserDto(result.get());
            }
            else {
                return null;
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public boolean chkUser(Integer userIdx) throws BaseException {
        try {
            return userRepository.existsById(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public UserDto login(SaveUserDto user) throws BaseException{
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
            UserDto result = new UserDto(chk.get());
            return result;
        }
        else{
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    public List<PetDto> getPetbyId(Integer userIdx) throws BaseException{
        try{
            List<PetDto> result = new ArrayList<>();
            Optional<User> user = userRepository.findByIdx(userIdx);
            if (user.isPresent()) {
                user.get().getPets().forEach(p -> {
                    result.add(new PetDto(p));
                });
            }

            return result;
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<DiaryListDto> getDiaryList(Integer userIdx) throws BaseException {
        try{
            User user = new User(userIdx);
            List<DiaryList> result = diaryListRepository.findByUser(user);
            List<DiaryListDto> list = new ArrayList<>();
            result.forEach(d -> {
                list.add(new DiaryListDto(d));
            });
            return list;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
