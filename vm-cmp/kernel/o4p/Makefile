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

export OSWALD_TOP  = $(MIKA_TOP)/vm-cmp/kernel/o4p
export COMMON_TOP  = $(MIKA_TOP)/vm-cmp/kernel/common
export OSWALD_TEST = o4ptests

export MIKA_TOP
export PLATFORM

export CFLAGS += -I $(OSWALD_TOP)/include
export CFLAGS += -I $(COMMON_TOP)/include

ifeq ($(HOSTOS), winnt)
    LINKLIBS += -lpthread
endif
ifeq ($(HOSTOS), linux)
    LINKLIBS += -lpthread
endif
ifeq ($(HOSTOS), netbsd)
    LINKLIBS += -pthread
endif

.PHONY : clean mika src test
mika: $(OSWALD_LIB)

$(OSWALD_LIB) : src
	@echo "objectfiles = " $(wildcard $(kernobjdir)/*.o)
	$(AR) $@ $(wildcard $(kernobjdir)/*.o)
	ranlib $@

src :
	make -C src $(MAKECMDGOALS)

test :
	make -C tests $(MAKECMDGOALS)

clean :
	-make -C tests $(MAKECMDGOALS)
	-make -C src $(MAKECMDGOALS)


