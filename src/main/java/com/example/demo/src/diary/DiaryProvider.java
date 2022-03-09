package com.example.demo.src.diary;

import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.model.*;
import com.example.demo.src.specification.*;
import com.example.demo.src.repository.*;
import com.example.demo.utils.AES128;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import static com.example.demo.config.BaseResponseStatus.*;

//Provider : Read의 비즈니스 로직 처리
@Service
@Transactional
public class DiaryProvider {

    private final JwtService jwtService;

    @Autowired
    DiaryListRepository diaryListRepository;

    @Autowired
    DiaryImgRepository diaryImgRepository;

    @Autowired
    DiaryRepository diaryRepository;

    @Autowired
    MoodRepository moodRepository;

    @Autowired
    HelpRepository helpRepository;

    @Autowired
    PetRepository petRepository;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public DiaryProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public List<DiaryListDto> getDiaryList(Integer userIdx, Integer type) throws BaseException {
        try {
            User user = new User(userIdx);
            List<DiaryList> result = diaryListRepository.findByUserAndTypeOrderByNumAsc(user, type);
            List<DiaryListDto> list = new ArrayList<>();
            result.forEach(d -> {
                list.add(new DiaryListDto(d));
            });
            return list;
        } catch (Exception exception) {
            if (exception instanceof BaseException) {
                throw (BaseException) exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public ResponseDiaryDto getDiary(Integer diaryIdx) throws BaseException {
        try {
            Optional<Diary> result = diaryRepository.findById(diaryIdx);

            if (result.isPresent()) {
                return new ResponseDiaryDto(result.get());
            } else {
                throw new BaseException(DATABASE_ERROR);
            }
        } catch (Exception exception) {
            if (exception instanceof BaseException) {
                throw (BaseException) exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public ResponseDiaryHome getDiaryHome(Integer userIdx, Integer type, Integer listIdx, Pageable pageable)
            throws BaseException {
        try {
            User user = new User(userIdx);
            Optional<DiaryList> result;
            if (listIdx != null) {
                result = diaryListRepository.findById(listIdx);
            } else {
                result = diaryListRepository.findTopByUserAndTypeOrderByNumAsc(user, type);
            }
            if (result.isPresent()) {
                DiaryList list = result.get();
                Specification spec = Specification.where(DiarySpecification.countByDiaryList(list));
                long limit = diaryRepository.count(spec);
                if (limit <= pageable.getPageSize() * pageable.getPageNumber()) {
                    throw new BaseException(NONE_PAGE);
                }
                List<DiarySummary> records = diaryRepository.findByDiaryList(list, pageable);
                return new ResponseDiaryHome(list.getContext(), records);
            } else {
                throw new BaseException(NONE_PAGE);
            }
        } catch (Exception exception) {
            if (exception instanceof BaseException) {
                throw (BaseException) exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public HelpHome getDiaryHelp(Integer userIdx) throws BaseException {
        try {
            User user = new User(userIdx);
            int limit = diaryRepository.countByUser(user);
            Random num = new Random();
            int idx = num.nextInt(limit);
            PageRequest pageRequest = PageRequest.of(idx, 1);
            List<DiarySummary> result = diaryRepository.findByUser(user, pageRequest);
            PageRequest request = PageRequest.of(0, 5);
            Page<Help> helpList = helpRepository.findAll(request);

            return new HelpHome(result.get(0), helpList.getContent());
        } catch (Exception exception) {
            if (exception instanceof BaseException) {
                throw (BaseException) exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public Miss getMiss(Integer userIdx, String name) throws BaseException {
        try {
            User user = new User(userIdx);
            long dayofpets = 0;
            Optional<Pet> chk = petRepository.findByUserAndName(user, name);
            if (chk.isPresent()) {
                Pet pet = chk.get();
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                Date birth = pet.getBirth();
                Date now = new Date();
                long calDate = now.getTime() - birth.getTime();
                dayofpets = calDate / (24 * 60 * 60 * 1000);
            } else {
                throw new BaseException(NONE_PETS_EXIST);
            }
            long diaryNum = diaryRepository.numOfPets(userIdx, name);

            return new Miss(diaryNum, dayofpets);
        } catch (Exception exception) {
            if (exception instanceof BaseException) {
                throw (BaseException) exception;
            }
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public HashMap<String, List<DiarySummary>> getDiaryMiss(Integer userIdx) throws BaseException {
        try {
            HashMap<String, List<DiarySummary>> result = new HashMap<>();
            String[] emotion = { "사랑", "기쁨", "보통", "우울", "화남", "심심" };
            int i = 0;
            while (i < 6) {
                List<Diary> rs = diaryRepository.findByTypeAndUser(emotion[i], userIdx);
                ArrayList<DiarySummary> diarys = new ArrayList<>();
                result.put(emotion[i], diarys);
                for (Diary d : rs) {
                    result.get(emotion[i]).add(new DiarySummary(d));
                }
                i++;
            }
            return result;
        } catch (Exception exception) {
            if (exception instanceof BaseException) {
                throw (BaseException) exception;
            }
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
