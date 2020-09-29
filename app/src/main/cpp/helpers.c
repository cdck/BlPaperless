/*
 * helpers.c
 * Copyright (c) 2012 Jacek Marchwicki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include "control.h"
#include <jni.h>
#include <string.h>
#include "helpers.h"

int register_native_methods(JNIEnv* env, const char* class_name, JNINativeMethod* methods, int num_methods)
{
	jclass clazz;

	clazz = (*env)->FindClass(env,class_name);
	if (clazz == NULL) {
		return -1;
	}
	if ((*env)->RegisterNatives(env,clazz, methods, num_methods) < 0) {
		return -1;
	}

	return 0;
}
