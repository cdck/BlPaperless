#ifndef  _JNI_WALLETNET_INIT_INTERFACE_H_
#define _JNI_WALLETNET_INIT_INTERFACE_H_
#include "base.h"
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
//register jni functions
int wallet_netinit_register_native_methods(JNIEnv* env);

//init net system
//data --> pbui_MeetCore_InitParam
//Sucess return 0,failed retunr -1
int Init_walletSys(JNIEnv *env, jobject thiz, jbyteArray data);

//benable 0:foregroud 1:backgroud
void jni_enablebackgroud(JNIEnv *env, jobject thiz, jint benable);

//for system function call
//sucess return a bytearray parse with type and method,failed return a null bytearray
jbyteArray jni_call(JNIEnv *env, jobject thiz, jint type, jint method, jbyteArray pdata);

//for init android camara capture
//channelstart is channelindex
int jni_AndroidDevice_initcapture(JNIEnv *env, jobject thiz, jint type, jint channelstart);

//for pass android camara capture data
//channelstart is channelindex
//iskeyframe is video encode key frame flag
//pts playtimestamp microseconds
int jni_AndroidDevice_call(JNIEnv *env, jobject thiz, jint channelstart, jint iskeyframe, jlong pts, jbyteArray pdata);

jbyteArray jni_AndroidDevice_NV21ToI420(JNIEnv *env, jobject thiz,jbyteArray pdata,jint width, jint height);
jbyteArray jni_AndroidDevice_NV21ToNV12(JNIEnv *env, jobject thiz, jbyteArray pdata, jint width, jint height);
jbyteArray jni_AndroidDevice_YV12ToNV12(JNIEnv *env, jobject thiz, jbyteArray pdata, jint width, jint height);

#ifdef __cplusplus
}
#endif

#endif
