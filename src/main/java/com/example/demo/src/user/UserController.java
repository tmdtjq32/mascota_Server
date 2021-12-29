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
    public BaseResponse<User> getUser(@PathVariable("userIdx") Integer userIdx) {
        try{
            User result = userProvider.getUser(userIdx);
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
    public BaseResponse<ResponseUserDto> getUser(@RequestBody SaveUserDto saveUserDto) {
        try{
            if (saveUserDto.getId() == null){
                return new BaseResponse<>(NONE_ID_EXIST);
            }

            if (saveUserDto.getPassword() == null){
                return new BaseResponse<>(NONE_PASSWORD_EXIST);
            }

            ResponseUserDto result = userService.createUser(saveUserDto);
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @PostMapping("/login")
    public BaseResponse<ResponseUserDto> login(@RequestBody SaveUserDto saveUserDto) {
        try{
            if (saveUserDto.getId() == null){
                return new BaseResponse<>(NONE_ID_EXIST);
            }

            if (saveUserDto.getPassword() == null){
                return new BaseResponse<>(NONE_PASSWORD_EXIST);
            }

            ResponseUserDto result = userProvider.login(saveUserDto);
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
