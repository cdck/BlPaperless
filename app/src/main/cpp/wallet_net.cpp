/*
 * wallet_net.cpp
 *
 *  Created on: 2014-4-8
 *      Author: root
 */
#include "control.h"
#include <jni.h>
#include <wallet_net.h>
#include "jni_wallet_netinit.h"
#ifdef __cplusplus
extern "C" {
#endif
#include "ffmpeg/libavcodec/jni.h"
#ifdef __cplusplus
};
#endif


jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
    {
        JNIEnv* env 	  = NULL;
        jint 		result = -1;
        if(logable) LOGI("JNI_OnLoad call!");

        do
	{
		if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK)
		{
			if(logable) LOGI("JNI_OnLoad failed %d", __LINE__);
			return -1;
		}

		if(logable) LOGI("JNI_OnLoad start av_jni_set_java_vm %d", __LINE__);
		av_jni_set_java_vm(vm, NULL);
		if(logable) LOGI("JNI_OnLoad end av_jni_set_java_vm %d", __LINE__);

		//网络初始化
		if (wallet_netinit_register_native_methods(env) != JNI_TRUE)
		{
			if(logable) LOGI("JNI_OnLoad failed %d", __LINE__);
			return -1;
		}
	} while (0);

	if(logable) LOGI("JNI_OnLoad return!");
	result = JNI_VERSION_1_6;
	return result;
}

void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
	if(logable) LOGI("JNI_OnUnload call");
}



