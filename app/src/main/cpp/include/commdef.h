#ifndef _COMMDEF_H_
#define _COMMDEF_H_

#include "base.h"

#pragma pack(push)
#pragma pack(1)


#define WALLET_NETPACKETHEADER  0xeff7ff07		//平台数据包标识 ---20151211
#define COMMVER					2		//通讯格式版本号

#define MSG_THREAD_QUIT			50000


//功能码240-255保留给网络层使用，应用层不得使用

#define	FUNC_QUERYATTRIB		1		//一次性查询寄存器
#define FUNC_LOGINATTRIBPUSH	2		//登记PUSH查询
#define FUNC_LOGOUTATTRIBPUSH	3		//注销PUSH查询
#define FUNC_UPDATEATTRIB		4		//更新寄存器值
#define	FUNC_QUERYMEDIAFILE		5		//获取媒体文件对应表
#define FUNC_LOGINMEDIAFILE		6		//增加媒体文件
#define FUNC_LOGOUTMEDIAFILE	7		//删除媒体文件
#define FUNC_UPDATEMEDIAFILE	8		//更新文件信息
#define	FUNC_LOGINEVENT			9		//注册事件
#define	FUNC_LOGOUTEVENT		10		//注销事件
#define	FUNC_UPDATEEVENT		11		//更新事件
#define FUNC_LOGINTRIGGER		12		//注册触发器
#define	FUNC_LOGOUTTRIGGER		13		//注销触发器
#define	FUNC_UPDATETRIGGER		14		//更新触发器
#define	FUNC_TRIGGEREXEC		15		//立即触发触发器
#define	FUNC_LOGINDEVICEGROUP	17		//注册设备组
#define	FUNC_LOGOUTDEVICEGROUP	18		//注销设备组
#define	FUNC_UPDATEDEVICEGROUP	19		//更新设备组
#define	FUNC_LOGINUSER			20		//注册用户
#define	FUNC_LOGOUTUSER			21		//注销用户
#define	FUNC_UPDATEUSER			22		//更新用户
#define	FUNC_WRITEDB			23		//写入服务器数据库
#define	FUNC_READDB				24		//从服务器数据库中读取
#define	FUNC_WRITELOG			25		//写LOG
#define	FUNC_READLOG			26		//读LOG
#define	FUNC_PLAYMEDIA			27		//下载媒体文件
#define	FUNC_STARTEXCHANGE		28		//设备交流握手
#define	FUNC_PLAYSTREAM			29		//播放流
#define FUNC_GETEVENTINFOSINGLE		30		//获取事件信息,ID号
#define	FUNC_LOGINDEVICE		31		//设备登记
#define	FUNC_LOGOUTDEVICE		32		//设备注销
#define	FUNC_UPDATE				33		//升级设备软件
#define FUNC_BINDUSER			34		//将用户绑定到本设备上，用于登录
#define FUNC_EXITUSER			35		//用户退出登录
#define	FUNC_DELDB				36		//删除数据库内的数据
#define FUNC_PUTMEDIA			37		//指示上传媒体文件时的媒体ID号和媒体服务器ID号，由区域服务器发给请求设备
#define FUNC_RETURN				38		//由服务器发送给终端，用于登记注册返回ID号、服务器ID等数据
#define FUNC_PUTSTREAM			39		//指示流源终端向服务器发送流数据
#define FUNC_GETTRIGGERINFOSINGLE		40		//获取触发器信息，ID获取
#define FUNC_GETMULTICASTADDR	41		//流服务器申请组播地址
#define FUNC_FREEMULTICASTADDR	42		//流服务器释放组播地址
#define FUNC_MEDIAISBEING		43		//媒体服务器发送给区域服务器，表示自身存在的媒体文件
#define FUNC_MEDIADELBEING		44		//媒体服务器发送给区域服务器，表示自身删除的媒体文件
#define FUNC_GETMEDIAINFOSINGLE	45		//获取媒体文件信息,纯ID获取
#define FUNC_GETMEDIAINFO		46		//获取媒体文件信息,ID或属性值获取
#define FUNC_GETTRIGGERINFO		47		//获取触发器信息
#define FUNC_GETDEVICEGROUPINFO	48		//获取设备组信息
#define FUNC_GETEVENTINFO		49		//获取事件信息
#define FUNC_GETSTREAMINFO		50		//获取流通道信息
#define FUNC_GETUSERINFO		51		//获取用户通道信息
#define FUNC_SETDEVICESYSTEATTRIB	52	//设置设备的系统属性（63号寄存器）
#define FUNC_LOGINMEDIAATTRIB			53		//增加媒体属性
#define FUNC_LOGOUTMEDIAATTRIB			54		//删除媒体属性
#define FUNC_UPDATEMEDIAATTRIB			55		//更新媒体属性信息
#define FUNC_GETMEDIAATTRIBINFOSINGLE	56		//获取媒体属性信息,纯ID获取
#define FUNC_GETMEDIAATTRIBINFO			57		//获取媒体属性信息,ID或属性值获取
#define FUNC_UPDATEMEDIAATTRIBTYPENAME	58		//更新媒体属性类别名
#define FUNC_GETMEDIAATTRIBTYPENAME		59		//获取媒体属性类别名
#define FUNC_GETMEDIAVERSION			60		//获取媒体版本信息
#define FUNC_GETTRIGGERTABLE			61		//获取触发器表，区域服务器发送给设备的触发器表，包含触发器ID和触发器MD5值
#define FUNC_GETEVENTTABLE				62		//获取事件表，区域服务器发送给设备的事件表，包含事件ID和事件MD5值
#define FUNC_REMOVEDEVICE				63		//删除设备
#define	FUNC_STOPEXCHANGE		64		//设备停止交流
#define	FUNC_EXCHANGESEND		65		//设备交流数据
#define	FUNC_GETUPDATEINFO		66		//获取升级设备软件版本
#define	FUNC_SETSYSTEMTIME		67		//设置系统时间
#define	FUNC_BACKUPDB			68		//请求备份数据库
#define	FUNC_GETDBDATA			69		//请求数据库数据
#define	FUNC_RESTOREDB			70		//请求恢复数据库
#define	FUNC_SETDBDATA			71		//下发数据库数据
#define	FUNC_DBOPERSTOP			72		//备份恢复操作执行错误返回
#define FUNC_GETMEDIASTATUS		  73		//获取媒体状态信息,ID或属性值获取
#define FUNC_GETMEDIASTATUSSINGLE 74		//获取媒体状态信息,纯ID获取
#define FUNC_SETSTREAMINFO			75		//设置流信息
#define FUNC_GETDEVICEINDENTIFYINFO 76		//获取识别码信息
#define FUNC_AREASERVERSYNC			77		//区域服务器同步数据,除区域服务器外,其它设备禁止使用
#define FUNC_AREASERVERLOGMONITOR	78		//日志监视
#define FUNC_SECONDFUNCTION			79		//功能码不够用, 二级协议头

/*以下为系统保留功能码*/
#define FUNC_TICK				240					//发送TICK值,附带NTP
#define FUNC_SERVERREPORT		241					//流服务器发送的组播检测包
#define FUNC_GETALLDEVIPINFO	242					//获取所有设备IP信息,守护程序使用,其它禁止使用
#define FUNC_RETURNALLDEVIPINFO	243					//返回所有设备IP信息,守护程序使用,其它禁止使用
#define FUNC_NATDEVIPINFO		244					//告诉指定设备对某设备进行NAT,守护程序使用,其它禁止使用
#define FUNC_DEVICEIDREPEAT		245					//通知ID号重复了，即您正在使用的ID号在服务器上还在线,可能是你强制下线后，服务器信息未更新，稍后10秒再重试链接
#define FUNC_VALIDATEDEVICEID	246					//设备验证返回信息

//二级功能码
#define SECOND_FUNC_SYSTEMFUNCTIONLIMIT			1 //系统功能限制
#define SECOND_FUNC_DELALLOCATEDEVICEID			2 //删除分配的设备ID
#define SECOND_FUNC_MODIFYALLOCATEDEVICEID		3 //修改分配的设备ID

#define SERVER_ID_AREA			0xff000000	//区域服务器起始地址
#define SERVER_ID_MEDIA			0xfe000000	//媒体服务器起始地址
#define SERVER_ID_STREAM		0xfd000000	//流服务器起始地址
#define SERVER_ID_MASK			8

#define DB_DEVICE					1				//数据库操作为设备方式
#define DB_USER						2				//数据库操作为用户名方式

#define DB_DATA_INT					0
#define DB_DATA_BLOB				1

#define DB_FILEDATA_MAXSIZE		40960
//数据库备份恢复操作错误码
//////////////////////////////////////////////////////////////////////////
#define DB_NOERROR		0
#define DB_OPERERROR	1	//操作错误
#define DB_EOF			2	//文件尾
#define DB_USER_ERROR	3	//用户认证失败
#define DB_BUSY			4	//备份恢复被其他用户占用
#define DB_NOREADY		5	//没准备好
#define DB_FILEERROR	6	//数据文件操作失败
#define DB_REBOOTERROR	7	//服务器自关闭出错
#define DB_ID_ERROR		8	//ID认证出错
#define DB_DBVERHIGH	9	//数据库版本比恢复的机器版本高
#define DB_DBDATAWRONG	10	//数据库文件数据错误
#define DB_TIMEOUT		11	//用户在指定的时间内未执行操作，强制超时处理

#define LOG_LEVEL_ERROR			0		//log信息的级别，0表示错误
#define LOG_LEVEL_WARN			1		//警告
#define LOG_LEVEL_INFO			2		//普通信息
#define LOG_LEVEL_DEBUG			3		//调试信息
#define LOG_LEVEL_SYSTEM_SHUTDOWN		21		//系统关闭
#define LOG_LEVEL_SYSTEM_START			20		//系统启动
#define LOG_LEVEL_DEVICE_ONLINE			19		//设备上线
#define LOG_LEVEL_DEVICE_OFFLINE		18		//设备离线


#define DEVICE_MODE_GROUP		1		//定义ID为组ID
#define DEVICE_MODE_DEVICE		2		//定义ID为设备ID
#define DEVICE_MODE_DEVIDENTIFY	3		//定义ID为设备识别码ID

#define LOGINMODE_POWERON		0		//为上电后注册设备,
#define LOGINMODE_OFFLINE		1		//为掉线后注册设备

#define RETURN_ERROR_NONE			0		//返回的错误值，此为正常，无错误
#define RETURN_ERROR_NOTBEING		1		//不存在
#define RETURN_ERROR_NOTONLINE		2		//不在线
#define RETURN_ERROR_NOSERVER		3		//无法找到可用的服务器
#define RETURN_ERROR_DENIAL			4		//拒绝操作
#define RETURN_ERROR_PASSWORD		5		//密码错误
#define RETURN_ERROR_FORMAT			6		//格式错误
#define RETURN_ERROR_NOPOWER		7		//无权限操作
#define RETURN_ERROR_ISBEING		8		//已存在
#define RETURN_ERROR_UPDATEVER		9		//升级的版本号低于系统正在使用的版本号
#define RETURN_ERROR_DBOFFLINE		10		//数据库没连接上
#define RETURN_ERROR_NOTCONNTECT	11		//未联接到区域服务
#define RETURN_ERROR_NORES			12		//没有资源了
#define RETURN_ERROR_TIMEOUT		13		//超时
#define RETURN_ERROR_ZERO			14      //数据为0
#define RETURN_ERROR_SERVERERROR    15		//区域服务器操作出错
#define RETURN_ERROR_NOSPACE		16		//没有足够的空间了
#define RETURN_ERROR_DBOPERERROR	17		//数据操作错误
#define RETURN_ERROR_NOTIDENTITY	18		//没有标识码
#define RETURN_ERROR_MAXDEVICENUM	19		//超过最大在线设备数
#define RETURN_ERROR_MAXERRORTIMES	20		//超过最大错误次数数,拒绝登陆
#define RETURN_ERROR_PARSEERROR		21		//区域服务器认证失败
#define RETURN_ERROR_PROTOCALNOMATCH	22		//协议版本不匹配
#define RETURN_ERROR_EXPIRATIONTIME		23		//区域服务器已经过期了 expiration Time
#define RETURN_ERROR_COMMVERNOMATCH		24		//协议版本不匹配,拒绝请求
#define RETURN_ERROR_OVERRUN			25		//超出范围
#define RETURN_ERROR_NOTOPEN			26		//无法打开
#define RETURN_ERROR_FORCEDSTOP			27		//强行停止
#define RETUNR_ERROR_LOGONERROR			28	    //登陆错误
#define RETUNR_ERROR_MAXONLINENUM		29	    //超过最大并发设备数,拒绝登陆

//媒体或流服务器返回的错误值
#define RETURN_ERROR_SEC_NONE			0			//此为正常，无错误
#define RETURN_ERROR_SEC_NOTBEING		0xf001		//媒体或流通道不存在
#define RETURN_ERROR_SEC_ISBEING		0xf002		//媒体或流通道已存在
#define RETURN_ERROR_SEC_SERVER			0xf003		//媒体或流服务器处理异常
#define RETURN_ERROR_SEC_NOTBEINGENCODE	0xf004		//流服务器找不到可用的流编码
#define RETURN_ERROR_SEC_SERVERBUSY		0xf005		//媒体或流服务器繁忙，拒绝处理
#define RETURN_ERROR_SEC_CHANNELBUSY	0xf006		//流服务器流通道输出太多了，拒绝处理
#define RETURN_ERROR_SEC_STOP			0xf007		//未能正常停止流通道或媒体请求
#define RETURN_ERROR_SEC_NOSPACE		0xf008		//没有足够的空间了
#define RETURN_ERROR_SEC_IDENTIFYOCCUPY	0xf009		//标识码已被占用,请重试
#define RETURN_ERROR_SEC_SENDERROR		0xf00a		//数据发送失败
#define RETURN_ERROR_SEC_INITIALERROR	0xf00b		//初始化请求失败

#define STREAM_MODE_DYNAMIC		0xff000000		//登记为动态地址流（自动分配）

//大类
#define MEDIA_FILETYPE_AUDIO    0x00000000 //音频
#define MEDIA_FILETYPE_VIDEO	0x20000000 //视频
#define MEDIA_FILETYPE_RECORD	0x40000000 //录制
#define MEDIA_FILETYPE_PICTURE	0x60000000 //图片
#define MEDIA_FILETYPE_UPDATE	0xe0000000 //升级
//#define MEDIA_FILETYPE_TEMP		0x80000000 //临时文件
#define MEDIA_FILETYPE_OTHER	0xa0000000 //其它文件
#define MAINTYPEBITMASK			0xe0000000
//小类
#define MEDIA_FILETYPE_PCM		0x01000000	//PCM文件
#define MEDIA_FILETYPE_MP3		0x02000000	//MP3文件
#define MEDIA_FILETYPE_ADPCM	0x03000000	//WAV文件
#define MEDIA_FILETYPE_FLAC		0x04000000	//FLAC文件
#define MEDIA_FILETYPE_MP4		0x07000000  //MP4文件
#define MEDIA_FILETYPE_MKV		0x08000000  //MKV文件
#define MEDIA_FILETYPE_RMVB		0x09000000  //RMVB文件
#define MEDIA_FILETYPE_RM		0x0a000000  //RM文件
#define MEDIA_FILETYPE_AVI		0x0b000000  //AVI文件
#define MEDIA_FILETYPE_BMP		0x0c000000  //bmp文件
#define MEDIA_FILETYPE_JPEG		0x0d000000  //jpeg文件
#define MEDIA_FILETYPE_PNG		0x0e000000  //png文件
#define MEDIA_FILETYPE_OTHERSUB	0x10000000  //其它文件
#define SUBTYPEBITMASK			0x1f000000

#define MEDIA_FILETYPE_NET		0x00000000 //网络文件
#define MEDIA_FILETYPE_TEMP		0x00800000 //临时文件
#define MEDIA_FILETYPE_LOCAL	0x00c00000 //本地文件
#define SAVETYPEBITMASK			0x00c00000

#define MEDIATYPEBITCOUNT       12		   //媒体类型占用多少位
#define MEDIATOTALTYPE			0xffc00000 //媒体类型

#define MAKEMEDIATYPE(x,y,z)	 (x | y | z)			//合成媒体类别
#define GETMAINTYPE(x)			 (x & MAINTYPEBITMASK)	//获取大类别
#define GETSUBTYPE(y)			 (y & SUBTYPEBITMASK)	//获取子类别
#define GETSAVETYPE(z)			 (z & SAVETYPEBITMASK)	//获取文件存放类型
#define IS_RECORD_MEDIA(mediaId) ((mediaId & MAINTYPEBITMASK) == MEDIA_FILETYPE_RECORD) //是否为录制文件
#define IS_UPDATE_MEDIA(mediaId) ((mediaId & MAINTYPEBITMASK) == MEDIA_FILETYPE_UPDATE) //是否为升级文件
#define IS_TEMP_MEDIA(mediaId)	 ((mediaId & SAVETYPEBITMASK) == MEDIA_FILETYPE_TEMP)   //是否为临时文件

#define MAX_PASSWORDLEN			32		//最大密码长度
#define MAX_IP4ADDRNAMELEN		40		//IPV4地址信息最大长度
#define MAX_DNSADDRNAMELEN		64		//dns名称最大长度
#define MAX_USERNAMELEN			20		//最大的用户名长度
#define MAX_SHORTCUTDESCRIBE	120		//快捷方式描述
#define MAX_FILENAMELEN			60		//最大文件名长度
#define MAX_TRIGGERNAMELEN		80		//最大触发器名长度
#define MAX_EVENTNAMELEN		80		//最大事件名长度
#define MAX_SHORTCUTNAMELEN		80		//快捷操作名长度
#define MAX_DEVICENAMELEN		40		//最大设备名长度
#define MAX_DEVICEGROUPNAMELEN	40		//最大设备组名长度
#define MAX_FILEPATHLEN			256		//最大文件路径长度
#define MAX_LOGLEN				1024	//最大的LOG长度
#define MAX_STREAMNAMELEN		40		//最大流名称长度

#define DEVICE_OFFLINE			0		//设备离线，属性寄存器里表示
#define DEVICE_ONLINE			1		//设备正常，属性寄存器里表示
#define DEVICE_HADREMOVE		0xfffffffe //0号寄存器,设备已经被删除
#define DEVICE_ATTRIB_STATUS	0		//设备属性值里，表示设备状态的寄存器，为离线或正常或设备故障码

#define DEVICE_ATTRIB_VERSION   5		//设备的软件版本和硬件版本,高16位为软件版本,低16位为硬件版本

#define DEVICE_UPDATE_WORKING	1		//设备升级中，属性寄存器里表示
#define DEVICE_UPDATE_ONIDLE	0		//设备不在升级状态，属性寄存器里表示
#define DEVICE_UPDATE_GIVEUP	2		//设备不处理,放弃升级
#define DEVICE_ATTRIB_UPDATE    6		//设备正在升级标志位

#define DEVICE_ATTRIB_SYSTEM	63		//设备属性值(数据图），表示系统设置的一些设备参数

//
#define  SECONDSERVER_ATTRIB_CPURATE	2	//cpu当前使用率
#define  SECONDSERVER_ATTRIB_NETRATE	3	//网卡带宽使用率
#define  STREAMSERVER_ATTRIB_CHANNELS	48	//流服务器当前接入的流通道号

#define ATTRIB_BIT_ALL			0xffffffffffffffffULL		//全部属性位
#define ATTRIB_BIT(n)			((int64u)1<<n)					//属性位

#define USER_POWER_CONTROL		ATTRIB_BIT(0)		//允许基本用户控制，如果不允许，则此用户只能查看用户界面而不能控制

#define USER_POWER_LEVEL1		(1<<1)				//编辑自己，查看和运行全部
#define USER_POWER_LEVEL2		(2<<1)				//编辑运行全部
#define USER_POWER_MEDIATYPE	ATTRIB_BIT(3)		//允许编辑媒体类别
#define USER_POWER_DEVICE		ATTRIB_BIT(4)		//允许编辑设备
#define USER_POWER_DEVICEGROUP	ATTRIB_BIT(5)		//允许编辑设备组
#define USER_POWER_USER			ATTRIB_BIT(6)		//允许编辑用户
//#define USER_POWER_ALLOWREMOVEDEVICE	ATTRIB_BIT(7)		//允许删除设备
#define USER_POWER_ALLOWMULTI	ATTRIB_BIT(63)		//允许用户同时登录多个设备

#define USER_STYLE_NEEDVERIFY	0x1			//是否需要提示用户确认

//区域服务器寄存器
#define AREA_REG_DEVICEONLINECOUNTS			1 //在线设备数量
#define AREA_REG__UPDATE					DEVICE_ATTRIB_UPDATE//设备正在升级标志位
#define AREA_REG_REGSTATUS					2 //区域服务器注册类型信息
#define AREA_REG_SYSTEM_VERSION				10//系统的软件版本和硬件版本,高16位为软件版本,低16位为硬件版本
#define AREA_REG_EXPIRATIONTIME				11//系统有效期
#define AREA_REG_MEDIAMODIFYDETAILINFO		48//增加 删除 更新 媒体文件的信息，数据结构参见下方
#define AREA_REG_MEDIAATTRIBMODIFYINFO		49 //增加 删除 更新 媒体属性的信息，数据结构参见下方
#define AREA_REG_GROUP						50 //增加、删除、更新组信息，数据结构参见下方
#define AREA_REG_DB							51 //增加、删除、更新数据库信息，数据结构参见下方
#define AREA_REG_EVENT						52 //增加、删除、更新事件信息，数据结构参见下方
#define AREA_REG_TRIGGER					53 //增加、删除、更新触发器信息，数据结构参见下方
#define AREA_REG_STREAM						54 //增加、删除、更新流信息，数据结构参见下方
#define AREA_REG_LOGINFO					55 //操作日志

//AREA_REG_REGSTATUS -- 解析(eg:0x00000101 解析出的是加密狗注册类型 状态值为1表示可用)
#define AREASERVER_ATTRIB_REGTYPEBIT		0x0000ff00			//区域服务器注册类型掩码
#define AREASERVER_ATTRIB_REGTYPEVALBIT		0x000000ff			//区域服务器注册类型对应的值的掩码
#define AREASERVER_ATTRIB_REGTYPE_FILE	0x0		//区域服务器使用了文件的注册方式
#define AREASERVER_ATTRIB_REGTYPE_UDOG	0x1		//区域服务器使用了加密狗的注册方式
#define AREASERVER_ATTRIB_REGTYPE_UDOG_OFFLINE	0x0		//加密狗不可用
#define AREASERVER_ATTRIB_REGTYPE_UDOG_ONLINE	0x1		//加密狗可用

#define AREASERVER_ATTRIB_GROUP_ADD		0x1		//区域服务器50号寄存器，func值，表示增加了一个设备组
#define AREASERVER_ATTRIB_GROUP_DEL		0x2		//50号寄存器，func值，表示删除一个或一批设备组
#define AREASERVER_ATTRIB_GROUP_UPDATE	0x3		//50号寄存器，func值，表示更新了一个设备组

#define AREASERVER_ATTRIB_MEDIA_ADD		0x11		//区域服务器48号寄存器，func值，表示增加了一个媒体文件
#define AREASERVER_ATTRIB_MEDIA_UPDATE	0x12		//48号寄存器，func值，表示更新了一个媒体文件
#define AREASERVER_ATTRIB_MEDIA_DEL		0x13		//48号寄存器，func值，表示删除一个或一批媒体文件
#define AREASERVER_ATTRIB_MEDIA_STATUS	0x14		//48号寄存器，func值，表示一个媒体文件状态改变

#define AREASERVER_ATTRIB_MEDIAATTRIB_ADD		 0x1		//区域服务器49号寄存器，func值，表示增加了一个媒体属性
#define AREASERVER_ATTRIB_MEDIAATTRIB_UPDATE	 0x2		//49号寄存器，func值，表示更新了一个媒体属性
#define AREASERVER_ATTRIB_MEDIAATTRIB_DEL		 0x3		//49号寄存器，func值，表示删除一个或一批媒体属性
#define AREASERVER_ATTRIB_MEDIAATTRIB_NAMEUPDATE 0x4		//49号寄存器，func值，表示更新媒体属性类别名称

#define AREASERVER_DB_CHANGE	0x11		//区域服务器51号寄存器，func值，表示数据库中增加或修改了一项
#define AREASERVER_DB_DEL		0x12		//51号寄存器，func值，表示删除数据库中的一项或多项

#define AREASERVER_EVENT_ADD	0x11		//区域服务器52号寄存器，func值，表示增加了一个事件
#define AREASERVER_EVENT_UPDATE	0x12		//52号寄存器，func值，表示更新了一个事件
#define AREASERVER_EVENT_DEL	0x13		//52号寄存器，func值，表示删除一个或一批事件

#define AREASERVER_TRIGGER_ADD		0x11		//区域服务器53号寄存器，func值，表示增加了一个触发器
#define AREASERVER_TRIGGER_UPDATE	0x12		//53号寄存器，func值，表示更新了一个触发器
#define AREASERVER_TRIGGER_DEL		0x13		//53号寄存器，func值，表示删除一个或一批触发器

#define CONDITION_CHANGE	0				//当值改变时
#define CONDITION_EQUAL		1				//当值等于时
#define CONDITION_GEQUAL	2				//当值大于等于时
#define CONDITION_LEQUAL	3				//当值小于等于时
#define CONDITION_UNTIL		10				//时长标签里，表示一直执行到特定的时间（实时时钟）

#define PLAY_STREAM_ID_CHANNEL			0x1 //为流通道
#define PLAY_STREAM_ID_DEVICE			0x2 //为设备
#define PLAY_STREAM_ID_DEVICEIDENTIFY   0x3 //为设备的识别码

#define MEDIAATTRIB_MAXATTRIB	  255
//媒体属性使用限制,将媒体属性类别 MEDIAATTRIB_TYPE_USE 的 MEDIAATTRIB_LIMIT(包括MEDIAATTRIB_LIMIT)以上为系统使用的媒体属性,用户不可以使用该范围内的值
#define MEDIAATTRIB_TYPE_USE	  3
#define MEDIAATTRIB_LIMIT         220
#define MEDIAATTRIB_IMPORTANTFILE 220  //重要的媒体文件
#define IS_IMPORTANT_MEDIAFILE(mdeiaattrib) (((mdeiaattrib >> 24) == MEDIAATTRIB_IMPORTANTFILE)? 1: 0) //判断是否为重要级别的文件

#define LOGONKEYINFO_MAXSIZE  128
#define MD5SELFSIZE 16	//MD5值字节大小
#define MD5SELFASCLLSIZE 32	//MD5值字节大小
//#define ENABLEMEDIAMD5ZIP  0 //压缩媒体总表

//
#define MAXTIMES_ERRORLOGIN		5  //最大登陆出错次数
#define MAXLOCKTIME_ERRORLOGIN  1//10 //最大登陆出错锁定时间,分钟

//删除设备操作标志
#define REMOVEDEVICE_FLAG_FORCEREMOVE  0x01


static const int32u g_idmask[33]=					//屏蔽码
{
	0x00000000,
	0x80000000,
	0xc0000000,
	0xe0000000,
	0xf0000000,
	0xf8000000,
	0xfc000000,
	0xfe000000,
	0xff000000,
	0xff800000,
	0xffc00000,
	0xffe00000,
	0xfff00000,
	0xfff80000,
	0xfffc0000,
	0xfffe0000,
	0xffff0000,
	0xffff8000,
	0xffffc000,
	0xffffe000,
	0xfffff000,
	0xfffff800,
	0xfffffc00,
	0xfffffe00,
	0xffffff00,
	0xffffff80,
	0xffffffc0,
	0xffffffe0,
	0xfffffff0,
	0xfffffff8,
	0xfffffffc,
	0xfffffffe,
	0xffffffff
};

typedef struct  tag_SecondNetHeader 
{
	int32u		func;
}SecondNetHeader, *pSecondNetHeader;

typedef struct _tag_address
{
	int32u id;
	int8u mode;
	int8u mask;
	int8u fill[2];
}Address,*PAddress;

typedef struct _tag_updatedevicefile	//升级文件结构
{
	int32u	fileId;								//文件ID号
	int16u	softVer;							//软件版本号
	int16u	hardVer;							//高字节硬件版本号，低字节非定单识别号
	int8u	filename[MAX_FILENAMELEN];			//文件名
	int8u	path[MAX_FILEPATHLEN];				//文件路径
}UpdateDeviceFile,*PUpdateDeviceFile;

typedef struct _tag_usershortcut		//用户快捷操作
{
	int32u execId[4];
	int8u name[4][MAX_SHORTCUTNAMELEN];
	int8u num;							//共有多少个事件ID，最多可以设置4个比如按钮按下和弹起分别显示什么名字和执行什么事件
	int8u style;							//风格
	int8u fill[2];
	int8u describ[MAX_SHORTCUTDESCRIBE]; //对本快捷操作的描述
}UserShortcut,*PUserShortcut;

typedef struct _tag_serveraddress_
{
	int8u addr[MAX_DNSADDRNAMELEN];				//ip地址or域名,用字符串表示，支持域名，有长度限制
	int16u port;				//端口号
	int8u fill[2];
}StuPeerAddress,*PStuPeerAddress;

typedef struct _tag_systemattrib
{
	int8u name[MAX_DEVICENAMELEN];		//设备名
	int32u parentId;						//为0表示不指定父设备，若当前设备需要指定为某个设备的子设备时，设本参数为父设备ID
	int32u bindUser;						//为0表示不绑定默认用户，若为其它值，则表明该设备在此用户权限下运作，登录无需输入密码
	int64u style;						//其它的一些设定值，用位表示
	StuPeerAddress stuAddr[5];
	int8u mask[MAX_IP4ADDRNAMELEN];						//屏蔽码
	int8u gateway[MAX_IP4ADDRNAMELEN];					//网关地址
	int8u dns1[MAX_IP4ADDRNAMELEN];						//dns地址
	int8u dns2[MAX_IP4ADDRNAMELEN];
	int8u defaultPri;					//默认优先级，0-63，默认值为32，用于该设备动态生成事件中的优先级
	int8u addrtype;						//地址类型，0为动态分配。1为静态分配，stuAddr[0]必需设置。2为不配置。注：服务器必需静态分配
	int8u defaultCallPri;				//默认寻呼优先级
	int8u defaultTalkPri;				//默认对讲优先级
}SystemAttrib,*PSystemAttrib;

typedef struct _tag_attrib_exchange		//交流寄存器
{
	int32u	deviceId;					//目的设备ID号
	int32u	sequence;					//序号
	int32u	dataSize;					//数据长度（不包含本结构）
}Attrib_Exchange,*PAttrib_Exchange;

//计算媒体同步信息的个数
#define CAL_MEDIA_SYNCINFOLEN(msec) (msec / 1000 + 2) //((msec > 0)? (msec / 1000 + 2): 0)
typedef struct _tag_atocfileinfosingle
{
	int8u  md5_self[MD5SELFSIZE];//本结构体信息把产生的md5值
	int8u  filename[MAX_FILENAMELEN];
	int32u mediaid;

	int32u creatorId;
	int32u createDeviceId;
	int32u msec; //播放时长，以ms为单位,如果为升级文件 记录升级的版本信息数据长度
	int32u attrib;
	int64u len;
	int8u  md5[MD5SELFSIZE];
	int32u syncinfolen;		//媒体额外信息长度
}AToCFileInfoSingle,*PAToCFileInfoSingle;

#define WHOLE_MEDIAFILE		 1 //完整可使用的文件
#define PART_MEDIAFILE		 2 //不完整的文件
#define NOTEXIST_MEDIAFILE	 3 //未检测到媒体服务器存在此文件
typedef struct _tag_atocfilestatussingle
{
	int32u mediaid;
	int32u status;

	//int64u lastplaytime;
	//int32u playtimes;
}AToCFileStatusSingle,*PAToCFileStatusSingle;

typedef struct _tag_areaattrib_meida	//区域服务器中48号寄存器中存放的数据，有媒体文件增加、更新和删除时会更新
{
	int32u	func;						//功能参见AREASERVER_ATTRIB_MEDIA_ADD等，如果功能码为AREASERVER_ATTRIB_MEDIA_DEL，不要用此结构，func下为一个WORD类型值，表示删除数据，再下表示具体的文件ID
	int64u  mediatimestamp;			//媒体修改的时间戳
	AToCFileInfoSingle fileInfo;	//具体的文件信息
	////如果为增加媒体文件紧接着会是媒体的同步信息,新增加的媒体文件暂时标记为未可用,要等媒体服务器向区域服务器成功登记后才可用
}AreaAttrib_Media,*PAreaAttrib_Media;

typedef struct _tag_areaattrib_deviceattrib	
{
	int32u	func;

	int32u devid;
	int32u devidentifyid;
	int	   devattriblen;
}AreaAttrib_DeviceAttrib,*PAreaAttrib_DeviceAttrib;

typedef struct _tag_areaattrib_mediadel	//区域服务器中48号寄存器中存放的数据，有媒体文件增加、更新和删除时会更新
{
	int32u	func;						//AREASERVER_ATTRIB_MEDIA_DEL
	int64u  mediatimestamp;				//媒体修改的时间戳
	int16u	num;
	int8u fill[2];
}AreaAttrib_MediaDel,*PAreaAttrib_MediaDel;

typedef struct _tag_areaattrib_mediastatus	//区域服务器中48号寄存器中存放的数据，有媒体文件状态改变
{
	int32u	func;						//AREASERVER_ATTRIB_MEDIA_STATUS
	AToCFileStatusSingle	status;
}AreaAttrib_MediaStatus,*PAreaAttrib_MediaStatus;

typedef struct _tag_areaattrib_meidaattrib	//区域服务器中49号寄存器中存放的数据，有媒体属性增加、更新和删除时会更新
{
	int32u	func;						//功能参见 AREASERVER_ATTRIB_MEDIAATTRIB_ADD等，如果功能码为AREASERVER_ATTRIB_MEDIAATTRIB_DEL，不要用此结构，func下为一个WORD类型值，表示删除数据，再下表示具体的属性的标识ID组
	int8u	attribname[MAX_FILENAMELEN];//属性名称
	int8u	mediaId;					//属性类别索引
	int8u	attrib;						//类别索引下的属性索引
	int8u fill[2];
}AreaAttrib_MediaAttrib,*PAreaAttrib_MediaAttrib;


typedef struct _tag_areaattrib_meidaattribdel	//区域服务器中49号寄存器中存放的数据，有媒体属性增加、更新和删除时会更新
{
	int32u	func;						//AREASERVER_ATTRIB_MEDIAATTRIB_DEL
	int16u	num;
	int8u fill[2];
}AreaAttrib_MediaAttribDel,*PAreaAttrib_MediaAttribDel;

typedef struct _tag_areaattrib_group	//区域服务器中50号寄存器中存放的数据，有设备组属性增加、更新和删除时会更新
{
	int8u func;							//增加 删除 更新
	int8u _fill;
	int16u deviceNum; //(增加,更新:组内的地址数据类型为 Address,表示组包含的地址信息),(删除:组内的地址数据类型为 int32u,表示组ID)
	int32u groupId;
	int32u creatorId;
	int32u createDeviceId;
	int8u name[MAX_DEVICEGROUPNAMELEN];
}AreaAttrib_Group,*PAreaAttrib_Group;


typedef struct _tag_areaattrib_db_change		//区域服务器中51号寄存器中存放的数据，数据库有数据改动
{
	int8u		func;					//改动
	int8u		mode;					//模式，DB_DEVICE,DB_USER
	int8u		isBlob;					//0=int,1=blob
	int8u		_fill;
	int32u		valueid;				//mode=DB_DEVICE,设备ID,mode=1,用户ID
	int32u		paramId;				//参数ID号
	int32u		val;					//isBlob=1,参数总长度,isBlob=0,参数值
}AreaAttrib_DB_Change, *PAreaAttrib_DB_Change;

typedef struct _tag_areaattrib_db_del		//区域服务器中51号寄存器中存放的数据，数据库有数据被删除
{
	int8u		func;					//删除
	int8u		mode;					//模式，DB_DEVICE,DB_USER
	int8u		isBlob;					//0=int,1=blob
	int8u		_fill;
	int32u		valueid;				//mode=DB_DEVICE,设备ID,mode=1,用户ID
	int32u		paramNum;				//参数个数
}AreaAttrib_DB_Del, *PAreaAttrib_DB_Del;

typedef struct _tag_areaattrib_event_change			//区域服务器中52号寄存器中存放的数据，有事件被添加或更新
{
	int8u		func;					//增加 更新
	int8u		_fill[3];
	int32u		len;					//事件的字节数
}AreaAttrib_Event_Change, *PAreaAttrib_Event_Change;

typedef struct _tag_areaattrib_event_del			//区域服务器中52号寄存器中存放的数据，有事件被删除
{
	int8u		func;					//删除
	int8u		_fill[3];
	int32u		num;					//共多少个ID
}AreaAttrib_Event_Del, *PAreaAttrib_Event_Del;

typedef struct _tag_areaattrib_trigger_change			//区域服务器中53号寄存器中存放的数据，有触发器被添加或更新
{
	int8u		func;					//增加 更新
	int8u		_fill[3];
	int32u		len;					//触发器的字节数
}AreaAttrib_Trigger_Change, *PAreaAttrib_Trigger_Change;

typedef struct _tag_areaattrib_trigger_del			//区域服务器中53号寄存器中存放的数据，有触发器被删除
{
	int8u		func;					//删除
	int8u		_fill[3];
	int32u		num;					//共多少个ID
}AreaAttrib_Trigger_Del, *PAreaAttrib_Trigger_Del;


//////////////////////////////////////////////////////////////////////////
#define AREASERVER_STREAM_ADD		0x1		//区域服务器54号寄存器，func值，表示设备上线,登记的流信息,数据格式为 StreamInfo 数组
#define AREASERVER_STREAM_UPDATE	0x2		//53号寄存器，func值，表示更新流信息,数据格式为 LittleStreamInfo 数组
#define AREASERVER_STREAM_DEL		0x3		//53号寄存器，func值，表示删除信息,数据格式为删除的流ID 数组

//stream subtype
#define STREAM_SUBTYPE_UNKNOW			   0 //
#define STREAM_SUBTYPE_AUDIO_LOOPBACK	   1 //音频回路采集
#define STREAM_SUBTYPE_AUDIO_CAPTURE	   2 //声卡采集
#define STREAM_SUBTYPE_SCREEN		       3 //屏幕采集
#define STREAM_SUBTYPE_CAMARA			   4 //摄像对采集
#define STREAM_SUBTYPE_IPC_HK			   5 //海康网络摄像头
#define STREAM_SUBTYPE_MIX_LOOPBACK_SCREEN 6 //混合流
#define STREAM_SUBTYPE_MIX_LOOPBACK_CAMARA 7 //混合流
#define STREAM_SUBTYPE_MIX_CAPTURE_SCREEN  8 //混合流
#define STREAM_SUBTYPE_MIX_CAPTURE_CAMARA  9 //混合流
#define STREAM_SUBTYPE_END				   10
typedef struct  tag_StreamInfo
{
	int32u deviceid;
	int32u streamid;
	int8u  streamindex;
	int8u  subtype;
	int8u  fill[2];
	int8u  streamname[MAX_STREAMNAMELEN];
}StreamInfo, *pStreamInfo;

typedef struct  tag_LittleStreamInfo
{
	int32u streamid;
	int8u  streamname[MAX_STREAMNAMELEN];
	int8u  subtype;					//
	int8u  _fill[3];
}LittleStreamInfo, *pLittleStreamInfo;

typedef struct _tag_areaattrib_stream_change
{
	int8u		func;					//
	int8u		_fill[3];
	int32u		num;
}areaattrib_stream_change, *pareaattrib_stream_change;

typedef struct _tag_areaattrib_loginfo	
{
	int32u  deviceid;
	int8u   level;
	int8u   _fill;
	int16u   loglen;
}AreaAttrib_LogInfo,*PAreaAttrib_LogInfo;

typedef struct _tag_area_userchange
{
	int32u		countDstId;				//目的ID或组号的数量
	int32u		createid;				//
	int32u		userid;
	int8u		name[MAX_USERNAMELEN];	//用户名
	int8u		password[MAX_PASSWORDLEN];//用户密码（MD5)
	int64u		power;					//用户权限，参考USER_POWER_
	int16u		triggerNum;				//与用户绑定的触发器ID个数
	int8u		shortcutNum;			//快捷操作个数
	int8u		defaultPri;				//默认优先级
	int8u		defaultCallPri;			//默认寻呼优先级
	int8u		defaultTalkPri;			//默认对讲优先级
	int8u		fill[2];
}Area_UserChange,*PArea_UserChange;

typedef struct _tag_return				//返回值
{
	int16u err;
	int32u val1;
	int32u val2;
}StuReturn,*PStuReturn;

/***********************************************************
设备ID号：设备的唯一标识号，32位。31-26位：大种类号，25-18位：小种类号，17-0位：设备号
		服务器：0XFFF80000/14段为区域服务器，0XFFF40000/14段为媒体服务器，0XFFF00000/14段为流服务器

流通道ID号：实时流通道编号，每一个实时流源都应有一个编号，一个设备最多可以拥有32个流通道，通道号为固定或动态分配
		通道号32位，31-16位为大类号，15-5位系统内编号，5-0位为设备内子通道号
		如，系统给此设备分配的设备通道类号为0xa0000020（27位），这个设备有16个通道，
		那么此设备分配通的道号为0xa0000020-0xa000002f

媒体文件ID号：每一个媒体文件在一个系统内都有唯一的ID号，ID号为32位，最高字3位为大类号，如0为音频文件，1为视频文件，高字节低五位为文件类型，如PCM、MP3
		0xe0000000/3为系统升级文件

设备属性：64个寄存器，0,63号寄存器为系统保留（只有这两个寄存器允许区域服务器修改），0-47为32位数值，48-63号寄存器为数据图值，寄存器中存放数据偏移量，
		0号寄存器为设备状态，登记PUSH查询此寄存器可以获知这个设备的上下线状态。63号寄存器存放系统设备对应的系统设置信息，如设备名等，
		约定：48号寄存器存放设备较少变动的状态值
		PUSH方式登记要查询的寄存器值后，在寄存器发生改变时，区域服务器会将此属性的最新状态推送至查询方。
************************************************************/


#pragma pack(pop)

#endif
