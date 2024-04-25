# 用户表
create table user
(
    id            bigint auto_increment comment '用户ID'
        primary key,
    user_name     varchar(256)             not null comment '用户名',
    user_avatar   varchar(512)             null comment '用户头像',
    user_password varchar(256)             not null comment '密码',
    gender        tinyint                  null comment '性别（0-男，1-女）',
    age           int                      null comment '年龄',
    phone         char(11)                 null comment '手机号',
    address       varchar(256)             null comment '收货地址',
    role          tinyint                  not null comment '角色（0-管理员，1-普通用户）',
    salt          varchar(10)              not null comment '盐',
    create_time   datetime default (now()) not null comment '创建时间',
    update_time   datetime default (now()) not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted    tinyint  default 0       not null comment '是否删除',
    constraint user_pk2
        unique (user_name),
    constraint user_pk3
        unique (phone)
)
    comment '用户表';

# 商品表
create table product
(
    id          bigint auto_increment comment '商品ID'
        primary key,
    name        varchar(256)                       not null comment '商品名称',
    image       varchar(1024)                      not null comment '商品图片',
    description varchar(512)                       null comment '商品描述',
    price       int                                not null comment '价格',
    stock       int                                not null comment '库存',
    status      tinyint  default 0                 not null comment '状态（0-上架，1-下架）',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint  default 0                 not null comment '是否删除（0-未删除，1-删除）'
)
    comment '商品表';

# 记录表
create table record
(
    id          bigint auto_increment comment '记录ID'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    product_id  bigint                             not null comment '商品ID',
    count       int      default 1                 not null comment '搜索次数',
    create_time datetime default (now())           not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint  default 0                 not null comment '是否删除（0-存在，1-删除）'
)
    comment '记录表';

# 购物车信息表
create table cart
(
    id          bigint auto_increment comment '购物车ID'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    product_id  bigint                             not null comment '商品ID',
    num         int                                not null comment '购买数量',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint  default 0                 not null comment '是否删除（0-未删除，1-删除）'
)
    comment '购物车信息表';
