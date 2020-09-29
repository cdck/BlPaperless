#ifndef  ANDROIDCAPTURE_INTERFACE_H_
#define   ANDROIDCAPTURE_INTERFACE_H_

#ifdef __cplusplus
extern "C" {
#endif

    //设置日志回调函数 必须在初始化前先调用
    typedef void (*AndroidDevice_LogCB)(int channelstart, int level, char* pmsg);
    void SetAndroidDeviceLogCallBack(AndroidDevice_LogCB pcb);
    
    #define ANDROID_OPERFLAG_PIXFORMAT     1
#define ANDROID_OPERFLAG_WIDTH 	       2
#define ANDROID_OPERFLAG_HEIGHT        3
#define ANDROID_OPERFLAG_START 	       4
#define ANDROID_OPERFLAG_STOP  	       5
     //设置数据通知回调函数 必须在初始化前先调用
    typedef int (*AndroidDevice_GetInfoCB)(int channelstart, int oper);
    void SetAndroidDeviceGetInfoCallBack(AndroidDevice_GetInfoCB pcb);
       
//初始化桌面采集
//type 流类型
//channelstart 流通道索引值
//成功返回0 失败返回-1
int AndroidDevice_initcapture(int type, int channelstart);

//初始化摄像头采集
//type 流类型
//data 采集数据
//成功返回操作属性对应的值 失败返回-1
int AndroidDevice_call(int channelstart, char*  pdata, int len);

#ifdef __cplusplus
}
#endif
#endif
