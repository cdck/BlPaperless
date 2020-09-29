
cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/include/)
include_directories(src/main/cpp/)
add_definitions(-D_LINUX -D_ANDROID -DDEVICETYPE=0)

#设置生成的so动态库最后输出的路径
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/)

add_library(
             native-lib
             SHARED
             src/main/cpp/jni_wallet_netinit.cpp
             src/main/cpp/wallet_net.cpp
             src/main/cpp/helpers.c
             )

add_library(avcodec-57 SHARED IMPORTED)
set_target_properties(avcodec-57  PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavcodec-57.so)

add_library(avdevice-57 SHARED IMPORTED)
set_target_properties(avdevice-57 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavdevice-57.so)

add_library(avfilter-6 SHARED IMPORTED)
set_target_properties(avfilter-6 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavfilter-6.so)

add_library(avformat-57 SHARED IMPORTED)
set_target_properties(avformat-57 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavformat-57.so)

add_library(avutil-55 SHARED IMPORTED)
set_target_properties(avutil-55 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavutil-55.so)

add_library(Codec SHARED IMPORTED)
set_target_properties(Codec PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libCodec.so)

add_library(ExecProc SHARED IMPORTED)
set_target_properties(ExecProc PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libExecProc.so)

add_library(meetcoreAnd SHARED IMPORTED)
set_target_properties(meetcoreAnd PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libmeetcoreAnd.so)

add_library(NetClient SHARED IMPORTED)
set_target_properties(NetClient PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libNetClient.so)

add_library(Device-OpenSles SHARED IMPORTED)
set_target_properties(Device-OpenSles PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libDevice-OpenSles.so)

add_library(PBmeetcoreAnd SHARED IMPORTED)
set_target_properties(PBmeetcoreAnd PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libPBmeetcoreAnd.so)

add_library(postproc-54 SHARED IMPORTED)
set_target_properties(postproc-54 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libpostproc-54.so)

add_library(meetAnd SHARED IMPORTED)
set_target_properties(meetAnd PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libmeetAnd.so)

add_library(SDL2 SHARED IMPORTED)
set_target_properties(SDL2 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libSDL2.so)

add_library(main SHARED IMPORTED)
set_target_properties(main PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libmain.so)

add_library(swresample-2 SHARED IMPORTED)
set_target_properties(swresample-2 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswresample-2.so)

add_library(swscale-4 SHARED IMPORTED)
set_target_properties(swscale-4 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswscale-4.so)

add_library(z STATIC IMPORTED)
set_target_properties(z PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libz.a)


find_library(log-lib log )
find_library(opensles-lib OpenSLES )
target_link_libraries(
                       native-lib
                       avcodec-57
                       avdevice-57
                       avfilter-6
                       avformat-57
                       avutil-55
                       postproc-54
                       swresample-2
                       swscale-4
                       SDL2
                       main
                       NetClient
                       Device-OpenSles
                       Codec
                       ExecProc
                       meetcoreAnd
                       PBmeetcoreAnd
                       meetAnd
                       z
                       ${log-lib}
                       ${opensles-lib})


