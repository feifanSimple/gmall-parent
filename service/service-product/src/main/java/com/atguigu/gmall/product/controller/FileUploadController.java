package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.Api;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Api(tags = "文件上传接口")
@RestController
@RequestMapping("admin/product")
public class FileUploadController {

    //  获取文件上传服务器的Ip 地址
    //  将服务的Ip 地址配置在配置文件中这种方式叫：软编码
    @Value("${fileServer.url}")
    private String fileUrl;  // fileUrl = http://192.168.200.128:8080/
    //  http://api.gmall.com/admin/product/fileUpload
    //  文件上传的时候，我们如何获取到文件的名称以及文件的数据?
    //  文件名称不能重复，后缀名要与源文件后缀名保持一致，设置文件的大小，格式等等......
    //  springMvc 讲文件上传的时候，使用过一个对象{MultipartFile  file}  file 与页面数据接口对象是一致！
    @RequestMapping("fileUpload")
    public Result<String> fileUpload(MultipartFile file) throws Exception{
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        String path = null;

        if (configFile!=null){
            // 初始化
            ClientGlobal.init(configFile);
            // 创建trackerClient
            TrackerClient trackerClient = new TrackerClient();
            // 获取trackerService
            TrackerServer trackerServer = trackerClient.getConnection();
            // 创建storageClient1
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
            path = storageClient1.upload_appender_file1(file.getBytes(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
            System.out.println(fileUrl + path);
        }
        return Result.ok(fileUrl+path);
    }

}

