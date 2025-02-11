-- 建库
create database IF NOT EXISTS wanan_picture;
-- 切换库
use wanan_picture;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(255)                           not null comment '账号',
    userPassword varchar(155)                           not null comment '密码',
    userName     varchar(255)                           null comment '用户名',
    userAvatar   varchar(1024)                          null comment '头像',
    userProfile  varchar(255)                           null comment '用户简介',
    userRole     varchar(255) default 'user'            not null comment '用户角色',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      tinyint      default 0                 not null comment '是否删除 0否 1是',
    unique key uk_userAccount (userAccount),
    index idx_userName (userName)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 图片表
create table if not exists picture
(
    id          bigint auto_increment comment 'id' primary key,
    url         varchar(512)                           not null comment '图片url',
    name        varchar(255)                           not null comment '图片名称',
    introduction varchar(255)                           null comment '图片简介',
    category    varchar(64)                           null comment '图片分类',
    tags        varchar(512)                           null comment '图片标签(Json数组)',
    picSize     bigint                                  null comment '图片大小',
    picWidth    int                                    null comment '图片宽度',
    picHeight   int                                    null comment '图片高度',
    picScale    double                            null comment '图片比例',
    picFormat   varchar(32)                            null comment '图片格式',
    userId      bigint                                 not null comment '创建用户id',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime    datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint      default 0                 not null comment '是否删除 0否 1是',
    index idx_userId (userId),
    index idx_category (category),
    index idx_name (name),
    index idx_tags (tags),
    index idx_introduction (introduction)
)comment '图片表' collate = utf8mb4_unicode_ci;