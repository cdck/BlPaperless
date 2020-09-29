/***************************************************************************
 *   Copyright (C) 2017 by 95   *
 ***************************************************************************/


#ifndef INTERPUBLIC_H
#define INTERPUBLIC_H
//#include "commdef.h"
#include "deviceid_suffix.h"
//**********************************
#define INTERFACEMODULE_VERSION "1.0.1"
//**********************************

//net data status
#define DATA_STATUS_NOGET  0 //表示未请求
#define DATA_STATUS_HADGET 1 //表示已经请求并且收到回复
#define DATA_STATUS_TOGET  2 //表示已发请求但未收到回复

//客户端所在的界面 role 
#define MEET_ROLE_IDLE		0 //没进入会议和后台
#define MEET_ROLE_COMMON	1 //进入会议
#define MEET_ROLE_SECRETARY 2 //进入后台

#define MEET_MAX_VOTENUM	6 //最大投票选项数

#define IS_DEVMEETDBSERVER(x)     (((x) & DEVICE_MEET_ID_MASK) == DEVICE_MEET_DB) //该设备ID是否会议数据库
#define IS_DEVMEETSERVICE(x)     (((x) & DEVICE_MEET_ID_MASK) == DEVICE_MEET_SERVICE) //该设备ID是否会议茶水服务终端
#define IS_DEVPROJECTIVE(x)     (((x) & DEVICE_MEET_ID_MASK) == DEVICE_MEET_PROJECTIVE) //该设备ID是否投影设备
#define IS_DEVVIDEOCAPTURE(x)     (((x) & DEVICE_MEET_ID_MASK) == DEVICE_MEET_CAPTURE) //该设备ID是否流采集设备
#define IS_MEET_CLIENT(x)     (((x) & DEVICE_MEET_ID_MASK) == DEVICE_MEET_CLIENT) //该设备ID是会议PC终端设备
#define IS_MEET_ONEKEYSHARE(x)     (((x) & DEVICE_MEET_ID_MASK) == DEVICE_MEET_ONEKEYSHARE) //该设备ID是会议PC终端设备
#define IS_MEET_PUBLISH(x)     (((x) & DEVICE_MEET_ID_MASK) == DEVICE_MEET_PUBLISH) //该设备ID是会议发布设备
#define IS_MEET_PHPCLIENT(x)     (((x) & DEVICE_MEET_PHPCLIENT) == DEVICE_MEET_PHPCLIENT) //该设备ID是PHP中转数据设备
#endif
