###########################################################################
# Copyright (c) 2018 by Chris Gray, KIFFER Ltd.  All rights reserved.     #
#                                                                         #
# Redistribution and use in source and binary forms, with or without      #
# modification, are permitted provided that the following conditions      #
# are met:                                                                #
# 1. Redistributions of source code must retain the above copyright       #
#    notice, this list of conditions and the following disclaimer.        #
# 2. Redistributions in binary form must reproduce the above copyright    #
#    notice, this list of conditions and the following disclaimer in the  #
#    documentation and/or other materials provided with the distribution. #
# 3. Neither the name of KIFFER Ltd nor the names of other contributors   #
#    may be used to endorse or promote products derived from this         #
#    software without specific prior written permission.                  #
#                                                                         #
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          #
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    #
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    #
# IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    #
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      #
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS #
# OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   #
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     #
# STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   #
# IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      #
# POSSIBILITY OF SUCH DAMAGE.                                             #
###########################################################################

CFLAGS += -I $(MIKA_TOP)/core-vm/include
CFLAGS += -I $(MIKA_TOP)/core-vm/hal/cpu/$(CPU)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/$(SCHEDULER)/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/kernel/common/include
CFLAGS += -I $(MIKA_TOP)/vm-cmp/network/native/hal/hostos/$(HOSTOS)/include
CFLAGS += -I $(MIKA_TOP)/vm-ext/jpda/jdwp/include

ifeq ($(HOSTOS), linux)
  CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/posix/include
endif
ifeq ($(HOSTOS), android)
  CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/posix/include
  CFLAGS += -I $(TOOLCHAIN_HEADERS)
endif
ifeq ($(HOSTOS), netbsd)
  CFLAGS += -I $(MIKA_TOP)/core-vm/hal/hostos/posix/include
endif

CFLAGS += -I $(gendir)
CFLAGS += -L $(libdir)
LDFLAGS += -lmika -loswald -lawt -lpthread

# 
# Add the dl library if we want to be able to load shared libraries.
# Also define SHARED_OBJECTS
#

ifeq ($(SHARED_OBJECTS), true)
  CFLAGS_shared_objects_linux = -DSHARED_OBJECTS 
  LDFLAGS_shared_objects_linux = -Wl,-export-dynamic
  CFLAGS_shared_objects_netbsd = -DSHARED_OBJECTS
  LDFLAGS_shared_objects_netbsd = -Wl,-export-dynamic

  CFLAGS += $(CFLAGS_shared_objects_$(HOSTOS))
  LDFLAGS += $(LDFLAGS_shared_objects_$(HOSTOS))

  ifeq ($(USE_LIBFFI), true)
    ifdef LIBFFI_A_LOCATION
      LDFLAGS += -L$(LIBFFI_A_LOCATION)
    endif
    LDFLAGS += -lffi
  endif
  ifeq ($(SCHEDULER), oswald)
    ifeq ($(SHARED_HEAP), true)
      CFLAGS += -DSHARED_HEAP
    endif
  endif
  LDFLAGS += -ldl
endif

ifeq "$(AWT)" "rudolph"
  ifeq "$(AWT_DEVICE)" "xsim"
    LDFLAGS += -lX11
  endif
endif

ifeq ($(MAPFILE), true)
  LDFLAGS += -Wl,--cref,-Map,$(OROOT)/map
endif

ifeq ($(JAVAX_COMM), true)
   CFLAGS += -lcomm
endif
ifeq ($(SCHEDULER), "o4e")
  CFLAGS += -L $(MIKA_TOP)/vm-cmp/fs/vfs/src/image.a
endif

.PHONY : awt filesystem network fp jpda math security core-vm generated

mika : main.o $(MIKA_LIB) $(OSWALD_LIB) $(AWT_LIB)
	$(CC) -o mika main.o $(CFLAGS) $(LDFLAGS)

main.o : main.c

$(MIKA_LIB) : awt filesystem network fp jpda math security core-vm generated 
	@echo "Archiving object files from directories $(wildcard $(objdir)/core-vm/*) $(wildcard $(objdir)/vm-cmp/*) $(wildcard $(objdir)/vm-ext/*) $(objdir)/generated"
	$(AR) $@ $(foreach dir, $(wildcard $(objdir)/core-vm/*), $(wildcard $(dir)/*.o)) $(foreach dir, $(wildcard $(objdir)/vm-cmp/*), $(wildcard $(dir)/*.o)) $(foreach dir, $(wildcard $(objdir)/vm-ext/*), $(wildcard $(dir)/*.o)) $(wildcard $(objdir)/generated/*.o)
	ranlib $@

filesystem :
	make -C ../vm-cmp/fs/$(FILESYSTEM) $(MAKECMDGOALS)

network :
	make -C ../vm-cmp/network/$(NETWORK) $(MAKECMDGOALS)

awt : core-vm $(gendir)/core-classes.h
	make -C ../vm-cmp/awt/$(AWT) $(MAKECMDGOALS)

tool :
	make -C tool $(MAKECMDGOALS)

jpda : core-vm $(gendir)/core-classes.h
	make -C ../vm-ext/jpda $(MAKECMDGOALS)

fp:
	make -C ../vm-cmp/fp/$(FLOATING_POINT) $(MAKECMDGOALS)

# TODO: we probably only need this if SECURITY_PROVIDER is not 'none'
security :
	make -C ../vm-cmp/security/provider/any/src/native/wonka/security $(MAKECMDGOALS)

math :
ifeq ($(MATH), native)
	make -C ../vm-cmp/math/native $(MAKECMDGOALS)
endif

generated :
	$(CC) -c -o $(objdir)/generated/core-classes.o $(CFLAGS) $(gendir)/core-classes.c
	$(CC) -c -o $(objdir)/generated/unicode.o $(CFLAGS) $(gendir)/unicode.c
ifeq ($(MATH), native)
	$(CC) -c -o $(objdir)/generated/math-classes.o $(CFLAGS) $(gendir)/math-classes.c
endif

core-vm:
	awk -f $(scriptdir)/unicode.awk src/vm/UnicodeData-Latest.txt > $(gendir)/unicode.c
	awk -f $(scriptdir)/core-classes-h.awk -f $(scriptdir)/common-classes-h.awk src/vm/core-classes.in > $(gendir)/core-classes.h
ifeq ($(JAVA5_SUPPORT), true)
	awk -f $(scriptdir)/core-classes-c.awk -f $(scriptdir)/common-classes-c.awk src/vm/core-classes5.in > $(gendir)/core-classes.c
else
	awk -f $(scriptdir)/core-classes-c.awk -f $(scriptdir)/common-classes-c.awk src/vm/core-classes.in > $(gendir)/core-classes.c
endif
	make -C src $(MAKECMDGOALS)
	make -C hal $(MAKECMDGOALS)


