package com.lzh.recommend.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.lzh.recommend.config.CosClientConfig;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 图片上传模版类
 *
 * @author lzh
 */
@Slf4j
public abstract class BasePictureUploadTemplate {

    @Resource
    private CosClientConfig clientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource      输入源
     * @param uploadPathPrefix 上传路径前缀
     * @return 图片地址
     */
    public String uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 校验文件
        validateFile(inputSource);

        // 重新拼接图片路径，不使用原始的文件名，避免重复和增加安全性
        String uuid = RandomUtil.randomString(16);
        String date = DateUtil.formatDate(new Date());
        String filename = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s", date, uuid, FileUtil.getSuffix(filename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        // 上传图片
        File tempFile = null;
        try {
            // 上传文件
            tempFile = File.createTempFile(uploadPath, null);
            transferFile(inputSource, tempFile);
            cosManager.putObject(uploadPath, tempFile);

            // 返回图片地址
            return clientConfig.getHost() + uploadPath;

        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 删除临时文件，释放资源
            this.deleteTempFile(tempFile);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file 文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.error("file delete error, filePath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 校验文件
     *
     * @param inputSource 输入源
     */
    protected abstract void validateFile(Object inputSource);

    /**
     * 获取原始文件名
     *
     * @param inputSource 输入源
     * @return 原始文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 将输入源中的文件保存到临时文件中
     *
     * @param inputSource 输入源
     * @param file        临时文件
     * @throws IOException IO异常
     */
    protected abstract void transferFile(Object inputSource, File file) throws IOException;
}
