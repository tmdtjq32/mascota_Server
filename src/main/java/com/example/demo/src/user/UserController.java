package com.example.demo.src.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.lang.Integer;


import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexDate;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;

    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService){
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @ResponseBody
    @GetMapping("/{userIdx}") 
    public BaseResponse<UserDto> getUser(@PathVariable("userIdx") Integer userIdx) {
        try{
            UserDto result = userProvider.getUser(userIdx);
            if (result == null){
                return new BaseResponse<>(NONE_USER_EXIST);
            }

            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @PostMapping("")
    public BaseResponse<UserDto> getUser(@RequestBody SaveUserDto saveUserDto) {
        try{
            if (saveUserDto.getId() == null){
                return new BaseResponse<>(NONE_ID_EXIST);
            }

            if (saveUserDto.getPassword() == null){
                return new BaseResponse<>(NONE_PASSWORD_EXIST);
            }

            UserDto result = userService.createUser(saveUserDto);
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @PostMapping("/login")
    public BaseResponse<UserDto> login(@RequestBody SaveUserDto saveUserDto) {
        try{
            if (saveUserDto.getId() == null){
                return new BaseResponse<>(NONE_ID_EXIST);
            }

            if (saveUserDto.getPassword() == null){
                return new BaseResponse<>(NONE_PASSWORD_EXIST);
            }

            UserDto result = userProvider.login(saveUserDto);
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @PatchMapping("/book")
    public BaseResponse<String> login(@RequestBody SaveBookDto saveBookDto) {
        try{
            int userIdxByJwt = jwtService.getUserIdx();

            if(!userProvider.chkUser(userIdxByJwt)){
                return new BaseResponse<>(NONE_USER_EXIST);
            }

            if (saveBookDto.getTitle() == null){
                return new BaseResponse<>(NONE_TITLE_EXIST);
            }

            if (saveBookDto.getNickname() == null){
                return new BaseResponse<>(NONE_NICKNAME_EXIST);
            }
            userService.createBook(saveBookDto, userIdxByJwt);
            return new BaseResponse<>("");
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/pet")
    public BaseResponse<List> getPetLists() {
        try{
            int userIdxByJwt = jwtService.getUserIdx();

            if(!userProvider.chkUser(userIdxByJwt)){
                return new BaseResponse<>(NONE_USER_EXIST);
            }

            List resultList = userProvider.getPetbyId(userIdxByJwt);
            return new BaseResponse<>(resultList);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
