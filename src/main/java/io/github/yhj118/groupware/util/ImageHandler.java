package io.github.yhj118.groupware.util;

import io.github.yhj118.groupware.employee.EmpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

// 사진 업로드 로직 구현 클래스
@Component // Component 적용 시 빈으로 등록.
public class ImageHandler {

    public String imageHandlerManager(EmpRequest params) throws IOException {
        MultipartFile profileImg = params.getProfileImg(); // 업로드한 사진 파일을 저장

        // 사진 파일이 비었는지?
        if (profileImg.isEmpty()) { // 기본값 false
            throw new IllegalArgumentException("업로드된 사진이 없음");
        }
        // 사진 용량이 5MB 이하인지?
        if (profileImg.getSize() > 5*1024*1024) {
            throw new IllegalArgumentException("용량이 5MB를 초과");
        }
        // 사진 파일 형식 검사
        String contentType = profileImg.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            throw new IllegalArgumentException("파일 타입이 안 맞음");
        }

        // UUID + 확장자
        String originalImagName = profileImg.getOriginalFilename(); // 사진 이름
        String extension = originalImagName.substring(originalImagName.lastIndexOf(".")); // .에서 자르기 ex) .jpg 남음
        String saveImgName = UUID.randomUUID().toString() + extension; // UUID + .jpg

        // 사진 파일 저장
        String uploadDir = System.getProperty("user.dir") + "/uploads/profile/"; // 파일 저장 경로
        File directory = new File(uploadDir); // 문자열로 된 경로를 시스템이 다룰 수 있는 File 형태로 명시.

        if (!directory.exists()) { // 경로가 존재하는지 검사
            directory.mkdirs(); // 저장 폴더가 없으면 생성
        }

        File saveFile = new File(uploadDir + saveImgName); // 파일 저장 경로 + UUID.jpg를 시스템이 다룰 수 있게 File 형태로 명시.
        profileImg.transferTo(saveFile); // 사진파일 데이터를 saveFile을 통해 저장

        // DB에 넣을 사진 경로
        String profileImgPath = "/uploads/profile/" + saveImgName;

        return profileImgPath;
    }

}
