/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <memory>
#include "participant.h"
/* Header for class com_twilio_video_Participant */

#ifndef _Included_com_twilio_video_Participant
#define _Included_com_twilio_video_Participant

#ifdef __cplusplus
extern "C" {
#endif

struct ParticipantDataContext {
    std::shared_ptr<twilio::video::Participant> participant;
};

JNIEXPORT jstring JNICALL Java_com_twilio_video_Participant_nativeGetIdentity
    (JNIEnv *, jobject, jlong);

JNIEXPORT jstring JNICALL Java_com_twilio_video_Participant_nativeGetSid
    (JNIEnv *, jobject, jlong);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_Participant_nativeIsConnected
    (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Participant_nativeRelease
    (JNIEnv *, jobject, jlong);


#ifdef __cplusplus
}
#endif
#endif
