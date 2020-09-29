#ifndef DEVICEID_SUFFIX_PROTECT
#define DEVICEID_SUFFIX_PROTECT

/*设备ID号分配规则
设备ID号为32位数据
高14位表示种类号，原则上每种设备各一个种类号
低18位表示设备号，分配以2^10为最小分配单位，预留2^18空间。
ID号0和0xffffffff保留
区域服务器：0xff000000/8
媒体服务器：0xfe000000/8
流服务器：  0xfd000000/8

分类					ID范围	16进制  			 ID范围					     使用状态      - .


PC电脑
PC端管理软件			0x00000001 -- <40000				 1 –  <262144			  已使用
PC端远程控制软件	    0x00040000 -- <80000			262144 –  <524288				未用
PC端用户操作软件		0x00080000 -- <c0000			524288 –  <786432
PC端软件备用			0x000c0000 -- <100000			786432 –  <1048576

手机Android
手机端管理软件			0x00100000 -- <140000			1048576-  <1310720
手机端远程控制软件		0x00140000 -- <180000			1310720-  <1572864
手机端用户操作软件		0x00180000 -- <1c0000			1572864-  <1835008		使用
手机端软件备用			0x001c0000 -- <200000			1835008-  <2097152

寻呼器Android
寻呼器软件				0x00200000 -- <240000			2097152-  <2359296		使用
寻呼器软件2				0x00240000 -- <280000			2359296-  <2621440
寻呼器软件3				0x00280000 -- <2c0000			2621440-  <2883584
寻呼器软件4				0x002c0000 -- <300000			2883584-  <3145728

点播Android
点播终端软件软件		0x00300000 -- <340000			3145728-  <3407872		使用
点播终端软件软件2		0x00340000 -- <380000			3407872-  <3670016
点播终端软件软件3		0x00380000 -- <3c0000			3670016-  <3932160
点播终端软件软件4		0x003c0000 -- <400000			3932160-  <4194304


Arm终端
播放终端				0x00400000 -- <440000			4194304-  <4456448      使用
告警终端				0x00440000 -- <480000			4456448-  <4718592
音源输入设备(终端)		0x00480000 -- <4c0000			4718592-  <4980736		使用
短路控制设备(终端)		0x004c0000 -- <500000			4980736-  <5242880



手机IOS
IOS端管理软件
IOS端远程控制软件
IOS端用户操作软件
IOS端软件备用

工程配置软件 0xfcfe0000
工厂配置软件 0xfcfd0000
*/

//工程配置软件 0xfcfe0000
//工厂配置软件 0xfcfd0000

//独立使用与设备ID不是同一种,但是注册方式是一样的
#define DEVICE_CFG_REGCTRL		0xfcff0000 //所有的公司注册管理软件的ID类别是这个
#define DEVICE_CFG_PROVIDER		0xfcfe0000 //所有的工程软件的ID类别是这个
#define DEVICE_CFG_INTERNAL		0xfcfd0000 //所有的工厂配置软件的ID类别是这个
#define DEVICE_CFG_MONITOR		0xfcfc0000 //所有的守护程序的ID类别是这个

#define SERVER_DEVICE_ID_MASK		0xff000000
#define CLIENT_DEVICE_ID_MASK		0xffff0000

#define SERVER_DEVICE_MASK		8 //默认的服务器类mask
#define CLIENT_DEVICE_MASK		16//默认的终端类mask


#define DEVICE_PC_SYSTEM			0x00000000
#define DEVICE_PC_CONTROL			0x00040000
#define DEVICE_PC_OPERATOR			0x00080000

#define DEVICE_ANDROID_SYSTEM		0x00100000
#define DEVICE_ANDROID_CONTROL		0x00140000
#define DEVICE_ANDROID_OPERATOR		0x00180000

#define DEVICE_PAGING_SYSTEM		0x00200000
#define DEVICE_PAGING_CONTROL		0x00240000
#define DEVICE_PAGING_OPERATOR		0x00280000

#define DEVICE_ORDER_SYSTEM			0x00300000
#define DEVICE_ORDER_CONTROL		0x00340000
#define DEVICE_ORDER_OPERATOR		0x00380000
#define DEVICE_ORDER_CAPTURE        0x003c0000

#define DEVICE_ARM_PLAY				0x00400000
#define DEVICE_ARM_WARNING			0x00440000
#define DEVICE_ARM_AUDIOINPUT		0x00480000
#define DEVICE_ARM_SHORTCONTROL		0x004c0000

//会议系统专属设备类
#define DEVICE_MEET_MASKBIT		14
#define DEVICE_MEET_ID_MASK		0xfffc0000

#define DEVICE_MEET_DB				0x01000000  //会议数据库(会议后台)
#define DEVICE_MEET_SERVICE			0x01040000  //会议茶水服务
#define DEVICE_MEET_PROJECTIVE		0x01080000  //会议投影机  
#define DEVICE_MEET_CAPTURE			0x010c0000  //会议流采集设备
#define DEVICE_MEET_CLIENT			0x01100000  //会议客户端 
#define DEVICE_MEET_PUBLISH			0x01140000  //会议发布
#define DEVICE_MEET_ONEKEYSHARE		0x01200000  //会议一键同屏 

//保留
//#define DEVICE_MEET_OPTION1			0x01140000
#define DEVICE_MEET_OPTION2			0x01180000
#define DEVICE_MEET_OPTION3			0x011c0000


#define CHECKE_DEVICEID(x, devtype, devmask) \
	devtype = (x & g_idmask[devmask]);

#endif
