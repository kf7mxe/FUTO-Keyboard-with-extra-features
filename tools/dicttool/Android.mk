#
# Copyright (C) 2012 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# HACK: Temporarily disable host tool build on Mac until the build system is ready for C++11.
LATINIME_HOST_OSNAME := $(shell uname -s)
ifneq ($(LATINIME_HOST_OSNAME), Darwin) # TODO: Remove this

LATINIME_DICTTOOL_AOSP_LOCAL_PATH := $(call my-dir)
LOCAL_PATH := $(LATINIME_DICTTOOL_AOSP_LOCAL_PATH)
LATINIME_HOST_NATIVE_LIBNAME := liblatinime-aosp-dicttool-host
include $(LOCAL_PATH)/NativeLib.mk

######################################
LOCAL_PATH := $(LATINIME_DICTTOOL_AOSP_LOCAL_PATH)
include $(CLEAR_VARS)

LATINIME_LOCAL_DIR := ../..
LATINIME_BASE_SOURCE_DIRECTORY := $(LATINIME_LOCAL_DIR)/java/src/com/android/inputmethod
LATINIME_ANNOTATIONS_SOURCE_DIRECTORY := $(LATINIME_BASE_SOURCE_DIRECTORY)/annotations
MAKEDICT_CORE_SOURCE_DIRECTORY := $(LATINIME_BASE_SOURCE_DIRECTORY)/latin/makedict
LATINIME_TESTS_SOURCE_DIRECTORY := $(LATINIME_LOCAL_DIR)/tests/src/com/android/inputmethod/latin

# Dependencies for Dicttool. Most of these files are needed by BinaryDictionary.java. Note that
# a significant part of the dependencies are mocked in the compat/ directory, with empty or
# nearly-empty implementations, for parts that we don't use in Dicttool.
LATINIME_SRC_FILES_FOR_DICTTOOL := \
        event/Combiner.java \
        event/Event.java \
        latin/BinaryDictionary.java \
        latin/DicTraverseSession.java \
        latin/Dictionary.java \
        latin/InputPointers.java \
        latin/LastComposedWord.java \
        latin/LatinImeLogger.java \
        latin/PrevWordsInfo.java \
        latin/SuggestedWords.java \
        latin/WordComposer.java \
        latin/settings/NativeSuggestOptions.java \
        latin/utils/BinaryDictionaryUtils.java \
        latin/utils/CollectionUtils.java \
        latin/utils/CombinedFormatUtils.java \
        latin/utils/CoordinateUtils.java \
        latin/utils/FileUtils.java \
        latin/utils/JniUtils.java \
        latin/utils/LocaleUtils.java \
        latin/utils/ResizableIntArray.java \
        latin/utils/StringUtils.java

LATINIME_TEST_SRC_FILES_FOR_DICTTOOL := \
        utils/ByteArrayDictBuffer.java

USED_TARGETED_SRCS := \
        $(addprefix $(LATINIME_BASE_SOURCE_DIRECTORY)/, $(LATINIME_SRC_FILES_FOR_DICTTOOL)) \
        $(addprefix $(LATINIME_TESTS_SOURCE_DIRECTORY)/, $(LATINIME_TEST_SRC_FILES_FOR_DICTTOOL))

DICTTOOL_ONDEVICE_TESTS_DIRECTORY := \
        $(LATINIME_LOCAL_DIR)/tests/src/com/android/inputmethod/latin/makedict/
DICTTOOL_COMPAT_TESTS_DIRECTORY := compat

LOCAL_MAIN_SRC_FILES := $(call all-java-files-under, $(MAKEDICT_CORE_SOURCE_DIRECTORY))
LOCAL_TOOL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_ANNOTATIONS_SRC_FILES := \
        $(call all-java-files-under, $(LATINIME_ANNOTATIONS_SOURCE_DIRECTORY))

LOCAL_SRC_FILES := $(LOCAL_TOOL_SRC_FILES) \
        $(filter-out $(addprefix %/, $(notdir $(LOCAL_TOOL_SRC_FILES))), $(LOCAL_MAIN_SRC_FILES)) \
        $(call all-java-files-under, $(DICTTOOL_COMPAT_TESTS_DIRECTORY)) \
        $(LOCAL_ANNOTATIONS_SRC_FILES) $(USED_TARGETED_SRCS) \
        $(LATINIME_BASE_SOURCE_DIRECTORY)/latin/Constants.java \
        $(call all-java-files-under, tests) \
        $(call all-java-files-under, $(DICTTOOL_ONDEVICE_TESTS_DIRECTORY))

LOCAL_JAVA_LIBRARIES := junit
LOCAL_ADDITIONAL_DEPENDENCIES := $(LATINIME_HOST_NATIVE_LIBNAME)
LOCAL_JAR_MANIFEST := etc/manifest.txt
LOCAL_MODULE := dicttool_aosp

include $(BUILD_HOST_JAVA_LIBRARY)
include $(LOCAL_PATH)/etc/Android.mk

endif # Darwin - TODO: Remove this

# Clear our private variables
LATINIME_DICTTOOL_AOSP_LOCAL_PATH :=
LATINIME_LOCAL_DIR :=
LATINIME_HOST_OSNAME :=
