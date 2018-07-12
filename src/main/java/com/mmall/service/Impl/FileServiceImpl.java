package com.mmall.service.Impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        // 拓展名
        // abc.jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传的文件名:{},上传的路径：{},新文件名:{}",fileName, path, uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            // 可写权限
            fileDir.setWritable(true);
            // 可以递归创建文件夹
            fileDir.mkdirs();
        }

        File targetFile = new File(path, uploadFileName);

        try {
            // 文件上传成功
            file.transferTo(targetFile);
            // 将targetFile上传到我们的FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 上传完之后 删除upload下面的文件
            targetFile.delete();

        } catch (IOException e) {
            logger.info("上传文件异常",e);
            return null;
        }
        return targetFile.getName();

    }
}
