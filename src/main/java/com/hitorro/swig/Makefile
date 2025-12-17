OSNAME := $(shell uname)
#TOP        = /swig/Examples
TOP        = $(SWIG_ROOT)/Examples
SWIG       = $(TOP)/../preinst-swig
CXXSRCS    = SwigUtils.cxx
TARGET     = SwigUtils
INTERFACE  = SwigUtils.i
SWIGOPT    = -package com.hitorro.util.swig
ifeq ($(OSNAME),Darwin)
  DFLAG=-D_DARWIN_C_SOURCE
endif

ifeq ($(OSNAME),SunOS)
endif

ifeq ($(OSNAME),Linux)
endif

java::
	echo "OSName= " $(OSNAME)
	echo "TOP= " $(TOP)
	$(MAKE) -f $(TOP)/Makefile CXXSRCS='$(CXXSRCS) $(DFLAG)' SWIG='$(SWIG)' \
	SWIGOPT='$(SWIGOPT) $(DFLAG)' TARGET='$(TARGET)' INTERFACE='$(INTERFACE)' java_cpp
clean::
	$(MAKE) -f $(TOP)/Makefile java_clean

check: all
