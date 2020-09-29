#ifndef LIB_MEETINTREFACE_MODULE_H
#define LIB_MEETINTREFACE_MODULE_H

#include "base.h"
#include "meetc/meetinterface_type.h"

#ifdef __cplusplus
extern "C" {
#endif

#if COMPILEMEETCPPDLL
#define  COMPILEMEETCPPDLL _declspec(dllexport)
#else
#if MEETCPPDLLUSE //动态库引用请定义该宏
#define  COMPILEMEETCPPDLL __declspec(dllimport)
#else
#define  COMPILEMEETCPPDLL 
#endif
#endif

	//检查该程序有没有运行另一个实例
	//检查出错返回-1 有另一个实例在运行返回1 没有实例运行返回0
	COMPILEMEETCPPDLL int meetcore_checkselfrun();

//MeetCore_InitParam programtype 程序类型
#define MEET_PROGRAM_TYPE_MEETCLIENT	 1 //会议终端软件
#define MEET_PROGRAM_TYPE_PROJECTIVE	 2 //投影软件
#define MEET_PROGRAM_TYPE_STREAMCAPTURE  3 //流采集软件
#define MEET_PROGRAM_TYPE_SERVICE		 4 //茶水服务软件
#define MEET_PROGRAM_TYPE_ANDROIDAPP	 5 //android APP
#define MEET_PROGRAM_TYPE_CLIENTDEBUG	 6 //TCP客户端调试
#define MEET_PROGRAM_TYPE_PUBLISHER	     7 //会议发布软件
#define MEET_PROGRAM_TYPE_PHPINTER	     8 //用于PHP的数据中转
#define MEET_PROGRAM_TYPE_BROADCAST	     9 //会议广播软件
#define MEET_PROGRAM_TYPE_TESTONLINE     10 //测试设备

	//会议模块回调数据接口
	//phdr 回调的数据 数据结构是 Type_HeaderInfo + 后接上真实的数据
	typedef int(*MEETCORE_CB)(pType_HeaderInfo phdr, void* puser);

	//MEETCORE_LOGCB loglevel 会议模块日志回调数据接口
#define MEETCORE_LOGLEVEL_ERROR     0//错误
#define MEETCORE_LOGLEVEL_INFO      1//信息
#define MEETCORE_LOGLEVEL_COMMON    2//信息
#define MEETCORE_LOGLEVEL_WARNING   3//警告
#define MEETCORE_LOGLEVEL_DEBUG     4//调试

	//MeetCore_InitParam logflag
#define MEETLOG_FLAG_LOGTOFILE  0x00000001 //保存到日志文件
#define MEETLOG_FLAG_PRINTFOUNT 0x00000002 //输出到console

	//loglevel 日志等级
	//pmsg 回调的数据
	typedef void(*MEETCORE_LOGCB)(int loglevel, const char* pmsg, void* puser);

	typedef struct 
	{
		const char* pconfigpathname;//每个程序都有对应的配置文件,这里传入client.ini文件路径名 utf8格式
		int			streamnum;//流通道个数,关于定义请查看平台术语描述 不需要设置为0

		void*		   puser;//用户指针 回调函数中会通过 void* puser 返回该指针
		MEETCORE_CB    pfunc;//回调函数 可为NULL

		int			   blogtofile;//参见 logflag定义
		MEETCORE_LOGCB plogfunc;//日志回调函数 不需要设置为NULL

		int			usekeystr;//使用传入的程序唯一标识,用于生成设备ID,不需要设置为0
		char		keystr[64];//程序标识,给那些无法获取唯一标识的设备使用

		int			programtype;//程序类型 用于区分程序来初始化接口数据 参见MEET_PROGRAM_TYPE_MEETCLIENT

		int			screenwidth;//屏幕宽,初始化视频显示帧缓存最大的宽,默认是1920
		int			screenheight;//屏幕高,初始化视频显示帧缓存最大的高,默认是1080
	}MeetCore_InitParam, *pMeetCore_InitParam;

	//初始化会议模块
	//0表示成功,-1表示失败
	COMPILEMEETCPPDLL int meetcore_init(pMeetCore_InitParam pa);

	//退出会议模块
	COMPILEMEETCPPDLL void meetcore_exit();

//meetcore_Call oper flag 处理数据返回的操作标志
#define MEETCORE_CALL_FLAG_JUSTRETURN		 0 //表示将数据返回给调用方
#define MEETCORE_CALL_FLAG_JUSTCALLBACK		 1 //表示将数据通过回调返回
#define MEETCORE_CALL_FLAG_CALLBACKANDRETURN 2 //表示将数据同时传递到回调也要返回数据

	//pdata数据
	//pretdata 返回的数据 数据结构是 Type_HeaderInfo + 后接上真实的数据
	//operflag 指示数据的操作标志 参见MEETCORE_CALL_FLAG_JUSTRETURN 定义
	//返回负数表示错误, 0表示没有返回值, 正值表示pretdata的大小
	COMPILEMEETCPPDLL int meetcore_Call(pType_HeaderInfo pdata, pType_HeaderInfo* pretdata, int32u operflag);

	//type 请求类型
	//用于释放meetcore_call 的pretdata资源
	COMPILEMEETCPPDLL void meetcore_free(pType_HeaderInfo pdata);
	
	typedef struct
	{
		int32u meetingid;//会议ID
		int32u roomid;//会场ID

		int32u role;//用户身份 参见meettype_interface.h MemState_MemFace
		int32u userid;//如果是参会人员则会参会人员ID和名称,如果是管理员则为管理员ID和名称
		const char*  username;
	}meetbase_forphp, *pmeetbase_forphp;
	COMPILEMEETCPPDLL int meetcore_SetMeetBaseForPhp(pmeetbase_forphp pdata);//修改缓存标识

	//获取type 对应的字符串 eg:type=TYPE_MEET_INTERFACE_TIME --> "TYPE_MEET_INTERFACE_TIME"
	COMPILEMEETCPPDLL const char* meetcore_GettypeStr(int32u type);

	//获取method 对应的字符串 eg:method=METHOD_MEET_INTERFACE_NOTIFY --> "METHOD_MEET_INTERFACE_NOTIFY"
	COMPILEMEETCPPDLL const char* meetcore_GetMethodStr(int32u method);

	//获取error 对应的字符串 eg:error=ERROR_MEET_INTERFACE_NOTINIT --> "ERROR_MEET_INTERFACE_NOTINIT"
	COMPILEMEETCPPDLL const char* meetcore_GetErrorStr(int32u err);

	//获取status 对应的字符串 eg:status=STATUS_ACCESSDENIED --> "STATUS_ACCESSDENIED"
	COMPILEMEETCPPDLL const char* meetcore_GetStatusMsg(int32u status);

	//输出日志,如果设置了日志回调函数,将日志回调给用户设置的回调函数
	COMPILEMEETCPPDLL void meetcore_printf(int loglevel, const char *pszFmt, ...);

	/************************封装一些常用函数**************************/
	//获取本机设备ID
	COMPILEMEETCPPDLL int32u meetcore_getselfdeviceid();

	//获取服务器的tickcount值 该值与服务器实时同步
	COMPILEMEETCPPDLL int32u meetcore_gettickcount();

	//获取本地微秒级时间 该值与服务器实时同步
	COMPILEMEETCPPDLL int64u meetcore_gettime();

	//获取本地微秒级UTC时间 该值与服务器实时同步
	COMPILEMEETCPPDLL int64u meetcore_getutctime();

	//获取设备名称
	//deviceid 要获取设备名称的设备ID
	//pnamebuf 传入接收名称的内存
	//buflen pnamebuf的大小
	//返回大小名称长度 utf8编码
	COMPILEMEETCPPDLL int    meetcore_getdevicename(int32u deviceid, char* pnamebuf, int buflen);

	//获取当前的硬件版本号
	COMPILEMEETCPPDLL int    meetcore_gethardver();

	//获取当前的软件件版本号
	COMPILEMEETCPPDLL int    meetcore_getsoftver();

	//检查本机是否在线
	//返回0离线 返回1在线
	COMPILEMEETCPPDLL int    meetcore_isselfonline();

	//检查指定设备是否在线
	//deviceid 要查询在线状态的设备ID
	//返回0离线 返回1在线
	COMPILEMEETCPPDLL int    meetcore_isdeviceonline(int32u deviceid);

	//获取上下文属性值
	//propertyid 上下文属性ID 参见meettypeface_type.h 中MEETCONTEXT_PROPERTY_定义
	COMPILEMEETCPPDLL int32u meetcore_getcontextproperty32(int32u propertyid);
	//设置上下文属性值
	//propertyid 上下文属性ID 参见meettypeface_type.h 中MEETCONTEXT_PROPERTY_定义
	//propertyval 要修改的属性值
	//返回值参见meettypeface_type.h 中ERROR_MEET_INTERFACE_ 定义
	COMPILEMEETCPPDLL int   meetcore_setcontextproperty32(int32u propertyid, int32u propertyval);

	//获取当前的会议ID
	COMPILEMEETCPPDLL int32u meetcore_curmeetingid();
	//设置当前的会议ID
	//返回值参见meettypeface_type.h 中ERROR_MEET_INTERFACE_ 定义
	COMPILEMEETCPPDLL int   meetcore_setcurmeetingid(int32u meetid);

	//检查指定的数据是否缓存,没有缓存的会向服务器发起缓存
	//type		对应的会议数据type 参见meettypeface_type.h 中TYPE_MEET_INTERFACE_定义 默认0
	//flag		执行缓存的标志 可以指定强制缓存标志, 参见meettypeface_type.h 中MEET_CACEH_FLAG_ 定义 默认0
	//parameter 缓存操作的参数 默认0 	
	//parameter用法描述 eg:需要缓存目录id=0表示缓存所有目录信息(不包括目录里的文件),当id=1时表示缓存该目录里的文件,如果id=0不支持则会返回ERROR_MEET_INTERFACE_PARAMETER
	//返回值参见meettypeface_type.h 中ERROR_MEET_INTERFACE_ 定义
	COMPILEMEETCPPDLL int   meetcore_checkcache(int32u type, int32u flag, int32u parameter);

#ifdef __cplusplus
}
#endif


#endif