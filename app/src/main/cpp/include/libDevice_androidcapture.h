#ifndef  ANDROIDCAPTURE_INTERFACE_H_
#define   ANDROIDCAPTURE_INTERFACE_H_

#include "base.h"
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
//pts 播放的时间戳 微秒
//iskeyframe 是否关键帧
//成功返回操作属性对应的值 失败返回-1
int AndroidDevice_call(int channelstart, char*  pdata, int len, int64u pts, int iskeyframe);

//StuAndroidDisplay_Param --->param
#define ANDROIDDISPLAY_PARAM_VIDEO			0x00000001 //该标志位置1表示视频数据,否则为音频
#define ANDROIDDISPLAY_PARAM_DECODE			0x00000002 //该标志位置1表示解码前的数据,否则为解码后的数据包
#define ANDROIDDISPLAY_PARAM_VIDEOKEYFRAME	0x00000004 //该标志位置1表示视频关键帧的数据
#define DECODECOUT_CODECDATASIZE 512 

typedef struct
{
	int param;//设置NOSDL后的回调标志
	int size;//pVideoOut大小,如果是解码前则为原始帧的大小,如果是解码后则为YUV总大小
	int codecid;//解码器ID ffmpeg3.2 (h264=28,h265=174,mpeg4=13,vp8=140,vp9=168) ffmpeg 4.0 later(h264=27,h265=173,mpeg4=12,vp8=139,vp9=167)
	int w;//视频的宽
	int h;//视频的高
	int64u pts; //视频帧播放的时间 微秒
	int linesize[4];//解码后的视频数据,这里对应的是YUV2420每个平面的大小,如{1920, 960, 960}
	char codecdata[DECODECOUT_CODECDATASIZE];
	int  codecsize;
}StuAndroidDisplay_Param, *PStuAndroidDisplay_Param;

typedef int(*Android_DISPSTART)(int resId);							//开始显示
typedef int(*Android_DISPSTOP)(int resId);							//停止显示
typedef void(*Android_DISPDATA)(int* resId, int resNum, char *pData, void *param/*StuAndroidDisplay_Param*/);	//显示数据

typedef struct
{
	Android_DISPSTART cbStart;
	Android_DISPSTOP cbStop;
	Android_DISPDATA cbData;
}StuAndroidDispCB, *PStuAndroidDispCB;
int AndroidDevice_initvideoPlay(PStuAndroidDispCB pa);//平台不使用SDL显示,由应用层处理显示
//
int AndroidDevice_NV21ToI420(const int8u* src_y, int src_stride_y,
	const int8u* src_uv, int src_stride_uv,
	int8u* dst_y, int dst_stride_y,
	int8u* dst_u, int dst_stride_u,
	int8u* dst_v, int dst_stride_v,
	int width, int height);

int AndroidDevice_NV21ToNV12(const int8u* src_y, int src_stride_y,
	const int8u* src_uv, int src_stride_uv,
	int8u* dst_y, int dst_stride_y,
	int8u* dst_vu, int dst_stride_vu,
	int width, int height);

int AndroidDevice_YV12ToNV12(const int8u* src_y, int src_stride_y,
	const int8u* src_v, int src_stride_v,
	const int8u* src_u, int src_stride_u,
	int8u* dst_y, int dst_stride_y,
	int8u* dst_uv, int dst_stride_uv,
	int width, int height);

#ifdef __cplusplus
}
#endif
#endif
