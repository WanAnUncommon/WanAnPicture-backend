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