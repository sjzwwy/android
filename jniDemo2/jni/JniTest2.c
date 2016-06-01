#include <string.h>
#include <android/log.h>
#include <assert.h>
#include <stdlib.h>

#include "com_example_jniDemo2_MyJniTest.h"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,"com.example.myapp", __VA_ARGS__)

//这个是签名的hashcode的Java端可以获得,把的到的值写在这里
const jint apksign = -344438290;

JNIEXPORT jstring JNICALL Java_com_example_jniDemo2_MyJniTest_getKey(JNIEnv *env, jclass j)
{
   return (*env)->NewStringUTF(env, "Hello from JNI !");
}

jint getSignHashCode(JNIEnv *env) {
    //get application context.
    jclass activityThread = (*env)->FindClass(env,"android/app/ActivityThread");
    jmethodID currentActivityThread = (*env)->GetStaticMethodID(env,activityThread, "currentActivityThread","()Landroid/app/ActivityThread;");
    jobject at = (*env)->CallStaticObjectMethod(env,activityThread, currentActivityThread);
    jmethodID getApplication = (*env)->GetMethodID(env,activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = (*env)->CallObjectMethod(env,at, getApplication);
//
//    //get Context class
    jclass context_clazz = (*env)->GetObjectClass(env,context);
//
//    //getPackageManager method ID
    jmethodID methodID_getPackageManager = (*env)->GetMethodID(env,context_clazz, "getPackageManager",
                                                            "()Landroid/content/pm/PackageManager;");
//    //get PackageManager object
    jobject packageManager = (*env)->CallObjectMethod(env,context,
                                                   methodID_getPackageManager);
//    //get PackageManager class
    jclass pm_clazz = (*env)->GetObjectClass(env,packageManager);
//
//    //getPackageInfo method ID
    jmethodID methodID_pm = (*env)->GetMethodID(env,pm_clazz, "getPackageInfo",
                                             "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
//    //getPackageName method ID.
    jmethodID methodID_pack = (*env)->GetMethodID(env,context_clazz, "getPackageName",
                                               "()Ljava/lang/String;");
//
//    //get package name.
    jstring application_package = (jstring) (*env)->CallObjectMethod(env,context, methodID_pack);
    const char *str = (*env)->GetStringUTFChars(env,application_package, 0);
//
//    //get PackageInfo object.
    jobject packageInfo = (*env)->CallObjectMethod(env,packageManager,
                                                methodID_pm, application_package, 64);
//    //get PackageInfo class.
    jclass packageinfo_clazz = (*env)->GetObjectClass(env,packageInfo);
    jfieldID fieldID_signatures = (*env)->GetFieldID(env,packageinfo_clazz, "signatures",
                                                  "[Landroid/content/pm/Signature;");
//    //get signatures array.
    jobjectArray signature_arr = (jobjectArray) (*env)->GetObjectField(env,packageInfo,
                                                                    fieldID_signatures);
//    //get Signature element 0.
    jobject signature = (*env)->GetObjectArrayElement(env,signature_arr, 0);
//
//    //get signature hashcode
    jclass signature_clazz = (*env)->GetObjectClass(env,signature);
    jmethodID methodID_hashcode = (*env)->GetMethodID(env,signature_clazz, "hashCode", "()I");
    jint hashCode = (*env)->CallIntMethod(env,signature, methodID_hashcode);
//
//    //compare hashcode.
    if (hashCode != apksign) {
        LOGI(" %d != %d\n", hashCode, apksign);
        //如果签名不一致就崩溃
        exit(0);
    }
    else
    {
        LOGI(" %d == %d\n", hashCode, apksign);
    }

    return hashCode;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
        if ((*vm)->GetEnv(vm,(void **) &env, JNI_VERSION_1_6) != JNI_OK) {
//            if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
                LOGI("load library error 1");
                return JNI_ERR;
            }
        assert(env != NULL);
        getSignHashCode(env);
    LOGI(" %d != %d\n", 1000, 100);
    return JNI_VERSION_1_6;
}

