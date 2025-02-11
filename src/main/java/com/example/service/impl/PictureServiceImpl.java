package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.model.entity.Picture;
import com.example.service.PictureService;
import com.example.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author lenovo
* @description 针对表【picture(图片表)】的数据库操作Service实现
* @createDate 2025-02-11 15:13:12
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




