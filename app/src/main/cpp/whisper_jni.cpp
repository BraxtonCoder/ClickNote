#include <jni.h>
#include <string>
#include <fstream>
#include <vector>
#include <android/log.h>
#include "whisper.h"
#include <memory>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "whisper_jni", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "whisper_jni", __VA_ARGS__)

struct WhisperContext {
    std::unique_ptr<whisper_context, void(*)(whisper_context*)> ctx;
    whisper_full_params params;

    WhisperContext(whisper_context* c) : ctx(c, whisper_free) {
        params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
        params.print_progress = false;
        params.print_special = false;
        params.print_realtime = false;
        params.print_timestamps = true;
        params.translate = false;
        params.language = "en";
        params.n_threads = 4;
    }
};

// Helper function to load audio file
bool load_wav_file(const char* path, std::vector<float>& pcmf32) {
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) {
        LOGE("Failed to open WAV file: %s", path);
        return false;
    }

    // Read WAV header
    char header[44];
    file.read(header, 44);

    // Get data size from header
    uint32_t data_size = *reinterpret_cast<uint32_t*>(&header[40]);
    uint16_t num_channels = *reinterpret_cast<uint16_t*>(&header[22]);
    uint32_t sample_rate = *reinterpret_cast<uint32_t*>(&header[24]);
    uint16_t bits_per_sample = *reinterpret_cast<uint16_t*>(&header[34]);

    // Read PCM data
    std::vector<int16_t> pcm16;
    pcm16.resize(data_size / sizeof(int16_t));
    file.read(reinterpret_cast<char*>(pcm16.data()), data_size);

    // Convert to float32
    pcmf32.resize(pcm16.size());
    for (size_t i = 0; i < pcm16.size(); i++) {
        pcmf32[i] = static_cast<float>(pcm16[i]) / 32768.0f;
    }

    return true;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_clicknote_service_WhisperLib_createModel(
    JNIEnv* env,
    jobject /* this */,
    jstring model_path
) {
    const char* path = env->GetStringUTFChars(model_path, nullptr);
    if (!path) return 0;

    whisper_context* ctx = whisper_init_from_file(path);
    env->ReleaseStringUTFChars(model_path, path);

    if (!ctx) {
        LOGE("Failed to load model: %s", path);
        return 0;
    }

    auto* wrapper = new WhisperContext(ctx);
    return reinterpret_cast<jlong>(wrapper);
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_clicknote_service_WhisperLib_transcribeChunk(
    JNIEnv* env,
    jobject /* this */,
    jlong handle,
    jbyteArray audio_data,
    jint length
) {
    auto* wrapper = reinterpret_cast<WhisperContext*>(handle);
    if (!wrapper || !wrapper->ctx) {
        LOGE("Invalid model handle");
        return nullptr;
    }

    // Get direct buffer info
    void* buf = env->GetDirectBufferAddress(audio_data);
    if (!buf || length <= 0) {
        LOGE("Invalid buffer");
        return nullptr;
    }

    // Process audio data
    if (whisper_full(wrapper->ctx.get(), wrapper->params, 
                     static_cast<float*>(buf), length / sizeof(float)) != 0) {
        LOGE("Failed to process audio");
        return nullptr;
    }

    // Get number of segments
    const int n_segments = whisper_full_n_segments(wrapper->ctx.get());
    if (n_segments <= 0) return nullptr;

    // Get segment class from WhisperLib
    jclass segmentClass = env->FindClass("com/example/clicknote/service/WhisperLib$NativeSegment");
    if (!segmentClass) return nullptr;

    // Get constructor
    jmethodID constructor = env->GetMethodID(segmentClass, "<init>", 
        "(Ljava/lang/String;JJ)V");
    if (!constructor) return nullptr;

    // Create array of segments
    jobjectArray segments = env->NewObjectArray(n_segments, segmentClass, nullptr);

    // Fill segments
    for (int i = 0; i < n_segments; i++) {
        const char* text = whisper_full_get_segment_text(wrapper->ctx.get(), i);
        const int64_t t0 = whisper_full_get_segment_t0(wrapper->ctx.get(), i);
        const int64_t t1 = whisper_full_get_segment_t1(wrapper->ctx.get(), i);

        jstring jtext = env->NewStringUTF(text);
        jobject segment = env->NewObject(segmentClass, constructor, jtext, 
            t0 * 10, t1 * 10);

        env->SetObjectArrayElement(segments, i, segment);
        env->DeleteLocalRef(jtext);
        env->DeleteLocalRef(segment);
    }

    return segments;
}

JNIEXPORT void JNICALL
Java_com_example_clicknote_service_WhisperLib_destroyModel(
    JNIEnv* /* env */,
    jobject /* this */,
    jlong handle
) {
    auto* wrapper = reinterpret_cast<WhisperContext*>(handle);
    if (wrapper) {
        delete wrapper;
    }
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_clicknote_service_WhisperLib_transcribe(
    JNIEnv* env, jobject /* this */, jlong handle, jstring audio_path) {
    WhisperContext* context = reinterpret_cast<WhisperContext*>(handle);
    const char* path = env->GetStringUTFChars(audio_path, 0);

    std::vector<float> pcmf32;
    if (!load_wav_file(path, pcmf32)) {
        env->ReleaseStringUTFChars(audio_path, path);
        return env->NewStringUTF("Failed to load audio file");
    }

    if (whisper_full(context->ctx.get(), context->params, pcmf32.data(), pcmf32.size()) != 0) {
        env->ReleaseStringUTFChars(audio_path, path);
        return env->NewStringUTF("Failed to process audio");
    }

    const int n_segments = whisper_full_n_segments(context->ctx.get());
    std::string result;

    for (int i = 0; i < n_segments; ++i) {
        const char* text = whisper_full_get_segment_text(context->ctx.get(), i);
        const int64_t t0 = whisper_full_get_segment_t0(context->ctx.get(), i);
        const int64_t t1 = whisper_full_get_segment_t1(context->ctx.get(), i);
        
        if (i > 0) {
            result += " ";
        }
        result += text;
    }

    env->ReleaseStringUTFChars(audio_path, path);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_clicknote_service_WhisperLib_transcribeWithTimestamps(
    JNIEnv* env, jobject /* this */, jlong handle, jstring audio_path) {
    
    WhisperContext* context = reinterpret_cast<WhisperContext*>(handle);
    if (!context || !context->ctx) {
        LOGE("Invalid model handle");
        return nullptr;
    }
    
    const char* path = env->GetStringUTFChars(audio_path, nullptr);
    
    // Load audio file
    std::vector<float> pcmf32;
    if (!load_wav_file(path, pcmf32)) {
        LOGE("Failed to load audio file: %s", path);
        env->ReleaseStringUTFChars(audio_path, path);
        return nullptr;
    }
    
    env->ReleaseStringUTFChars(audioPath, path);
    
    // Run inference
    if (whisper_full(context->ctx.get(), context->params, pcmf32.data(), pcmf32.size()) != 0) {
        LOGE("Failed to run inference");
        return nullptr;
    }
    
    // Get segments
    const int n_segments = whisper_full_n_segments(context->ctx.get());
    
    // Find NativeSegment class
    jclass segmentClass = env->FindClass("com/example/clicknote/service/WhisperLib$NativeSegment");
    if (!segmentClass) {
        LOGE("Failed to find NativeSegment class");
        return nullptr;
    }
    
    // Get constructor
    jmethodID constructor = env->GetMethodID(segmentClass, "<init>", "(Ljava/lang/String;JJ)V");
    if (!constructor) {
        LOGE("Failed to get NativeSegment constructor");
        return nullptr;
    }
    
    // Create array
    jobjectArray segments = env->NewObjectArray(n_segments, segmentClass, nullptr);
    
    // Fill array with segments
    for (int i = 0; i < n_segments; ++i) {
        const char* text = whisper_full_get_segment_text(context->ctx.get(), i);
        int64_t start = whisper_full_get_segment_t0(context->ctx.get(), i);
        int64_t end = whisper_full_get_segment_t1(context->ctx.get(), i);
        
        jstring jtext = env->NewStringUTF(text);
        jobject segment = env->NewObject(segmentClass, constructor, jtext, start, end);
        
        env->SetObjectArrayElement(segments, i, segment);
        env->DeleteLocalRef(jtext);
        env->DeleteLocalRef(segment);
    }
    
    return segments;
}

} // extern "C" 