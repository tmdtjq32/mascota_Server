package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.model.*;
import com.example.demo.src.repository.*;
import com.example.demo.src.specification.*;
import com.example.demo.utils.AES128;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.data.jpa.domain.Specification;

import javax.sql.DataSource;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
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
    PetRepository petRepository;

    @Autowired
    DiaryRepository diaryRepository;

    @Autowired
    MoodRepository moodRepository;

    @Autowired
    public UserService(UserProvider userProvider, JwtService jwtService) {
        this.userProvider = userProvider;
        this.jwtService = jwtService;
    }

    public ResponseUser createUser(SaveUserDto user) throws BaseException {
        Optional<User> chk = userRepository.selectById(user.getId());
        if (chk.isPresent()){
            throw new BaseException(ALREADY_USER_EXIST);
        }
        try{
            String pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(user.getPassword());
            user.setPassword(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        try{
            User saveUser = userRepository.save(user.toEntity());
            ResponseUser result = new ResponseUser(saveUser);
            String jwt = jwtService.createJwt(saveUser.getIdx());
            result.setJwt(jwt);
            return result;
        } catch (Exception exception) {
            if (exception instanceof BaseException){
                throw (BaseException)exception;
            }
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
            if (exception instanceof BaseException){
                throw (BaseException)exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public boolean modifyPassword(SaveUserDto saveUserDto) throws BaseException {
        String confirm, newPassword;
        try{
            confirm = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(saveUserDto.getPassword());
            newPassword = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(saveUserDto.getUpdatepassword());
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        try{
            Optional<User> result = userRepository.selectById(saveUserDto.getId());
            if (result.isPresent()) {
                User user = result.get();
                if (!user.getPassword().equals(confirm)){
                    return false;
                }
                else{
                    user.setPassword(newPassword);
                    userRepository.save(user);
                    return true;
                }
            }
            return false;
        } catch(Exception exception){
            if (exception instanceof BaseException){
                throw (BaseException)exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<PetDto> createPet(PetDto pet, Integer userIdx) throws BaseException {
        try{
            Optional<User> result = userRepository.findByIdx(userIdx);
            if (result.isPresent()) {
                User user = result.get();
                int init = user.getPets().size();
                Pet updatePet = new Pet(user, pet);
                for (Pet p : user.getPets()){
                    if (p.getName().equals(updatePet.getName()) && p.getType().equals(updatePet.getType())){
                        throw new BaseException(ALREADY_PET_EXIST);
                    }
                }
                user.addPet(updatePet);
                petRepository.save(updatePet);
                List<PetDto> answer = new ArrayList<>();
                user.getPets().forEach(p -> {
                    answer.add(new PetDto(p));
                });
                return answer;
            }
            else{
                throw new BaseException(DATABASE_ERROR);
            }
        } catch (Exception exception) {
            if (exception instanceof BaseException){
                throw (BaseException)exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public PetDto updatePet(PetDto pet, Integer petIdx, Integer userIdx) throws BaseException {
        try{
            Optional<Pet> result = petRepository.findById(petIdx);
            User user = new User(userIdx);
            if (result.isPresent()) {
                Pet now = result.get();
                if (pet.getImgurl() != null){
                    now.setImgurl(pet.getImgurl());
                }
                if (pet.getName() != null){
                    System.out.println(now.getName());
                    System.out.println(pet.getName());
                    moodRepository.changeMood(pet.getName(),userIdx,now.getName());
                    now.setName(pet.getName());
                }
                if (pet.getType() != null){
                    now.setType(pet.getType());
                }
                if (pet.getBirth() != null){
                    now.setBirth(pet.getBirth());
                }
                petRepository.save(now);
                return new PetDto(now);
            }
            throw new BaseException(NONE_PET_EXIST);
        } catch (Exception exception) {
            if (exception instanceof BaseException){
                throw (BaseException)exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void deletePet(Integer petIdx, Integer userIdx) throws BaseException {
        try{
            Optional<Pet> result = petRepository.findById(petIdx);
            if (result.isPresent()) {
                Pet now = result.get();
                User user = new User(userIdx);
                String petName = now.getName();
                List<Diary> byPet = diaryRepository.findByNameAndUser(now.getName(),user);
                diaryRepository.deleteAll(byPet);
                petRepository.deleteById(petIdx);
            }
            else {
                throw new BaseException(NONE_PET_EXIST);
            }
        } catch (Exception exception) {
            if (exception instanceof BaseException){
                throw (BaseException)exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
