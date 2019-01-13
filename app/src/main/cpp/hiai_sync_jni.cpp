#include <jni.h>
#include <string>
#include <memory.h>
#include "include/HIAIModelManager.h"
#include "include/ErrorCode.h"
#include "include/ops.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <sstream>

#define LOG_TAG "SYNC_DDK_MSG"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace std;

static HIAI_ModelManager *modelManager = NULL;

static HIAI_TensorBuffer *inputtensor = NULL;
static HIAI_TensorBuffer *inputtensor2 = NULL;

static HIAI_TensorBuffer *outputtensor = NULL;

static HIAI_ModelBuffer *modelBuffer = NULL;

static int input_N = 0;
static int input_C = 0;
static int input_H = 0;
static int input_W = 0;
static int input_N2 = 0;
static int input_C2 = 0;
static int input_H2 = 0;
static int input_W2 = 0;
static int output_N = 0;
static int output_C = 0;
static int output_H = 0;
static int output_W = 0;

//get Input and Output N C H W from model  after loading success the model
static void getInputAndOutputFromModel(const char *modelName){
    HIAI_ModelTensorInfo* modelTensorInfo = HIAI_ModelManager_getModelTensorInfo(modelManager, modelName);
    if (modelTensorInfo == NULL){
        LOGE("HIAI_ModelManager_getModelTensorInfo failed!!");
        return ;
    }

    /**
     * if your model have muli-input and muli-output
     * you can get N C H W from model like as below:
     *
     for (int i = 0; i < modelTensorInfo->input_cnt; ++i)
    {
        LOGI("input[%u] N: %u-C: %u-H: %u-W: %u\n", i, modelTensorInfo->input_shape[i*4], modelTensorInfo->input_shape[i*4 + 1],
               modelTensorInfo->input_shape[i*4 + 2], modelTensorInfo->input_shape[i*4 + 3]);


        HIAI_TensorBuffer* input = HIAI_TensorBuffer_create(modelTensorInfo->input_shape[i*4], modelTensorInfo->input_shape[i*4 + 1],
                                                            modelTensorInfo->input_shape[i*4 + 2], modelTensorInfo->input_shape[i*4 + 3]);
     }
     */

    LOGI("input count: %u  output:%u\n",  modelTensorInfo->input_cnt,modelTensorInfo->output_cnt);

    //get N C H W from model, The case use 1 input and 1 output ,So we take a simplified approach here
    LOGI("input N:%u C:%u H:%u W:%u\n",  modelTensorInfo->input_shape[0], modelTensorInfo->input_shape[1],
         modelTensorInfo->input_shape[2], modelTensorInfo->input_shape[3]);
    input_N = modelTensorInfo->input_shape[0];
    input_C = modelTensorInfo->input_shape[1];
    input_H = modelTensorInfo->input_shape[2];
    input_W = modelTensorInfo->input_shape[3];

    input_N2 = modelTensorInfo->input_shape[4];
    input_C2 = modelTensorInfo->input_shape[5];
    input_H2 = modelTensorInfo->input_shape[6];
    input_W2 = modelTensorInfo->input_shape[7];

    LOGI("output N:%u C:%u H:%u W:%u\n",  modelTensorInfo->output_shape[0], modelTensorInfo->output_shape[1],
         modelTensorInfo->output_shape[2], modelTensorInfo->output_shape[3]);
    output_N = modelTensorInfo->output_shape[0];
    output_C = modelTensorInfo->output_shape[1];
    output_H = modelTensorInfo->output_shape[2];
    output_W = modelTensorInfo->output_shape[3];

    if(modelTensorInfo != NULL){
        HIAI_ModelManager_releaseModelTensorInfo(modelTensorInfo);
        modelTensorInfo = NULL;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_isausmanov_scriboai_model_ModelManager_loadModelSync(JNIEnv *env, jclass type,
                                                                      jstring jmodelName, jobject assetManager) {
    const char *modelName = env->GetStringUTFChars(jmodelName, 0);

    char modelname[128] = {0};

    strcat(modelname, modelName);
    strcat(modelname, ".cambricon");

    modelManager = HIAI_ModelManager_create(NULL);

    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);

    LOGI("model name is %s", modelname);

    AAsset *asset = AAssetManager_open(mgr, modelname, AASSET_MODE_BUFFER);

    if (nullptr == asset) {
        LOGE("AAsset is null...\n");
    }

    const void *data = AAsset_getBuffer(asset);

    if (nullptr == data) {
        LOGE("model buffer is null...\n");
    }

    off_t len = AAsset_getLength(asset);

    if (0 == len) {
        LOGE("model buffer length is 0...\n");
    }

    HIAI_ModelBuffer *modelBuffer = HIAI_ModelBuffer_create_from_buffer(modelName,
                                                                        (void *) data, len,
                                                                        HIAI_DevPerf::HIAI_DEVPREF_HIGH);
    HIAI_ModelBuffer *modelBufferArray[] = {modelBuffer};

    int ret = HIAI_ModelManager_loadFromModelBuffers(modelManager, modelBufferArray, 1);

    LOGI("load model from assets ret = %d", ret);

    getInputAndOutputFromModel(modelName);

    env->ReleaseStringUTFChars(jmodelName, modelName);

    AAsset_close(asset);

    return ret;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_isausmanov_scriboai_model_ModelManager_unloadModelSync(JNIEnv *env, jclass type) {
    if (NULL == modelManager) {
        LOGE("please load model first.");
        return -1;
    } else {
        if (modelBuffer != NULL) {
            HIAI_ModelBuffer_destroy(modelBuffer);
            modelBuffer = NULL;
        }

        int ret = HIAI_ModelManager_unloadModel(modelManager);

        LOGI("JNI unload model ret:%d", ret);

        HIAI_ModelManager_destroy(modelManager);
        modelManager = NULL;

        return ret;
    }
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_isausmanov_scriboai_model_ModelManager_runModelSync(JNIEnv *env, jclass type, jstring jmodelName,
                                                                     jfloatArray jbuf) {

    if (NULL == modelManager) {
        LOGE("please load model first");
        return NULL;
    }

    if (NULL == jbuf) {
        LOGE("please input data");
        return NULL;
    }

    float *dataBuff = env->GetFloatArrayElements(jbuf, NULL);

    const char *modelName = env->GetStringUTFChars(jmodelName, 0);

    //Todo: modify input tensor
    inputtensor = HIAI_TensorBuffer_create(input_N, input_C, input_H, input_W);
    inputtensor2 = HIAI_TensorBuffer_create(input_N2, input_C2, input_H2, input_W2);

    HIAI_TensorBuffer *inputtensorbuffer[] = {inputtensor, inputtensor2};

    //Todo: modify  output tensor
    outputtensor = HIAI_TensorBuffer_create(output_N, output_C, output_H, output_W);

    HIAI_TensorBuffer *outputtensorbuffer[] = {outputtensor};

    float *inputbuffer = (float *) HIAI_TensorBuffer_getRawBuffer(inputtensor);

    int length = HIAI_TensorBuffer_getBufferSize(inputtensor);

    LOGI("SYNC JNI runModel modelname:%s", modelName);
    memcpy(inputbuffer, dataBuff, length);

    float time_use;
    struct timeval tpstart, tpend;
    gettimeofday(&tpstart, NULL);

    int ret;
    int outputSize;
    if (!strcmp(modelName, "Speakerchangedetection")) {
        ret = HIAI_ModelManager_runModel(
                modelManager,
                inputtensorbuffer,
                2,
                outputtensorbuffer,
                1,
                32,
                modelName);
        outputSize = 32;
    }
    else {
        LOGI("Well we're here so wtf:%s", modelName);
        ret = HIAI_ModelManager_runModel(
                modelManager,
                inputtensorbuffer,
                2,
                outputtensorbuffer,
                1,
                15360,
                modelName);
        outputSize = 15360;
    }

    if(ret != NO_ERROR){
        LOGE("run model ret: %d", ret);
        return NULL;
    } else{
        LOGI("run model ret: %d", ret);
    }

    gettimeofday(&tpend, NULL);
    time_use = 1000000 * (tpend.tv_sec - tpstart.tv_sec) + tpend.tv_usec - tpstart.tv_usec;

    LOGI("infrence time %f ms.", time_use / 1000);

    float *outputBuffer = (float *) HIAI_TensorBuffer_getRawBuffer(outputtensor);

    jfloatArray result = env->NewFloatArray(outputSize);
    env->SetFloatArrayRegion(result, 0, outputSize, outputBuffer);

    if (inputtensor != NULL) {
        HIAI_TensorBuffer_destroy(inputtensor);
        inputtensor = NULL;
    }

    if (outputtensor != NULL) {
        HIAI_TensorBuffer_destroy(outputtensor);
        outputtensor = NULL;
    }

    return result;
}