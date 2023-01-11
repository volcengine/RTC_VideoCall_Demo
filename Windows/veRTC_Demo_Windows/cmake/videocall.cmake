include_directories(
  ${PORJECT_ROOT_PATH}/videocall
)

file (GLOB_RECURSE VIDEOCALL_SOURCE  
	${PORJECT_ROOT_PATH}/videocall/*.cc 
	${PORJECT_ROOT_PATH}/videocall/*.h 
	${PORJECT_ROOT_PATH}/videocall/*.ui
)

set(VIDEOCALL_SRC 
	${VIDEOCALL_SOURCE}
)

set(VIDEOCALL_QRC 
    ${PORJECT_ROOT_PATH}/videocall/resource/video_call_resource.qrc
)

