LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

# Add static library for nvram
# LOCAL_STATIC_JAVA_LIBRARIES := vendor.mediatek.hardware.nvram-V1.1-java-static
LOCAL_STATIC_JAVA_LIBRARIES := vendor.mediatek.hardware.nvram-V1.1-java
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := JNvRAM_V11
LOCAL_PRIVATE_PLATFORM_APIS := true
include $(BUILD_PACKAGE)
