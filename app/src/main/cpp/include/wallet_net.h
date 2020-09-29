/*
 * wallet_net.h
 *
 *  Created on: 2014-4-8
 *      Author: root
 */

#ifndef WALLET_NET_H_
#define WALLET_NET_H_
#include <jni.h>
#include "helpers.h"

#ifndef LOGI
#include <android/log.h>
#define LOGI(...) {__android_log_print(ANDROID_LOG_INFO, __FILE__, __VA_ARGS__);}
#endif

#define FALSE 0
#define TRUE  1

#ifndef NELEM
#define NELEM(x) ((int)(sizeof(x) / sizeof((x)[0])))
#endif

JNIEnv*		 Adapter_GetEnv();

#endif /* WALLET_NET_H_ */
