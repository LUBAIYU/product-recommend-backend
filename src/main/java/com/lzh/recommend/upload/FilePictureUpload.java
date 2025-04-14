package com.lzh.recommend.upload;

import cn.hutool.core.io.FileUtil;
import com.lzh.recommend.constant.PictureConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件上传图片业务类
 *
 * @author lzh
 */
@Slf4j
@Service
public class FilePictureUpload extends BasePictureUploadTemplate {

    @Override
    protected void validateFile(Object inputSource) {
        // 类型转换
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验大小(限制最大为 10MB)
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > PictureConsts.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过 10MB");
        // 校验后缀
        String filename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(filename);
        ThrowUtils.throwIf(!PictureConsts.ALLOW_SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件格式不支持");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        return ((MultipartFile) inputSource).getOriginalFilename();
    }

    @Override
    protected void transferFile(Object inputSource, File file) throws IOException {
        ((MultipartFile) inputSource).transferTo(file);
    }
}
