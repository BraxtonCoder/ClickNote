cmake_minimum_required(VERSION 3.22.1)

project("clicknote")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add Whisper library
add_library(whisper STATIC
    whisper.cpp
    whisper.h
    ggml.c
    ggml.h
)

target_compile_definitions(whisper PRIVATE
    GGML_USE_ACCELERATE
)

# Add our JNI library
add_library(whisper_jni SHARED
    whisper_jni.cpp
)

# Find required packages
find_library(log-lib log)
find_library(android-lib android)

# Link libraries
target_link_libraries(whisper_jni
    whisper
    ${log-lib}
    ${android-lib}
)

# Include directories
target_include_directories(whisper_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Set compiler flags
target_compile_options(whisper_jni PRIVATE
    -Wall
    -Wextra
    -Werror
    -fno-exceptions
    -fno-rtti
)

# Enable optimization for release builds
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3")

# Set output directories
set_target_properties(whisper_jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}"
) 