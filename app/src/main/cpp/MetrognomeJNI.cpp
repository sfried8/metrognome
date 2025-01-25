//
// Created by Sam on 1/19/2025.
//
/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

static const char *TAG = "MetrognomeJNI";

#include <android/log.h>

#include "metrognome.h"

// JNI functions are "C" calling convention
#ifdef __cplusplus
extern "C" {
#endif

using namespace oboe;

// Use a static object so we don't have to worry about it getting deleted at the wrong time.
static MetronomeNoiseMaker sPlayer;

/**
 * Native (JNI) implementation of AudioPlayer.startAudiostreamNative()
 */
JNIEXPORT jint JNICALL Java_com_friedman_metrognome_AudioPlayer_startAudioStreamNative(
        JNIEnv * /* env */, jobject) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "%s", __func__);
    Result result = sPlayer.open();
    if (result == Result::OK) {
        result = sPlayer.start();
        sPlayer.setBpm(240);

    }
    return (jint) result;
}

/**
 * Native (JNI) implementation of AudioPlayer.stopAudioStreamNative()
 */
JNIEXPORT jint JNICALL Java_com_friedman_metrognome_AudioPlayer_stopAudioStreamNative(
        JNIEnv * /* env */, jobject) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "%s", __func__);
    // We need to close() even if the stop() fails because we need to delete the resources.
    Result result1 = sPlayer.stop();
    Result result2 = sPlayer.close();
    // Return first failure code.
    return (jint) ((result1 != Result::OK) ? result1 : result2);
}

#ifdef __cplusplus
}
#endif
extern "C"
JNIEXPORT jint JNICALL
Java_com_friedman_metrognome_AudioPlayer_audioSetBpm(JNIEnv *env, jobject thiz, jint bpm) {
    sPlayer.setBpm(bpm);
    return (jint) Result::OK;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_friedman_metrognome_AudioPlayer_setMeasuresNative(JNIEnv *env, jobject thiz,
                                                     jobjectArray measures) {
    // TODO: implement setMeasures()
    int len = env->GetArrayLength(measures);
    std::vector<MeasureData> measureVec(len);
    for (int i = 0; i < len; i++) {
        jobject measureObj = env->GetObjectArrayElement(measures, i);
        jclass measureClass = env->GetObjectClass(measureObj);
        jint timeSignatureTop = env->GetIntField(measureObj, env->GetFieldID(measureClass, "timeSignatureTop", "I"));
        jint timeSignatureBottom = env->GetIntField(measureObj, env->GetFieldID(measureClass, "timeSignatureBottom", "I"));
        jfieldID accentPatternFieldId = env->GetFieldID(measureClass, "accentPattern", "[I");
        auto accentPatternArray = (jintArray) env->GetObjectField(measureObj, accentPatternFieldId);
        jsize len2 = env->GetArrayLength(accentPatternArray);
        jint *accentPatternIntArray = env->GetIntArrayElements(accentPatternArray, nullptr);
        std::vector<int>accentPatternIntVector(accentPatternIntArray, accentPatternIntArray + len2);
        std::vector<EighthNoteGrouping> accentPattern(len2);
        for (int j = 0; j < len2; j++) {
            accentPattern[j] = (EighthNoteGrouping) accentPatternIntVector[j];
        }
        env->ReleaseIntArrayElements(accentPatternArray, accentPatternIntArray, 0);
        measureVec[i] = MeasureData{TimeSignature{(int)timeSignatureTop, (int)timeSignatureBottom}, accentPattern};
    }
    sPlayer.setMeasures(measureVec);
    return (jint) Result::OK;
}