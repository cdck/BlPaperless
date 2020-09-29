#include "control.h"
#include <jni.h>
#include <wallet_net.h>
#include <malloc.h>
#include <string.h>
#include <list>
#include <libDevice_androidcapture.h>
#include "jni_wallet_netinit.h"
#include "meetc/meetinterface_type.h"
#include "meetc/InterFaceCorePBModule.h"
#include "libDevice_androidcapture.h"

// c call java method

#if Paperless_TYPE == 1
static const char *main_interface_class_path_name = "com/paperless/model/NativeUtil";
#elif Paperless_TYPE == 2
static const char* main_interface_class_path_name = "com/wind/myapplication/NativeUtil";
#else
static const char* main_interface_class_path_name = "com/wind/myapplication/NativeUtil";
#endif


int wallet_netinit_register_native_methods(JNIEnv *env) {
    static JNINativeMethod native_methods[] =
            {
                    {"Init_walletSys",  "([B)I",    (void *) Init_walletSys},
                    {"call_method",     "(II[B)[B", (void *) jni_call},
                    {"enablebackgroud", "(I)V",     (void *) jni_enablebackgroud},
                    {"InitAndCapture",  "(II)I",    (void *) jni_AndroidDevice_initcapture},
                    {"call",            "(IIJ[B)I",  (void *) jni_AndroidDevice_call},
                    {"NV21ToI420",     "([BII)[B",  (void *) jni_AndroidDevice_NV21ToI420},
					{ "NV21ToNV12", "([BII)[B", (void *)jni_AndroidDevice_NV21ToNV12 },
					{ "YV12ToNV12", "([BII)[B", (void *)jni_AndroidDevice_YV12ToNV12 },
            };

    if (register_native_methods(env, main_interface_class_path_name, native_methods, NELEM(native_methods)) == -1) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

typedef struct JniCacheData {
    int32u type;
    int32u method;
    void *pdata;
    int datalen;
} JniCacheData, *pJniCacheData;

typedef struct InternalGlobalParam {
    JavaVM *javavm;
    jobject thiz;
    jclass clssJNINet;

    jmethodID merrorProc;
    jmethodID mCallBack_DataProc;
    jmethodID mDoCaptureOper;
	jmethodID mCallBack_YUVDisplayProc;
	jmethodID mCallBack_VideoDecodeProc;
	jmethodID mCallBack_StartDisplay;
	jmethodID mCallBack_StopDisplay;

    jint bisbackgroud;//是否处于后台运行
    std::list<pJniCacheData> *cachelist;
} InternalGlobalParam, *pInternalGlobalParam;

static pInternalGlobalParam g_pinternalparam = NULL;

void AddCacheData(int32u type, int32u method, void *pdata, int datalen) {
    pJniCacheData ptmp = new JniCacheData;
    do {
        if (ptmp == NULL)
            return;

        ptmp->type = type;
        ptmp->method = method;
        ptmp->datalen = datalen;
        if (ptmp->datalen > 0) {
            ptmp->pdata = new char[datalen];
            if (ptmp->pdata == NULL)
                break;
            memcpy(ptmp->pdata, pdata, datalen);
        }
        g_pinternalparam->cachelist->push_back(ptmp);
        return;
    } while (0);
    delete ptmp;
}

/* functions*/
pInternalGlobalParam GetGlobalParam() {
    return g_pinternalparam;
}

JNIEnv *Adapter_GetEnv() {
    int status;
    JNIEnv *envnow = NULL;

    if (g_pinternalparam == NULL)
        return NULL;

    status = g_pinternalparam->javavm->GetEnv((void **) &envnow, JNI_VERSION_1_6);
    if (status != JNI_OK) {
        status = g_pinternalparam->javavm->AttachCurrentThread(&envnow, NULL);
        if (status < 0) {
            if(logable) LOGI("Adapter_GetEnv get status2 return null");
            return NULL;
        }
    }
    return envnow;
}

int jni_initforjava(JNIEnv *env, jobject obj) {
    int ret;
    jclass clss;
    do {
        if(logable) LOGI("jni_initforjava call %d", __LINE__);
        if (g_pinternalparam)
            return 0;

        g_pinternalparam = (pInternalGlobalParam) malloc(
                sizeof(InternalGlobalParam));
        if (g_pinternalparam == NULL) {
            if(logable) LOGI("jni_initforjava %d", __LINE__);
            break;
        }
        memset(g_pinternalparam, 0, sizeof(InternalGlobalParam));
        g_pinternalparam->cachelist = new std::list<pJniCacheData>;
        ret = env->GetJavaVM(&g_pinternalparam->javavm);
        if (ret) {
            if(logable) LOGI("jni_initforjava %d", __LINE__);
            break;
        }

        g_pinternalparam->thiz = env->NewGlobalRef(obj);
        if (g_pinternalparam->thiz == NULL) {
            if(logable) LOGI("jni_initforjava %d", __LINE__);
            break;
        }

        clss = env->FindClass(main_interface_class_path_name);
        if (clss == NULL) {
            if(logable) LOGI("jni_initforjava %d", __LINE__);
            break;
        }
        g_pinternalparam->clssJNINet = (jclass) env->NewGlobalRef(clss);
        if (g_pinternalparam->clssJNINet == NULL) {
            env->DeleteLocalRef(clss);
            if(logable) LOGI("jni_initforjava %d", __LINE__);
            break;
        }
        env->DeleteLocalRef(clss);

        g_pinternalparam->mCallBack_DataProc = env->GetMethodID(
                g_pinternalparam->clssJNINet, "callback_method", "(II[BI)I");
        if (g_pinternalparam->mCallBack_DataProc == NULL) {
            if(logable) LOGI("jni_initforjava mCallBack_DataProc %d", __LINE__);
            break;
        }
        g_pinternalparam->mDoCaptureOper = env->GetMethodID(g_pinternalparam->clssJNINet,
                                                            "callback", "(II)I");
        if (g_pinternalparam->mDoCaptureOper == NULL) {
            if(logable) LOGI("jni_initforjava mDoCaptureOper %d", __LINE__);
            break;
        }
        g_pinternalparam->merrorProc = env->GetMethodID(g_pinternalparam->clssJNINet,
                                                            "error_ret", "(III)V");
        if (g_pinternalparam->merrorProc == NULL) {
            if(logable) LOGI("jni_initforjava merrorProc %d", __LINE__);
            //break;
        }
		g_pinternalparam->mCallBack_YUVDisplayProc = env->GetMethodID(
			g_pinternalparam->clssJNINet, "callback_yuvdisplay", "(III[B[B[B)I");//(int res,int w, int h, byte[] y,byte[] u,byte[] v)
		if (g_pinternalparam->mCallBack_YUVDisplayProc == NULL) {
			if(logable) LOGI("jni_initforjava mCallBack_YUVDisplayProc %d", __LINE__);
			break;
		}
		g_pinternalparam->mCallBack_VideoDecodeProc = env->GetMethodID(
			g_pinternalparam->clssJNINet, "callback_videodecode", "(IIIII[BJ[B)I"); //(int iskeyframe, int res,int codecid, int w, int h, byte[] packet,long pts,byte[] codecdata)
		if (g_pinternalparam->mCallBack_VideoDecodeProc == NULL) {
			if(logable) LOGI("jni_initforjava mCallBack_VideoDecodeProc %d", __LINE__);
			break;
		}
		g_pinternalparam->mCallBack_StartDisplay = env->GetMethodID(
			g_pinternalparam->clssJNINet, "callback_startdisplay", "(I)I"); //(int res)
		if (g_pinternalparam->mCallBack_StartDisplay == NULL) {
			if(logable) LOGI("jni_initforjava mCallBack_StartDisplay %d", __LINE__);
			break;
		}
		g_pinternalparam->mCallBack_StopDisplay = env->GetMethodID(
			g_pinternalparam->clssJNINet, "callback_stopdisplay", "(I)I");//(int res)
		if (g_pinternalparam->mCallBack_StopDisplay == NULL) {
			if(logable) LOGI("jni_initforjava mCallBack_StopDisplay %d", __LINE__);
			break;
		}
        ret = 0;
    } while (0);

    if(logable) LOGI("jni_initforjava return %d, retcode:%d", __LINE__, ret);
    return ret;
}

int localCB_DISPSTART(int resId)						//开始显示
{
	if(logable) LOGI("-------------localCB_DISPSTART line:%d  resId:%d!\n", __LINE__, resId);
	//ctprintf("----start play res:%d !\n", resId);
	int status, battach = 0;
	JNIEnv *env = NULL;

	status = g_pinternalparam->javavm->GetEnv((void **)&env, JNI_VERSION_1_6);
	if (status != JNI_OK) {
		status = g_pinternalparam->javavm->AttachCurrentThread(&env, NULL);
		if (status < 0) {
			if(logable) LOGI("Adapter_GetEnv get status2 return null");
			return 0;
		}
		battach = 1;
	}

	env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_StartDisplay, (jint)resId);
	if (battach)
		g_pinternalparam->javavm->DetachCurrentThread();
	return 0;
}

int localCB_DISPSTOP(int resId)							//停止显示
{
	if(logable) LOGI("-------------localCB_DISPSTOP line:%d  resId:%d!\n", __LINE__, resId);
	//ctprintf("****stop play res:%d !\n", resId);
	int status, battach = 0;
	JNIEnv *env = NULL;

	status = g_pinternalparam->javavm->GetEnv((void **)&env, JNI_VERSION_1_6);
	if (status != JNI_OK) {
		status = g_pinternalparam->javavm->AttachCurrentThread(&env, NULL);
		if (status < 0) {
			if(logable) LOGI("Adapter_GetEnv get status2 return null");
			return 0;
		}
		battach = 1;
	}

	env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_StopDisplay, (jint)resId);
	if (battach)
		g_pinternalparam->javavm->DetachCurrentThread();
	return 0;
}

void localCB_DISPDATA(int* resId, int resNum, char *pData, void *param)	//显示数据
{
	int status, battach = 0;
	JNIEnv *env = NULL;

	status = g_pinternalparam->javavm->GetEnv((void **)&env, JNI_VERSION_1_6);
	if (status != JNI_OK) {
		status = g_pinternalparam->javavm->AttachCurrentThread(&env, NULL);
		if (status < 0) {
			if(logable) LOGI("Adapter_GetEnv get status2 return null");
			return;
		}
		battach = 1;
	}

	PStuAndroidDisplay_Param pParam = (PStuAndroidDisplay_Param)(param);
	if (pParam->param & ANDROIDDISPLAY_PARAM_DECODE)
	{
		//ctprintf("no decode data****play resnum:%d pData:%x !\n", resNum, pData);
		jint iskeyframe = (pParam->param & ANDROIDDISPLAY_PARAM_VIDEOKEYFRAME)? 1: 0;
		jbyteArray barray = env->NewByteArray(pParam->size);
		jbyteArray codecdata = env->NewByteArray(pParam->codecsize);
		env->SetByteArrayRegion(barray, 0, pParam->size, (const jbyte *)pData);
		env->SetByteArrayRegion(codecdata, 0, pParam->codecsize, (const jbyte *)pParam->codecdata);

		if (pParam->codecid == 28)
			pParam->codecid = 27;
		else if (pParam->codecid == 13)
			pParam->codecid = 12;
		else if (pParam->codecid == 174)
			pParam->codecid = 173;
		else if (pParam->codecid == 140)
			pParam->codecid = 139;
		else if (pParam->codecid == 168)
			pParam->codecid = 167;

		for (int ni = 0; ni < resNum; ++ni)
		{
			//if(logable) LOGI("-------video codec:%d resId:%d packetsize:%d!\n", pParam->codecid, resId[ni], pParam->size);
			env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_VideoDecodeProc,iskeyframe, 
				(jint)resId[ni], (jint)pParam->codecid, (jint)pParam->w, (jint)pParam->h, barray, (jlong)pParam->pts, codecdata);
		}
		env->DeleteLocalRef(barray);
		env->DeleteLocalRef(codecdata);
	}
	else
	{
		//ctprintf("decode data ****play resnum:%d pData:%x!\n", resNum, pParam->param);
		int ysize = pParam->linesize[0] * pParam->h;
		int usize = pParam->linesize[1] * (pParam->h / 2);
		int vsize = usize;
		jbyteArray y = env->NewByteArray(ysize);
		jbyteArray u = env->NewByteArray(usize);
		jbyteArray v = env->NewByteArray(vsize);
		env->SetByteArrayRegion(y, 0, ysize, (const jbyte *)pData);
		env->SetByteArrayRegion(u, 0, usize, (const jbyte *)pData + ysize);
		env->SetByteArrayRegion(v, 0, vsize, (const jbyte *)pData + ysize + usize);
		for (int ni = 0; ni < resNum; ++ni)
		{
			//if(logable) LOGI("-------------display yuv resId:%d yuvsize:{%d,%d,%d}!\n", resId[ni], pParam->linesize[0], pParam->linesize[1], pParam->linesize[2]);
			env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_YUVDisplayProc,
				(jint)resId[ni], (jint)pParam->w, (jint)pParam->h, y, u, v);
		}
		env->DeleteLocalRef(y);
		env->DeleteLocalRef(u);
		env->DeleteLocalRef(v);
	}

	if (battach)
		g_pinternalparam->javavm->DetachCurrentThread();
}

void CBLogInfo(int loglevel, const char *pmsg, void *puser) {
    if(logable) LOGI("demolog [%d] %s", loglevel, pmsg);
}

int CallbackFunc(int32u meetingid, int32u type, int32u method, void *pdata, int datalen, void *puser) {
   /* if (type != TYPE_MEET_INTERFACE_TIME && type != TYPE_MEET_INTERFACE_MEDIAPLAYPOSINFO)
	{
		switch (type)
		{
		default:
			if(logable) LOGI("demoCb:%d:%s, %d:%s!\n", type, meetcore_GettypeStr(type), method, meetcore_GetMethodStr(method));
			break;
		}
	}*/
//    if (type != TYPE_MEET_INTERFACE_TIME && type != TYPE_MEET_INTERFACE_MEDIAPLAYPOSINFO) {
//        if(logable) LOGI("jni_initforjava %d  type=%d  method=%d  len=%d", __LINE__, type, method, datalen);
//    }

    if (g_pinternalparam->bisbackgroud) {
        if (type != TYPE_MEET_INTERFACE_TIME && type != TYPE_MEET_INTERFACE_MEDIAPLAYPOSINFO) {
            AddCacheData(type, method, pdata, datalen);
        }
        return 0;
    }
    int status, battach = 0;
    JNIEnv *env = NULL;

    status = g_pinternalparam->javavm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status != JNI_OK) {
        status = g_pinternalparam->javavm->AttachCurrentThread(&env, NULL);
        if (status < 0) {
            if(logable) LOGI("Adapter_GetEnv get status2 return null");
            return 0;
        }
        battach = 1;
    }

    if (!g_pinternalparam->cachelist->empty()) {
        pJniCacheData ptmp;
        std::list<pJniCacheData>::iterator iter = g_pinternalparam->cachelist->begin();
        for (; iter != g_pinternalparam->cachelist->end();) {
            ptmp = *iter;
            ++iter;
            if (ptmp->datalen > 0) {
                jbyteArray barray = env->NewByteArray(ptmp->datalen);
                env->SetByteArrayRegion(barray, 0, ptmp->datalen, (const jbyte *) ptmp->pdata);
                env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_DataProc, (jint) ptmp->type,
                                   (jint) ptmp->method, barray, (jint) ptmp->datalen);
                env->DeleteLocalRef(barray);
                ptmp->datalen = 0;
                delete[] ((char *) ptmp->pdata);
            } else {
                env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_DataProc, (jint) ptmp->type,
                                   (jint) ptmp->method, NULL, 0);
            }
            delete ptmp;
        }
        g_pinternalparam->cachelist->clear();
    }

    if (datalen > 0) {
        jbyteArray barray = env->NewByteArray(datalen);
        env->SetByteArrayRegion(barray, 0, datalen, (const jbyte *) pdata);
        env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_DataProc, (jint) type, (jint) method, barray,
                           (jint) datalen);
        env->DeleteLocalRef(barray);
    } else {
        env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mCallBack_DataProc, (jint) type, (jint) method, NULL, 0);
    }

    if (battach)
        g_pinternalparam->javavm->DetachCurrentThread();
    return 0;
}

void jni_enablebackgroud(JNIEnv *env, jobject thiz, jint benable) {
    g_pinternalparam->bisbackgroud = benable;
}

jbyteArray jni_call(JNIEnv *env, jobject thiz, jint type, jint method, jbyteArray pdata) {
    int ret = -1;
    jbyte *jBuf = 0;
    jint length = 0;

    if(logable) LOGI("195 jni_call  :type:%d method:%d ", type, method);
    if (NULL != pdata) {
        jBuf = env->GetByteArrayElements(pdata, JNI_FALSE);
        length = env->GetArrayLength(pdata);
    }
    void *pretdata = NULL;
    int retdatalen = 0;
    if(logable) LOGI("203 jni_call  :type:%d method:%d length:%d ", type, method, length);
    ret = meetPB_call(type, method, jBuf, length, &pretdata, &retdatalen, 0);
    if (jBuf) {
        //只要非0 就可以进入方
        //释放
        env->ReleaseByteArrayElements(pdata, jBuf, 0);
    }
    if(g_pinternalparam->merrorProc != NULL)
         env->CallVoidMethod(g_pinternalparam->thiz, g_pinternalparam->merrorProc, (jint) type, (jint) method, ret);
    if (ret < ERROR_MEET_INTERFACE_NULL || retdatalen <= 0)
    {
        if(logable) LOGI("jni_call failed:type:%d method:%d  ret:%d retdatalen:%d ", type, method, ret, retdatalen);
        return NULL;
    }
    jbyteArray barray = env->NewByteArray(retdatalen);
    env->SetByteArrayRegion(barray, 0, retdatalen, (const jbyte *) pretdata);
    meetPB_free(pretdata, retdatalen);
    return barray;
}

int Init_walletSys(JNIEnv *env, jobject thiz, jbyteArray pdata) {
    int ret = -1;

    do {
        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
        if (g_pinternalparam)
            return 0;

        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
        if (0 != jni_initforjava(env, thiz))
            break;

        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
        jbyte *jBuf = env->GetByteArrayElements(pdata, JNI_FALSE);
        jint length = env->GetArrayLength(pdata);

        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
        MeetPBCore_CallBack cb = {0};
        cb.pfunc = CallbackFunc;
        cb.plogfunc = CBLogInfo;
        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
        SetmeetPB_callbackFunction(&cb);
        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
        ret = meetPB_call(/*Pb_TYPE_MEET_INTERFACE_INITENV, Pb_METHOD_MEET_INTERFACE_START*/0, 0, jBuf, length, 0, 0, 0);
        env->ReleaseByteArrayElements(pdata, jBuf, 0);
        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);

		StuAndroidDispCB dispinfo = { 0 };
		dispinfo.cbStart = localCB_DISPSTART;
		dispinfo.cbStop = localCB_DISPSTOP;
		dispinfo.cbData = localCB_DISPDATA;
        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
		AndroidDevice_initvideoPlay(&dispinfo);

        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
        if (ret == ERROR_MEET_INTERFACE_NULL) {
            if(logable) LOGI("%s: %d, ret:%d ", __FUNCTION__, __LINE__, ret);
            ret = 0;
        }
        if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
    } while (0);
    if(logable) LOGI("%s: %d ", __FUNCTION__, __LINE__);
    return ret;
}

void InternalAndroidDevice_LogCB(int channelstart, int level, char *pmsg) {
    if(logable) LOGI("InternalAndroidDevice_LogCB %s!", pmsg);
}

int InternalAndroidDevice_GetInfoCB(int channelstart, int oper) {
    int status, battach = 0, vals = 0;
    JNIEnv *env = NULL;

    if(logable) LOGI("InternalAndroidDevice_GetInfoCB %d", __LINE__);
    status = g_pinternalparam->javavm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status != JNI_OK) {
        status = g_pinternalparam->javavm->AttachCurrentThread(&env, NULL);
        if (status < 0) {
            if(logable) LOGI("InternalAndroidDevice_GetInfoCB %d", __LINE__);
            return 0;
        }
        battach = 1;
    }
    if(logable) LOGI("InternalAndroidDevice_GetInfoCB %d", __LINE__);
    vals = env->CallIntMethod(g_pinternalparam->thiz, g_pinternalparam->mDoCaptureOper, (jint) channelstart, (jint) oper);

    if (battach)
        g_pinternalparam->javavm->DetachCurrentThread();

    return vals;
}

//android device capture
int jni_AndroidDevice_initcapture(JNIEnv *env, jobject thiz, jint type, jint channelstart) {
    if(logable) LOGI("------------------------------------------------jni_AndroidDevice_initcapture line:%d!\n", __LINE__);
    SetAndroidDeviceLogCallBack(InternalAndroidDevice_LogCB);
    SetAndroidDeviceGetInfoCallBack(InternalAndroidDevice_GetInfoCB);
    if (AndroidDevice_initcapture(type, channelstart) == 0) {
        if(logable) LOGI("----------------------------------------------------------jni_AndroidDevice_initcapture type: %d, channel:%d!\n",
             type, channelstart);
        return 0;
    }

    if(logable) LOGI("------------------------------------------------jni_AndroidDevice_initcapture line:%d erro!\n", __LINE__);
    return -1;
}

int jni_AndroidDevice_call(JNIEnv *env, jobject thiz, jint channelstart, jint iskeyframe, jlong pts,
                           jbyteArray pdata) {
    jbyte *jBuf = env->GetByteArrayElements(pdata, JNI_FALSE);
    jint length = env->GetArrayLength(pdata);
    int64u pts_l = (int64u)pts;
    //if(logable) LOGI("capturedata:type:%d iskeyframe: %d, pts:%llu, len:%d!\n", channelstart, iskeyframe, pts_l, length);

    AndroidDevice_call(channelstart, (char *) jBuf, length, pts_l, iskeyframe);
    env->ReleaseByteArrayElements(pdata, jBuf, 0);
    return 0;
}

jbyteArray jni_AndroidDevice_NV21ToI420(JNIEnv *env, jobject thiz,jbyteArray pdata,jint width, jint height)
{
    int8u *jBuf = (int8u *)env->GetByteArrayElements(pdata, JNI_FALSE);
    jint length = env->GetArrayLength(pdata);

	int8u* jyuvBuf = new int8u[length];
    if(0 != AndroidDevice_NV21ToI420(jBuf, width, jBuf + (width * height), width, jyuvBuf, width, jyuvBuf + (width * height),  width / 2, jyuvBuf + (width * height) + (width / 2 * height / 2),  width / 2, width, height))
    {
		delete[] jyuvBuf;
        env->ReleaseByteArrayElements(pdata, (jbyte*)jBuf, 0);
        return NULL;
    }
	jbyteArray barray = env->NewByteArray(length);
	env->SetByteArrayRegion(barray, 0, length, (const jbyte *)jyuvBuf);
    env->ReleaseByteArrayElements(pdata, (jbyte*)jBuf, 0);
	delete[] jyuvBuf;
    return barray;
}

jbyteArray jni_AndroidDevice_NV21ToNV12(JNIEnv *env, jobject thiz, jbyteArray pdata, jint width, jint height)
{
	int8u *jBuf = (int8u *)env->GetByteArrayElements(pdata, JNI_FALSE);
	jint length = env->GetArrayLength(pdata);

	int8u* jyuvBuf = new int8u[length];
	if (0 != AndroidDevice_NV21ToNV12(jBuf, width, jBuf + (width * height), width, jyuvBuf, width, jyuvBuf + (width * height), width, width, height))
	{
		delete[] jyuvBuf;
		env->ReleaseByteArrayElements(pdata, (jbyte*)jBuf, 0);
		return NULL;
	}
	jbyteArray barray = env->NewByteArray(length);
	env->SetByteArrayRegion(barray, 0, length, (const jbyte *)jyuvBuf);
	env->ReleaseByteArrayElements(pdata, (jbyte*)jBuf, 0);
	delete[] jyuvBuf;
	return barray;
}

jbyteArray jni_AndroidDevice_YV12ToNV12(JNIEnv *env, jobject thiz, jbyteArray pdata, jint width, jint height)
{
	int8u *jBuf = (int8u *)env->GetByteArrayElements(pdata, JNI_FALSE);
	jint length = env->GetArrayLength(pdata);

	int8u* jyuvBuf = new int8u[length];
	if (0 != AndroidDevice_YV12ToNV12(jBuf, width, jBuf + (width * height), width / 2, jBuf + (width * height) + (width / 2 * height / 2), width / 2, jyuvBuf, width, jyuvBuf + (width * height), width, width, height))
	{
		delete[] jyuvBuf;
		env->ReleaseByteArrayElements(pdata, (jbyte*)jBuf, 0);
		return NULL;
	}
	jbyteArray barray = env->NewByteArray(length);
	env->SetByteArrayRegion(barray, 0, length, (const jbyte *)jyuvBuf);
	env->ReleaseByteArrayElements(pdata, (jbyte*)jBuf, 0);
	delete[] jyuvBuf;
	return barray;
}